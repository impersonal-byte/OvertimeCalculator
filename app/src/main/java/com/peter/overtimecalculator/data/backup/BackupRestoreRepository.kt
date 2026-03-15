package com.peter.overtimecalculator.data.backup

import com.peter.overtimecalculator.data.backup.BackupSnapshotCodec
import com.peter.overtimecalculator.data.db.MonthlyConfigEntity
import com.peter.overtimecalculator.data.db.OvertimeDao
import com.peter.overtimecalculator.data.db.OvertimeEntryEntity
import com.peter.overtimecalculator.data.db.HolidayOverrideEntity
import com.peter.overtimecalculator.domain.BackupHolidayOverride
import com.peter.overtimecalculator.domain.BackupMonthlyConfig
import com.peter.overtimecalculator.domain.BackupOvertimeEntry
import com.peter.overtimecalculator.domain.BackupSnapshot
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.RestorePreview
import java.time.Instant

/**
 * Repository for backup and restore operations.
 * 
 * Provides:
 * - exportSnapshot: Creates a full-fidelity snapshot from Room data
 * - previewRestore: Validates and parses a snapshot without writing
 * - restoreSnapshot: Applies a validated snapshot to Room
 * 
 * This service is wired through AppContainer for use by UI.
 */
class BackupRestoreRepository(
    private val dao: OvertimeDao,
    private val codec: BackupSnapshotCodec,
    private val runInTransaction: suspend (suspend () -> Unit) -> Unit = { block -> block() },
) {
    
    /**
     * Export a full snapshot from Room data.
     * Reads all configs, entries, and overrides to create a complete backup.
     */
    suspend fun exportSnapshot(): BackupSnapshot {
        val configs = dao.getAllConfigs()
        val entries = dao.getAllEntries()
        val overrides = dao.getAllOverrides()
        
        return BackupSnapshot(
            schemaVersion = BackupSnapshot.CURRENT_SCHEMA_VERSION,
            createdAt = Instant.now().toString(),
            monthlyConfigs = configs.map { entity ->
                BackupMonthlyConfig(
                    yearMonth = java.time.YearMonth.parse(entity.yearMonth),
                    hourlyRate = entity.hourlyRate,
                    rateSource = entity.rateSource,
                    weekdayRate = entity.weekdayRate,
                    restDayRate = entity.restDayRate,
                    holidayRate = entity.holidayRate,
                    lockedByUser = entity.lockedByUser,
                )
            },
            overtimeEntries = entries.map { entity ->
                BackupOvertimeEntry(
                    date = entity.date,
                    minutes = entity.minutes,
                )
            },
            holidayOverrides = overrides.map { entity ->
                BackupHolidayOverride(
                    date = entity.date,
                    dayType = entity.dayType,
                )
            },
        )
    }
    
    /**
     * Preview a restore operation without writing.
     * Validates the snapshot and returns compatibility information.
     */
    fun previewRestore(snapshot: BackupSnapshot): RestorePreview {
        return codec.validate(snapshot)
    }
    
    /**
     * Restore a validated snapshot to Room.
     * Applies all configs, entries, and overrides in a single transaction.
     * 
     * Note: This excludes update-session state and holiday cache metadata
     * as per the backup contract - only restores durable business truth.
     */
    suspend fun restoreSnapshot(snapshot: BackupSnapshot): Result<Unit> {
        return try {
            // Validate first
            val preview = previewRestore(snapshot)
            if (!preview.isCompatible) {
                return Result.failure(IllegalArgumentException("Incompatible snapshot version: ${preview.schemaVersion}"))
            }
            
            // Convert to entities and restore
            val configEntities = snapshot.monthlyConfigs.map { config ->
                MonthlyConfigEntity(
                    yearMonth = config.yearMonth.toString(),
                    hourlyRate = config.hourlyRate,
                    rateSource = config.rateSource,
                    weekdayRate = config.weekdayRate,
                    restDayRate = config.restDayRate,
                    holidayRate = config.holidayRate,
                    lockedByUser = config.lockedByUser,
                )
            }
            
            val entryEntities = snapshot.overtimeEntries.map { entry ->
                OvertimeEntryEntity(
                    date = entry.date,
                    minutes = entry.minutes,
                )
            }
            
            val overrideEntities = snapshot.holidayOverrides.map { override ->
                HolidayOverrideEntity(
                    date = override.date,
                    dayType = override.dayType,
                )
            }
            
            runInTransaction {
                // Replace all data - clear old rows first, then insert new ones
                // This ensures true replace semantics: rows not in snapshot are removed
                dao.deleteAllConfigs()
                dao.deleteAllEntries()
                dao.deleteAllOverrides()
                dao.upsertConfigs(configEntities)
                dao.upsertEntries(entryEntities)
                dao.upsertOverrides(overrideEntities)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
