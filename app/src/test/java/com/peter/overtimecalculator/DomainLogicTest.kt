package com.peter.overtimecalculator

import com.peter.overtimecalculator.data.holiday.HolidayRulesJsonParser
import com.peter.overtimecalculator.domain.ConfigPropagationPlanner
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HolidayCalendar
import com.peter.overtimecalculator.domain.HolidayRulesSnapshot
import com.peter.overtimecalculator.domain.HolidayYearRules
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.MonthlyConfig
import com.peter.overtimecalculator.domain.MonthlyOvertimeCalculator
import com.peter.overtimecalculator.domain.PayFormula
import com.peter.overtimecalculator.domain.ReverseHourlyRateCalculator
import com.peter.overtimecalculator.domain.decimal
import com.peter.overtimecalculator.ui.CalendarCellColorRole
import com.peter.overtimecalculator.ui.CalendarCellIntensityTier
import com.peter.overtimecalculator.ui.buildCalendarCellPresentations
import com.peter.overtimecalculator.ui.formatDecimalHours
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainLogicTest {
    private val holidayCalendar = HolidayCalendar {
        HolidayRulesSnapshot(
            schemaVersion = 1,
            updatedAt = "2026-03-09T00:00:00Z",
            years = mapOf(
                2026 to HolidayYearRules(
                    holidayDates = setOf(LocalDate.parse("2026-10-01")),
                    workingDates = setOf(LocalDate.parse("2026-10-10")),
                ),
            ),
        )
    }

    @Test
    fun payFormula_usesConfiguredMultiplier() {
        val config = testConfig(hourlyRate = decimal("50.00"))

        assertDecimalEquals("150.00", PayFormula.calculatePay(config, minutes = 120, dayType = DayType.WORKDAY))
        assertDecimalEquals("200.00", PayFormula.calculatePay(config, minutes = 120, dayType = DayType.REST_DAY))
        assertDecimalEquals("300.00", PayFormula.calculatePay(config, minutes = 120, dayType = DayType.HOLIDAY))
    }

    @Test
    fun holidayCalendar_userOverrideBeatsOfficialRules() {
        assertEquals(DayType.HOLIDAY, holidayCalendar.resolveDayType(LocalDate.parse("2026-10-01"), null))
        assertEquals(DayType.WORKDAY, holidayCalendar.resolveDayType(LocalDate.parse("2026-10-01"), DayType.WORKDAY))
        assertEquals(DayType.WORKDAY, holidayCalendar.resolveDayType(LocalDate.parse("2026-10-10"), null))
        assertEquals(DayType.REST_DAY, holidayCalendar.resolveDayType(LocalDate.parse("2026-03-07"), null))
    }

    @Test
    fun holidayCalendar_resolvesOfficialOffWeekdaysAsRestDaysFromSchemaVersion1Rules() {
        val calendar = HolidayCalendar {
            HolidayRulesJsonParser.parse(
                """
                {
                  "schemaVersion": 1,
                  "updatedAt": "2026-03-09T00:00:00Z",
                  "years": {
                    "2026": {
                      "holidayDates": ["2026-10-01"],
                      "restDates": ["2026-10-06"],
                      "workingDates": ["2026-10-10"]
                    }
                  }
                }
                """.trimIndent(),
            )
        }

        assertEquals(DayType.REST_DAY, calendar.resolveDayType(LocalDate.parse("2026-10-06"), null))
    }

    @Test
    fun monthlyCalculator_deductsCompTimeByPriority() {
        val config = testConfig(hourlyRate = decimal("40.00"))
        val calculator = MonthlyOvertimeCalculator(holidayCalendar)
        val result = calculator.calculate(
            yearMonth = YearMonth.of(2026, 10),
            config = config,
            entryMinutesByDate = mapOf(
                LocalDate.parse("2026-10-08") to 120,
                LocalDate.parse("2026-10-10") to 180,
                LocalDate.parse("2026-10-01") to 240,
                LocalDate.parse("2026-10-09") to -180,
            ),
            overrideTypesByDate = emptyMap(),
        )

        assertEquals(540, result.summary.grossOvertimeMinutes)
        assertEquals(180, result.summary.compMinutes)
        assertEquals(360, result.summary.totalMinutes)
        assertDecimalEquals("600.00", result.summary.totalPay)
        assertEquals(0, result.summary.uncoveredCompMinutes)
    }

    @Test
    fun monthlyCalculator_flagsCompTimeThatExceedsMonthlyBalance() {
        val calculator = MonthlyOvertimeCalculator(holidayCalendar)
        val result = calculator.calculate(
            yearMonth = YearMonth.of(2026, 3),
            config = testConfig(hourlyRate = decimal("50.00")),
            entryMinutesByDate = mapOf(
                LocalDate.parse("2026-03-02") to 120,
                LocalDate.parse("2026-03-03") to -240,
            ),
            overrideTypesByDate = emptyMap(),
        )

        assertEquals(-120, result.summary.totalMinutes)
        assertDecimalEquals("0.00", result.summary.totalPay)
        assertEquals(120, result.summary.uncoveredCompMinutes)
    }

    @Test
    fun monthlyCalculator_usesRestDayRateForOfficialOffWeekdays() {
        val calculator = MonthlyOvertimeCalculator(
            HolidayCalendar {
                HolidayRulesJsonParser.parse(
                    """
                    {
                      "schemaVersion": 1,
                      "updatedAt": "2026-03-09T00:00:00Z",
                      "years": {
                        "2026": {
                          "holidayDates": ["2026-10-01"],
                          "restDates": ["2026-10-06"],
                          "workingDates": ["2026-10-10"]
                        }
                      }
                    }
                    """.trimIndent(),
                )
            },
        )
        val result = calculator.calculate(
            yearMonth = YearMonth.of(2026, 10),
            config = testConfig(hourlyRate = decimal("40.00")),
            entryMinutesByDate = mapOf(LocalDate.parse("2026-10-06") to 240),
            overrideTypesByDate = emptyMap(),
        )

        assertEquals(
            DayType.REST_DAY,
            result.dayCells.first { it.date == LocalDate.parse("2026-10-06") }.dayType,
        )
        assertDecimalEquals("320.00", result.summary.totalPay)
    }

    @Test
    fun reverseHourlyRateCalculator_usesPayableMinutesAfterCompDeduction() {
        val calculator = ReverseHourlyRateCalculator(holidayCalendar)
        val result = calculator.calculate(
            yearMonth = YearMonth.of(2026, 10),
            overtimePayInput = decimal("330.00"),
            config = testConfig(hourlyRate = decimal("0.00")),
            entryMinutesByDate = mapOf(
                LocalDate.parse("2026-10-08") to 120,
                LocalDate.parse("2026-10-10") to 120,
                LocalDate.parse("2026-10-01") to 180,
                LocalDate.parse("2026-10-09") to -180,
            ),
            overrideTypesByDate = emptyMap(),
        )

        assertDecimalEquals("10.50000000", result.weightedHours)
        assertDecimalEquals("31.43", result.hourlyRate)
    }

    @Test
    fun calendarCellPresentation_formatsPositiveNegativeAndZeroHours() {
        assertEquals("2.5h", formatDecimalHours(150))
        assertEquals("-2.0h", formatDecimalHours(-120))
        assertEquals("", formatDecimalHours(0))
    }

    @Test
    fun calendarCellPresentation_marksCompTimeSeparately() {
        val presentation = buildCalendarCellPresentations(
            listOf(dayCell("2026-03-03", -120, DayType.WORKDAY, decimal("0.00"))),
        ).getValue(LocalDate.parse("2026-03-03"))

        assertEquals(CalendarCellColorRole.COMP_TIME, presentation.colorRole)
        assertEquals("-2.0h", presentation.hoursLabel)
        assertTrue(presentation.tier != CalendarCellIntensityTier.NONE)
    }

    @Test
    fun configPropagationPlanner_updatesOnlyFutureUnlockedMonths() {
        val planner = ConfigPropagationPlanner()
        val selected = testConfig(yearMonth = YearMonth.of(2026, 3), hourlyRate = decimal("88.00"), lockedByUser = true)
        val planned = planner.plan(
            allConfigs = listOf(
                testConfig(yearMonth = YearMonth.of(2026, 2), hourlyRate = decimal("50.00"), lockedByUser = true),
                testConfig(yearMonth = YearMonth.of(2026, 4), hourlyRate = decimal("60.00"), lockedByUser = false),
                testConfig(yearMonth = YearMonth.of(2026, 5), hourlyRate = decimal("70.00"), lockedByUser = true),
            ),
            updatedSelected = selected,
        )

        assertEquals(2, planned.size)
        assertTrue(planned.any { it.yearMonth == YearMonth.of(2026, 3) && it.hourlyRate.compareTo(decimal("88.00")) == 0 })
        assertTrue(planned.any { it.yearMonth == YearMonth.of(2026, 4) && it.hourlyRate.compareTo(decimal("88.00")) == 0 && !it.lockedByUser })
    }

    private fun testConfig(
        yearMonth: YearMonth = YearMonth.of(2026, 10),
        hourlyRate: BigDecimal = decimal("60.00"),
        lockedByUser: Boolean = false,
    ): MonthlyConfig {
        return MonthlyConfig(
            yearMonth = yearMonth,
            hourlyRate = hourlyRate,
            rateSource = HourlyRateSource.MANUAL,
            weekdayRate = decimal("1.50"),
            restDayRate = decimal("2.00"),
            holidayRate = decimal("3.00"),
            lockedByUser = lockedByUser,
        )
    }

    private fun dayCell(date: String, minutes: Int, dayType: DayType, pay: BigDecimal) =
        com.peter.overtimecalculator.domain.DayCellUiState(
            date = LocalDate.parse(date),
            overtimeMinutes = minutes,
            dayType = dayType,
            pay = pay,
        )

    private fun assertDecimalEquals(expected: String, actual: BigDecimal) {
        assertEquals(0, actual.compareTo(BigDecimal(expected)))
    }
}
