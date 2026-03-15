package com.peter.overtimecalculator.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.peter.overtimecalculator.ui.settings.SettingsDestinations

@Composable
internal fun OvertimeAppShell(
    navController: NavHostController,
    currentRoute: String,
    uiState: AppUiState,
    updateUiState: AppUpdateUiState,
    snackbarHostState: SnackbarHostState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (java.time.LocalDate) -> Unit,
    onSaveHourlyRate: (String) -> Unit,
    onSaveMultipliers: (String, String, String) -> Unit,
    onReverseEngineer: (String) -> Unit,
    onCheckForUpdates: () -> Unit,
    onCalendarStartDayChange: (CalendarStartDay) -> Unit,
    onAppThemeChange: (com.peter.overtimecalculator.domain.AppTheme) -> Unit,
    onUseDynamicColorChange: (Boolean) -> Unit,
    onSeedColorChange: (com.peter.overtimecalculator.domain.SeedColor) -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onExportDataClick: () -> Unit,
    onModeSwitch: () -> Unit,
    onDismissEditor: () -> Unit,
    onSaveEditor: (java.time.LocalDate, Int, com.peter.overtimecalculator.domain.DayType?) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppTopBar(
                currentRoute = currentRoute,
                onSettings = { navController.navigate(SettingsDestinations.GRAPH_ROUTE) },
            )
        },
    ) { innerPadding ->
        OvertimeNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            updateUiState = updateUiState,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onDayClick = onDayClick,
            onSaveHourlyRate = onSaveHourlyRate,
            onSaveMultipliers = onSaveMultipliers,
            onReverseEngineer = onReverseEngineer,
            onCheckForUpdates = onCheckForUpdates,
            onCalendarStartDayChange = onCalendarStartDayChange,
            onAppThemeChange = onAppThemeChange,
            onUseDynamicColorChange = onUseDynamicColorChange,
            onSeedColorChange = onSeedColorChange,
            onBackupClick = onBackupClick,
            onRestoreClick = onRestoreClick,
            onExportDataClick = onExportDataClick,
            onModeSwitch = onModeSwitch,
        )
    }

    uiState.editor?.let { editor ->
        CompTimeDayEditorSheet(
            editor = editor,
            onDismiss = onDismissEditor,
            onSave = { totalMinutes, overrideDayType ->
                onSaveEditor(editor.date, totalMinutes, overrideDayType)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    currentRoute: String,
    onSettings: () -> Unit,
) {
    if (currentRoute.startsWith("settings")) return

    TopAppBar(
        title = {
            Text("加班工资计算器")
        },
        actions = {
            IconButton(onClick = onSettings, modifier = Modifier.testTag("settings_button")) {
                Icon(Icons.Default.Settings, contentDescription = "设置")
            }
        },
    )
}
