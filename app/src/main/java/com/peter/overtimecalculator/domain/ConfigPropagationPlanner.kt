package com.peter.overtimecalculator.domain

class ConfigPropagationPlanner {
    fun plan(allConfigs: List<MonthlyConfig>, updatedSelected: MonthlyConfig): List<MonthlyConfig> {
        val futureUpdates = allConfigs
            .filter { it.yearMonth > updatedSelected.yearMonth && !it.lockedByUser }
            .map { future ->
                future.copy(
                    hourlyRate = updatedSelected.hourlyRate,
                    rateSource = updatedSelected.rateSource,
                    weekdayRate = updatedSelected.weekdayRate,
                    restDayRate = updatedSelected.restDayRate,
                    holidayRate = updatedSelected.holidayRate,
                )
            }

        return listOf(updatedSelected) + futureUpdates
    }
}
