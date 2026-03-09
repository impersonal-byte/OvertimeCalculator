package com.peter.overtimecalculator

import com.peter.overtimecalculator.data.holiday.HolidayTimorApiParser
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HolidayTimorApiParserTest {
    @Test
    fun parser_mapsHolidayAndAdjustedWorkdayFromTypePayload() {
        val yearRules = HolidayTimorApiParser.parseYear(
            """
            {
              "code": 0,
              "type": {
                "2026-10-01": { "type": 2, "name": "国庆节", "week": 4 },
                "2026-10-10": { "type": 3, "name": "国庆节后补班", "week": 6 },
                "2026-10-11": { "type": 1, "name": "周日", "week": 7 }
              }
            }
            """.trimIndent(),
        )

        assertTrue(LocalDate.parse("2026-10-01") in yearRules.holidayDates)
        assertTrue(LocalDate.parse("2026-10-10") in yearRules.workingDates)
        assertFalse(LocalDate.parse("2026-10-11") in yearRules.holidayDates)
        assertFalse(LocalDate.parse("2026-10-11") in yearRules.workingDates)
    }

    @Test
    fun parser_canFallbackToHolidayPayloadWhenTypePayloadIsMissing() {
        val yearRules = HolidayTimorApiParser.parseYear(
            """
            {
              "code": 0,
              "holiday": {
                "10-01": {
                  "holiday": true,
                  "name": "国庆节",
                  "date": "2026-10-01"
                },
                "10-10": {
                  "holiday": false,
                  "name": "国庆节后补班",
                  "date": "2026-10-10"
                },
                "10-11": {
                  "holiday": true,
                  "name": "周日",
                  "date": "2026-10-11"
                }
              }
            }
            """.trimIndent(),
        )

        assertTrue(LocalDate.parse("2026-10-01") in yearRules.holidayDates)
        assertTrue(LocalDate.parse("2026-10-10") in yearRules.workingDates)
        assertFalse(LocalDate.parse("2026-10-11") in yearRules.holidayDates)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parser_rejectsErrorResponses() {
        HolidayTimorApiParser.parseYear("""{"code":1}""")
    }
}
