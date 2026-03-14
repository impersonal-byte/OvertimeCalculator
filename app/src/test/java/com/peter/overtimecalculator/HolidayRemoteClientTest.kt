package com.peter.overtimecalculator

import com.peter.overtimecalculator.data.holiday.HttpUrlConnectionHolidayRemoteClient
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HolidayRemoteClientTest {
    @Test
    fun fetch_returnsBody_forSuccessfulResponse() {
        val server = httpServer(statusCode = 200, responseBody = """[{"date":"2026-10-01","status":3}]""")
        try {
            val client = HttpUrlConnectionHolidayRemoteClient()

            val body = client.fetch(serverUrl(server))

            assertEquals("""[{"date":"2026-10-01","status":3}]""", body)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun fetch_throwsHelpfulError_forNon2xxResponse() {
        val server = httpServer(statusCode = 503, responseBody = "unavailable")
        try {
            val client = HttpUrlConnectionHolidayRemoteClient()

            val error = runCatching { client.fetch(serverUrl(server)) }.exceptionOrNull()

            requireNotNull(error)
            assertTrue(error.message.orEmpty().contains("503"))
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
