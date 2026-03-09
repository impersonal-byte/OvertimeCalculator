package com.peter.overtimecalculator.domain

data class AppUpdateInfo(
    val versionName: String,
    val releaseNotes: String,
    val apkUrl: String,
    val publishedAt: String,
)

data class ReleaseAsset(
    val name: String,
    val downloadUrl: String,
)

sealed interface UpdateUiState {
    data object Idle : UpdateUiState

    data object Checking : UpdateUiState

    data class UpToDate(val currentVersion: String) : UpdateUiState

    data class UpdateAvailable(val remoteVersion: String) : UpdateUiState

    data class Downloading(val remoteVersion: String, val progressPercent: Int?) : UpdateUiState

    data class ReadyToInstall(val remoteVersion: String) : UpdateUiState

    data class Error(val message: String) : UpdateUiState
}

sealed interface UpdateCheckResult {
    data class Available(val update: AppUpdateInfo) : UpdateCheckResult

    data class UpToDate(val currentVersion: String) : UpdateCheckResult

    data class Failure(val message: String) : UpdateCheckResult
}

sealed interface DownloadStatus {
    data class Running(val progressPercent: Int?) : DownloadStatus

    data object Successful : DownloadStatus

    data class Failed(val message: String) : DownloadStatus
}

sealed interface InstallResult {
    data object Launched : InstallResult

    data object PermissionRequired : InstallResult

    data class Failed(val message: String) : InstallResult
}

data class PendingUpdateDownload(
    val downloadId: Long,
    val versionName: String,
)

object AppUpdateVersioning {
    fun isRemoteNewer(currentVersion: String, remoteVersion: String): Boolean {
        val remote = parse(remoteVersion)
        val current = parse(currentVersion)
        for (index in 0..2) {
            if (remote[index] != current[index]) {
                return remote[index] > current[index]
            }
        }
        return false
    }

    fun normalize(versionName: String): String {
        return versionName.trim().removePrefix("v").removePrefix("V")
    }

    private fun parse(versionName: String): List<Int> {
        val parts = normalize(versionName)
            .split(".")
            .mapNotNull { it.toIntOrNull() }

        return listOf(
            parts.getOrElse(0) { 0 },
            parts.getOrElse(1) { 0 },
            parts.getOrElse(2) { 0 },
        )
    }
}

object AppUpdateAssetSelector {
    fun selectApkAsset(versionName: String, assets: List<ReleaseAsset>): ReleaseAsset? {
        val normalized = AppUpdateVersioning.normalize(versionName)
        val preferredName = "OvertimeCalculator-$normalized-universal.apk"
        return assets.firstOrNull { it.name == preferredName }
            ?: assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
    }
}
