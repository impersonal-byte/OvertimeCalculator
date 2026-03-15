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
            val eventDeferred = async { viewModel.events.first { it is UiEvent.ShowSnackbar } as UiEvent.ShowSnackbar }

            viewModel.confirmRestore()
            drainUntil { eventDeferred.isCompleted }

            val event = withTimeout(5_000) { eventDeferred.await() }
            assertEquals("数据恢复成功", event.message)
            withTimeout(5_000) {
                while (viewModel.restoreConfirmation.value != null) {
                    delay(20)
                }
            }
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
        val snapshot = BackupSnapshot(
            schemaVersion = BackupSnapshot.CURRENT_SCHEMA_VERSION,
            createdAt = Instant.now().toString(),
            monthlyConfigs = listOf(
                BackupMonthlyConfig(
                    yearMonth = YearMonth.of(2026, 3),
                    hourlyRate = BigDecimal("80.00"),
                    rateSource = HourlyRateSource.MANUAL,
                    weekdayRate = BigDecimal("1.50"),
                    restDayRate = BigDecimal("2.00"),
                    holidayRate = BigDecimal("3.00"),
                    lockedByUser = true,
                ),
            ),
            overtimeEntries = listOf(
                BackupOvertimeEntry(date = "2026-03-20", minutes = 150),
            ),
            holidayOverrides = listOf(
                BackupHolidayOverride(date = "2026-03-08", dayType = DayType.REST_DAY),
            ),
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
