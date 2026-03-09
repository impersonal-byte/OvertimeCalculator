package com.peter.overtimecalculator.ui

import com.peter.overtimecalculator.domain.DayCellUiState
import com.peter.overtimecalculator.domain.DayType
import java.math.BigDecimal
import java.math.RoundingMode
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
    COMP_TIME,
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
    val maxPay = dayCells.maxOfOrNull(DayCellUiState::pay)?.takeIf { it > BigDecimal.ZERO } ?: BigDecimal.ZERO

    return dayCells.associate { cell ->
        val tier = if (cell.overtimeMinutes < 0) {
            resolveCompTier(cell.overtimeMinutes)
        } else {
            resolveTier(cell.pay, maxPay)
        }
        val role = if (cell.overtimeMinutes < 0) {
            CalendarCellColorRole.COMP_TIME
        } else {
            resolveColorRole(cell.dayType, tier)
        }
        cell.date to CalendarCellPresentation(
            date = cell.date,
            hoursLabel = formatDecimalHours(cell.overtimeMinutes),
            tier = tier,
            colorRole = role,
        )
    }
}

internal fun formatDecimalHours(totalMinutes: Int): String {
    if (totalMinutes == 0) return ""
    return String.format(Locale.US, "%.1fh", totalMinutes / 60.0)
}

private fun resolveTier(pay: BigDecimal, maxPay: BigDecimal): CalendarCellIntensityTier {
    if (pay <= BigDecimal.ZERO || maxPay <= BigDecimal.ZERO) return CalendarCellIntensityTier.NONE

    val ratio = pay.divide(maxPay, 4, RoundingMode.HALF_UP).toFloat()
    return when {
        ratio < 0.33f -> CalendarCellIntensityTier.LOW
        ratio < 0.66f -> CalendarCellIntensityTier.MID
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

private fun resolveCompTier(totalMinutes: Int): CalendarCellIntensityTier {
    val ratio = (kotlin.math.abs(totalMinutes) / (8 * 60.0)).coerceIn(0.0, 1.0)
    return when {
        ratio < 0.34 -> CalendarCellIntensityTier.LOW
        ratio < 0.67 -> CalendarCellIntensityTier.MID
        else -> CalendarCellIntensityTier.HIGH
    }
}
