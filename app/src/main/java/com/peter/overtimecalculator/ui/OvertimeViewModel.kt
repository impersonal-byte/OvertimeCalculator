package com.peter.overtimecalculator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peter.overtimecalculator.appContainer
import com.peter.overtimecalculator.data.AppContainer
import com.peter.overtimecalculator.data.backup.BackupSnapshotCodec
import com.peter.overtimecalculator.domain.DayCellUiState
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.DomainResult
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.INTERNAL_SCALE
import com.peter.overtimecalculator.domain.MonthlyConfig
import com.peter.overtimecalculator.domain.MonthlySummaryUiState
import com.peter.overtimecalculator.domain.ObservedMonth
import com.peter.overtimecalculator.domain.RestorePreview
import com.peter.overtimecalculator.domain.ZeroDecimal
import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.domain.SeedColor
import com.peter.overtimecalculator.domain.isPositive
import com.peter.overtimecalculator.domain.toDisplayString
import java.io.File
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayEditorUiState(
    val date: LocalDate,
    val currentMinutes: Int,
    val currentOverride: DayType?,
    val resolvedDayType: DayType,
)

private data class AppearancePreferences(
    val calendarStartDay: CalendarStartDay,
    val appTheme: AppTheme,
    val useDynamicColor: Boolean,
    val seedColor: SeedColor,
)

data class AppUiState(
    val selectedMonth: YearMonth,
    val dayCells: List<DayCellUiState>,
    val summary: MonthlySummaryUiState,
    val config: MonthlyConfig,
    val calendarStartDay: CalendarStartDay,
    val appTheme: AppTheme,
    val useDynamicColor: Boolean,
    val seedColor: SeedColor,
    val editor: DayEditorUiState? = null,
) {
    companion object {
        fun empty(): AppUiState {
            val month = YearMonth.now()
            val config = MonthlyConfig(
                yearMonth = month,
                hourlyRate = ZeroDecimal,
                rateSource = HourlyRateSource.MANUAL,
                weekdayRate = java.math.BigDecimal("1.50"),
                restDayRate = java.math.BigDecimal("2.00"),
                holidayRate = java.math.BigDecimal("3.00"),
                lockedByUser = false,
            )
            return AppUiState(
                selectedMonth = month,
                dayCells = emptyList(),
                summary = MonthlySummaryUiState(
                    totalMinutes = 0,
                    totalPay = ZeroDecimal,
                    yearMonth = month,
                    hourlyRate = ZeroDecimal,
                    rateSource = HourlyRateSource.MANUAL,
                ),
                config = config,
                calendarStartDay = CalendarStartDay.MONDAY,
                appTheme = AppTheme.SYSTEM,
                useDynamicColor = false,
                seedColor = SeedColor.CLAY,
            )
        }
    }
}

data class RestoreConfirmationUiState(
    val createdAt: String,
    val monthCount: Int,
    val entryCount: Int,
    val overrideCount: Int,
)

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent

    data class ShareCsvExport(val file: File) : UiEvent

    data object TriggerHaptic : UiEvent

    data class NavigateHomeAfterRestore(val month: YearMonth) : UiEvent

    data class CreateBackup(val encodedBackup: String, val fileName: String) : UiEvent

    data object PickRestoreFile : UiEvent
}

enum class CalendarStartDay {
    MONDAY,
    SUNDAY,
}

@OptIn(ExperimentalCoroutinesApi::class)
class OvertimeViewModel(
    application: Application,
    containerOverride: AppContainer? = null,
) : AndroidViewModel(application) {
    private val appContainer = containerOverride ?: application.appContainer
    private val repository = appContainer.repository
    private val saveOvertimeUseCase = appContainer.saveOvertimeUseCase
    private val updateManualHourlyRateUseCase = appContainer.updateManualHourlyRateUseCase
    private val updateMultipliersUseCase = appContainer.updateMultipliersUseCase
    private val reverseEngineerHourlyRateUseCase = appContainer.reverseEngineerHourlyRateUseCase
    private val appearancePreferencesRepository = appContainer.appearancePreferencesRepository
    private val backupRestoreRepository = appContainer.backupRestoreRepository
    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val selectedEditorDate = MutableStateFlow<LocalDate?>(null)
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    private val _restoreConfirmation = MutableStateFlow<RestoreConfirmationUiState?>(null)
    private var pendingRestoreBackup: String? = null

    val events: Flow<UiEvent> = _events.receiveAsFlow()
    val restoreConfirmation: StateFlow<RestoreConfirmationUiState?> = _restoreConfirmation

    private val observedMonth = selectedMonth
        .flatMapLatest(repository::observeMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val appearancePreferences = appearancePreferencesRepository.snapshot
        .map { snapshot ->
            buildAppearancePreferences(
                calendarStartDay = snapshot.calendarStartDay,
                appTheme = snapshot.appTheme,
                useDynamicColor = snapshot.useDynamicColor,
                seedColor = snapshot.seedColor,
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            appearancePreferencesRepository.snapshot.value.let { snapshot ->
                buildAppearancePreferences(
                    calendarStartDay = snapshot.calendarStartDay,
                    appTheme = snapshot.appTheme,
                    useDynamicColor = snapshot.useDynamicColor,
                    seedColor = snapshot.seedColor,
                )
            },
        )

    val uiState: StateFlow<AppUiState> = combine(
        selectedMonth,
        observedMonth,
        selectedEditorDate,
        appearancePreferences,
    ) { month, observed, editorDate, appearance ->
        buildAppUiState(
            month = month,
            observed = observed,
            editorDate = editorDate,
            appearance = appearance,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppUiState.empty(),
    )

    init {
        ensureMonth(selectedMonth.value)
    }

    fun previousMonth() {
        val month = selectedMonth.value.minusMonths(1)
        showMonth(month)
    }

    fun nextMonth() {
        val month = selectedMonth.value.plusMonths(1)
        showMonth(month)
    }

    fun showMonth(month: YearMonth) {
        selectedMonth.value = month
        selectedEditorDate.value = null
        ensureMonth(month)
    }

    fun openEditor(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) {
            viewModelScope.launch {
                _events.send(UiEvent.ShowSnackbar("未来日期不能录入加班"))
            }
            return
        }
        selectedEditorDate.value = date
    }

    fun dismissEditor() {
        selectedEditorDate.value = null
    }

    fun updateCalendarStartDay(startDay: CalendarStartDay) {
        appearancePreferencesRepository.saveCalendarStartDay(startDay)
    }

    fun updateTheme(theme: AppTheme) {
        appearancePreferencesRepository.saveAppTheme(theme)
    }

    fun updateUseDynamicColor(useDynamic: Boolean) {
        appearancePreferencesRepository.saveUseDynamicColor(useDynamic)
    }

    fun updateSeedColor(seed: SeedColor) {
        appearancePreferencesRepository.saveSeedColor(seed)
    }

    fun saveOvertime(date: LocalDate, hoursText: String, minutesText: String, overrideDayType: DayType?) {
        viewModelScope.launch {
            val hours = hoursText.ifBlank { "0" }.toIntOrNull()
            val minutes = minutesText.ifBlank { "0" }.toIntOrNull()
            if (hours == null || minutes == null) {
                _events.send(UiEvent.ShowSnackbar("请输入有效的工时"))
                return@launch
            }
            if (hours < 0 || minutes < 0) {
                _events.send(UiEvent.ShowSnackbar("时长不能为负数"))
                return@launch
            }
            if (minutes >= 60) {
                _events.send(UiEvent.ShowSnackbar("分钟请输入 0 到 59"))
                return@launch
            }
            handleWriteResult(
                result = saveOvertimeUseCase(date, hours * 60 + minutes, overrideDayType),
                successMessage = null,
                closeEditorOnSuccess = true,
            )
        }
    }

    fun saveOvertimeMinutes(date: LocalDate, totalMinutes: Int, overrideDayType: DayType?) {
        viewModelScope.launch {
            handleWriteResult(
                result = saveOvertimeUseCase(date, totalMinutes, overrideDayType),
                successMessage = null,
                closeEditorOnSuccess = true,
            )
        }
    }

    fun updateManualHourlyRate(rateText: String) {
        viewModelScope.launch {
            val hourlyRate = rateText.toBigDecimalOrNull()
            if (hourlyRate == null) {
                _events.send(UiEvent.ShowSnackbar("请输入有效的时薪"))
                return@launch
            }
            handleWriteResult(
                result = updateManualHourlyRateUseCase(selectedMonth.value, hourlyRate),
                successMessage = "已保存时薪",
            )
        }
    }

    fun updateMultipliers(weekday: String, restDay: String, holiday: String) {
        viewModelScope.launch {
            val weekdayRate = weekday.toBigDecimalOrNull()
            val restDayRate = restDay.toBigDecimalOrNull()
            val holidayRate = holiday.toBigDecimalOrNull()
            if (weekdayRate == null || restDayRate == null || holidayRate == null) {
                _events.send(UiEvent.ShowSnackbar("请输入有效的倍率"))
                return@launch
            }
            handleWriteResult(
                result = updateMultipliersUseCase(
                    yearMonth = selectedMonth.value,
                    weekdayRate = weekdayRate,
                    restDayRate = restDayRate,
                    holidayRate = holidayRate,
                ),
                successMessage = "已保存倍率",
            )
        }
    }

    fun reverseEngineerHourlyRate(overtimePayText: String) {
        viewModelScope.launch {
            val overtimePay = overtimePayText.toBigDecimalOrNull()
            if (overtimePay == null) {
                _events.send(UiEvent.ShowSnackbar("请输入有效的已发加班工资总额"))
                return@launch
            }
            when (val result = reverseEngineerHourlyRateUseCase(selectedMonth.value, overtimePay)) {
                is DomainResult.Success -> {
                    _events.send(
                        UiEvent.ShowSnackbar(
                            "反推时薪 ¥${result.value.hourlyRate.toDisplayString()}，加权工时 ${result.value.weightedHours.toDisplayString()}h",
                        ),
                    )
                    _events.send(UiEvent.TriggerHaptic)
                }
                is DomainResult.Failure -> {
                    _events.send(UiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    fun exportMonthlyCsv() {
        val uiStateSnapshot = uiState.value
        viewModelScope.launch {
            val exportResult = kotlinx.coroutines.withContext(Dispatchers.IO) {
                runCatching { createCsvFile(uiStateSnapshot) }
            }
            exportResult
                .onSuccess { file ->
                    _events.send(UiEvent.ShareCsvExport(file))
                }
                .onFailure {
                    _events.send(UiEvent.ShowSnackbar("导出 CSV 失败，请稍后重试"))
                }
        }
    }

    /**
     * Creates a full backup and triggers the save dialog via SAF.
     */
    fun createBackup() {
        viewModelScope.launch {
            val backupResult = kotlinx.coroutines.withContext(Dispatchers.IO) {
                runCatching {
                    val snapshot = backupRestoreRepository.exportSnapshot()
                    val encoded = BackupSnapshotCodec().encode(snapshot)
                    val fileName = "overtime_backup_${java.time.LocalDate.now()}${com.peter.overtimecalculator.domain.BackupSnapshot.BACKUP_FILE_EXTENSION}"
                    Pair(encoded, fileName)
                }
            }
            backupResult
                .onSuccess { (encoded, fileName) ->
                    _events.send(UiEvent.CreateBackup(encoded, fileName))
                }
                .onFailure {
                    _events.send(UiEvent.ShowSnackbar("创建备份失败，请稍后重试"))
                }
        }
    }

    /**
     * Triggers the SAF document picker to select a backup file for restore.
     */
    fun pickRestoreFile() {
        viewModelScope.launch {
            _events.send(UiEvent.PickRestoreFile)
        }
    }

    fun previewRestoreBackup(encodedBackup: String) {
        viewModelScope.launch {
            val previewResult = kotlinx.coroutines.withContext(Dispatchers.IO) {
                runCatching {
                    val normalized = encodedBackup.removePrefix("\uFEFF")
                    if (normalized.startsWith("日期,")) {
                        throw IllegalArgumentException("CSV 文件不是备份文件，请选择 .obackup 格式的备份文件")
                    }
                    val snapshot = BackupSnapshotCodec().decode(normalized)
                    val preview = backupRestoreRepository.previewRestore(snapshot)
                    if (preview !is RestorePreview.Compatible) {
                        throw IllegalArgumentException("不支持的备份文件格式，版本: ${preview.schemaVersion}")
                    }
                    pendingRestoreBackup = normalized
                    _restoreConfirmation.value = RestoreConfirmationUiState(
                        createdAt = preview.createdAt,
                        monthCount = preview.monthCount,
                        entryCount = preview.entryCount,
                        overrideCount = preview.overrideCount,
                    )
                }
            }
            previewResult.onFailure { error ->
                pendingRestoreBackup = null
                _restoreConfirmation.value = null
                val message = when {
                    error.message?.contains("CSV", ignoreCase = true) == true ->
                        "CSV 文件不是备份文件，请选择 .obackup 格式的备份文件"
                    error.message?.contains("Unsupported backup schema version", ignoreCase = true) == true ->
                        "备份文件格式不兼容"
                    error.message?.contains("Missing key", ignoreCase = true) == true ->
                        "备份文件格式不兼容"
                    error.message?.contains("不支持", ignoreCase = true) == true ->
                        error.message ?: "备份文件格式不兼容"
                    else ->
                        "读取备份文件失败，请确认文件未损坏"
                }
                _events.send(UiEvent.ShowSnackbar(message))
            }
        }
    }

    fun dismissRestoreConfirmation() {
        pendingRestoreBackup = null
        _restoreConfirmation.value = null
    }

    fun confirmRestore() {
        val encodedBackup = pendingRestoreBackup ?: return
        viewModelScope.launch {
            val restoreResult = kotlinx.coroutines.withContext(Dispatchers.IO) {
                runCatching {
                    val snapshot = BackupSnapshotCodec().decode(encodedBackup)
                    backupRestoreRepository.restoreSnapshot(snapshot).getOrThrow()
                    restoredMonthFrom(snapshot)
                }
            }
            restoreResult
                .onSuccess { restoredMonth ->
                    if (restoredMonth != null) {
                        showMonth(restoredMonth)
                    }
                    dismissRestoreConfirmation()
                    if (restoredMonth != null) {
                        _events.send(UiEvent.NavigateHomeAfterRestore(restoredMonth))
                    }
                    _events.send(UiEvent.ShowSnackbar("数据恢复成功"))
                    _events.send(UiEvent.TriggerHaptic)
                }
                .onFailure { error ->
                    dismissRestoreConfirmation()
                    val message = when {
                        error.message?.contains("CSV", ignoreCase = true) == true ->
                            "CSV 文件不是备份文件，请选择 .obackup 格式的备份文件"
                        error.message?.contains("不支持", ignoreCase = true) == true ->
                            error.message ?: "备份文件格式不兼容"
                        else ->
                            "恢复数据失败，请稍后重试"
                    }
                    _events.send(UiEvent.ShowSnackbar(message))
                }
        }
    }

    private fun ensureMonth(month: YearMonth) {
        viewModelScope.launch {
            repository.ensureMonthExists(month)
        }
    }

    private fun buildAppearancePreferences(
        calendarStartDay: CalendarStartDay,
        appTheme: AppTheme,
        useDynamicColor: Boolean,
        seedColor: SeedColor,
    ): AppearancePreferences {
        return AppearancePreferences(
            calendarStartDay = calendarStartDay,
            appTheme = appTheme,
            useDynamicColor = useDynamicColor,
            seedColor = seedColor,
        )
    }

    private fun buildAppUiState(
        month: YearMonth,
        observed: ObservedMonth?,
        editorDate: LocalDate?,
        appearance: AppearancePreferences,
    ): AppUiState {
        return if (observed == null) {
            buildEmptyUiState(
                month = month,
                appearance = appearance,
            )
        } else {
            AppUiState(
                selectedMonth = month,
                dayCells = observed.dayCells,
                summary = observed.summary,
                config = observed.config,
                calendarStartDay = appearance.calendarStartDay,
                appTheme = appearance.appTheme,
                useDynamicColor = appearance.useDynamicColor,
                seedColor = appearance.seedColor,
                editor = buildDayEditorUiState(
                    editorDate = editorDate,
                    observed = observed,
                ),
            )
        }
    }

    private fun buildEmptyUiState(
        month: YearMonth,
        appearance: AppearancePreferences,
    ): AppUiState {
        return AppUiState.empty().copy(
            selectedMonth = month,
            calendarStartDay = appearance.calendarStartDay,
            appTheme = appearance.appTheme,
            useDynamicColor = appearance.useDynamicColor,
            seedColor = appearance.seedColor,
        )
    }

    private fun buildDayEditorUiState(
        editorDate: LocalDate?,
        observed: ObservedMonth,
    ): DayEditorUiState? {
        return editorDate?.let { date ->
            val resolvedDayType = observed.dayCells.firstOrNull { it.date == date }?.dayType ?: DayType.WORKDAY
            DayEditorUiState(
                date = date,
                currentMinutes = observed.entryMinutesByDate[date] ?: 0,
                currentOverride = observed.overrideTypesByDate[date],
                resolvedDayType = resolvedDayType,
            )
        }
    }

    private suspend fun handleWriteResult(
        result: DomainResult<Unit>,
        successMessage: String?,
        closeEditorOnSuccess: Boolean = false,
    ) {
        when (result) {
            is DomainResult.Success -> {
                if (closeEditorOnSuccess) {
                    selectedEditorDate.value = null
                }
                if (successMessage != null) {
                    _events.send(UiEvent.ShowSnackbar(successMessage))
                }
                _events.send(UiEvent.TriggerHaptic)
            }
            is DomainResult.Failure -> {
                _events.send(UiEvent.ShowSnackbar(result.message))
            }
        }
    }

    private fun createCsvFile(uiStateValue: AppUiState): File {
        val csvContent = buildString {
            append("日期,类型,加班/调休(分钟),当天计费预估(元)\n")
            uiStateValue.dayCells.filter { it.overtimeMinutes != 0 }.forEach { cell ->
                val typeStr = when (cell.dayType) {
                    DayType.WORKDAY -> "工作日"
                    DayType.REST_DAY -> "休息日"
                    DayType.HOLIDAY -> "节假日"
                }
                append("${cell.date},$typeStr,${cell.overtimeMinutes},${cell.pay.toDisplayString()}\n")
            }
            append("\n汇总月份,${uiStateValue.selectedMonth}\n")
            append("总计分钟,${uiStateValue.summary.totalMinutes}\n")
            append("加权总工时(小时),${calculateWeightedHours(uiStateValue.summary).toDisplayString()}\n")
            append("总计收益预估(元),${uiStateValue.summary.totalPay.toDisplayString()}\n")
        }
        val exportDir = File(getApplication<Application>().cacheDir, "exports")
        exportDir.mkdirs()
        return File(exportDir, "Overtime_Records_${uiStateValue.selectedMonth}.csv").apply {
            writeBytes(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + csvContent.toByteArray())
        }
    }

    private fun calculateWeightedHours(summary: MonthlySummaryUiState) =
        if (summary.hourlyRate.isPositive()) {
            summary.totalPay.divide(summary.hourlyRate, INTERNAL_SCALE, RoundingMode.HALF_UP)
        } else {
            ZeroDecimal
        }

    private fun restoredMonthFrom(snapshot: com.peter.overtimecalculator.domain.BackupSnapshot): YearMonth? {
        return snapshot.overtimeEntries.maxOfOrNull { java.time.LocalDate.parse(it.date) }?.let { YearMonth.from(it) }
            ?: snapshot.holidayOverrides.maxOfOrNull { java.time.LocalDate.parse(it.date) }?.let { YearMonth.from(it) }
            ?: snapshot.monthlyConfigs.filter { it.lockedByUser }.maxOfOrNull { it.yearMonth }
            ?: snapshot.monthlyConfigs.maxOfOrNull { it.yearMonth }
    }
}
