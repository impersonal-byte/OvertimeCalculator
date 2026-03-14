package com.peter.overtimecalculator.data.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.peter.overtimecalculator.domain.InstallResult

interface UpdateInstallGateway {
    fun installDownloadedApk(downloadId: Long): InstallResult

    fun openInstallPermissionSettings()
}

class AndroidUpdateInstallGateway(
    private val appContext: Context,
    private val downloadManager: DownloadManager,
    private val sessionStore: UpdateSessionStore,
) : UpdateInstallGateway {
    override fun installDownloadedApk(downloadId: Long): InstallResult {
        if (!appContext.packageManager.canRequestPackageInstalls()) {
            return InstallResult.PermissionRequired
        }

        val downloadUri = downloadManager.getUriForDownloadedFile(downloadId)
            ?: return InstallResult.Failed("安装包不可用，请重新下载")

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            data = downloadUri
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            setDataAndType(downloadUri, APK_MIME_TYPE)
        }

        return runCatching {
            appContext.startActivity(installIntent)
            sessionStore.clearPendingDownload()
            InstallResult.Launched
        }.getOrElse {
            InstallResult.Failed("无法启动安装界面")
        }
    }

    override fun openInstallPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${appContext.packageName}"),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(intent)
    }

    private companion object {
        private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
