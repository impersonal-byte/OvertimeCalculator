package com.peter.overtimecalculator.ui

import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peter.overtimecalculator.domain.BackupSnapshot
import java.time.YearMonth
import kotlinx.coroutines.launch

private const val SAF_BACKUP_CREATE_MIME_TYPE = "application/octet-stream"

@Composable
internal fun OvertimeAppEffects(
    viewModel: OvertimeViewModel,
    appUpdateViewModel: AppUpdateViewModel,
    updateMessage: String?,
    snackbarHostState: SnackbarHostState,
    tickHaptic: TickHapticFeedback,
    onNavigateHomeAfterRestore: (YearMonth) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val restoreConfirmation by viewModel.restoreConfirmation.collectAsStateWithLifecycle()
    var pendingBackupContent by remember { mutableStateOf<String?>(null) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(SAF_BACKUP_CREATE_MIME_TYPE),
    ) { uri ->
        val encodedBackup = pendingBackupContent
        pendingBackupContent = null
        if (uri == null || encodedBackup == null) {
            return@rememberLauncherForActivityResult
        }

        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(encodedBackup.toByteArray(Charsets.UTF_8))
                output.flush()
            } ?: error("无法创建备份文件")
        }.onSuccess {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("备份已保存")
            }
        }.onFailure {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("保存备份失败，请稍后重试")
            }
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        runCatching {
            val displayName = context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else {
                    null
                }
            }
            val content = context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            } ?: error("无法读取备份文件")
            Triple(displayName, content, uri)
        }.onSuccess { (displayName, content, _) ->
            if (content.isBlank()) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("备份文件为空，请重新导出备份后再试")
                }
                return@onSuccess
            }

            if (displayName != null && !displayName.endsWith(BackupSnapshot.BACKUP_FILE_EXTENSION, ignoreCase = true)) {
                viewModel.previewRestoreBackup(content)
                return@onSuccess
            }

            viewModel.previewRestoreBackup(content)
        }
            .onFailure {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("读取备份文件失败，请稍后重试")
                }
            }
    }

    HandleUpdateMessageEffect(
        updateMessage = updateMessage,
        onMessageConsumed = appUpdateViewModel::clearMessage,
        snackbarHostState = snackbarHostState,
    )
    HandleUiEventsEffect(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        tickHaptic = tickHaptic,
        onCreateBackup = { encodedBackup, fileName ->
            pendingBackupContent = encodedBackup
            createBackupLauncher.launch(fileName)
        },
        onPickRestoreFile = {
            openBackupLauncher.launch(arrayOf("application/octet-stream", BackupSnapshot.BACKUP_MIME_TYPE, "application/json", "*/*"))
        },
        onNavigateHomeAfterRestore = onNavigateHomeAfterRestore,
    )
    HandleUpdateResumeEffect(appUpdateViewModel = appUpdateViewModel)

    restoreConfirmation?.let { confirmation ->
        RestoreConfirmationDialog(
            confirmation = confirmation,
            onConfirm = viewModel::confirmRestore,
            onDismiss = viewModel::dismissRestoreConfirmation,
        )
    }
}

@Composable
private fun HandleUpdateMessageEffect(
    updateMessage: String?,
    onMessageConsumed: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    LaunchedEffect(updateMessage) {
        val message = updateMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onMessageConsumed()
    }
}

@Composable
private fun HandleUiEventsEffect(
    viewModel: OvertimeViewModel,
    snackbarHostState: SnackbarHostState,
    tickHaptic: TickHapticFeedback,
    onCreateBackup: (encodedBackup: String, fileName: String) -> Unit,
    onPickRestoreFile: () -> Unit,
    onNavigateHomeAfterRestore: (YearMonth) -> Unit,
) {
    val context = LocalContext.current

    LaunchedEffect(viewModel, context) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.ShareCsvExport -> {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        event.file,
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "导出本月加薪数据"))
                }
                is UiEvent.CreateBackup -> onCreateBackup(event.encodedBackup, event.fileName)
                UiEvent.PickRestoreFile -> onPickRestoreFile()
                is UiEvent.NavigateHomeAfterRestore -> onNavigateHomeAfterRestore(event.month)
                UiEvent.TriggerHaptic -> tickHaptic.performTick()
            }
        }
    }
}

@Composable
private fun RestoreConfirmationDialog(
    confirmation: RestoreConfirmationUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认恢复备份") },
        text = {
            Text(
                "将恢复 ${confirmation.monthCount} 个月配置、${confirmation.entryCount} 条加班记录、" +
                    "${confirmation.overrideCount} 条节假日覆盖。当前数据会被覆盖。",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认恢复")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun HandleUpdateResumeEffect(
    appUpdateViewModel: AppUpdateViewModel,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, appUpdateViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                appUpdateViewModel.onHostResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
