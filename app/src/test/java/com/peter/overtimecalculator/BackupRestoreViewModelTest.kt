package com.peter.overtimecalculator

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.peter.overtimecalculator.data.AppContainer
import com.peter.overtimecalculator.domain.BackupHolidayOverride
import com.peter.overtimecalculator.domain.BackupMonthlyConfig
import com.peter.overtimecalculator.domain.BackupOvertimeEntry
import com.peter.overtimecalculator.domain.BackupSnapshot
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.ui.OvertimeViewModel
import com.peter.overtimecalculator.ui.UiEvent
import java.math.BigDecimal
import java.time.Instant
import java.time.YearMonth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class BackupRestoreViewModelTest {
    private lateinit var application: Application
    private lateinit var container: AppContainer
    private lateinit var viewModel: OvertimeViewModel

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        container = AppContainer(
            context = application,
            applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        )
        viewModel = OvertimeViewModel(application, container)
        clearBackupState()
    }

    @Test
    fun createBackup_emitsBackupDocumentRequestWithObackupFileName() {
        runBlocking {
            val eventDeferred = async { viewModel.events.first { it is UiEvent.CreateBackup } as UiEvent.CreateBackup }

            viewModel.createBackup()
            drainUntil { eventDeferred.isCompleted }

            val event = withTimeout(5_000) { eventDeferred.await() }
            assertTrue(event.encodedBackup.contains("\"schemaVersion\":1"))
            assertTrue(event.fileName.endsWith(BackupSnapshot.BACKUP_FILE_EXTENSION))
        }
    }

    @Test
    fun pickRestoreFile_emitsPickerEvent() {
        runBlocking {
            val eventDeferred = async { viewModel.events.first { it is UiEvent.PickRestoreFile } }

            viewModel.pickRestoreFile()
            drainUntil { eventDeferred.isCompleted }

            withTimeout(5_000) { eventDeferred.await() }
        }
    }

    @Test
    fun previewRestoreBackup_withValidBackup_populatesConfirmationState() {
        runBlocking {
            viewModel.previewRestoreBackup(validEncodedBackup())

            val confirmation = awaitRestoreConfirmation()
            assertNotNull(confirmation)
            assertEquals(1, confirmation?.monthCount)
            assertEquals(1, confirmation?.entryCount)
            assertEquals(1, confirmation?.overrideCount)
        }
    }

    @Test
    fun createBackup_output_canBePreviewedByRestoreFlow() {
        runBlocking {
            val eventDeferred = async { viewModel.events.first { it is UiEvent.CreateBackup } as UiEvent.CreateBackup }

            viewModel.createBackup()
            drainUntil { eventDeferred.isCompleted }
            val backupEvent = withTimeout(5_000) { eventDeferred.await() }

            viewModel.previewRestoreBackup(backupEvent.encodedBackup)

            val confirmation = awaitRestoreConfirmation()
            assertNotNull(confirmation)
        }
    }

    @Test
    fun previewRestoreBackup_rejectsCsvBeforeRestore() {
        runBlocking {
            val eventDeferred = async { viewModel.events.first { it is UiEvent.ShowSnackbar } as UiEvent.ShowSnackbar }

            viewModel.previewRestoreBackup("日期,类型,加班/调休(分钟)\n2026-01-01,工作日,60")
            drainUntil { eventDeferred.isCompleted }

            val event = withTimeout(5_000) { eventDeferred.await() }
            assertTrue(event.message.contains("CSV"))
            assertNull(viewModel.restoreConfirmation.value)
        }
    }

    @Test
    fun confirmRestore_afterPreview_restoresAndClearsConfirmation() {
        runBlocking {
            viewModel.previewRestoreBackup(validEncodedBackup())
            awaitRestoreConfirmation()
            val eventDeferred = async { viewModel.events.take(3).toList() }

            viewModel.confirmRestore()
            drainUntil { eventDeferred.isCompleted }

            val events = withTimeout(5_000) { eventDeferred.await() }
            val navigateHome = events[0] as UiEvent.NavigateHomeAfterRestore
            assertEquals(YearMonth.of(2026, 3), navigateHome.month)
            val snackbar = events[1] as UiEvent.ShowSnackbar
            assertEquals("数据恢复成功", snackbar.message)
            assertTrue(events[2] is UiEvent.TriggerHaptic)
            assertEquals(YearMonth.of(2026, 3), viewModel.uiState.value.selectedMonth)
            withTimeout(5_000) {
                while (viewModel.restoreConfirmation.value != null) {
                    delay(20)
                }
            }
        }
    }

    @Test
    fun confirmRestore_withMultipleMonths_jumpsToLatestRestoredMonth() {
        runBlocking {
            viewModel.previewRestoreBackup(encodedBackupForMonths(YearMonth.of(2026, 1), YearMonth.of(2026, 2)))
            awaitRestoreConfirmation()
            val eventDeferred = async { viewModel.events.take(3).toList() }

            viewModel.confirmRestore()
            drainUntil { eventDeferred.isCompleted }

            val events = withTimeout(5_000) { eventDeferred.await() }
            val navigateHome = events[0] as UiEvent.NavigateHomeAfterRestore
            assertEquals(YearMonth.of(2026, 2), navigateHome.month)
        }
    }

    @Test
    fun confirmRestore_ignoresFutureMaterializedConfigWithoutMeaningfulData() {
        runBlocking {
            val snapshot = BackupSnapshot(
                schemaVersion = BackupSnapshot.CURRENT_SCHEMA_VERSION,
                createdAt = Instant.now().toString(),
                monthlyConfigs = listOf(
                    BackupMonthlyConfig(
                        yearMonth = YearMonth.of(2026, 1),
                        hourlyRate = BigDecimal("80.00"),
                        rateSource = HourlyRateSource.MANUAL,
                        weekdayRate = BigDecimal("1.50"),
                        restDayRate = BigDecimal("2.00"),
                        holidayRate = BigDecimal("3.00"),
                        lockedByUser = true,
                    ),
                    BackupMonthlyConfig(
                        yearMonth = YearMonth.of(2026, 4),
                        hourlyRate = BigDecimal("80.00"),
                        rateSource = HourlyRateSource.MANUAL,
                        weekdayRate = BigDecimal("1.50"),
                        restDayRate = BigDecimal("2.00"),
                        holidayRate = BigDecimal("3.00"),
                        lockedByUser = false,
                    ),
                ),
                overtimeEntries = listOf(
                    BackupOvertimeEntry(date = "2026-01-20", minutes = 150),
                ),
                holidayOverrides = emptyList(),
            )

            viewModel.previewRestoreBackup(com.peter.overtimecalculator.data.backup.BackupSnapshotCodec().encode(snapshot))
            awaitRestoreConfirmation()
            val eventDeferred = async { viewModel.events.take(3).toList() }

            viewModel.confirmRestore()
            drainUntil { eventDeferred.isCompleted }

            val events = withTimeout(5_000) { eventDeferred.await() }
            val navigateHome = events[0] as UiEvent.NavigateHomeAfterRestore
            assertEquals(YearMonth.of(2026, 1), navigateHome.month)
        }
    }

    private fun awaitRestoreConfirmation() = runBlocking {
        drainUntil { viewModel.restoreConfirmation.value != null }
        viewModel.restoreConfirmation.value
    }

    private suspend fun drainUntil(condition: () -> Boolean) {
        withTimeout(5_000) {
            while (!condition()) {
                shadowOf(Looper.getMainLooper()).idle()
                delay(20)
            }
        }
    }

    private fun validEncodedBackup(): String {
        return encodedBackupForMonths(YearMonth.of(2026, 3))
    }

    private fun encodedBackupForMonths(vararg months: YearMonth): String {
        val snapshot = BackupSnapshot(
            schemaVersion = BackupSnapshot.CURRENT_SCHEMA_VERSION,
            createdAt = Instant.now().toString(),
            monthlyConfigs = months.map {
                BackupMonthlyConfig(
                    yearMonth = it,
                    hourlyRate = BigDecimal("80.00"),
                    rateSource = HourlyRateSource.MANUAL,
                    weekdayRate = BigDecimal("1.50"),
                    restDayRate = BigDecimal("2.00"),
                    holidayRate = BigDecimal("3.00"),
                    lockedByUser = true,
                )
            },
            overtimeEntries = months.map {
                BackupOvertimeEntry(date = it.atDay(20).toString(), minutes = 150)
            },
            holidayOverrides = months.map {
                BackupHolidayOverride(date = it.atDay(8).toString(), dayType = DayType.REST_DAY)
            },
        )
        return com.peter.overtimecalculator.data.backup.BackupSnapshotCodec().encode(snapshot)
    }

    private fun clearBackupState() {
        runBlocking {
            container.backupRestoreRepository.restoreSnapshot(
                BackupSnapshot(
                    schemaVersion = BackupSnapshot.CURRENT_SCHEMA_VERSION,
                    createdAt = Instant.now().toString(),
                    monthlyConfigs = emptyList(),
                    overtimeEntries = emptyList(),
                    holidayOverrides = emptyList(),
                ),
            ).getOrThrow()
        }
        viewModel.dismissRestoreConfirmation()
    }
}
