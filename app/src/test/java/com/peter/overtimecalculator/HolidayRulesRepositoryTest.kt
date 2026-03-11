package com.peter.overtimecalculator

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import androidx.test.core.app.ApplicationProvider
import com.peter.overtimecalculator.data.holiday.HolidayRefreshResult
import com.peter.overtimecalculator.data.holiday.HolidayRulesRepository
import java.io.File
import java.time.LocalDate
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
class HolidayRulesRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun refreshRemoteRules_fetchesCurrentAndNextYear_andPersistsMetadataAndCache() = runTest {
        val requestedUrls = mutableListOf<String>()
        val context = isolatedContext("refresh")
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = HolidayRulesRepository(
            context = context,
            applicationScope = backgroundScope,
            ioDispatcher = dispatcher,
            currentYearProvider = { 2026 },
            remoteReader = { url ->
                requestedUrls += url
                when (url) {
                    "https://api.haoshenqi.top/holiday?date=2026" ->
                        """
                        [
                          { "date": "2026-10-01", "status": 3 },
                          { "date": "2026-10-10", "status": 2 }
                        ]
                        """.trimIndent()
                    "https://api.haoshenqi.top/holiday?date=2027" ->
                        """[{ "date": "2027-01-01", "status": 3 }]"""
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
                "https://api.haoshenqi.top/holiday?date=2026",
                "https://api.haoshenqi.top/holiday?date=2027",
            ),
            requestedUrls,
        )
        assertTrue(LocalDate.parse("2026-10-01") in repository.currentRules().years.getValue(2026).holidayDates)
        assertTrue(LocalDate.parse("2026-10-10") in repository.currentRules().years.getValue(2026).workingDates)
        assertTrue(LocalDate.parse("2027-01-01") in repository.currentRules().years.getValue(2027).holidayDates)
        assertTrue(repository.currentRules().years.containsKey(2028))

        val metadata = repository.readRemoteMetadata()
        requireNotNull(metadata)
        assertEquals(HolidayRulesRepository.DEFAULT_REMOTE_URL_TEMPLATE, metadata.sourceUrl)
        assertFalse(metadata.updatedAt.isBlank())
        assertTrue(metadata.fetchedAtEpochMillis > 0L)

        val cachedRepository = HolidayRulesRepository(
            context = context,
            applicationScope = backgroundScope,
            ioDispatcher = dispatcher,
            currentYearProvider = { 2026 },
            remoteReader = { error("Cached initialization should not hit network") },
        )
        advanceUntilIdle()

        assertTrue(LocalDate.parse("2027-01-01") in cachedRepository.currentRules().years.getValue(2027).holidayDates)
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
            remoteReader = { """[{ "date": "2026-10-02", "status": 0 }]""" },
        )
        advanceUntilIdle()

        val result = repository.refreshRemoteRules()

        assertEquals(HolidayRefreshResult.Skipped, result)
        assertEquals("2026-03-09T00:00:00Z", repository.currentRules().updatedAt)
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
}
