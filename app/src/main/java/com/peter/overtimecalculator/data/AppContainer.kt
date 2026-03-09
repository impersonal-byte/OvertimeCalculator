package com.peter.overtimecalculator.data

import android.content.Context
import androidx.room.Room
import com.peter.overtimecalculator.data.db.AppDatabase
import com.peter.overtimecalculator.data.repository.OvertimeRepository
import com.peter.overtimecalculator.data.update.AndroidUpdateManager
import com.peter.overtimecalculator.data.update.UpdateManager
import com.peter.overtimecalculator.domain.ConfigPropagationPlanner
import com.peter.overtimecalculator.domain.HolidayCalendar
import com.peter.overtimecalculator.domain.MonthlyOvertimeCalculator
import com.peter.overtimecalculator.domain.ReverseHourlyRateCalculator

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "overtime-calculator.db",
    ).build()

    private val holidayCalendar = HolidayCalendar()

    val updateManager: UpdateManager = AndroidUpdateManager(context)

    val repository = OvertimeRepository(
        database = database,
        dao = database.overtimeDao(),
        holidayCalendar = holidayCalendar,
        configPropagationPlanner = ConfigPropagationPlanner(),
        monthlyOvertimeCalculator = MonthlyOvertimeCalculator(holidayCalendar),
        reverseHourlyRateCalculator = ReverseHourlyRateCalculator(holidayCalendar),
    )
}
