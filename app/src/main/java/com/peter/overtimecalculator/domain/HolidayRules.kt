package com.peter.overtimecalculator.domain

import java.time.LocalDate

data class HolidayYearRules(
    val holidayDates: Set<LocalDate>,
    val workingDates: Set<LocalDate>,
)

data class HolidayRulesSnapshot(
    val schemaVersion: Int,
    val updatedAt: String,
    val years: Map<Int, HolidayYearRules>,
) {
    companion object {
        val Empty = HolidayRulesSnapshot(
            schemaVersion = 1,
            updatedAt = "",
            years = emptyMap(),
        )
    }
}

data class HolidayRemoteMetadata(
    val sourceUrl: String,
    val fetchedAtEpochMillis: Long,
    val updatedAt: String,
)

class HolidayCalendar(
    private val rulesProvider: () -> HolidayRulesSnapshot = { HolidayRulesSnapshot.Empty },
) {
    fun resolveDayType(date: LocalDate, override: DayType?): DayType {
        if (override != null) return override

        val yearRules = rulesProvider().years[date.year]
        if (yearRules != null) {
            if (date in yearRules.holidayDates) return DayType.HOLIDAY
            if (date in yearRules.workingDates) return DayType.WORKDAY
        }

        return when (date.dayOfWeek.value) {
            6, 7 -> DayType.REST_DAY
            else -> DayType.WORKDAY
        }
    }
}
