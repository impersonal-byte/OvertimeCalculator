package com.peter.overtimecalculator

import com.peter.overtimecalculator.data.update.GitHubUpdateReleaseChecker
import com.peter.overtimecalculator.domain.UpdateCheckResult
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateReleaseCheckerTest {
    @Test
    fun checkLatestRelease_returnsAvailable_whenRemoteReleaseIsNewer() = runTest {
        val body = """
            {
              "tag_name": "v1.2.1",
              "body": "Bug fixes",
              "published_at": "2026-03-01T00:00:00Z",
              "assets": [
                {
                  "name": "OvertimeCalculator-1.2.1-universal.apk",
                  "browser_download_url": "https://example.com/release.apk"
                }
              ]
            }
        """.trimIndent()
        val server = httpServer(statusCode = 200, responseBody = body)
        try {
            val checker = GitHubUpdateReleaseChecker(latestReleaseUrl = serverUrl(server))

            val result = checker.checkLatestRelease(currentVersionName = "1.2.0")

            val available = result as UpdateCheckResult.Available
            assertEquals("1.2.1", available.update.versionName)
            assertEquals("https://example.com/release.apk", available.update.apkUrl)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun checkLatestRelease_returnsFailure_forForbiddenResponse() = runTest {
        val server = httpServer(statusCode = 403, responseBody = "rate limited")
        try {
            val checker = GitHubUpdateReleaseChecker(latestReleaseUrl = serverUrl(server))

            val result = checker.checkLatestRelease(currentVersionName = "1.2.0")

            val failure = result as UpdateCheckResult.Failure
            assertTrue(failure.message.contains("GitHub 请求受限"))
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun checkLatestRelease_returnsFailure_whenNoApkAssetExists() = runTest {
        val body = """
            {
              "tag_name": "v1.2.1",
              "body": "Bug fixes",
              "published_at": "2026-03-01T00:00:00Z",
              "assets": [
                {
                  "name": "notes.txt",
                  "browser_download_url": "https://example.com/notes.txt"
                }
              ]
            }
        """.trimIndent()
        val server = httpServer(statusCode = 200, responseBody = body)
        try {
            val checker = GitHubUpdateReleaseChecker(latestReleaseUrl = serverUrl(server))

            val result = checker.checkLatestRelease(currentVersionName = "1.2.0")

            val failure = result as UpdateCheckResult.Failure
            assertTrue(failure.message.contains("未找到可下载安装包"))
        } finally {
            server.stop(0)
        }
    }

    private fun httpServer(statusCode: Int, responseBody: String): HttpServer {
        return HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/") { exchange: HttpExchange ->
                val bytes = responseBody.toByteArray(StandardCharsets.UTF_8)
                exchange.sendResponseHeaders(statusCode, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            start()
        }
    }

    private fun serverUrl(server: HttpServer): String {
        return "http://127.0.0.1:${server.address.port}/"
    }
}
