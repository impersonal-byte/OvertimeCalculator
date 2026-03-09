package com.peter.overtimecalculator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.peter.overtimecalculator.OvertimeApplication
import com.peter.overtimecalculator.domain.DayCellUiState
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.DownloadStatus
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.InstallResult
import com.peter.overtimecalculator.domain.MonthlyConfig
import com.peter.overtimecalculator.domain.MonthlySummaryUiState
import com.peter.overtimecalculator.domain.PendingUpdateDownload
import com.peter.overtimecalculator.domain.UpdateCheckResult
import com.peter.overtimecalculator.domain.UpdateUiState
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DayEditorUiState(
    val date: LocalDate,
    val currentMinutes: Int,
    val currentOverride: DayType?,
)

data class AppUiState(
    val selectedMonth: YearMonth,
    val dayCells: List<DayCellUiState>,
    val summary: MonthlySummaryUiState,
    val config: MonthlyConfig,
    val currentVersionName: String,
    val updateState: UpdateUiState,
    val awaitingInstallPermission: Boolean,
    val editor: DayEditorUiState? = null,
    val message: String? = null,
    val feedbackSignal: Long = 0L,
) {
    companion object {
        fun empty(currentVersionName: String): AppUiState {
            val month = YearMonth.now()
            val config = MonthlyConfig(
                yearMonth = month,
                hourlyRate = 0.0,
                rateSource = HourlyRateSource.MANUAL,
                weekdayRate = 1.5,
                restDayRate = 2.0,
                holidayRate = 3.0,
                lockedByUser = false,
            )
            return AppUiState(
                selectedMonth = month,
                dayCells = emptyList(),
                summary = MonthlySummaryUiState(
                    totalMinutes = 0,
                    totalPay = 0.0,
                    yearMonth = month,
                    hourlyRate = 0.0,
                    rateSource = HourlyRateSource.MANUAL,
                ),
                config = config,
                currentVersionName = currentVersionName,
                updateState = UpdateUiState.Idle,
                awaitingInstallPermission = false,
            )
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OvertimeViewModel(application: Application) : AndroidViewModel(application) {
    private val appContainer = (application as OvertimeApplication).appContainer
    private val repository = appContainer.repository
    private val updateManager = appContainer.updateManager
    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val selectedEditorDate = MutableStateFlow<LocalDate?>(null)
    private val message = MutableStateFlow<String?>(null)
    private val feedbackSignal = MutableStateFlow(0L)
    private val updateState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    private val awaitingInstallPermission = MutableStateFlow(updateManager.isAwaitingInstallPermission())
    private var downloadMonitorJob: Job? = null

    private val observedMonth = selectedMonth
        .flatMapLatest(repository::observeMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val baseUiState = combine(
        selectedMonth,
        observedMonth,
        selectedEditorDate,
        message,
        feedbackSignal,
    ) { month, observed, editorDate, snackbar, tickSignal ->
        if (observed == null) {
            AppUiState.empty(updateManager.currentVersionName).copy(
                selectedMonth = month,
                message = snackbar,
                feedbackSignal = tickSignal,
            )
        } else {
            AppUiState(
                selectedMonth = month,
                dayCells = observed.dayCells,
                summary = observed.summary,
                config = observed.config,
                currentVersionName = updateManager.currentVersionName,
                updateState = UpdateUiState.Idle,
                awaitingInstallPermission = false,
                editor = editorDate?.let { date ->
                    DayEditorUiState(
                        date = date,
                        currentMinutes = observed.entryMinutesByDate[date] ?: 0,
                        currentOverride = observed.overrideTypesByDate[date],
                    )
                },
                message = snackbar,
                feedbackSignal = tickSignal,
            )
        }
    }

    val uiState: StateFlow<AppUiState> = combine(
        baseUiState,
        updateState,
        awaitingInstallPermission,
    ) { baseState, currentUpdateState, waitingPermission ->
        baseState.copy(
            updateState = currentUpdateState,
            awaitingInstallPermission = waitingPermission,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppUiState.empty(updateManager.currentVersionName),
    )

    init {
        ensureMonth(selectedMonth.value)
        restorePendingUpdate()
    }

    fun previousMonth() {
        val month = selectedMonth.value.minusMonths(1)
        selectedMonth.value = month
        selectedEditorDate.value = null
        ensureMonth(month)
    }

    fun nextMonth() {
        val month = selectedMonth.value.plusMonths(1)
        selectedMonth.value = month
        selectedEditorDate.value = null
        ensureMonth(month)
    }

    fun openEditor(date: LocalDate) {
        selectedEditorDate.value = date
    }

    fun dismissEditor() {
        selectedEditorDate.value = null
    }

    fun clearMessage() {
        message.value = null
    }

    fun saveOvertime(date: LocalDate, hoursText: String, minutesText: String, overrideDayType: DayType?) {
        viewModelScope.launch {
            runCatching {
                val hours = hoursText.ifBlank { "0" }.toInt()
                val minutes = minutesText.ifBlank { "0" }.toInt()
                require(hours >= 0 && minutes >= 0) { "时长不能为负数" }
                require(minutes < 60) { "分钟请输入 0 到 59" }
                repository.saveOvertime(date, hours * 60 + minutes, overrideDayType)
            }.onSuccess {
                selectedEditorDate.value = null
                emitFeedback()
            }.onFailure {
                message.value = it.message ?: "保存失败"
            }
        }
    }

    fun saveOvertimeMinutes(date: LocalDate, totalMinutes: Int, overrideDayType: DayType?) {
        viewModelScope.launch {
            runCatching {
                require(totalMinutes >= 0) { "时长不能为负数" }
                repository.saveOvertime(date, totalMinutes, overrideDayType)
            }.onSuccess {
                selectedEditorDate.value = null
                emitFeedback()
            }.onFailure {
                message.value = it.message ?: "保存失败"
            }
        }
    }

    fun updateManualHourlyRate(rateText: String) {
        viewModelScope.launch {
            runCatching {
                repository.updateManualHourlyRate(selectedMonth.value, rateText.toDouble())
                "已保存时薪"
            }.onSuccess {
                message.value = it
                emitFeedback()
            }.onFailure {
                message.value = it.message ?: "保存时薪失败"
            }
        }
    }

    fun updateMultipliers(weekday: String, restDay: String, holiday: String) {
        viewModelScope.launch {
            runCatching {
                repository.updateMultipliers(
                    yearMonth = selectedMonth.value,
                    weekdayRate = weekday.toDouble(),
                    restDayRate = restDay.toDouble(),
                    holidayRate = holiday.toDouble(),
                )
                "已保存倍率"
            }.onSuccess {
                message.value = it
                emitFeedback()
            }.onFailure {
                message.value = it.message ?: "保存倍率失败"
            }
        }
    }

    fun reverseEngineerHourlyRate(overtimePayText: String) {
        viewModelScope.launch {
            runCatching {
                val result = repository.reverseEngineerHourlyRate(
                    selectedMonth.value,
                    overtimePayText.toDouble(),
                )
                "反推时薪 ¥${"%.2f".format(result.hourlyRate)}，加权工时 ${"%.2f".format(result.weightedHours)}h"
            }.onSuccess {
                message.value = it
                emitFeedback()
            }.onFailure {
                message.value = it.message ?: "反推时薪失败"
            }
        }
    }

    fun checkForUpdates() {
        val currentState = updateState.value
        if (currentState is UpdateUiState.Checking || currentState is UpdateUiState.Downloading) {
            return
        }

        viewModelScope.launch {
            updateState.value = UpdateUiState.Checking
            when (val result = updateManager.checkLatestRelease()) {
                is UpdateCheckResult.Available -> {
                    updateState.value = UpdateUiState.UpdateAvailable(result.update.versionName)
                    message.value = "发现新版本 ${result.update.versionName}，开始下载"
                    val downloadId = updateManager.startDownload(result.update)
                    emitFeedback()
                    startDownloadMonitor(downloadId, result.update.versionName)
                }
                is UpdateCheckResult.UpToDate -> {
                    updateState.value = UpdateUiState.UpToDate(result.currentVersion)
                    message.value = "当前已是最新版本"
                }
                is UpdateCheckResult.Failure -> {
                    updateState.value = UpdateUiState.Error(result.message)
                    message.value = result.message
                }
            }
        }
    }

    fun onHostResumed() {
        viewModelScope.launch {
            if (awaitingInstallPermission.value) {
                val pendingDownload = updateManager.getPendingDownload()
                if (pendingDownload == null) {
                    awaitingInstallPermission.value = false
                    updateManager.setAwaitingInstallPermission(false)
                    return@launch
                }
                if (!getApplication<Application>().packageManager.canRequestPackageInstalls()) {
                    awaitingInstallPermission.value = false
                    updateManager.setAwaitingInstallPermission(false)
                    updateState.value = UpdateUiState.Error("未获得安装未知应用权限")
                    message.value = "未获得安装未知应用权限，请稍后重试"
                    return@launch
                }
                attemptInstall(pendingDownload.downloadId, pendingDownload.versionName)
            } else if (downloadMonitorJob?.isActive != true) {
                restorePendingUpdate()
            }
        }
    }

    private fun restorePendingUpdate() {
        val pendingDownload = updateManager.getPendingDownload() ?: return
        when (val status = updateManager.queryDownload(pendingDownload.downloadId)) {
            is DownloadStatus.Running -> {
                updateState.value = UpdateUiState.Downloading(pendingDownload.versionName, status.progressPercent)
                startDownloadMonitor(pendingDownload.downloadId, pendingDownload.versionName)
            }
            DownloadStatus.Successful -> {
                updateState.value = UpdateUiState.ReadyToInstall(pendingDownload.versionName)
                viewModelScope.launch {
                    attemptInstall(pendingDownload.downloadId, pendingDownload.versionName)
                }
            }
            is DownloadStatus.Failed -> {
                updateManager.clearPendingDownload()
                awaitingInstallPermission.value = false
                updateState.value = UpdateUiState.Error(status.message)
            }
        }
    }

    private fun startDownloadMonitor(downloadId: Long, versionName: String) {
        downloadMonitorJob?.cancel()
        downloadMonitorJob = viewModelScope.launch {
            updateState.value = UpdateUiState.Downloading(versionName, null)
            while (true) {
                when (val status = withContext(Dispatchers.IO) { updateManager.queryDownload(downloadId) }) {
                    is DownloadStatus.Running -> {
                        updateState.value = UpdateUiState.Downloading(versionName, status.progressPercent)
                        delay(750)
                    }
                    DownloadStatus.Successful -> {
                        updateState.value = UpdateUiState.ReadyToInstall(versionName)
                        attemptInstall(downloadId, versionName)
                        return@launch
                    }
                    is DownloadStatus.Failed -> {
                        updateManager.clearPendingDownload()
                        awaitingInstallPermission.value = false
                        updateState.value = UpdateUiState.Error(status.message)
                        message.value = status.message
                        return@launch
                    }
                }
            }
        }
    }

    private fun attemptInstall(downloadId: Long, versionName: String) {
        when (val result = updateManager.installDownloadedApk(downloadId)) {
            InstallResult.Launched -> {
                awaitingInstallPermission.value = false
                updateManager.setAwaitingInstallPermission(false)
                updateState.value = UpdateUiState.ReadyToInstall(versionName)
                message.value = "下载完成，正在打开安装界面"
            }
            InstallResult.PermissionRequired -> {
                awaitingInstallPermission.value = true
                updateManager.setAwaitingInstallPermission(true)
                updateState.value = UpdateUiState.ReadyToInstall(versionName)
                message.value = "请允许安装未知应用，返回后会继续安装"
                updateManager.openInstallPermissionSettings()
            }
            is InstallResult.Failed -> {
                updateState.value = UpdateUiState.Error(result.message)
                message.value = result.message
            }
        }
    }

    private fun ensureMonth(month: YearMonth) {
        viewModelScope.launch {
            repository.ensureMonthExists(month)
        }
    }

    private fun emitFeedback() {
        feedbackSignal.value += 1
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return OvertimeViewModel(application) as T
                }
            }
        }
    }
}
