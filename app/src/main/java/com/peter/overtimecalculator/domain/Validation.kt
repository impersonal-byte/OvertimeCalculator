package com.peter.overtimecalculator.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>

    data class Failure(val message: String) : DomainResult<Nothing>
}

interface OvertimeWriteGateway {
    suspend fun saveOvertime(date: LocalDate, minutes: Int, overrideDayType: DayType?)

    suspend fun updateManualHourlyRate(yearMonth: YearMonth, hourlyRate: BigDecimal)

    suspend fun updateMultipliers(
        yearMonth: YearMonth,
        weekdayRate: BigDecimal,
        restDayRate: BigDecimal,
        holidayRate: BigDecimal,
    )

    suspend fun reverseEngineerHourlyRate(
        yearMonth: YearMonth,
        overtimePayInput: BigDecimal,
    ): ReverseRateResult

    fun resolveDayType(date: LocalDate, overrideType: DayType?): DayType
}

object OvertimeEntryValidator {
    const val MAX_OVERTIME_MINUTES = 16 * 60
    const val MIN_COMP_MINUTES = -8 * 60

    fun validate(minutes: Int, resolvedDayType: DayType): DomainResult<Unit> {
        if (minutes !in MIN_COMP_MINUTES..MAX_OVERTIME_MINUTES) {
            return DomainResult.Failure("工时调整必须在 -8.0h 到 16.0h 之间")
        }
        if (minutes < 0 && resolvedDayType != DayType.WORKDAY) {
            return DomainResult.Failure("只有工作日才能申请调休")
        }
        return DomainResult.Success(Unit)
    }
}

object PayConfigValidator {
    fun validateManualHourlyRate(hourlyRate: BigDecimal): DomainResult<Unit> {
        return if (hourlyRate.isNonNegative()) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure("时薪不能小于 0")
        }
    }

    fun validateMultipliers(
        weekdayRate: BigDecimal,
        restDayRate: BigDecimal,
        holidayRate: BigDecimal,
    ): DomainResult<Unit> {
        return if (weekdayRate.isPositive() && restDayRate.isPositive() && holidayRate.isPositive()) {
            DomainResult.Success(Unit)
        } else {
            DomainResult.Failure("倍率必须大于 0")
        }
    }
}

suspend fun <T> domainResultOf(block: suspend () -> T): DomainResult<T> {
    return try {
        DomainResult.Success(block())
    } catch (error: IllegalArgumentException) {
        DomainResult.Failure(error.message ?: "操作失败")
    }
}
