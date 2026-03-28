package com.peter.overtimecalculator

import com.peter.overtimecalculator.data.holiday.HolidayRulesJsonParser
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HolidayCalendar
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class HolidayRulesJsonParserTest {
    @Test
    fun parser_readsHolidayRestAndWorkingDatesFromSchemaVersion1() {
        val snapshot = HolidayRulesJsonParser.parse(
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

        val calendar = HolidayCalendar { snapshot }
        assertEquals("2026-03-09T00:00:00Z", snapshot.updatedAt)
        assertEquals(DayType.HOLIDAY, calendar.resolveDayType(LocalDate.parse("2026-10-01"), null))
        assertEquals(DayType.REST_DAY, calendar.resolveDayType(LocalDate.parse("2026-10-06"), null))
        assertEquals(DayType.WORKDAY, calendar.resolveDayType(LocalDate.parse("2026-10-10"), null))
    }

    @Test
    fun parser_keepsSchemaVersion1ReadableByDefaultingRestDatesToEmpty() {
        val snapshot = HolidayRulesJsonParser.parse(
            """
            {
              "schemaVersion": 1,
              "updatedAt": "2026-03-09T00:00:00Z",
              "years": {
                "2026": {
                  "holidayDates": ["2026-10-01"],
                  "workingDates": ["2026-10-10"]
                }
              }
            }
            """.trimIndent(),
        )

        val calendar = HolidayCalendar { snapshot }
        assertEquals(DayType.HOLIDAY, calendar.resolveDayType(LocalDate.parse("2026-10-01"), null))
        assertEquals(DayType.WORKDAY, calendar.resolveDayType(LocalDate.parse("2026-10-06"), null))
        assertEquals(DayType.WORKDAY, calendar.resolveDayType(LocalDate.parse("2026-10-10"), null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun parser_rejectsUnsupportedSchemaVersion() {
        HolidayRulesJsonParser.parse(
            """
            {
              "schemaVersion": 2,
              "updatedAt": "2026-03-09T00:00:00Z",
              "years": {}
            }
            """.trimIndent(),
        )
    }
}
