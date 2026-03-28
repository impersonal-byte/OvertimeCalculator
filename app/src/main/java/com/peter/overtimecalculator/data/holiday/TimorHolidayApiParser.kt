package com.peter.overtimecalculator.data.holiday

import com.peter.overtimecalculator.domain.HolidayYearRules
import java.time.LocalDate
import org.json.JSONObject

object TimorHolidayApiParser {
    fun parse(json: String): HolidayYearRules {
        val root = try {
            JSONObject(json)
        } catch (error: Exception) {
            throw IllegalArgumentException("Timor holiday payload must be a JSON object", error)
        }

        val holidayObject = root.optJSONObject("holiday")
            ?: throw IllegalArgumentException("Timor holiday payload must contain a holiday object")

        val holidayDates = linkedSetOf<LocalDate>()
        val restDates = linkedSetOf<LocalDate>()
        val workingDates = linkedSetOf<LocalDate>()
        val keys = holidayObject.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val entry = holidayObject.optJSONObject(key)
                ?: throw IllegalArgumentException("Timor holiday entry $key must be an object")
            val date = try {
                LocalDate.parse(entry.getString("date"))
            } catch (error: Exception) {
                throw IllegalArgumentException("Timor holiday entry $key is missing a valid date", error)
            }
            val wage = try {
                entry.getInt("wage")
            } catch (error: Exception) {
                throw IllegalArgumentException("Timor holiday entry $key is missing a valid wage", error)
            }
            val isHoliday = if (entry.has("holiday")) {
                entry.getBoolean("holiday")
            } else {
                throw IllegalArgumentException("Timor holiday entry $key is missing a valid holiday flag")
            }

            when {
                isHoliday && wage == 3 -> holidayDates += date
                isHoliday && wage == 2 -> restDates += date
                !isHoliday && wage == 1 -> workingDates += date
            }
        }

        return HolidayYearRules(
            holidayDates = holidayDates,
            restDates = restDates,
            workingDates = workingDates,
        )
    }
}
