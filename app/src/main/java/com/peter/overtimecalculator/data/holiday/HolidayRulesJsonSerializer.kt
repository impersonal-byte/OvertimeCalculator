package com.peter.overtimecalculator.data.holiday

import com.peter.overtimecalculator.domain.HolidayRulesSnapshot
import java.time.LocalDate

object HolidayRulesJsonSerializer {
    fun serialize(snapshot: HolidayRulesSnapshot): String {
        val yearsJson = snapshot.years.toSortedMap().entries.joinToString(separator = ",") { (year, rules) ->
            buildString {
                append("\"")
                append(year)
                append("\":{")
                append("\"holidayDates\":")
                append(rules.holidayDates.toJsonArray())
                append(",")
                append("\"restDates\":")
                append(rules.restDates.toJsonArray())
                append(",")
                append("\"workingDates\":")
                append(rules.workingDates.toJsonArray())
                append("}")
            }
        }

        return buildString {
            append("{")
            append("\"schemaVersion\":")
            append(snapshot.schemaVersion)
            append(",")
            append("\"updatedAt\":\"")
            append(snapshot.updatedAt.escapeJson())
            append("\",")
            append("\"years\":{")
            append(yearsJson)
            append("}")
            append("}")
        }
    }
}

private fun Set<LocalDate>.toJsonArray(): String {
    return toList()
        .sorted()
        .joinToString(separator = ",", prefix = "[", postfix = "]") { "\"$it\"" }
}

private fun String.escapeJson(): String {
    return replace("\\", "\\\\")
        .replace("\"", "\\\"")
}
