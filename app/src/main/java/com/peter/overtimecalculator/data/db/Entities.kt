package com.peter.overtimecalculator.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.MonthlyConfig
import java.time.LocalDate
import java.time.YearMonth

@Entity(tableName = "monthly_config")
data class MonthlyConfigEntity(
    @PrimaryKey val yearMonth: String,
    val hourlyRate: Double,
    val rateSource: HourlyRateSource,
    val weekdayRate: Double,
    val restDayRate: Double,
    val holidayRate: Double,
    val lockedByUser: Boolean,
)

@Entity(tableName = "overtime_entry")
data class OvertimeEntryEntity(
    @PrimaryKey val date: String,
    val minutes: Int,
)

@Entity(tableName = "holiday_override")
data class HolidayOverrideEntity(
    @PrimaryKey val date: String,
    val dayType: DayType,
)

fun MonthlyConfigEntity.toDomain(): MonthlyConfig = MonthlyConfig(
    yearMonth = YearMonth.parse(yearMonth),
    hourlyRate = hourlyRate,
    rateSource = rateSource,
    weekdayRate = weekdayRate,
    restDayRate = restDayRate,
    holidayRate = holidayRate,
    lockedByUser = lockedByUser,
)

fun MonthlyConfig.toEntity(): MonthlyConfigEntity = MonthlyConfigEntity(
    yearMonth = yearMonth.toString(),
    hourlyRate = hourlyRate,
    rateSource = rateSource,
    weekdayRate = weekdayRate,
    restDayRate = restDayRate,
    holidayRate = holidayRate,
    lockedByUser = lockedByUser,
)

fun OvertimeEntryEntity.toPair(): Pair<LocalDate, Int> = LocalDate.parse(date) to minutes

fun HolidayOverrideEntity.toPair(): Pair<LocalDate, DayType> = LocalDate.parse(date) to dayType

