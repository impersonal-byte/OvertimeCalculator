package com.peter.overtimecalculator.data.holiday

import com.peter.overtimecalculator.domain.HolidayYearRules
import java.time.LocalDate
import org.json.JSONObject

object HolidayTimorApiParser {
    private const val SuccessCode = 0
    private const val TypeWeekend = 1
    private const val TypeHoliday = 2
    private const val TypeAdjustedWorkday = 3

    fun parseYear(json: String): HolidayYearRules {
        val root = JSONObject(json)
        require(root.optInt("code", -1) == SuccessCode) { "Timor holiday API returned an error" }

        val holidayDates = linkedSetOf<LocalDate>()
        val workingDates = linkedSetOf<LocalDate>()

        val typeObject = root.optJSONObject("type")
        if (typeObject != null) {
            val keys = typeObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val typeEntry = typeObject.optJSONObject(key) ?: continue
                val date = LocalDate.parse(key)
                when (typeEntry.optInt("type", -1)) {
                    TypeHoliday -> holidayDates += date
                    TypeAdjustedWorkday -> workingDates += date
                    TypeWeekend -> Unit
                }
            }
        } else {
            val holidayObject = root.optJSONObject("holiday")
            if (holidayObject != null) {
                val keys = holidayObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val entry = holidayObject.optJSONObject(key) ?: continue
                    val date = LocalDate.parse(entry.getString("date"))
                    when {
                        !entry.optBoolean("holiday", false) -> workingDates += date
                        !entry.optString("name").startsWith("周") -> holidayDates += date
                    }
                }
            }
        }

        return HolidayYearRules(
            holidayDates = holidayDates,
            workingDates = workingDates,
        )
    }
}
