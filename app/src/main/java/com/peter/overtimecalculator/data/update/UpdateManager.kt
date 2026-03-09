package com.peter.overtimecalculator.data.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import com.peter.overtimecalculator.BuildConfig
import com.peter.overtimecalculator.domain.AppUpdateAssetSelector
import com.peter.overtimecalculator.domain.AppUpdateInfo
import com.peter.overtimecalculator.domain.AppUpdateVersioning
import com.peter.overtimecalculator.domain.DownloadStatus
import com.peter.overtimecalculator.domain.InstallResult
import com.peter.overtimecalculator.domain.PendingUpdateDownload
import com.peter.overtimecalculator.domain.ReleaseAsset
import com.peter.overtimecalculator.domain.UpdateCheckResult
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

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

class AndroidUpdateManager(context: Context) : UpdateManager {
    private val appContext = context.applicationContext
    private val downloadManager = appContext.getSystemService(DownloadManager::class.java)
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override val currentVersionName: String
        get() = BuildConfig.VERSION_NAME

    override suspend fun checkLatestRelease(): UpdateCheckResult = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 15_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                setRequestProperty("User-Agent", "OvertimeCalculator/$currentVersionName")
            }

            connection.use { http ->
                when (val code = http.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        val body = http.inputStream.bufferedReader().use { it.readText() }
                        parseRelease(body)
                    }
                    HttpURLConnection.HTTP_NOT_FOUND -> {
                        UpdateCheckResult.Failure("未找到可用的发布版本")
                    }
                    HttpURLConnection.HTTP_FORBIDDEN -> {
                        UpdateCheckResult.Failure("GitHub 请求受限，请稍后再试")
                    }
                    else -> {
                        UpdateCheckResult.Failure("检查更新失败：HTTP $code")
                    }
                }
            }
        }.getOrElse { throwable ->
            val message = if (throwable is IOException) {
                "网络连接失败，请稍后再试"
            } else {
                throwable.message ?: "检查更新失败"
            }
            UpdateCheckResult.Failure(message)
        }
    }

    override fun startDownload(update: AppUpdateInfo): Long {
        val fileName = "OvertimeCalculator-${update.versionName}-universal.apk"
        val request = DownloadManager.Request(Uri.parse(update.apkUrl))
            .setTitle("加薪 ${update.versionName}")
            .setDescription("正在下载更新包")
            .setMimeType(APK_MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadId = downloadManager.enqueue(request)
        preferences.edit()
            .putLong(KEY_DOWNLOAD_ID, downloadId)
            .putString(KEY_REMOTE_VERSION, update.versionName)
            .putBoolean(KEY_AWAITING_PERMISSION, false)
            .apply()
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
                DownloadManager.STATUS_FAILED -> {
                    DownloadStatus.Failed("下载失败，请检查网络后重试")
                }
                else -> {
                    val soFar = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val progress = if (total > 0L) ((soFar * 100) / total).toInt().coerceIn(0, 100) else null
                    DownloadStatus.Running(progress)
                }
            }
        }
    }

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
            clearPendingDownload()
            InstallResult.Launched
        }.getOrElse {
            InstallResult.Failed("无法启动安装界面")
        }
    }

    override fun getPendingDownload(): PendingUpdateDownload? {
        val downloadId = preferences.getLong(KEY_DOWNLOAD_ID, INVALID_DOWNLOAD_ID)
        val versionName = preferences.getString(KEY_REMOTE_VERSION, null)
        if (downloadId == INVALID_DOWNLOAD_ID || versionName.isNullOrBlank()) {
            return null
        }
        return PendingUpdateDownload(downloadId, versionName)
    }

    override fun clearPendingDownload() {
        preferences.edit()
            .remove(KEY_DOWNLOAD_ID)
            .remove(KEY_REMOTE_VERSION)
            .remove(KEY_AWAITING_PERMISSION)
            .apply()
    }

    override fun setAwaitingInstallPermission(awaiting: Boolean) {
        preferences.edit().putBoolean(KEY_AWAITING_PERMISSION, awaiting).apply()
    }

    override fun isAwaitingInstallPermission(): Boolean {
        return preferences.getBoolean(KEY_AWAITING_PERMISSION, false)
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

    private fun parseRelease(body: String): UpdateCheckResult {
        val release = JSONObject(body)
        val versionName = AppUpdateVersioning.normalize(release.optString("tag_name"))
        if (!AppUpdateVersioning.isRemoteNewer(currentVersionName, versionName)) {
            return UpdateCheckResult.UpToDate(currentVersionName)
        }

        val assets = release.optJSONArray("assets").toReleaseAssets()
        val apkAsset = AppUpdateAssetSelector.selectApkAsset(versionName, assets)
            ?: return UpdateCheckResult.Failure("发现新版本，但未找到可下载安装包")

        return UpdateCheckResult.Available(
            AppUpdateInfo(
                versionName = versionName,
                releaseNotes = release.optString("body"),
                apkUrl = apkAsset.downloadUrl,
                publishedAt = release.optString("published_at"),
            ),
        )
    }

    private fun JSONArray?.toReleaseAssets(): List<ReleaseAsset> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val asset = optJSONObject(index) ?: continue
                val name = asset.optString("name")
                val url = asset.optString("browser_download_url")
                if (name.isNotBlank() && url.isNotBlank()) {
                    add(ReleaseAsset(name = name, downloadUrl = url))
                }
            }
        }
    }

    private fun HttpURLConnection.use(block: (HttpURLConnection) -> UpdateCheckResult): UpdateCheckResult {
        return try {
            block(this)
        } finally {
            disconnect()
        }
    }

    companion object {
        private const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/impersonal-byte/OvertimeCalculator/releases/latest"
        private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        private const val PREFS_NAME = "app-update-prefs"
        private const val KEY_DOWNLOAD_ID = "download_id"
        private const val KEY_REMOTE_VERSION = "remote_version"
        private const val KEY_AWAITING_PERMISSION = "awaiting_permission"
        private const val INVALID_DOWNLOAD_ID = -1L
    }
}
