package com.peter.overtimecalculator.data.update

import android.app.DownloadManager
import android.content.Context
import com.peter.overtimecalculator.BuildConfig
import com.peter.overtimecalculator.domain.AppUpdateInfo
import com.peter.overtimecalculator.domain.DownloadStatus
import com.peter.overtimecalculator.domain.InstallResult
import com.peter.overtimecalculator.domain.PendingUpdateDownload
import com.peter.overtimecalculator.domain.UpdateCheckResult

interface UpdateManager {
    val currentVersionName: String

    suspend fun checkLatestRelease(): UpdateCheckResult

    fun startDownload(update: AppUpdateInfo): Long

    fun queryDownload(downloadId: Long): DownloadStatus

    fun installDownloadedApk(downloadId: Long): InstallResult

    fun getPendingDownload(): PendingUpdateDownload?

    fun clearPendingDownload()

    fun setAwaitingInstallPermission(awaiting: Boolean)

    fun isAwaitingInstallPermission(): Boolean

    fun openInstallPermissionSettings()
}

class AndroidUpdateManager(
    context: Context,
    private val releaseChecker: UpdateReleaseChecker = GitHubUpdateReleaseChecker(),
    private val sessionStore: UpdateSessionStore = SharedPreferencesUpdateSessionStore.create(context.applicationContext),
    private val downloadGateway: UpdateDownloadGateway = AndroidUpdateDownloadGateway(
        context = context.applicationContext,
        downloadManager = context.applicationContext.getSystemService(DownloadManager::class.java),
        sessionStore = SharedPreferencesUpdateSessionStore.create(context.applicationContext),
    ),
    private val installGateway: UpdateInstallGateway = AndroidUpdateInstallGateway(
        appContext = context.applicationContext,
        downloadManager = context.applicationContext.getSystemService(DownloadManager::class.java),
        sessionStore = SharedPreferencesUpdateSessionStore.create(context.applicationContext),
    ),
) : UpdateManager {
    private val appContext = context.applicationContext

    override val currentVersionName: String
        get() = BuildConfig.VERSION_NAME

    override suspend fun checkLatestRelease(): UpdateCheckResult = releaseChecker.checkLatestRelease(currentVersionName)

    override fun startDownload(update: AppUpdateInfo): Long = downloadGateway.startDownload(update)

    override fun queryDownload(downloadId: Long): DownloadStatus = downloadGateway.queryDownload(downloadId)

    override fun installDownloadedApk(downloadId: Long): InstallResult = installGateway.installDownloadedApk(downloadId)

    override fun getPendingDownload(): PendingUpdateDownload? = sessionStore.getPendingDownload()

    override fun clearPendingDownload() = sessionStore.clearPendingDownload()

    override fun setAwaitingInstallPermission(awaiting: Boolean) = sessionStore.setAwaitingInstallPermission(awaiting)

    override fun isAwaitingInstallPermission(): Boolean = sessionStore.isAwaitingInstallPermission()

    override fun openInstallPermissionSettings() = installGateway.openInstallPermissionSettings()

    companion object
}
