package com.peter.overtimecalculator.data.holiday

import com.peter.overtimecalculator.domain.HolidayYearRules
import java.time.LocalDate
import org.json.JSONArray

object HolidayHaoshenqiApiParser {
    private const val StatusWeekday = 0
    private const val StatusWeekend = 1
    private const val StatusAdjustedWorkday = 2
    private const val StatusHoliday = 3

    fun parse(json: String): HolidayYearRules {
        val payload = try {
            JSONArray(json)
        } catch (error: Exception) {
            throw IllegalArgumentException("Holiday API payload must be a JSON array", error)
        }

        val holidayDates = linkedSetOf<LocalDate>()
        val workingDates = linkedSetOf<LocalDate>()

        for (index in 0 until payload.length()) {
            val entry = payload.optJSONObject(index)
                ?: throw IllegalArgumentException("Holiday API entry at index $index must be an object")
            val date = try {
                LocalDate.parse(entry.getString("date"))
            } catch (error: Exception) {
                throw IllegalArgumentException("Holiday API entry at index $index is missing a valid date", error)
            }
            val status = try {
                entry.getInt("status")
            } catch (error: Exception) {
                throw IllegalArgumentException("Holiday API entry at index $index is missing a valid status", error)
            }

            when (status) {
                StatusHoliday -> holidayDates += date
                StatusAdjustedWorkday -> workingDates += date
                StatusWeekday, StatusWeekend -> Unit
            }
        }

        return HolidayYearRules(
            holidayDates = holidayDates,
            workingDates = workingDates,
        )
    }
}
