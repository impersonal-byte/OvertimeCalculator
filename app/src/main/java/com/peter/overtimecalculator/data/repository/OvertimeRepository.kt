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
import com.peter.overtimecalculator.data.holiday.HolidayRulesRepository
import com.peter.overtimecalculator.domain.ConfigPropagationPlanner
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HolidayCalendar
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.MonthlyConfig
import com.peter.overtimecalculator.domain.MonthlyOvertimeCalculator
import com.peter.overtimecalculator.domain.ObservedMonth
import com.peter.overtimecalculator.domain.OvertimeWriteGateway
import com.peter.overtimecalculator.domain.ReverseHourlyRateCalculator
import com.peter.overtimecalculator.domain.ReverseRateResult
import com.peter.overtimecalculator.domain.ZeroDecimal
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class OvertimeRepository(
    private val database: AppDatabase,
    private val dao: OvertimeDao,
    private val holidayCalendar: HolidayCalendar,
    private val holidayRulesRepository: HolidayRulesRepository,
    private val configPropagationPlanner: ConfigPropagationPlanner,
    private val monthlyOvertimeCalculator: MonthlyOvertimeCalculator,
    private val reverseHourlyRateCalculator: ReverseHourlyRateCalculator,
) : OvertimeWriteGateway {
    fun observeMonth(yearMonth: YearMonth): Flow<ObservedMonth> {
        val startDate = yearMonth.atDay(1).toString()
        val endDate = yearMonth.atEndOfMonth().toString()
        return combine(
            dao.observeAllConfigs(),
            dao.observeEntriesInRange(startDate, endDate),
            dao.observeOverridesInRange(startDate, endDate),
            holidayRulesRepository.rules,
        ) { configs, entries, overrides, _ ->
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

    override suspend fun saveOvertime(date: LocalDate, minutes: Int, overrideDayType: DayType?) {
        database.withTransaction {
            ensureMaterializedConfig(date.toYearMonth(), dao.getAllConfigs().map(MonthlyConfigEntity::toDomain))

            if (minutes != 0) {
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

    override suspend fun updateManualHourlyRate(yearMonth: YearMonth, hourlyRate: BigDecimal) {
        updateConfig(yearMonth) { base ->
            base.copy(
                hourlyRate = hourlyRate,
                rateSource = HourlyRateSource.MANUAL,
            )
        }
    }

    override suspend fun updateMultipliers(
        yearMonth: YearMonth,
        weekdayRate: BigDecimal,
        restDayRate: BigDecimal,
        holidayRate: BigDecimal,
    ) {
        updateConfig(yearMonth) { base ->
            base.copy(
                weekdayRate = weekdayRate,
                restDayRate = restDayRate,
                holidayRate = holidayRate,
            )
        }
    }

    override suspend fun reverseEngineerHourlyRate(
        yearMonth: YearMonth,
        overtimePayInput: BigDecimal,
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

    override fun resolveDayType(date: LocalDate, overrideType: DayType?): DayType {
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
            hourlyRate = ZeroDecimal,
            rateSource = HourlyRateSource.MANUAL,
            weekdayRate = BigDecimal("1.50"),
            restDayRate = BigDecimal("2.00"),
            holidayRate = BigDecimal("3.00"),
            lockedByUser = false,
        )
    }

    private fun LocalDate.toYearMonth(): YearMonth = YearMonth.of(year, month)
}
