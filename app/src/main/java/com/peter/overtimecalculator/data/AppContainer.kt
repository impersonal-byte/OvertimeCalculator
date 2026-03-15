package com.peter.overtimecalculator.data

import android.content.Context
import androidx.room.Room
import com.peter.overtimecalculator.data.db.AppDatabase
import com.peter.overtimecalculator.data.holiday.HolidayRulesRepository
import com.peter.overtimecalculator.data.holiday.HttpUrlConnectionHolidayRemoteClient
import com.peter.overtimecalculator.data.holiday.HolidaySyncWorker
import com.peter.overtimecalculator.data.repository.OvertimeRepository
import com.peter.overtimecalculator.data.update.AndroidUpdateManager
import com.peter.overtimecalculator.data.update.UpdateManager
import com.peter.overtimecalculator.domain.ConfigPropagationPlanner
import com.peter.overtimecalculator.domain.HolidayCalendar
import com.peter.overtimecalculator.domain.MonthlyOvertimeCalculator
import com.peter.overtimecalculator.domain.ReverseEngineerHourlyRateUseCase
import com.peter.overtimecalculator.domain.ReverseHourlyRateCalculator
import com.peter.overtimecalculator.domain.SaveOvertimeUseCase
import com.peter.overtimecalculator.domain.UpdateManualHourlyRateUseCase
import com.peter.overtimecalculator.domain.UpdateMultipliersUseCase
import com.peter.overtimecalculator.data.backup.BackupRestoreRepository
import com.peter.overtimecalculator.data.backup.BackupSnapshotCodec
import kotlinx.coroutines.CoroutineScope

class AppContainer(
    private val context: Context,
    applicationScope: CoroutineScope,
) {
    private val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "overtime-calculator.db",
    ).addMigrations(AppDatabase.Migration1To2).build()

    val holidayRulesRepository = HolidayRulesRepository(
        context = context,
        applicationScope = applicationScope,
        remoteClient = HttpUrlConnectionHolidayRemoteClient(),
    )

    private val holidayCalendar = HolidayCalendar(holidayRulesRepository::currentRules)

    val updateManager: UpdateManager = AndroidUpdateManager(context)

    val appearancePreferencesRepository: AppearancePreferencesRepository =
        SharedPreferencesAppearancePreferencesRepository.create(context)

    val repository = OvertimeRepository(
        database = database,
        dao = database.overtimeDao(),
        holidayCalendar = holidayCalendar,
        holidayRulesRepository = holidayRulesRepository,
        configPropagationPlanner = ConfigPropagationPlanner(),
        monthlyOvertimeCalculator = MonthlyOvertimeCalculator(holidayCalendar),
        reverseHourlyRateCalculator = ReverseHourlyRateCalculator(holidayCalendar),
    )

    val saveOvertimeUseCase = SaveOvertimeUseCase(repository)

    val updateManualHourlyRateUseCase = UpdateManualHourlyRateUseCase(repository)

    val updateMultipliersUseCase = UpdateMultipliersUseCase(repository)

    val reverseEngineerHourlyRateUseCase = ReverseEngineerHourlyRateUseCase(repository)

    val backupRestoreRepository: BackupRestoreRepository = BackupRestoreRepository(
        dao = database.overtimeDao(),
        codec = BackupSnapshotCodec(),
    )

    fun scheduleHolidaySync() {
        HolidaySyncWorker.enqueue(context)
    }
}
