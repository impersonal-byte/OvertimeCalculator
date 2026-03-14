package com.peter.overtimecalculator.data.update

import com.peter.overtimecalculator.domain.AppUpdateAssetSelector
import com.peter.overtimecalculator.domain.AppUpdateInfo
import com.peter.overtimecalculator.domain.AppUpdateVersioning
import com.peter.overtimecalculator.domain.ReleaseAsset
import com.peter.overtimecalculator.domain.UpdateCheckResult
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

interface UpdateReleaseChecker {
    suspend fun checkLatestRelease(currentVersionName: String): UpdateCheckResult
}

class GitHubUpdateReleaseChecker(
    private val latestReleaseUrl: String = LATEST_RELEASE_URL,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UpdateReleaseChecker {
    override suspend fun checkLatestRelease(currentVersionName: String): UpdateCheckResult = withContext(ioDispatcher) {
        runCatching {
            val connection = (URL(latestReleaseUrl).openConnection() as HttpURLConnection).apply {
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
                        parseRelease(body, currentVersionName)
                    }
                    HttpURLConnection.HTTP_NOT_FOUND -> UpdateCheckResult.Failure("未找到可用的发布版本")
                    HttpURLConnection.HTTP_FORBIDDEN -> UpdateCheckResult.Failure("GitHub 请求受限，请稍后再试")
                    else -> UpdateCheckResult.Failure("检查更新失败：HTTP $code")
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

    private fun parseRelease(body: String, currentVersionName: String): UpdateCheckResult {
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
    }
}
