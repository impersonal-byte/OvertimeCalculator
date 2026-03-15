package com.peter.overtimecalculator

import com.peter.overtimecalculator.data.backup.BackupSnapshotCodec
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BackupSnapshotTest {

    private val codec = BackupSnapshotCodec()

    @Test
    fun encodingThenDecoding_preservesMonthlyConfigsEntriesAndOverrides() {
        // Given: A snapshot with configs, entries, and overrides spanning two months
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
                ),
                BackupMonthlyConfig(
                    yearMonth = YearMonth.of(2026, 2),
                    hourlyRate = BigDecimal("65.00"),
                    rateSource = HourlyRateSource.REVERSE_ENGINEERED,
                    weekdayRate = BigDecimal("1.50"),
                    restDayRate = BigDecimal("2.00"),
                    holidayRate = BigDecimal("3.00"),
                    lockedByUser = true,
                ),
            ),
            overtimeEntries = listOf(
                BackupOvertimeEntry(date = "2026-01-15", minutes = 120),
                BackupOvertimeEntry(date = "2026-01-20", minutes = 180),
                BackupOvertimeEntry(date = "2026-02-10", minutes = 90),
            ),
            holidayOverrides = listOf(
                BackupHolidayOverride(date = "2026-01-01", dayType = DayType.HOLIDAY),
                BackupHolidayOverride(date = "2026-02-14", dayType = DayType.REST_DAY),
            ),
        )

        // When: Encode then decode the snapshot
        val encoded = codec.encode(snapshot)
        val decoded = codec.decode(encoded)

        // Then: All data is preserved
        assertEquals(snapshot.monthlyConfigs.size, decoded.monthlyConfigs.size)
        assertEquals(snapshot.overtimeEntries.size, decoded.overtimeEntries.size)
        assertEquals(snapshot.holidayOverrides.size, decoded.holidayOverrides.size)

        // Verify monthly configs match
        val configMap = decoded.monthlyConfigs.associateBy { it.yearMonth }
        assertEquals(BigDecimal("60.00"), configMap[YearMonth.of(2026, 1)]?.hourlyRate)
        assertEquals(HourlyRateSource.MANUAL, configMap[YearMonth.of(2026, 1)]?.rateSource)
        assertEquals(BigDecimal("65.00"), configMap[YearMonth.of(2026, 2)]?.hourlyRate)
        assertEquals(HourlyRateSource.REVERSE_ENGINEERED, configMap[YearMonth.of(2026, 2)]?.rateSource)

        // Verify entries match
        val entryMap = decoded.overtimeEntries.associateBy { it.date }
        assertEquals(120, entryMap["2026-01-15"]?.minutes)
        assertEquals(180, entryMap["2026-01-20"]?.minutes)
        assertEquals(90, entryMap["2026-02-10"]?.minutes)

        // Verify overrides match
        val overrideMap = decoded.holidayOverrides.associateBy { it.date }
        assertEquals(DayType.HOLIDAY, overrideMap["2026-01-01"]?.dayType)
        assertEquals(DayType.REST_DAY, overrideMap["2026-02-14"]?.dayType)
    }

    @Test
    fun snapshotPayload_includesSchemaVersionAndCreationMetadata() {
        // Given: A snapshot with known creation time
        val beforeCreation = Instant.now().toString()
        val snapshot = BackupSnapshot(
            schemaVersion = 1,
            createdAt = beforeCreation,
            monthlyConfigs = emptyList(),
            overtimeEntries = emptyList(),
            holidayOverrides = emptyList(),
        )

        // When: Encode and decode
        val encoded = codec.encode(snapshot)
        val decoded = codec.decode(encoded)

        // Then: Schema version and creation metadata are present
        assertEquals(1, decoded.schemaVersion)
        assertNotNull(decoded.createdAt)
        assertTrue(decoded.createdAt.isNotEmpty())
    }

    @Test
    fun decodingRejects_unknownSchemaVersions() {
        // Given: A payload with unsupported schema version (999)
        val invalidPayload = """
        {
            "schemaVersion": 999,
            "createdAt": "2026-01-15T10:00:00Z",
            "monthlyConfigs": [],
            "overtimeEntries": [],
            "holidayOverrides": []
        }
        """.trimIndent()

        // When: Attempt to decode
        try {
            codec.decode(invalidPayload)
            fail("Expected exception for unknown schema version")
        } catch (e: Exception) {
            // Then: Exception mentions schema version
            assertTrue(e.message?.contains("schema") == true || e.message?.contains("version") == true)
        }
    }

    @Test
    fun decode_acceptsBackupPayloadWithBomWhitespaceAndTrailingNoise() {
        val encoded = codec.encode(
            BackupSnapshot(
                schemaVersion = 1,
                createdAt = Instant.now().toString(),
                monthlyConfigs = listOf(
                    BackupMonthlyConfig(
                        yearMonth = YearMonth.of(2026, 4),
                        hourlyRate = BigDecimal("88.00"),
                        rateSource = HourlyRateSource.MANUAL,
                        weekdayRate = BigDecimal("1.50"),
                        restDayRate = BigDecimal("2.00"),
                        holidayRate = BigDecimal("3.00"),
                        lockedByUser = true,
                    ),
                ),
                overtimeEntries = listOf(BackupOvertimeEntry(date = "2026-04-05", minutes = 60)),
                holidayOverrides = listOf(BackupHolidayOverride(date = "2026-04-06", dayType = DayType.REST_DAY)),
            ),
        )

        val payloadFromDocumentProvider = "\uFEFF\n  $encoded\u0000\n"

        val decoded = codec.decode(payloadFromDocumentProvider)

        assertEquals(1, decoded.schemaVersion)
        assertEquals(1, decoded.monthlyConfigs.size)
        assertEquals(YearMonth.of(2026, 4), decoded.monthlyConfigs.first().yearMonth)
        assertEquals(1, decoded.overtimeEntries.size)
        assertEquals("2026-04-05", decoded.overtimeEntries.first().date)
        assertEquals(1, decoded.holidayOverrides.size)
        assertEquals(DayType.REST_DAY, decoded.holidayOverrides.first().dayType)
    }

    @Test
    fun snapshotContract_doesNotIncludeUpdateSessionOrHolidayCacheMetadata() {
        // Given: A snapshot as it would be created from entities
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
                ),
            ),
            overtimeEntries = listOf(
                BackupOvertimeEntry(date = "2026-01-15", minutes = 120),
            ),
            holidayOverrides = emptyList(),
        )

        // When: Encode the snapshot
        val encoded = codec.encode(snapshot)

        // Then: No update-session or holiday-cache fields in the encoded payload
        assertFalse(encoded.contains("updateSession"))
        assertFalse(encoded.contains("lastModified"))
        assertFalse(encoded.contains("holidayCache"))
        assertFalse(encoded.contains("remoteCache"))
        assertFalse(encoded.contains("fetchedAt"))
    }

    @Test
    fun backupContract_exposesDedicatedExtensionAndMimeType() {
        // Then: Verify extension and MIME type are defined and different from CSV
        assertTrue(BackupSnapshot.BACKUP_FILE_EXTENSION.isNotEmpty())
        assertTrue(BackupSnapshot.BACKUP_MIME_TYPE.isNotEmpty())

        // Must not be CSV
        assertFalse(BackupSnapshot.BACKUP_FILE_EXTENSION.contains("csv", ignoreCase = true))
        assertFalse(BackupSnapshot.BACKUP_MIME_TYPE.contains("text/csv"))
        assertFalse(BackupSnapshot.BACKUP_MIME_TYPE.contains("csv"))

        // Should be something like ".obackup" or ".osnap" and "application/overtime-backup"
        assertTrue(
            BackupSnapshot.BACKUP_FILE_EXTENSION.startsWith(".") ||
            BackupSnapshot.BACKUP_FILE_EXTENSION.length in 1..10
        )
    }

    @Test
    fun validationProducesRestorePreview_forConfirmationUI() {
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
                ),
            ),
            overtimeEntries = listOf(
                BackupOvertimeEntry(date = "2026-01-15", minutes = 120),
            ),
            holidayOverrides = listOf(
                BackupHolidayOverride(date = "2026-01-01", dayType = DayType.HOLIDAY),
            ),
        )

        // When: Validate the snapshot
        val preview = codec.validate(snapshot)

        // Then: RestorePreview contains version, scope, and compatibility info
        assertTrue(preview is RestorePreview)
        val restorePreview = preview as RestorePreview
        assertEquals(1, restorePreview.schemaVersion)
        assertTrue(restorePreview.monthCount >= 0)
        assertTrue(restorePreview.entryCount >= 0)
        assertTrue(restorePreview.overrideCount >= 0)
        assertTrue(restorePreview.isCompatible)
    }

    @Test
    fun validationDetectsIncompatibleVersions() {
        // Given: A snapshot with future unsupported version
        val snapshot = BackupSnapshot(
            schemaVersion = 999,
            createdAt = Instant.now().toString(),
            monthlyConfigs = emptyList(),
            overtimeEntries = emptyList(),
            holidayOverrides = emptyList(),
        )

        // When: Validate
        val preview = codec.validate(snapshot)

        // Then: Preview indicates incompatibility
        assertTrue(preview is RestorePreview)
        val restorePreview = preview as RestorePreview
        assertFalse(restorePreview.isCompatible)
    }
}
