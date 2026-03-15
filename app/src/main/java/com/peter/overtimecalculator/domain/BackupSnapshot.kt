package com.peter.overtimecalculator.domain

import java.math.BigDecimal
import java.time.YearMonth

/**
 * Versioned backup snapshot contract for full-fidelity portability.
 * 
 * This contract captures the durable business state of the app:
 * - Monthly configurations (rates, rate sources, multipliers)
 * - Overtime entries per date
 * - Holiday type overrides per date
 * 
 * This is separate from CSV export which only shows current month view.
 */
data class BackupSnapshot(
    val schemaVersion: Int,
    val createdAt: String,
    val monthlyConfigs: List<BackupMonthlyConfig>,
    val overtimeEntries: List<BackupOvertimeEntry>,
    val holidayOverrides: List<BackupHolidayOverride>,
) {
    companion object {
        /** Current supported schema version */
        const val CURRENT_SCHEMA_VERSION = 1
        
        /** Supported schema versions */
        val SUPPORTED_VERSIONS = setOf(1)
        
        /** Dedicated backup file extension (not CSV) */
        const val BACKUP_FILE_EXTENSION = ".obackup"
        
        /** Dedicated MIME type for backup files */
        const val BACKUP_MIME_TYPE = "application/overtime-backup"
    }
}

/**
 * Backup representation of monthly configuration.
 * Mirrors MonthlyConfigEntity but without update-session metadata.
 */
data class BackupMonthlyConfig(
    val yearMonth: YearMonth,
    val hourlyRate: BigDecimal,
    val rateSource: HourlyRateSource,
    val weekdayRate: BigDecimal,
    val restDayRate: BigDecimal,
    val holidayRate: BigDecimal,
    val lockedByUser: Boolean,
)

/**
 * Backup representation of an overtime entry.
 */
data class BackupOvertimeEntry(
    val date: String,
    val minutes: Int,
)

/**
 * Backup representation of a holiday override.
 */
data class BackupHolidayOverride(
    val date: String,
    val dayType: DayType,
)

/**
 * Validation result that drives confirmation UI before destructive restore.
 * Provides version, scope, and compatibility information.
 */
sealed class RestorePreview {
    abstract val schemaVersion: Int
    abstract val monthCount: Int
    abstract val entryCount: Int
    abstract val overrideCount: Int
    abstract val isCompatible: Boolean
    
    data class Compatible(
        override val schemaVersion: Int,
        override val monthCount: Int,
        override val entryCount: Int,
        override val overrideCount: Int,
        val createdAt: String,
    ) : RestorePreview() {
        override val isCompatible: Boolean = true
    }
    
    data class Incompatible(
        override val schemaVersion: Int,
        val supportedVersions: Set<Int>,
    ) : RestorePreview() {
        override val monthCount: Int = 0
        override val entryCount: Int = 0
        override val overrideCount: Int = 0
        override val isCompatible: Boolean = false
    }
}
