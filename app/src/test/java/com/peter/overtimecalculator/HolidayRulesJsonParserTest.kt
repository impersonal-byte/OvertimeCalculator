package com.peter.overtimecalculator

import com.peter.overtimecalculator.data.holiday.HolidayRulesJsonParser
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HolidayRulesJsonParserTest {
    @Test
    fun parser_readsHolidayAndWorkingDates() {
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

        val yearRules = snapshot.years.getValue(2026)
        assertEquals("2026-03-09T00:00:00Z", snapshot.updatedAt)
        assertTrue(LocalDate.parse("2026-10-01") in yearRules.holidayDates)
        assertTrue(LocalDate.parse("2026-10-10") in yearRules.workingDates)
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
