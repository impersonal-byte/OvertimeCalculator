package com.peter.overtimecalculator.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.peter.overtimecalculator.ui.settings.SettingsGraphActions
import com.peter.overtimecalculator.ui.settings.SettingsGraphState
import com.peter.overtimecalculator.ui.settings.settingsGraph

internal const val HomeRoute = "home"

@Composable
internal fun OvertimeNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: AppUiState,
    updateUiState: AppUpdateUiState,
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
    onExportDataClick: () -> Unit,
    onModeSwitch: () -> Unit,
) {
    val settingsState = SettingsGraphState(
        uiState = uiState,
        updateUiState = updateUiState,
        innerPadding = PaddingValues(0.dp),
    )
    val settingsActions = SettingsGraphActions(
        onSaveHourlyRate = onSaveHourlyRate,
        onSaveMultipliers = onSaveMultipliers,
        onReverseEngineer = onReverseEngineer,
        onCheckForUpdates = onCheckForUpdates,
        onCalendarStartDayChange = onCalendarStartDayChange,
        onAppThemeChange = onAppThemeChange,
        onUseDynamicColorChange = onUseDynamicColorChange,
        onSeedColorChange = onSeedColorChange,
        onExportDataClick = onExportDataClick,
        onModeSwitch = onModeSwitch,
    )

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable(HomeRoute) {
            HomeScreen(
                uiState = uiState,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onDayClick = onDayClick,
            )
        }
        settingsGraph(
            navController = navController,
            state = settingsState,
            actions = settingsActions,
        )
    }
}
