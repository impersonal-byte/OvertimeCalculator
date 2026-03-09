package com.peter.overtimecalculator.domain

import java.time.LocalDate
import java.time.YearMonth

enum class DayType {
    WORKDAY,
    REST_DAY,
    HOLIDAY,
}

enum class HourlyRateSource {
    MANUAL,
    REVERSE_ENGINEERED,
}

data class MonthlyConfig(
    val yearMonth: YearMonth,
    val hourlyRate: Double,
    val rateSource: HourlyRateSource,
    val weekdayRate: Double,
    val restDayRate: Double,
    val holidayRate: Double,
    val lockedByUser: Boolean,
)

data class DayCellUiState(
    val date: LocalDate,
    val overtimeMinutes: Int,
    val dayType: DayType,
    val pay: Double,
)

data class MonthlySummaryUiState(
    val totalMinutes: Int,
    val totalPay: Double,
    val yearMonth: YearMonth,
    val hourlyRate: Double,
    val rateSource: HourlyRateSource,
)

data class ReverseRateResult(
    val hourlyRate: Double,
    val weightedHours: Double,
    val overtimePayInput: Double,
)

data class ObservedMonth(
    val config: MonthlyConfig,
    val summary: MonthlySummaryUiState,
    val dayCells: List<DayCellUiState>,
    val entryMinutesByDate: Map<LocalDate, Int>,
    val overrideTypesByDate: Map<LocalDate, DayType>,
)

