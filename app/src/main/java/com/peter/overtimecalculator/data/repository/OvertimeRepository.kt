package com.peter.overtimecalculator.data.repository

import androidx.room.withTransaction
import com.peter.overtimecalculator.data.db.AppDatabase
import com.peter.overtimecalculator.data.db.HolidayOverrideEntity
import com.peter.overtimecalculator.data.db.MonthlyConfigEntity
import com.peter.overtimecalculator.data.db.OvertimeDao
import com.peter.overtimecalculator.data.db.OvertimeEntryEntity
import com.peter.overtimecalculator.data.db.toDomain
import com.peter.overtimecalculator.data.db.toEntity
import com.peter.overtimecalculator.data.db.toPair
import com.peter.overtimecalculator.domain.ConfigPropagationPlanner
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HolidayCalendar
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.MonthlyConfig
import com.peter.overtimecalculator.domain.MonthlyOvertimeCalculator
import com.peter.overtimecalculator.domain.ObservedMonth
import com.peter.overtimecalculator.domain.ReverseHourlyRateCalculator
import com.peter.overtimecalculator.domain.ReverseRateResult
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class OvertimeRepository(
    private val database: AppDatabase,
    private val dao: OvertimeDao,
    private val holidayCalendar: HolidayCalendar,
    private val configPropagationPlanner: ConfigPropagationPlanner,
    private val monthlyOvertimeCalculator: MonthlyOvertimeCalculator,
    private val reverseHourlyRateCalculator: ReverseHourlyRateCalculator,
) {
    fun observeMonth(yearMonth: YearMonth): Flow<ObservedMonth> {
        val startDate = yearMonth.atDay(1).toString()
        val endDate = yearMonth.atEndOfMonth().toString()
        return combine(
            dao.observeAllConfigs(),
            dao.observeEntriesInRange(startDate, endDate),
            dao.observeOverridesInRange(startDate, endDate),
        ) { configs, entries, overrides ->
            val resolvedConfig = resolveConfig(configs.map(MonthlyConfigEntity::toDomain), yearMonth)
            monthlyOvertimeCalculator.calculate(
                yearMonth = yearMonth,
                config = resolvedConfig,
                entryMinutesByDate = entries.associate(OvertimeEntryEntity::toPair),
                overrideTypesByDate = overrides.associate(HolidayOverrideEntity::toPair),
            )
        }
    }

    suspend fun ensureMonthExists(yearMonth: YearMonth) {
        database.withTransaction {
            ensureMaterializedConfig(yearMonth, dao.getAllConfigs().map(MonthlyConfigEntity::toDomain))
        }
    }

    suspend fun saveOvertime(date: LocalDate, minutes: Int, overrideDayType: DayType?) {
        database.withTransaction {
            ensureMaterializedConfig(date.toYearMonth(), dao.getAllConfigs().map(MonthlyConfigEntity::toDomain))
            if (minutes > 0) {
                dao.upsertEntry(OvertimeEntryEntity(date = date.toString(), minutes = minutes))
            } else {
                dao.deleteEntry(date.toString())
            }

            if (overrideDayType == null) {
                dao.deleteHolidayOverride(date.toString())
            } else {
                dao.upsertHolidayOverride(HolidayOverrideEntity(date = date.toString(), dayType = overrideDayType))
            }
        }
    }

    suspend fun updateManualHourlyRate(yearMonth: YearMonth, hourlyRate: Double) {
        require(hourlyRate >= 0.0) { "时薪不能小于 0" }
        updateConfig(yearMonth) { base ->
            base.copy(
                hourlyRate = hourlyRate,
                rateSource = HourlyRateSource.MANUAL,
            )
        }
    }

    suspend fun updateMultipliers(
        yearMonth: YearMonth,
        weekdayRate: Double,
        restDayRate: Double,
        holidayRate: Double,
    ) {
        require(weekdayRate > 0.0 && restDayRate > 0.0 && holidayRate > 0.0) { "倍率必须大于 0" }
        updateConfig(yearMonth) { base ->
            base.copy(
                weekdayRate = weekdayRate,
                restDayRate = restDayRate,
                holidayRate = holidayRate,
            )
        }
    }

    suspend fun reverseEngineerHourlyRate(
        yearMonth: YearMonth,
        overtimePayInput: Double,
    ): ReverseRateResult {
        return database.withTransaction {
            val configs = dao.getAllConfigs().map(MonthlyConfigEntity::toDomain)
            val selectedConfig = ensureMaterializedConfig(yearMonth, configs)
            val startDate = yearMonth.atDay(1).toString()
            val endDate = yearMonth.atEndOfMonth().toString()
            val entries = dao.getEntriesInRange(startDate, endDate).associate(OvertimeEntryEntity::toPair)
            val overrides = dao.getOverridesInRange(startDate, endDate).associate(HolidayOverrideEntity::toPair)
            val result = reverseHourlyRateCalculator.calculate(
                yearMonth = yearMonth,
                overtimePayInput = overtimePayInput,
                config = selectedConfig,
                entryMinutesByDate = entries,
                overrideTypesByDate = overrides,
            )

            applyUpdatedConfig(
                allConfigs = dao.getAllConfigs().map(MonthlyConfigEntity::toDomain),
                updatedSelected = selectedConfig.copy(
                    hourlyRate = result.hourlyRate,
                    rateSource = HourlyRateSource.REVERSE_ENGINEERED,
                    lockedByUser = true,
                ),
            )
            result
        }
    }

    fun resolveDayType(date: LocalDate, overrideType: DayType?): DayType {
        return holidayCalendar.resolveDayType(date, overrideType)
    }

    private suspend fun updateConfig(
        yearMonth: YearMonth,
        transform: (MonthlyConfig) -> MonthlyConfig,
    ) {
        database.withTransaction {
            val allConfigs = dao.getAllConfigs().map(MonthlyConfigEntity::toDomain)
            val selectedConfig = ensureMaterializedConfig(yearMonth, allConfigs)
            applyUpdatedConfig(
                allConfigs = dao.getAllConfigs().map(MonthlyConfigEntity::toDomain),
                updatedSelected = transform(selectedConfig).copy(lockedByUser = true),
            )
        }
    }

    private suspend fun applyUpdatedConfig(
        allConfigs: List<MonthlyConfig>,
        updatedSelected: MonthlyConfig,
    ) {
        dao.upsertConfigs(configPropagationPlanner.plan(allConfigs, updatedSelected).map(MonthlyConfig::toEntity))
    }

    private suspend fun ensureMaterializedConfig(
        yearMonth: YearMonth,
        allConfigs: List<MonthlyConfig>,
    ): MonthlyConfig {
        val existing = allConfigs.firstOrNull { it.yearMonth == yearMonth }
        if (existing != null) return existing

        val resolved = resolveConfig(allConfigs, yearMonth).copy(yearMonth = yearMonth, lockedByUser = false)
        dao.upsertConfig(resolved.toEntity())
        return resolved
    }

    private fun resolveConfig(allConfigs: List<MonthlyConfig>, yearMonth: YearMonth): MonthlyConfig {
        return allConfigs
            .sortedBy { it.yearMonth }
            .lastOrNull { it.yearMonth <= yearMonth }
            ?: defaultConfig(yearMonth)
    }

    private fun defaultConfig(yearMonth: YearMonth): MonthlyConfig {
        return MonthlyConfig(
            yearMonth = yearMonth,
            hourlyRate = 0.0,
            rateSource = HourlyRateSource.MANUAL,
            weekdayRate = 1.5,
            restDayRate = 2.0,
            holidayRate = 3.0,
            lockedByUser = false,
        )
    }

    private fun LocalDate.toYearMonth(): YearMonth = YearMonth.of(year, month)
}
