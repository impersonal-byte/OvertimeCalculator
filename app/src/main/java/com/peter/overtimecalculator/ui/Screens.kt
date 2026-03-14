package com.peter.overtimecalculator.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun OvertimeCalculatorApp(
    viewModel: OvertimeViewModel,
    appUpdateViewModel: AppUpdateViewModel,
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val updateUiState by appUpdateViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val tickHaptic = rememberTickHapticFeedback()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: HomeRoute

    OvertimeAppEffects(
        viewModel = viewModel,
        appUpdateViewModel = appUpdateViewModel,
        updateMessage = updateUiState.message,
        snackbarHostState = snackbarHostState,
        tickHaptic = tickHaptic,
    )

    OvertimeAppShell(
        navController = navController,
        currentRoute = currentRoute,
        uiState = uiState,
        updateUiState = updateUiState,
        snackbarHostState = snackbarHostState,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onDayClick = viewModel::openEditor,
        onSaveHourlyRate = viewModel::updateManualHourlyRate,
        onSaveMultipliers = viewModel::updateMultipliers,
        onReverseEngineer = viewModel::reverseEngineerHourlyRate,
        onCheckForUpdates = appUpdateViewModel::checkForUpdates,
        onCalendarStartDayChange = viewModel::updateCalendarStartDay,
        onAppThemeChange = viewModel::updateTheme,
        onUseDynamicColorChange = viewModel::updateUseDynamicColor,
        onSeedColorChange = viewModel::updateSeedColor,
        onExportDataClick = viewModel::exportMonthlyCsv,
        onModeSwitch = tickHaptic::performTick,
        onDismissEditor = viewModel::dismissEditor,
        onSaveEditor = viewModel::saveOvertimeMinutes,
    )
}
