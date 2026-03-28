package com.peter.overtimecalculator.data.holiday

import com.peter.overtimecalculator.domain.HolidayRulesSnapshot
import com.peter.overtimecalculator.domain.HolidayYearRules
import java.time.LocalDate
import org.json.JSONObject

object HolidayRulesJsonParser {
    private const val SupportedSchemaVersion = 1

    fun parse(json: String): HolidayRulesSnapshot {
        val root = JSONObject(json)
        val schemaVersion = root.getInt("schemaVersion")
        require(schemaVersion == SupportedSchemaVersion) { "Unsupported holiday schema version: $schemaVersion" }

        val yearsObject = root.getJSONObject("years")
        val years = mutableMapOf<Int, HolidayYearRules>()
        val keys = yearsObject.keys()
        while (keys.hasNext()) {
            val yearKey = keys.next()
            val yearData = yearsObject.getJSONObject(yearKey)
            years[yearKey.toInt()] = HolidayYearRules(
                holidayDates = yearData.getJSONArray("holidayDates").toLocalDateSet(),
                restDates = yearData.optJSONArray("restDates")?.toLocalDateSet() ?: emptySet(),
                workingDates = yearData.getJSONArray("workingDates").toLocalDateSet(),
            )
        }

        return HolidayRulesSnapshot(
            schemaVersion = schemaVersion,
            updatedAt = root.optString("updatedAt"),
            years = years.toMap(),
        )
    }
}

private fun org.json.JSONArray.toLocalDateSet(): Set<LocalDate> {
    return buildSet(length()) {
        for (index in 0 until length()) {
            add(LocalDate.parse(getString(index)))
        }
    }
}
