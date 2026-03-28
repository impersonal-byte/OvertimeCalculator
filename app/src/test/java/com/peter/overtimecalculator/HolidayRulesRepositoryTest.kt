package com.peter.overtimecalculator

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import androidx.test.core.app.ApplicationProvider
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HolidayCalendar
import com.peter.overtimecalculator.data.holiday.HolidayRefreshResult
import com.peter.overtimecalculator.data.holiday.HolidayRemoteClient
import com.peter.overtimecalculator.data.holiday.HolidayRulesRepository
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
@OptIn(ExperimentalCoroutinesApi::class)
class HolidayRulesRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val timorUrlTemplate = "https://timor.tech/api/holiday/year/%d/?type=Y"

    @Test
    fun refreshRemoteRules_fetchesTimorYears_andPersistsRestDaySemanticsInCache() = runTest {
        val requestedUrls = mutableListOf<String>()
        val context = isolatedContext("refresh")
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = HolidayRulesRepository(
            context = context,
            applicationScope = backgroundScope,
            ioDispatcher = dispatcher,
            currentYearProvider = { 2026 },
            remoteUrlTemplate = timorUrlTemplate,
            remoteClient = fakeRemoteClient { url ->
                requestedUrls += url
                when (url) {
                    timorUrlTemplate.format(2026) ->
                        """
                        {
                          "holiday": {
                            "10-01": { "holiday": true, "name": "国庆节", "wage": 3, "date": "2026-10-01" },
                            "10-06": { "holiday": true, "name": "国庆节", "wage": 2, "date": "2026-10-06" },
                            "10-10": { "holiday": false, "name": "国庆节后补班", "wage": 1, "after": true, "target": "国庆节", "date": "2026-10-10" }
                          }
                        }
                        """.trimIndent()
                    timorUrlTemplate.format(2027) ->
                        """
                        {
                          "holiday": {
                            "01-01": { "holiday": true, "name": "元旦", "wage": 3, "date": "2027-01-01" }
                          }
                        }
                        """.trimIndent()
                    else -> error("Unexpected URL: $url")
                }
            },
        )
        advanceUntilIdle()

        val result = repository.refreshRemoteRules()
        advanceUntilIdle()

        assertEquals(HolidayRefreshResult.Updated, result)
        assertEquals(
            listOf(
                timorUrlTemplate.format(2026),
                timorUrlTemplate.format(2027),
            ),
            requestedUrls,
        )
        val calendar = HolidayCalendar(repository::currentRules)
        assertEquals(DayType.HOLIDAY, calendar.resolveDayType(LocalDate.parse("2026-10-01"), null))
        assertEquals(DayType.REST_DAY, calendar.resolveDayType(LocalDate.parse("2026-10-06"), null))
        assertEquals(DayType.WORKDAY, calendar.resolveDayType(LocalDate.parse("2026-10-10"), null))
        assertTrue(LocalDate.parse("2027-01-01") in repository.currentRules().years.getValue(2027).holidayDates)
        assertTrue(repository.currentRules().years.containsKey(2028))

        val metadata = repository.readRemoteMetadata()
        requireNotNull(metadata)
        assertEquals(timorUrlTemplate, metadata.sourceUrl)
        assertFalse(metadata.updatedAt.isBlank())
        assertTrue(metadata.fetchedAtEpochMillis > 0L)

        val cachedRepository = HolidayRulesRepository(
            context = context,
            applicationScope = backgroundScope,
            ioDispatcher = dispatcher,
            currentYearProvider = { 2026 },
            remoteUrlTemplate = timorUrlTemplate,
            remoteClient = fakeRemoteClient { error("Cached initialization should not hit network") },
        )
        advanceUntilIdle()

        val cachedCalendar = HolidayCalendar(cachedRepository::currentRules)
        assertEquals(DayType.REST_DAY, cachedCalendar.resolveDayType(LocalDate.parse("2026-10-06"), null))
    }

    @Test
    fun refreshRemoteRules_skipsWhenPayloadContainsNoOverrides() = runTest {
        val context = isolatedContext("skip")
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = HolidayRulesRepository(
            context = context,
            applicationScope = backgroundScope,
            ioDispatcher = dispatcher,
            currentYearProvider = { 2026 },
            remoteUrlTemplate = timorUrlTemplate,
            remoteClient = fakeRemoteClient { """{ "holiday": {} }""" },
        )
        advanceUntilIdle()

        val result = repository.refreshRemoteRules()

        assertEquals(HolidayRefreshResult.Skipped, result)
        assertEquals("2026-03-24T00:00:00Z", repository.currentRules().updatedAt)
        assertTrue(LocalDate.parse("2026-10-01") in repository.currentRules().years.getValue(2026).holidayDates)
    }

    private fun isolatedContext(name: String): Context {
        val base = ApplicationProvider.getApplicationContext<Context>()
        val root = temporaryFolder.newFolder(name)
        return object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this

            override fun getAssets(): AssetManager = base.assets

            override fun getDataDir(): File = root

            override fun getFilesDir(): File = File(root, "files").apply(File::mkdirs)

            override fun getCacheDir(): File = File(root, "cache").apply(File::mkdirs)

            override fun getNoBackupFilesDir(): File = File(root, "no-backup").apply(File::mkdirs)
        }
    }

    private fun fakeRemoteClient(fetcher: (String) -> String): HolidayRemoteClient {
        return object : HolidayRemoteClient {
            override fun fetch(remoteUrl: String): String = fetcher(remoteUrl)
        }
    }
}
