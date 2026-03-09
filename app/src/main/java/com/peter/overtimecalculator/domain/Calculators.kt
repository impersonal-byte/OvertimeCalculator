package com.peter.overtimecalculator.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

private data class MinutesByType(
    val workday: Int = 0,
    val restDay: Int = 0,
    val holiday: Int = 0,
) {
    val total: Int get() = workday + restDay + holiday

    fun plus(dayType: DayType, minutes: Int): MinutesByType {
        return when (dayType) {
            DayType.WORKDAY -> copy(workday = workday + minutes)
            DayType.REST_DAY -> copy(restDay = restDay + minutes)
            DayType.HOLIDAY -> copy(holiday = holiday + minutes)
        }
    }
}

private data class DeductionResult(
    val payableMinutes: MinutesByType,
    val uncoveredCompMinutes: Int,
)

private fun distributeMonthlyMinutes(
    yearMonth: YearMonth,
    entryMinutesByDate: Map<LocalDate, Int>,
    overrideTypesByDate: Map<LocalDate, DayType>,
    holidayCalendar: HolidayCalendar,
): Pair<MinutesByType, Int> {
    var positiveMinutes = MinutesByType()
    var compMinutes = 0

    (1..yearMonth.lengthOfMonth()).forEach { day ->
        val date = yearMonth.atDay(day)
        val minutes = entryMinutesByDate[date] ?: 0
        val dayType = holidayCalendar.resolveDayType(date, overrideTypesByDate[date])

        when {
            minutes > 0 -> positiveMinutes = positiveMinutes.plus(dayType, minutes)
            minutes < 0 -> compMinutes += -minutes
        }
    }

    return positiveMinutes to compMinutes
}

private fun applyCompTimeDeduction(positiveMinutes: MinutesByType, compMinutes: Int): DeductionResult {
    var remainingCompMinutes = compMinutes

    val payableWorkday = (positiveMinutes.workday - remainingCompMinutes).coerceAtLeast(0)
    remainingCompMinutes = (remainingCompMinutes - positiveMinutes.workday).coerceAtLeast(0)

    val payableRestDay = (positiveMinutes.restDay - remainingCompMinutes).coerceAtLeast(0)
    remainingCompMinutes = (remainingCompMinutes - positiveMinutes.restDay).coerceAtLeast(0)

    val payableHoliday = (positiveMinutes.holiday - remainingCompMinutes).coerceAtLeast(0)
    remainingCompMinutes = (remainingCompMinutes - positiveMinutes.holiday).coerceAtLeast(0)

    return DeductionResult(
        payableMinutes = MinutesByType(
            workday = payableWorkday,
            restDay = payableRestDay,
            holiday = payableHoliday,
        ),
        uncoveredCompMinutes = remainingCompMinutes,
    )
}

private fun calculatePayableTotalPay(config: MonthlyConfig, payableMinutes: MinutesByType): BigDecimal {
    return listOf(
        PayFormula.calculatePay(config, payableMinutes.workday, DayType.WORKDAY),
        PayFormula.calculatePay(config, payableMinutes.restDay, DayType.REST_DAY),
        PayFormula.calculatePay(config, payableMinutes.holiday, DayType.HOLIDAY),
    ).fold(ZeroDecimal) { total, pay -> total + pay }.toMoneyScale()
}

private fun calculateWeightedHours(config: MonthlyConfig, payableMinutes: MinutesByType): BigDecimal {
    return (
        payableMinutes.workday.toDecimalHours().multiply(PayFormula.multiplierFor(config, DayType.WORKDAY)) +
            payableMinutes.restDay.toDecimalHours().multiply(PayFormula.multiplierFor(config, DayType.REST_DAY)) +
            payableMinutes.holiday.toDecimalHours().multiply(PayFormula.multiplierFor(config, DayType.HOLIDAY))
        ).toInternalScale()
}

object PayFormula {
    fun multiplierFor(config: MonthlyConfig, dayType: DayType): BigDecimal {
        return when (dayType) {
            DayType.WORKDAY -> config.weekdayRate
            DayType.REST_DAY -> config.restDayRate
            DayType.HOLIDAY -> config.holidayRate
        }
    }

    fun calculatePay(config: MonthlyConfig, minutes: Int, dayType: DayType): BigDecimal {
        if (minutes <= 0 || config.hourlyRate.isZeroOrNegative()) return ZeroDecimal
        return config.hourlyRate
            .multiply(minutes.toDecimalHours())
            .multiply(multiplierFor(config, dayType))
            .toMoneyScale()
    }
}

class MonthlyOvertimeCalculator(private val holidayCalendar: HolidayCalendar) {
    fun calculate(
        yearMonth: YearMonth,
        config: MonthlyConfig,
        entryMinutesByDate: Map<LocalDate, Int>,
        overrideTypesByDate: Map<LocalDate, DayType>,
    ): ObservedMonth {
        val dayCells = (1..yearMonth.lengthOfMonth()).map { day ->
            val date = yearMonth.atDay(day)
            val minutes = entryMinutesByDate[date] ?: 0
            val dayType = holidayCalendar.resolveDayType(date, overrideTypesByDate[date])
            DayCellUiState(
                date = date,
                overtimeMinutes = minutes,
                dayType = dayType,
                pay = PayFormula.calculatePay(config, minutes, dayType),
            )
        }

        val (positiveMinutes, compMinutes) = distributeMonthlyMinutes(
            yearMonth = yearMonth,
            entryMinutesByDate = entryMinutesByDate,
            overrideTypesByDate = overrideTypesByDate,
            holidayCalendar = holidayCalendar,
        )
        val deductionResult = applyCompTimeDeduction(positiveMinutes, compMinutes)

        return ObservedMonth(
            config = config,
            summary = MonthlySummaryUiState(
                totalMinutes = positiveMinutes.total - compMinutes,
                totalPay = calculatePayableTotalPay(config, deductionResult.payableMinutes),
                yearMonth = yearMonth,
                hourlyRate = config.hourlyRate,
                rateSource = config.rateSource,
                uncoveredCompMinutes = deductionResult.uncoveredCompMinutes,
                grossOvertimeMinutes = positiveMinutes.total,
                compMinutes = compMinutes,
            ),
            dayCells = dayCells,
            entryMinutesByDate = entryMinutesByDate,
            overrideTypesByDate = overrideTypesByDate,
        )
    }
}

class ReverseHourlyRateCalculator(private val holidayCalendar: HolidayCalendar) {
    fun calculate(
        yearMonth: YearMonth,
        overtimePayInput: BigDecimal,
        config: MonthlyConfig,
        entryMinutesByDate: Map<LocalDate, Int>,
        overrideTypesByDate: Map<LocalDate, DayType>,
    ): ReverseRateResult {
        val (positiveMinutes, compMinutes) = distributeMonthlyMinutes(
            yearMonth = yearMonth,
            entryMinutesByDate = entryMinutesByDate,
            overrideTypesByDate = overrideTypesByDate,
            holidayCalendar = holidayCalendar,
        )
        val weightedHours = calculateWeightedHours(
            config = config,
            payableMinutes = applyCompTimeDeduction(positiveMinutes, compMinutes).payableMinutes,
        )

        require(weightedHours.isPositive()) { "当前月份没有可用于反推的有效加班余额" }
        require(overtimePayInput.isPositive()) { "请输入大于 0 的已发加班工资总额" }

        return ReverseRateResult(
            hourlyRate = overtimePayInput
                .divide(weightedHours, INTERNAL_SCALE, RoundingMode.HALF_UP)
                .toMoneyScale(),
            weightedHours = weightedHours,
            overtimePayInput = overtimePayInput.toMoneyScale(),
        )
    }
}
