package com.peter.overtimecalculator.domain

import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

private fun Double.roundCurrency(): Double = (this * 100.0).roundToInt() / 100.0

class HolidayCalendar {
    private val officialHolidayDates = setOf(
        "2026-01-01", "2026-01-02", "2026-01-03",
        "2026-02-15", "2026-02-16", "2026-02-17", "2026-02-18", "2026-02-19",
        "2026-02-20", "2026-02-21", "2026-02-22", "2026-02-23",
        "2026-04-04", "2026-04-05", "2026-04-06",
        "2026-05-01", "2026-05-02", "2026-05-03", "2026-05-04", "2026-05-05",
        "2026-06-19", "2026-06-20", "2026-06-21",
        "2026-09-25", "2026-09-26", "2026-09-27",
        "2026-10-01", "2026-10-02", "2026-10-03", "2026-10-04", "2026-10-05",
        "2026-10-06", "2026-10-07",
        "2027-01-01", "2027-02-06", "2027-05-01", "2027-06-09", "2027-09-15",
        "2027-10-01",
        "2028-01-01", "2028-01-26", "2028-05-01", "2028-05-28", "2028-10-01",
        "2028-10-03",
        "2029-01-01", "2029-02-13", "2029-05-01", "2029-06-16", "2029-09-22",
        "2029-10-01",
        "2030-01-01", "2030-02-03", "2030-05-01", "2030-06-05", "2030-09-12",
        "2030-10-01",
    ).map(LocalDate::parse).toSet()

    private val officialWorkingDates = setOf(
        "2026-01-04", "2026-02-14", "2026-02-28", "2026-05-09", "2026-09-20",
        "2026-10-10",
    ).map(LocalDate::parse).toSet()

    fun resolveDayType(date: LocalDate, override: DayType?): DayType {
        if (override != null) return override
        if (date in officialHolidayDates) return DayType.HOLIDAY
        if (date in officialWorkingDates) return DayType.WORKDAY
        return when (date.dayOfWeek.value) {
            6, 7 -> DayType.REST_DAY
            else -> DayType.WORKDAY
        }
    }
}

object PayFormula {
    fun multiplierFor(config: MonthlyConfig, dayType: DayType): Double {
        return when (dayType) {
            DayType.WORKDAY -> config.weekdayRate
            DayType.REST_DAY -> config.restDayRate
            DayType.HOLIDAY -> config.holidayRate
        }
    }

    fun calculatePay(config: MonthlyConfig, minutes: Int, dayType: DayType): Double {
        if (minutes <= 0 || config.hourlyRate <= 0.0) return 0.0
        return (config.hourlyRate * (minutes / 60.0) * multiplierFor(config, dayType)).roundCurrency()
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

        return ObservedMonth(
            config = config,
            summary = MonthlySummaryUiState(
                totalMinutes = dayCells.sumOf { it.overtimeMinutes },
                totalPay = dayCells.sumOf { it.pay }.roundCurrency(),
                yearMonth = yearMonth,
                hourlyRate = config.hourlyRate,
                rateSource = config.rateSource,
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
        overtimePayInput: Double,
        config: MonthlyConfig,
        entryMinutesByDate: Map<LocalDate, Int>,
        overrideTypesByDate: Map<LocalDate, DayType>,
    ): ReverseRateResult {
        val weightedHours = (1..yearMonth.lengthOfMonth()).sumOf { day ->
            val date = yearMonth.atDay(day)
            val minutes = entryMinutesByDate[date] ?: 0
            if (minutes <= 0) {
                0.0
            } else {
                val dayType = holidayCalendar.resolveDayType(date, overrideTypesByDate[date])
                (minutes / 60.0) * PayFormula.multiplierFor(config, dayType)
            }
        }

        require(weightedHours > 0.0) { "当前月份还没有可用于反推的加班明细" }
        require(overtimePayInput > 0.0) { "请输入大于 0 的已发加班工资" }

        return ReverseRateResult(
            hourlyRate = (overtimePayInput / weightedHours).roundCurrency(),
            weightedHours = weightedHours,
            overtimePayInput = overtimePayInput.roundCurrency(),
        )
    }
}

