package com.peter.overtimecalculator

import com.peter.overtimecalculator.data.backup.BackupRestoreRepository
import com.peter.overtimecalculator.data.backup.BackupSnapshotCodec
import com.peter.overtimecalculator.data.db.HolidayOverrideEntity
import com.peter.overtimecalculator.data.db.MonthlyConfigEntity
import com.peter.overtimecalculator.data.db.OvertimeDao
import com.peter.overtimecalculator.data.db.OvertimeEntryEntity
import com.peter.overtimecalculator.domain.BackupHolidayOverride
import com.peter.overtimecalculator.domain.BackupMonthlyConfig
import com.peter.overtimecalculator.domain.BackupOvertimeEntry
import com.peter.overtimecalculator.domain.BackupSnapshot
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.RestorePreview
import java.math.BigDecimal
import java.time.Instant
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for BackupRestoreRepository.
 * Verifies export, preview, and restore operations for full-fidelity snapshots.
 */
class BackupRestoreRepositoryTest {

    private val codec = BackupSnapshotCodec()
    
    // Mock DAO for testing - simulates PRODUCTION behavior (upsert, not clear)
    // This exposes the bug where upsert doesn't clear old rows not in snapshot
    private val mockDao = object : OvertimeDao {
        // Use LinkedHashMap to preserve insertion order for deterministic tests
        private val configs = LinkedHashMap<String, MonthlyConfigEntity>()
        private val entries = LinkedHashMap<String, OvertimeEntryEntity>()
        private val overrides = LinkedHashMap<String, HolidayOverrideEntity>()

        override suspend fun getAllConfigs(): List<MonthlyConfigEntity> = configs.values.toList()
        override suspend fun getAllEntries(): List<OvertimeEntryEntity> = entries.values.toList()
        override suspend fun getAllOverrides(): List<HolidayOverrideEntity> = overrides.values.toList()
        
        // PRODUCTION behavior: upsert only replaces matching PKs, does NOT clear
        override suspend fun upsertConfigs(entities: List<MonthlyConfigEntity>) {
            entities.forEach { configs[it.yearMonth] = it }
        }
        override suspend fun upsertEntries(entities: List<OvertimeEntryEntity>) {
            entities.forEach { entries[it.date] = it }
        }
        override suspend fun upsertOverrides(entities: List<HolidayOverrideEntity>) {
            entities.forEach { overrides[it.date] = it }
        }
        
        // Delete all methods for clear-and-replace
        override suspend fun deleteAllConfigs() { configs.clear() }
        override suspend fun deleteAllEntries() { entries.clear() }
        override suspend fun deleteAllOverrides() { overrides.clear() }
        
        // Unused for these tests
        override suspend fun getEntriesInRange(startDate: String, endDate: String): List<OvertimeEntryEntity> = emptyList()
        override suspend fun getOverridesInRange(startDate: String, endDate: String): List<HolidayOverrideEntity> = emptyList()
        override fun observeAllConfigs(): Flow<List<MonthlyConfigEntity>> = flowOf(emptyList())
        override fun observeEntriesInRange(startDate: String, endDate: String): Flow<List<OvertimeEntryEntity>> = flowOf(emptyList())
        override fun observeOverridesInRange(startDate: String, endDate: String): Flow<List<HolidayOverrideEntity>> = flowOf(emptyList())
        override suspend fun upsertConfig(entity: MonthlyConfigEntity) {}
        override suspend fun upsertEntry(entity: OvertimeEntryEntity) {}
        override suspend fun deleteEntry(date: String) {}
        override suspend fun upsertHolidayOverride(entity: HolidayOverrideEntity) {}
        override suspend fun deleteHolidayOverride(date: String) {}
    }

    @Test
    fun exportSnapshot_returnsPayloadCoveringRoomTruthOnly() {
        // Given: A repository with populated DAO
        val repository = BackupRestoreRepository(mockDao, codec)
        
        // Pre-populate mock DAO with test data
        runBlocking {
            mockDao.upsertConfigs(listOf(
                MonthlyConfigEntity(
                    yearMonth = "2026-01",
                    hourlyRate = BigDecimal("60.00"),
                    rateSource = HourlyRateSource.MANUAL,
                    weekdayRate = BigDecimal("1.50"),
                    restDayRate = BigDecimal("2.00"),
                    holidayRate = BigDecimal("3.00"),
                    lockedByUser = false,
                )
            ))
            mockDao.upsertEntries(listOf(
                OvertimeEntryEntity(date = "2026-01-15", minutes = 120)
            ))
            mockDao.upsertOverrides(listOf(
                HolidayOverrideEntity(date = "2026-01-01", dayType = DayType.HOLIDAY)
            ))
        }

        // When: Export snapshot
        val snapshot = runBlocking { repository.exportSnapshot() }

        // Then: Snapshot covers Room truth only (configs, entries, overrides)
        assertNotNull(snapshot)
        assertTrue(snapshot.monthlyConfigs.isNotEmpty())
        assertTrue(snapshot.overtimeEntries.isNotEmpty())
        assertTrue(snapshot.holidayOverrides.isNotEmpty())
        
        // Verify data matches what was inserted
        assertEquals(1, snapshot.monthlyConfigs.size)
        assertEquals("2026-01", snapshot.monthlyConfigs[0].yearMonth.toString())
        assertEquals(1, snapshot.overtimeEntries.size)
        assertEquals("2026-01-15", snapshot.overtimeEntries[0].date)
        assertEquals(1, snapshot.holidayOverrides.size)
        assertEquals("2026-01-01", snapshot.holidayOverrides[0].date)
    }

    @Test
    fun previewRestore_parsesAndReportsDetailsWithoutWriting() {
        // Given: A valid backup snapshot
        val snapshot = BackupSnapshot(
            schemaVersion = 1,
            createdAt = Instant.now().toString(),
            monthlyConfigs = listOf(
                BackupMonthlyConfig(
                    yearMonth = YearMonth.of(2026, 1),
                    hourlyRate = BigDecimal("60.00"),
                    rateSource = HourlyRateSource.MANUAL,
                    weekdayRate = BigDecimal("1.50"),
                    restDayRate = BigDecimal("2.00"),
                    holidayRate = BigDecimal("3.00"),
                    lockedByUser = false,
                )
            ),
            overtimeEntries = listOf(
                BackupOvertimeEntry(date = "2026-01-15", minutes = 120)
            ),
            holidayOverrides = listOf(
                BackupHolidayOverride(date = "2026-01-01", dayType = DayType.HOLIDAY)
            ),
        )
        val repository = BackupRestoreRepository(mockDao, codec)

        // When: Preview restore
        val preview = repository.previewRestore(snapshot)

        // Then: Returns version/scope/incompatibility details without writing
        assertTrue(preview is RestorePreview.Compatible)
        val compatible = preview as RestorePreview.Compatible
        assertEquals(1, compatible.schemaVersion)
        assertEquals(1, compatible.monthCount)
        assertEquals(1, compatible.entryCount)
        assertEquals(1, compatible.overrideCount)
        assertTrue(compatible.isCompatible)
        assertNotNull(compatible.createdAt)
    }

    @Test
    fun restoreSnapshot_appliesOnlyValidatedSnapshots() {
        // Given: A repository and a valid snapshot
        val snapshot = BackupSnapshot(
            schemaVersion = 1,
            createdAt = Instant.now().toString(),
            monthlyConfigs = listOf(
                BackupMonthlyConfig(
                    yearMonth = YearMonth.of(2026, 2),
                    hourlyRate = BigDecimal("65.00"),
                    rateSource = HourlyRateSource.MANUAL,
                    weekdayRate = BigDecimal("1.50"),
                    restDayRate = BigDecimal("2.00"),
                    holidayRate = BigDecimal("3.00"),
                    lockedByUser = true,
                )
            ),
            overtimeEntries = listOf(
                BackupOvertimeEntry(date = "2026-02-10", minutes = 90)
            ),
            holidayOverrides = listOf(
                BackupHolidayOverride(date = "2026-02-14", dayType = DayType.REST_DAY)
            ),
        )
        val repository = BackupRestoreRepository(mockDao, codec)

        // When: Restore snapshot
        val result = runBlocking { repository.restoreSnapshot(snapshot) }

        // Then: Apply only validated snapshots and preserve exact durable rows
        assertTrue(result.isSuccess)
        
        // Verify data was actually written
        val restoredConfigs = runBlocking { mockDao.getAllConfigs() }
        val restoredEntries = runBlocking { mockDao.getAllEntries() }
        val restoredOverrides = runBlocking { mockDao.getAllOverrides() }
        
        assertEquals(1, restoredConfigs.size)
        assertEquals("2026-02", restoredConfigs[0].yearMonth)
        assertEquals(1, restoredEntries.size)
        assertEquals("2026-02-10", restoredEntries[0].date)
        assertEquals(1, restoredOverrides.size)
        assertEquals("2026-02-14", restoredOverrides[0].date)
    }

    @Test
    fun restoreSnapshot_excludesUpdateSessionAndHolidayCacheMetadata() {
        // Given: A snapshot (which by contract excludes update-session and holiday cache)
        val snapshot = BackupSnapshot(
            schemaVersion = 1,
            createdAt = Instant.now().toString(),
            monthlyConfigs = listOf(
                BackupMonthlyConfig(
                    yearMonth = YearMonth.of(2026, 1),
                    hourlyRate = BigDecimal("60.00"),
                    rateSource = HourlyRateSource.MANUAL,
                    weekdayRate = BigDecimal("1.50"),
                    restDayRate = BigDecimal("2.00"),
                    holidayRate = BigDecimal("3.00"),
                    lockedByUser = false,
                )
            ),
            overtimeEntries = emptyList(),
            holidayOverrides = emptyList(),
        )
        val repository = BackupRestoreRepository(mockDao, codec)

        // When: Encode the snapshot that will be restored
        val encoded = codec.encode(snapshot)

        // Then: No update-session or holiday-cache fields in the encoded payload
        assertFalse(encoded.contains("updateSession"))
        assertFalse(encoded.contains("lastModified"))
        assertFalse(encoded.contains("holidayCache"))
        assertFalse(encoded.contains("remoteCache"))
        assertFalse(encoded.contains("fetchedAt"))
    }

    @Test
    fun previewRestore_detectsIncompatibleSnapshots() {
        // Given: An incompatible snapshot with future schema version
        val incompatibleSnapshot = BackupSnapshot(
            schemaVersion = 999,
            createdAt = Instant.now().toString(),
            monthlyConfigs = emptyList(),
            overtimeEntries = emptyList(),
            holidayOverrides = emptyList(),
        )
        val repository = BackupRestoreRepository(mockDao, codec)

        // When: Preview restore on incompatible snapshot
        val preview = repository.previewRestore(incompatibleSnapshot)

        // Then: Preview indicates incompatibility
        assertTrue(preview is RestorePreview.Incompatible)
        val incompatible = preview as RestorePreview.Incompatible
        assertFalse(incompatible.isCompatible)
        assertEquals(999, incompatible.schemaVersion)
    }

    @Test
    fun restoreSnapshot_clearsOldDataNotInSnapshot_trueReplaceBehavior() {
        // Given: DAO has existing data (Jan + Feb configs)
        runBlocking {
            mockDao.upsertConfigs(listOf(
                MonthlyConfigEntity(
                    yearMonth = "2026-01",
                    hourlyRate = BigDecimal("60.00"),
                    rateSource = HourlyRateSource.MANUAL,
                    weekdayRate = BigDecimal("1.50"),
                    restDayRate = BigDecimal("2.00"),
                    holidayRate = BigDecimal("3.00"),
                    lockedByUser = false,
                ),
                MonthlyConfigEntity(
                    yearMonth = "2026-02",
                    hourlyRate = BigDecimal("65.00"),
                    rateSource = HourlyRateSource.MANUAL,
                    weekdayRate = BigDecimal("1.50"),
                    restDayRate = BigDecimal("2.00"),
                    holidayRate = BigDecimal("3.00"),
                    lockedByUser = false,
                )
            ))
            mockDao.upsertEntries(listOf(
                OvertimeEntryEntity(date = "2026-01-15", minutes = 120),
                OvertimeEntryEntity(date = "2026-02-10", minutes = 90)
            ))
            mockDao.upsertOverrides(listOf(
                HolidayOverrideEntity(date = "2026-01-01", dayType = DayType.HOLIDAY)
            ))
        }
        
        // Verify initial state
        var initialConfigs = runBlocking { mockDao.getAllConfigs() }
        assertEquals("Should have 2 configs initially", 2, initialConfigs.size)
        
        // When: Restore a snapshot with ONLY March data (subset)
        val marchOnlySnapshot = BackupSnapshot(
            schemaVersion = 1,
            createdAt = Instant.now().toString(),
            monthlyConfigs = listOf(
                BackupMonthlyConfig(
                    yearMonth = YearMonth.of(2026, 3),
                    hourlyRate = BigDecimal("70.00"),
                    rateSource = HourlyRateSource.MANUAL,
                    weekdayRate = BigDecimal("1.50"),
                    restDayRate = BigDecimal("2.00"),
                    holidayRate = BigDecimal("3.00"),
                    lockedByUser = false,
                )
            ),
            overtimeEntries = listOf(
                BackupOvertimeEntry(date = "2026-03-20", minutes = 180)
            ),
            holidayOverrides = emptyList(),
        )
        val repository = BackupRestoreRepository(mockDao, codec)
        
        val result = runBlocking { repository.restoreSnapshot(marchOnlySnapshot) }
        
        // Then: True replace - old Jan/Feb data is GONE, only March remains
        assertTrue(result.isSuccess)
        
        val finalConfigs = runBlocking { mockDao.getAllConfigs() }
        val finalEntries = runBlocking { mockDao.getAllEntries() }
        val finalOverrides = runBlocking { mockDao.getAllOverrides() }
        
        // Must have ONLY March data - Jan/Feb should be cleared
        assertEquals("Should have exactly 1 config (March only)", 1, finalConfigs.size)
        assertEquals("2026-03", finalConfigs[0].yearMonth)
        
        assertEquals("Should have exactly 1 entry (March only)", 1, finalEntries.size)
        assertEquals("2026-03-20", finalEntries[0].date)
        
        assertEquals("Should have no overrides", 0, finalOverrides.size)
    }
}
