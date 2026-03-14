package com.peter.overtimecalculator.data.holiday

import java.net.HttpURLConnection
import java.net.URL

interface HolidayRemoteClient {
    fun fetch(remoteUrl: String): String
}

class HttpUrlConnectionHolidayRemoteClient : HolidayRemoteClient {
    override fun fetch(remoteUrl: String): String {
        val connection = (URL(remoteUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "JiaxinHolidaySync/1.0")
        }

        return try {
            val statusCode = connection.responseCode
            require(statusCode in 200..299) { "Holiday rules request failed: $statusCode" }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
