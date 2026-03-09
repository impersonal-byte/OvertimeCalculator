package com.peter.overtimecalculator.data.holiday

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.peter.overtimecalculator.domain.HolidayRemoteMetadata
import com.peter.overtimecalculator.domain.HolidayRulesSnapshot
import com.peter.overtimecalculator.domain.HolidayYearRules
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Context.holidayRulesDataStore: DataStore<Preferences> by preferencesDataStore(name = "holiday-rules")

sealed interface HolidayRefreshResult {
    data object Updated : HolidayRefreshResult

    data object Skipped : HolidayRefreshResult

    data class Failed(val retryable: Boolean) : HolidayRefreshResult
}

class HolidayRulesRepository(
    private val context: Context,
    private val applicationScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val currentYearProvider: () -> Int = { LocalDate.now().year },
    private val remoteUrlTemplate: String = DEFAULT_REMOTE_URL_TEMPLATE,
) {
    private val dataStore = context.holidayRulesDataStore
    private val baselineRules = context.assets.open(BASELINE_ASSET_PATH).bufferedReader().use { reader ->
        HolidayRulesJsonParser.parse(reader.readText())
    }
    private val _rules = MutableStateFlow(baselineRules)

    val rules: StateFlow<HolidayRulesSnapshot> = _rules.asStateFlow()

    init {
        applicationScope.launch(ioDispatcher) {
            val cachedJson = dataStore.data.first()[KEY_REMOTE_JSON]
            val cachedRules = cachedJson?.let(::parseRemoteOrNull)
            _rules.value = mergeSnapshots(baselineRules, cachedRules)
        }
    }

    fun currentRules(): HolidayRulesSnapshot = _rules.value

    suspend fun refreshIfStale(maxAgeHours: Long = 24): HolidayRefreshResult {
        val preferences = dataStore.data.first()
        val lastFetchEpochMillis = preferences[KEY_FETCHED_AT_EPOCH_MILLIS]
        val hasRemoteCache = !preferences[KEY_REMOTE_JSON].isNullOrBlank()
        val isStale = lastFetchEpochMillis == null ||
            System.currentTimeMillis() - lastFetchEpochMillis >= TimeUnit.HOURS.toMillis(maxAgeHours)
        return if (!hasRemoteCache || isStale) refreshRemoteRules() else HolidayRefreshResult.Skipped
    }

    suspend fun refreshRemoteRules(): HolidayRefreshResult {
        return withContext(ioDispatcher) {
            val overlaySnapshot = try {
                fetchRemoteOverlaySnapshot()
            } catch (_: Exception) {
                return@withContext HolidayRefreshResult.Failed(retryable = true)
            }
            if (overlaySnapshot.years.isEmpty()) {
                return@withContext HolidayRefreshResult.Skipped
            }

            val fetchedAt = System.currentTimeMillis()
            val serializedOverlay = HolidayRulesJsonSerializer.serialize(overlaySnapshot)
            dataStore.edit { preferences ->
                preferences[KEY_REMOTE_JSON] = serializedOverlay
                preferences[KEY_FETCHED_AT_EPOCH_MILLIS] = fetchedAt
                preferences[KEY_REMOTE_UPDATED_AT] = overlaySnapshot.updatedAt
            }
            _rules.value = mergeSnapshots(baselineRules, overlaySnapshot)
            HolidayRefreshResult.Updated
        }
    }

    suspend fun readRemoteMetadata(): HolidayRemoteMetadata? {
        val preferences = dataStore.data.first()
        val fetchedAt = preferences[KEY_FETCHED_AT_EPOCH_MILLIS] ?: return null
        return HolidayRemoteMetadata(
            sourceUrl = remoteUrlTemplate,
            fetchedAtEpochMillis = fetchedAt,
            updatedAt = preferences[KEY_REMOTE_UPDATED_AT].orEmpty(),
        )
    }

    private fun parseRemoteOrNull(json: String): HolidayRulesSnapshot? {
        return try {
            HolidayRulesJsonParser.parse(json)
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchRemoteOverlaySnapshot(): HolidayRulesSnapshot {
        val currentYear = currentYearProvider()
        val remoteYears = linkedMapOf<Int, HolidayYearRules>()

        for (year in currentYear..(currentYear + REMOTE_FORWARD_YEARS)) {
            val json = try {
                fetchRemoteJson(year)
            } catch (_: Exception) {
                continue
            }
            val parsedYearRules = try {
                HolidayTimorApiParser.parseYear(json)
            } catch (_: Exception) {
                continue
            }
            if (parsedYearRules.hasMeaningfulOverrides()) {
                remoteYears[year] = parsedYearRules
            }
        }

        return HolidayRulesSnapshot(
            schemaVersion = baselineRules.schemaVersion,
            updatedAt = Instant.now().toString(),
            years = remoteYears,
        )
    }

    private fun fetchRemoteJson(year: Int): String {
        val remoteUrl = remoteUrlTemplate.format(year)
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

    private fun mergeSnapshots(
        baseline: HolidayRulesSnapshot,
        overlay: HolidayRulesSnapshot?,
    ): HolidayRulesSnapshot {
        if (overlay == null || overlay.years.isEmpty()) return baseline
        return baseline.copy(
            updatedAt = overlay.updatedAt.ifBlank { baseline.updatedAt },
            years = baseline.years + overlay.years,
        )
    }

    companion object {
        const val BASELINE_ASSET_PATH = "holidays/cn_mainland.json"
        const val DEFAULT_REMOTE_URL_TEMPLATE =
            "https://timor.tech/api/holiday/year/%d/?type=Y&week=Y"
        private const val REMOTE_FORWARD_YEARS = 1

        private val KEY_REMOTE_JSON = stringPreferencesKey("remote_json")
        private val KEY_FETCHED_AT_EPOCH_MILLIS = longPreferencesKey("fetched_at_epoch_millis")
        private val KEY_REMOTE_UPDATED_AT = stringPreferencesKey("remote_updated_at")
    }
}

private fun HolidayYearRules.hasMeaningfulOverrides(): Boolean {
    return holidayDates.isNotEmpty() || workingDates.isNotEmpty()
}
