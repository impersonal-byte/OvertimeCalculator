package com.peter.overtimecalculator.domain

import java.math.BigDecimal
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

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class SeedColor(val label: String) {
    CLAY("Clay"),
    MINT_GREEN("Mint"),
    AQUA("Aqua"),
    SKY_BLUE("Sky"),
    LAVENDER("Lavender"),
    ORCHID("Orchid"),
    LILAC("Lilac"),
    ROSE("Rose"),
}

data class MonthlyConfig(
    val yearMonth: YearMonth,
    val hourlyRate: BigDecimal,
    val rateSource: HourlyRateSource,
    val weekdayRate: BigDecimal,
    val restDayRate: BigDecimal,
    val holidayRate: BigDecimal,
    val lockedByUser: Boolean,
)

data class DayCellUiState(
    val date: LocalDate,
    val overtimeMinutes: Int,
    val dayType: DayType,
    val pay: BigDecimal,
)

data class MonthlySummaryUiState(
    val totalMinutes: Int,
    val totalPay: BigDecimal,
    val yearMonth: YearMonth,
    val hourlyRate: BigDecimal,
    val rateSource: HourlyRateSource,
    val uncoveredCompMinutes: Int = 0,
    val grossOvertimeMinutes: Int = 0,
    val compMinutes: Int = 0,
)

data class ReverseRateResult(
    val hourlyRate: BigDecimal,
    val weightedHours: BigDecimal,
    val overtimePayInput: BigDecimal,
)

data class ObservedMonth(
    val config: MonthlyConfig,
    val summary: MonthlySummaryUiState,
    val dayCells: List<DayCellUiState>,
    val entryMinutesByDate: Map<LocalDate, Int>,
    val overrideTypesByDate: Map<LocalDate, DayType>,
)
