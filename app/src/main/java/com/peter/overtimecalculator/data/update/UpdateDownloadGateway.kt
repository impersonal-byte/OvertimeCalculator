package com.peter.overtimecalculator.data.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.peter.overtimecalculator.domain.AppUpdateInfo
import com.peter.overtimecalculator.domain.DownloadStatus

interface UpdateDownloadGateway {
    fun startDownload(update: AppUpdateInfo): Long

    fun queryDownload(downloadId: Long): DownloadStatus
}

class AndroidUpdateDownloadGateway(
    private val context: Context,
    private val downloadManager: DownloadManager,
    private val sessionStore: UpdateSessionStore,
) : UpdateDownloadGateway {
    override fun startDownload(update: AppUpdateInfo): Long {
        val fileName = "OvertimeCalculator-${update.versionName}-universal.apk"
        val request = DownloadManager.Request(Uri.parse(update.apkUrl))
            .setTitle("加薪 ${update.versionName}")
            .setDescription("正在下载更新包")
            .setMimeType(APK_MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadId = downloadManager.enqueue(request)
        sessionStore.savePendingDownload(downloadId, update.versionName)
        return downloadId
    }

    override fun queryDownload(downloadId: Long): DownloadStatus {
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query).use { cursor ->
            if (cursor == null || !cursor.moveToFirst()) {
                return DownloadStatus.Failed("下载记录不存在")
            }

            return when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.Successful
                DownloadManager.STATUS_FAILED -> DownloadStatus.Failed("下载失败，请检查网络后重试")
                else -> {
                    val soFar = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val progress = if (total > 0L) ((soFar * 100) / total).toInt().coerceIn(0, 100) else null
                    DownloadStatus.Running(progress)
                }
            }
        }
    }

    private companion object {
        private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
