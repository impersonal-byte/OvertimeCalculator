package com.peter.overtimecalculator

import com.peter.overtimecalculator.data.holiday.HolidayHaoshenqiApiParser
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HolidayHaoshenqiApiParserTest {
    @Test
    fun parser_mapsHolidayAndAdjustedWorkdayFromArrayPayload() {
        val yearRules = HolidayHaoshenqiApiParser.parse(
            """
            [
              { "date": "2026-10-01", "status": 3 },
              { "date": "2026-10-10", "status": 2 },
              { "date": "2026-10-11", "status": 1 }
            ]
            """.trimIndent(),
        )

        assertTrue(LocalDate.parse("2026-10-01") in yearRules.holidayDates)
        assertTrue(LocalDate.parse("2026-10-10") in yearRules.workingDates)
        assertFalse(LocalDate.parse("2026-10-11") in yearRules.holidayDates)
        assertFalse(LocalDate.parse("2026-10-11") in yearRules.workingDates)
    }

    @Test
    fun parser_acceptsMonthAndDayPayloadsBecausePayloadShapeMatches() {
        val yearRules = HolidayHaoshenqiApiParser.parse(
            """
            [
              { "date": "2026-10-01", "status": 3 },
              { "date": "2026-10-02", "status": 0 },
              { "date": "2026-10-10", "status": 2 }
            ]
            """.trimIndent(),
        )

        assertTrue(LocalDate.parse("2026-10-01") in yearRules.holidayDates)
        assertTrue(LocalDate.parse("2026-10-10") in yearRules.workingDates)
        assertFalse(LocalDate.parse("2026-10-02") in yearRules.holidayDates)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parser_rejectsNonArrayPayloads() {
        HolidayHaoshenqiApiParser.parse("""{"date":"2026-10-01","status":3}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun parser_rejectsEntriesMissingRequiredFields() {
        HolidayHaoshenqiApiParser.parse("""[{ "date": "2026-10-01" }]""")
    }
}
