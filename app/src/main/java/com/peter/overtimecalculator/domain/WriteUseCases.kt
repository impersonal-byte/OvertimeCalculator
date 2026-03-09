package com.peter.overtimecalculator.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class SaveOvertimeUseCase(
    private val gateway: OvertimeWriteGateway,
) {
    suspend operator fun invoke(
        date: LocalDate,
        minutes: Int,
        overrideDayType: DayType?,
    ): DomainResult<Unit> {
        val resolvedDayType = gateway.resolveDayType(date, overrideDayType)
        return when (val validation = OvertimeEntryValidator.validate(minutes, resolvedDayType)) {
            is DomainResult.Failure -> validation
            is DomainResult.Success -> domainResultOf {
                gateway.saveOvertime(date, minutes, overrideDayType)
            }
        }
    }
}

class UpdateManualHourlyRateUseCase(
    private val gateway: OvertimeWriteGateway,
) {
    suspend operator fun invoke(yearMonth: YearMonth, hourlyRate: BigDecimal): DomainResult<Unit> {
        return when (val validation = PayConfigValidator.validateManualHourlyRate(hourlyRate)) {
            is DomainResult.Failure -> validation
            is DomainResult.Success -> domainResultOf {
                gateway.updateManualHourlyRate(yearMonth, hourlyRate)
            }
        }
    }
}

class UpdateMultipliersUseCase(
    private val gateway: OvertimeWriteGateway,
) {
    suspend operator fun invoke(
        yearMonth: YearMonth,
        weekdayRate: BigDecimal,
        restDayRate: BigDecimal,
        holidayRate: BigDecimal,
    ): DomainResult<Unit> {
        return when (val validation = PayConfigValidator.validateMultipliers(weekdayRate, restDayRate, holidayRate)) {
            is DomainResult.Failure -> validation
            is DomainResult.Success -> domainResultOf {
                gateway.updateMultipliers(yearMonth, weekdayRate, restDayRate, holidayRate)
            }
        }
    }
}

class ReverseEngineerHourlyRateUseCase(
    private val gateway: OvertimeWriteGateway,
) {
    suspend operator fun invoke(
        yearMonth: YearMonth,
        overtimePayInput: BigDecimal,
    ): DomainResult<ReverseRateResult> {
        if (!overtimePayInput.isPositive()) {
            return DomainResult.Failure("请输入大于 0 的已发加班工资总额")
        }
        return domainResultOf {
            gateway.reverseEngineerHourlyRate(yearMonth, overtimePayInput)
        }
    }
}
