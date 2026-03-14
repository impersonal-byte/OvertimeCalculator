package com.peter.overtimecalculator.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material3.SnackbarHostState

@Composable
internal fun OvertimeAppEffects(
    viewModel: OvertimeViewModel,
    appUpdateViewModel: AppUpdateViewModel,
    updateMessage: String?,
    snackbarHostState: SnackbarHostState,
    tickHaptic: TickHapticFeedback,
) {
    HandleUpdateMessageEffect(
        updateMessage = updateMessage,
        onMessageConsumed = appUpdateViewModel::clearMessage,
        snackbarHostState = snackbarHostState,
    )
    HandleUiEventsEffect(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        tickHaptic = tickHaptic,
    )
    HandleUpdateResumeEffect(appUpdateViewModel = appUpdateViewModel)
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
                UiEvent.TriggerHaptic -> tickHaptic.performTick()
            }
        }
    }
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
