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
import org.json.JSONArray
import org.json.JSONObject

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
        return JSONObject().apply {
            put("schemaVersion", snapshot.schemaVersion)
            put("createdAt", snapshot.createdAt)
            put(
                "monthlyConfigs",
                JSONArray().apply {
                    snapshot.monthlyConfigs.forEach { put(encodeMonthlyConfig(it)) }
                },
            )
            put(
                "overtimeEntries",
                JSONArray().apply {
                    snapshot.overtimeEntries.forEach { put(encodeOvertimeEntry(it)) }
                },
            )
            put(
                "holidayOverrides",
                JSONArray().apply {
                    snapshot.holidayOverrides.forEach { put(encodeHolidayOverride(it)) }
                },
            )
        }.toString()
    }

    /**
     * Decode a JSON string to a BackupSnapshot.
     * @throws IllegalArgumentException if schema version is not supported
     */
    fun decode(json: String): BackupSnapshot {
        val normalized = sanitize(json)
        val root = JSONObject(normalized)
        val schemaVersion = root.optInt("schemaVersion", -1)
        val createdAt = root.optString("createdAt")
        if (schemaVersion == -1) {
            throw IllegalArgumentException("Missing key: schemaVersion")
        }
        if (createdAt.isBlank()) {
            throw IllegalArgumentException("Missing key: createdAt")
        }
        
        // Validate schema version
        if (schemaVersion !in BackupSnapshot.SUPPORTED_VERSIONS) {
            throw IllegalArgumentException("Unsupported backup schema version: $schemaVersion. Supported versions: ${BackupSnapshot.SUPPORTED_VERSIONS}")
        }
        
        val monthlyConfigs = decodeMonthlyConfigs(root.optJSONArray("monthlyConfigs") ?: JSONArray())
        val overtimeEntries = decodeOvertimeEntries(root.optJSONArray("overtimeEntries") ?: JSONArray())
        val holidayOverrides = decodeHolidayOverrides(root.optJSONArray("holidayOverrides") ?: JSONArray())
        
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

    private fun encodeMonthlyConfig(config: BackupMonthlyConfig): JSONObject {
        return JSONObject().apply {
            put("yearMonth", config.yearMonth.toString())
            put("hourlyRate", config.hourlyRate.toString())
            put("rateSource", config.rateSource.name)
            put("weekdayRate", config.weekdayRate.toString())
            put("restDayRate", config.restDayRate.toString())
            put("holidayRate", config.holidayRate.toString())
            put("lockedByUser", config.lockedByUser)
        }
    }

    private fun encodeOvertimeEntry(entry: BackupOvertimeEntry): JSONObject {
        return JSONObject().apply {
            put("date", entry.date)
            put("minutes", entry.minutes)
        }
    }

    private fun encodeHolidayOverride(override: BackupHolidayOverride): JSONObject {
        return JSONObject().apply {
            put("date", override.date)
            put("dayType", override.dayType.name)
        }
    }

    private fun decodeMonthlyConfigs(array: JSONArray): List<BackupMonthlyConfig> {
        return buildList {
            for (index in 0 until array.length()) {
                val config = array.getJSONObject(index)
                add(
                    BackupMonthlyConfig(
                        yearMonth = YearMonth.parse(config.getString("yearMonth")),
                        hourlyRate = BigDecimal(config.getString("hourlyRate")),
                        rateSource = HourlyRateSource.valueOf(config.getString("rateSource")),
                        weekdayRate = BigDecimal(config.getString("weekdayRate")),
                        restDayRate = BigDecimal(config.getString("restDayRate")),
                        holidayRate = BigDecimal(config.getString("holidayRate")),
                        lockedByUser = config.getBoolean("lockedByUser"),
                    ),
                )
            }
        }
    }

    private fun decodeOvertimeEntries(array: JSONArray): List<BackupOvertimeEntry> {
        return buildList {
            for (index in 0 until array.length()) {
                val entry = array.getJSONObject(index)
                add(
                    BackupOvertimeEntry(
                        date = entry.getString("date"),
                        minutes = entry.getInt("minutes"),
                    ),
                )
            }
        }
    }

    private fun decodeHolidayOverrides(array: JSONArray): List<BackupHolidayOverride> {
        return buildList {
            for (index in 0 until array.length()) {
                val override = array.getJSONObject(index)
                add(
                    BackupHolidayOverride(
                        date = override.getString("date"),
                        dayType = DayType.valueOf(override.getString("dayType")),
                    ),
                )
            }
        }
    }

    private fun sanitize(raw: String): String {
        val withoutBom = raw.removePrefix("\uFEFF")
        val withoutNulls = withoutBom.replace("\u0000", "")
        val trimmed = withoutNulls.trim()
        val objectStart = trimmed.indexOf('{')
        val objectEnd = trimmed.lastIndexOf('}')
        if (objectStart >= 0 && objectEnd >= objectStart) {
            return trimmed.substring(objectStart, objectEnd + 1)
        }
        return trimmed
    }
}
