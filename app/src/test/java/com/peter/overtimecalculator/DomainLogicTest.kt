package com.peter.overtimecalculator

import com.peter.overtimecalculator.domain.ConfigPropagationPlanner
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HolidayCalendar
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.MonthlyConfig
import com.peter.overtimecalculator.domain.MonthlyOvertimeCalculator
import com.peter.overtimecalculator.domain.PayFormula
import com.peter.overtimecalculator.domain.ReverseHourlyRateCalculator
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainLogicTest {
    private val holidayCalendar = HolidayCalendar()

    @Test
    fun payFormula_usesConfiguredMultiplier() {
        val config = testConfig(hourlyRate = 50.0)

        assertEquals(150.0, PayFormula.calculatePay(config, minutes = 120, dayType = DayType.WORKDAY), 0.001)
        assertEquals(200.0, PayFormula.calculatePay(config, minutes = 120, dayType = DayType.REST_DAY), 0.001)
        assertEquals(300.0, PayFormula.calculatePay(config, minutes = 120, dayType = DayType.HOLIDAY), 0.001)
    }

    @Test
    fun holidayCalendar_userOverrideBeatsOfficialRules() {
        assertEquals(DayType.HOLIDAY, holidayCalendar.resolveDayType(LocalDate.parse("2026-10-01"), null))
        assertEquals(DayType.WORKDAY, holidayCalendar.resolveDayType(LocalDate.parse("2026-10-01"), DayType.WORKDAY))
        assertEquals(DayType.WORKDAY, holidayCalendar.resolveDayType(LocalDate.parse("2026-10-10"), null))
        assertEquals(DayType.REST_DAY, holidayCalendar.resolveDayType(LocalDate.parse("2026-03-07"), null))
    }

    @Test
    fun monthlyCalculator_summaryMatchesDailySum() {
        val config = testConfig(hourlyRate = 40.0)
        val calculator = MonthlyOvertimeCalculator(holidayCalendar)
        val result = calculator.calculate(
            yearMonth = YearMonth.of(2026, 10),
            config = config,
            entryMinutesByDate = mapOf(
                LocalDate.parse("2026-10-01") to 120,
                LocalDate.parse("2026-10-10") to 60,
            ),
            overrideTypesByDate = emptyMap(),
        )

        assertEquals(180, result.summary.totalMinutes)
        assertEquals(result.dayCells.sumOf { it.pay }, result.summary.totalPay, 0.001)
    }

    @Test
    fun reverseHourlyRateCalculator_handlesMixedDayTypes() {
        val calculator = ReverseHourlyRateCalculator(holidayCalendar)
        val result = calculator.calculate(
            yearMonth = YearMonth.of(2026, 10),
            overtimePayInput = 660.0,
            config = testConfig(hourlyRate = 0.0),
            entryMinutesByDate = mapOf(
                LocalDate.parse("2026-10-01") to 60,
                LocalDate.parse("2026-10-10") to 60,
                LocalDate.parse("2026-10-11") to 60,
            ),
            overrideTypesByDate = emptyMap(),
        )

        assertEquals(6.5, result.weightedHours, 0.001)
        assertEquals(101.54, result.hourlyRate, 0.001)
    }

    @Test
    fun configPropagationPlanner_updatesOnlyFutureUnlockedMonths() {
        val planner = ConfigPropagationPlanner()
        val selected = testConfig(yearMonth = YearMonth.of(2026, 3), hourlyRate = 88.0, lockedByUser = true)
        val planned = planner.plan(
            allConfigs = listOf(
                testConfig(yearMonth = YearMonth.of(2026, 2), hourlyRate = 50.0, lockedByUser = true),
                testConfig(yearMonth = YearMonth.of(2026, 4), hourlyRate = 60.0, lockedByUser = false),
                testConfig(yearMonth = YearMonth.of(2026, 5), hourlyRate = 70.0, lockedByUser = true),
            ),
            updatedSelected = selected,
        )

        assertEquals(2, planned.size)
        assertTrue(planned.any { it.yearMonth == YearMonth.of(2026, 3) && it.hourlyRate == 88.0 })
        assertTrue(planned.any { it.yearMonth == YearMonth.of(2026, 4) && it.hourlyRate == 88.0 && !it.lockedByUser })
    }

    private fun testConfig(
        yearMonth: YearMonth = YearMonth.of(2026, 10),
        hourlyRate: Double = 60.0,
        lockedByUser: Boolean = false,
    ): MonthlyConfig {
        return MonthlyConfig(
            yearMonth = yearMonth,
            hourlyRate = hourlyRate,
            rateSource = HourlyRateSource.MANUAL,
            weekdayRate = 1.5,
            restDayRate = 2.0,
            holidayRate = 3.0,
            lockedByUser = lockedByUser,
        )
    }
}
