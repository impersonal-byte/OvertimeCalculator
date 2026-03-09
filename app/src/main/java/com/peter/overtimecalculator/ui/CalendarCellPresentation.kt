package com.peter.overtimecalculator.ui

import com.peter.overtimecalculator.domain.DayCellUiState
import com.peter.overtimecalculator.domain.DayType
import java.time.LocalDate
import java.util.Locale

enum class CalendarCellIntensityTier {
    NONE,
    LOW,
    MID,
    HIGH,
}

enum class CalendarCellColorRole {
    DEFAULT,
    WORKDAY_OVERTIME,
    REST_DAY_OVERTIME,
    HOLIDAY_OVERTIME,
    HOLIDAY_OVERTIME_HIGH,
}

data class CalendarCellPresentation(
    val date: LocalDate,
    val hoursLabel: String,
    val tier: CalendarCellIntensityTier,
    val colorRole: CalendarCellColorRole,
)

fun buildCalendarCellPresentations(dayCells: List<DayCellUiState>): Map<LocalDate, CalendarCellPresentation> {
    val maxPay = dayCells.maxOfOrNull(DayCellUiState::pay)?.takeIf { it > 0.0 } ?: 0.0

    return dayCells.associate { cell ->
        val tier = resolveTier(cell.pay, maxPay)
        val role = resolveColorRole(cell.dayType, tier)
        cell.date to CalendarCellPresentation(
            date = cell.date,
            hoursLabel = formatDecimalHours(cell.overtimeMinutes),
            tier = tier,
            colorRole = role,
        )
    }
}

internal fun formatDecimalHours(totalMinutes: Int): String {
    if (totalMinutes <= 0) return ""
    return String.format(Locale.US, "%.1fh", totalMinutes / 60.0)
}

private fun resolveTier(pay: Double, maxPay: Double): CalendarCellIntensityTier {
    if (pay <= 0.0 || maxPay <= 0.0) return CalendarCellIntensityTier.NONE

    val ratio = pay / maxPay
    return when {
        ratio < 0.33 -> CalendarCellIntensityTier.LOW
        ratio < 0.66 -> CalendarCellIntensityTier.MID
        else -> CalendarCellIntensityTier.HIGH
    }
}

private fun resolveColorRole(dayType: DayType, tier: CalendarCellIntensityTier): CalendarCellColorRole {
    if (tier == CalendarCellIntensityTier.NONE) return CalendarCellColorRole.DEFAULT

    return when (dayType) {
        DayType.WORKDAY -> CalendarCellColorRole.WORKDAY_OVERTIME
        DayType.REST_DAY -> CalendarCellColorRole.REST_DAY_OVERTIME
        DayType.HOLIDAY -> {
            if (tier == CalendarCellIntensityTier.HIGH) {
                CalendarCellColorRole.HOLIDAY_OVERTIME_HIGH
            } else {
                CalendarCellColorRole.HOLIDAY_OVERTIME
            }
        }
    }
}
