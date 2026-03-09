package com.peter.overtimecalculator

import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.DomainResult
import com.peter.overtimecalculator.domain.OvertimeEntryValidator
import com.peter.overtimecalculator.domain.OvertimeWriteGateway
import com.peter.overtimecalculator.domain.PayConfigValidator
import com.peter.overtimecalculator.domain.ReverseEngineerHourlyRateUseCase
import com.peter.overtimecalculator.domain.ReverseRateResult
import com.peter.overtimecalculator.domain.SaveOvertimeUseCase
import com.peter.overtimecalculator.domain.UpdateManualHourlyRateUseCase
import com.peter.overtimecalculator.domain.UpdateMultipliersUseCase
import com.peter.overtimecalculator.domain.decimal
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WriteUseCaseTest {
    private val testMonth = YearMonth.of(2026, 10)

    @Test
    fun overtimeEntryValidator_allowsWorkdayCompTimeWithinBounds() {
        val result = OvertimeEntryValidator.validate(minutes = -480, resolvedDayType = DayType.WORKDAY)

        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun overtimeEntryValidator_rejectsCompTimeOnNonWorkday() {
        val result = OvertimeEntryValidator.validate(minutes = -30, resolvedDayType = DayType.REST_DAY)

        assertEquals("只有工作日才能申请调休", (result as DomainResult.Failure).message)
    }

    @Test
    fun overtimeEntryValidator_rejectsMinutesOutsideBounds() {
        val result = OvertimeEntryValidator.validate(minutes = 961, resolvedDayType = DayType.WORKDAY)

        assertEquals("工时调整必须在 -8.0h 到 16.0h 之间", (result as DomainResult.Failure).message)
    }

    @Test
    fun payConfigValidator_rejectsNegativeHourlyRate() {
        val result = PayConfigValidator.validateManualHourlyRate(decimal("-1.00"))

        assertEquals("时薪不能小于 0", (result as DomainResult.Failure).message)
    }

    @Test
    fun payConfigValidator_rejectsNonPositiveMultiplier() {
        val result = PayConfigValidator.validateMultipliers(decimal("1.50"), BigDecimal.ZERO, decimal("3.00"))

        assertEquals("倍率必须大于 0", (result as DomainResult.Failure).message)
    }

    @Test
    fun saveOvertimeUseCase_blocksInvalidCompTimeBeforePersistence() = runTest {
        val gateway = FakeOvertimeWriteGateway(resolvedDayType = DayType.REST_DAY)
        val result = SaveOvertimeUseCase(gateway)(
            date = LocalDate.parse("2026-10-10"),
            minutes = -60,
            overrideDayType = null,
        )

        assertTrue(result is DomainResult.Failure)
        assertFalse(gateway.saveOvertimeCalled)
    }

    @Test
    fun saveOvertimeUseCase_persistsWhenValidationSucceeds() = runTest {
        val gateway = FakeOvertimeWriteGateway(resolvedDayType = DayType.WORKDAY)
        val result = SaveOvertimeUseCase(gateway)(
            date = LocalDate.parse("2026-10-08"),
            minutes = 120,
            overrideDayType = DayType.WORKDAY,
        )

        assertTrue(result is DomainResult.Success)
        assertTrue(gateway.saveOvertimeCalled)
    }

    @Test
    fun updateManualHourlyRateUseCase_blocksNegativeRateBeforePersistence() = runTest {
        val gateway = FakeOvertimeWriteGateway()
        val result = UpdateManualHourlyRateUseCase(gateway)(testMonth, decimal("-1.00"))

        assertTrue(result is DomainResult.Failure)
        assertFalse(gateway.updateManualHourlyRateCalled)
    }

    @Test
    fun updateMultipliersUseCase_blocksInvalidMultiplierBeforePersistence() = runTest {
        val gateway = FakeOvertimeWriteGateway()
        val result = UpdateMultipliersUseCase(gateway)(testMonth, decimal("1.50"), BigDecimal.ZERO, decimal("3.00"))

        assertTrue(result is DomainResult.Failure)
        assertFalse(gateway.updateMultipliersCalled)
    }

    @Test
    fun reverseEngineerHourlyRateUseCase_blocksInvalidInputBeforeRepositoryCall() = runTest {
        val gateway = FakeOvertimeWriteGateway()
        val result = ReverseEngineerHourlyRateUseCase(gateway)(testMonth, BigDecimal.ZERO)

        assertTrue(result is DomainResult.Failure)
        assertFalse(gateway.reverseEngineerCalled)
    }

    private class FakeOvertimeWriteGateway(
        private val resolvedDayType: DayType = DayType.WORKDAY,
    ) : OvertimeWriteGateway {
        var saveOvertimeCalled = false
        var updateManualHourlyRateCalled = false
        var updateMultipliersCalled = false
        var reverseEngineerCalled = false

        override suspend fun saveOvertime(date: LocalDate, minutes: Int, overrideDayType: DayType?) {
            saveOvertimeCalled = true
        }

        override suspend fun updateManualHourlyRate(yearMonth: YearMonth, hourlyRate: BigDecimal) {
            updateManualHourlyRateCalled = true
        }

        override suspend fun updateMultipliers(
            yearMonth: YearMonth,
            weekdayRate: BigDecimal,
            restDayRate: BigDecimal,
            holidayRate: BigDecimal,
        ) {
            updateMultipliersCalled = true
        }

        override suspend fun reverseEngineerHourlyRate(
            yearMonth: YearMonth,
            overtimePayInput: BigDecimal,
        ): ReverseRateResult {
            reverseEngineerCalled = true
            return ReverseRateResult(
                hourlyRate = decimal("25.00"),
                weightedHours = decimal("10.00000000"),
                overtimePayInput = overtimePayInput,
            )
        }

        override fun resolveDayType(date: LocalDate, overrideType: DayType?): DayType {
            return overrideType ?: resolvedDayType
        }
    }
}
