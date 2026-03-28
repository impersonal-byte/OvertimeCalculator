package com.peter.overtimecalculator

import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HolidayCalendar
import com.peter.overtimecalculator.domain.HolidayRulesSnapshot
import com.peter.overtimecalculator.domain.HolidayYearRules
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TimorHolidayApiParserTest {
    @Test
    fun parser_mapsWageAwareEntriesIntoHolidayRestAndWorkdaySemantics() {
        val parserClass = Class.forName("com.peter.overtimecalculator.data.holiday.TimorHolidayApiParser")
        val parserInstance = parserClass.getField("INSTANCE").get(null)
        val parseMethod = parserClass.getMethod("parse", String::class.java)
        val yearRules = parseMethod.invoke(
            parserInstance,
            """
            {
              "holiday": {
                "10-01": { "holiday": true, "name": "国庆节", "wage": 3, "date": "2026-10-01" },
                "10-06": { "holiday": true, "name": "国庆节", "wage": 2, "date": "2026-10-06" },
                "10-10": { "holiday": false, "name": "国庆节后补班", "wage": 1, "after": true, "target": "国庆节", "date": "2026-10-10" }
              }
            }
            """.trimIndent(),
        ) as HolidayYearRules

        val calendar = HolidayCalendar {
            HolidayRulesSnapshot(
                schemaVersion = 2,
                updatedAt = "2026-03-09T00:00:00Z",
                years = mapOf(2026 to yearRules),
            )
        }

        assertEquals(DayType.HOLIDAY, calendar.resolveDayType(LocalDate.parse("2026-10-01"), null))
        assertEquals(DayType.REST_DAY, calendar.resolveDayType(LocalDate.parse("2026-10-06"), null))
        assertEquals(DayType.WORKDAY, calendar.resolveDayType(LocalDate.parse("2026-10-10"), null))
    }
}
