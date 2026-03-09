package com.peter.overtimecalculator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.peter.overtimecalculator.OvertimeApplication
import com.peter.overtimecalculator.domain.DownloadStatus
import com.peter.overtimecalculator.domain.InstallResult
import com.peter.overtimecalculator.domain.UpdateCheckResult
import com.peter.overtimecalculator.domain.UpdateUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppUpdateUiState(
    val currentVersionName: String,
    val updateState: UpdateUiState,
    val awaitingInstallPermission: Boolean,
    val message: String? = null,
) {
    companion object {
        fun empty(currentVersionName: String): AppUpdateUiState {
            return AppUpdateUiState(
                currentVersionName = currentVersionName,
                updateState = UpdateUiState.Idle,
                awaitingInstallPermission = false,
            )
        }
    }
}

class AppUpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val updateManager = (application as OvertimeApplication).appContainer.updateManager
    private val updateState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    private val awaitingInstallPermission = MutableStateFlow(updateManager.isAwaitingInstallPermission())
    private val message = MutableStateFlow<String?>(null)
    private var downloadMonitorJob: Job? = null

    val uiState: StateFlow<AppUpdateUiState> = combine(
        updateState,
        awaitingInstallPermission,
        message,
    ) { currentUpdateState, waitingPermission, snackbar ->
        AppUpdateUiState(
            currentVersionName = updateManager.currentVersionName,
            updateState = currentUpdateState,
            awaitingInstallPermission = waitingPermission,
            message = snackbar,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppUpdateUiState.empty(updateManager.currentVersionName),
    )

    init {
        restorePendingUpdate()
    }

    fun clearMessage() {
        message.value = null
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

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return AppUpdateViewModel(application) as T
                }
            }
        }
    }
}
