package com.peter.overtimecalculator.data.backup

import com.peter.overtimecalculator.domain.BackupHolidayOverride
import com.peter.overtimecalculator.domain.BackupMonthlyConfig
import com.peter.overtimecalculator.domain.BackupOvertimeEntry
import com.peter.overtimecalculator.domain.BackupSnapshot
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.RestorePreview
import java.math.BigDecimal
import java.time.YearMonth

/**
 * Codec for encoding and decoding backup snapshots.
 * 
 * This codec handles the structured backup format (JSON) which is separate
 * from the CSV export in OvertimeViewModel.
 */
class BackupSnapshotCodec {

    /**
     * Encode a BackupSnapshot to a JSON string.
     */
    fun encode(snapshot: BackupSnapshot): String {
        return buildString {
            append("{")
            append("\"schemaVersion\":${snapshot.schemaVersion},")
            append("\"createdAt\":\"${snapshot.createdAt}\",")
            append("\"monthlyConfigs\":[")
            snapshot.monthlyConfigs.forEachIndexed { index, config ->
                if (index > 0) append(",")
                append(encodeMonthlyConfig(config))
            }
            append("],")
            append("\"overtimeEntries\":[")
            snapshot.overtimeEntries.forEachIndexed { index, entry ->
                if (index > 0) append(",")
                append(encodeOvertimeEntry(entry))
            }
            append("],")
            append("\"holidayOverrides\":[")
            snapshot.holidayOverrides.forEachIndexed { index, override ->
                if (index > 0) append(",")
                append(encodeHolidayOverride(override))
            }
            append("]")
            append("}")
        }
    }

    /**
     * Decode a JSON string to a BackupSnapshot.
     * @throws IllegalArgumentException if schema version is not supported
     */
    fun decode(json: String): BackupSnapshot {
        // Simple JSON parsing without external library
        val schemaVersion = extractInt(json, "schemaVersion")
        val createdAt = extractString(json, "createdAt")
        
        // Validate schema version
        if (schemaVersion !in BackupSnapshot.SUPPORTED_VERSIONS) {
            throw IllegalArgumentException("Unsupported backup schema version: $schemaVersion. Supported versions: ${BackupSnapshot.SUPPORTED_VERSIONS}")
        }
        
        val monthlyConfigs = decodeMonthlyConfigs(json)
        val overtimeEntries = decodeOvertimeEntries(json)
        val holidayOverrides = decodeHolidayOverrides(json)
        
        return BackupSnapshot(
            schemaVersion = schemaVersion,
            createdAt = createdAt,
            monthlyConfigs = monthlyConfigs,
            overtimeEntries = overtimeEntries,
            holidayOverrides = holidayOverrides,
        )
    }

    /**
     * Validate a backup snapshot and produce a RestorePreview.
     */
    fun validate(snapshot: BackupSnapshot): RestorePreview {
        return if (snapshot.schemaVersion in BackupSnapshot.SUPPORTED_VERSIONS) {
            RestorePreview.Compatible(
                schemaVersion = snapshot.schemaVersion,
                monthCount = snapshot.monthlyConfigs.size,
                entryCount = snapshot.overtimeEntries.size,
                overrideCount = snapshot.holidayOverrides.size,
                createdAt = snapshot.createdAt,
            )
        } else {
            RestorePreview.Incompatible(
                schemaVersion = snapshot.schemaVersion,
                supportedVersions = BackupSnapshot.SUPPORTED_VERSIONS,
            )
        }
    }

    private fun encodeMonthlyConfig(config: BackupMonthlyConfig): String {
        return buildString {
            append("{")
            append("\"yearMonth\":\"${config.yearMonth}\",")
            append("\"hourlyRate\":\"${config.hourlyRate}\",")
            append("\"rateSource\":\"${config.rateSource.name}\",")
            append("\"weekdayRate\":\"${config.weekdayRate}\",")
            append("\"restDayRate\":\"${config.restDayRate}\",")
            append("\"holidayRate\":\"${config.holidayRate}\",")
            append("\"lockedByUser\":${config.lockedByUser}")
            append("}")
        }
    }

    private fun encodeOvertimeEntry(entry: BackupOvertimeEntry): String {
        return "{\"date\":\"${entry.date}\",\"minutes\":${entry.minutes}}"
    }

    private fun encodeHolidayOverride(override: BackupHolidayOverride): String {
        return "{\"date\":\"${override.date}\",\"dayType\":\"${override.dayType.name}\"}"
    }

    private fun extractInt(json: String, key: String): Int {
        val regex = """"$key"\s*:\s*(\d+)""".toRegex()
        val match = regex.find(json) ?: throw IllegalArgumentException("Missing key: $key")
        return match.groupValues[1].toInt()
    }

    private fun extractString(json: String, key: String): String {
        val regex = """"$key"\s*:\s*"([^"]*)"""".toRegex()
        val match = regex.find(json) ?: throw IllegalArgumentException("Missing key: $key")
        return match.groupValues[1]
    }

    private fun decodeMonthlyConfigs(json: String): List<BackupMonthlyConfig> {
        val configs = mutableListOf<BackupMonthlyConfig>()
        // Match each config object within the monthlyConfigs array
        val configRegex = """\{"yearMonth":"([^"]+)","hourlyRate":"([^"]+)","rateSource":"([^"]+)","weekdayRate":"([^"]+)","restDayRate":"([^"]+)","holidayRate":"([^"]+)","lockedByUser":(true|false)}""".toRegex()
        
        // Find all matches in the entire JSON (not just within extracted array)
        configRegex.findAll(json).forEach { m ->
            configs.add(
                BackupMonthlyConfig(
                    yearMonth = YearMonth.parse(m.groupValues[1]),
                    hourlyRate = BigDecimal(m.groupValues[2]),
                    rateSource = HourlyRateSource.valueOf(m.groupValues[3]),
                    weekdayRate = BigDecimal(m.groupValues[4]),
                    restDayRate = BigDecimal(m.groupValues[5]),
                    holidayRate = BigDecimal(m.groupValues[6]),
                    lockedByUser = m.groupValues[7].toBoolean(),
                )
            )
        }
        return configs
    }

    private fun decodeOvertimeEntries(json: String): List<BackupOvertimeEntry> {
        val entries = mutableListOf<BackupOvertimeEntry>()
        val entryRegex = """\{"date":"([^"]+)","minutes":(-?\d+)}""".toRegex()
        
        entryRegex.findAll(json).forEach { m ->
            entries.add(
                BackupOvertimeEntry(
                    date = m.groupValues[1],
                    minutes = m.groupValues[2].toInt(),
                )
            )
        }
        return entries
    }

    private fun decodeHolidayOverrides(json: String): List<BackupHolidayOverride> {
        val overrides = mutableListOf<BackupHolidayOverride>()
        val overrideRegex = """\{"date":"([^"]+)","dayType":"([^"]+)"}""".toRegex()
        
        overrideRegex.findAll(json).forEach { m ->
            overrides.add(
                BackupHolidayOverride(
                    date = m.groupValues[1],
                    dayType = DayType.valueOf(m.groupValues[2]),
                )
            )
        }
        return overrides
    }
}
