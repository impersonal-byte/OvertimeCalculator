package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun SettingsMainRoute(
    state: SettingsGraphState,
    onBack: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToData: () -> Unit,
    onNavigateToAbout: () -> Unit,
) {
    SettingsMainScreen(
        uiState = state.uiState,
        updateUiState = state.updateUiState,
        onBack = onBack,
        onNavigateToTheme = onNavigateToTheme,
        onNavigateToRules = onNavigateToRules,
        onNavigateToPreferences = onNavigateToPreferences,
        onNavigateToData = onNavigateToData,
        onNavigateToAbout = onNavigateToAbout,
        modifier = Modifier.padding(state.innerPadding),
    )
}

@Composable
internal fun ThemeSettingsRoute(
    state: SettingsGraphState,
    actions: SettingsGraphActions,
    onBack: () -> Unit,
) {
    ThemeSettingsScreen(
        uiState = state.uiState,
        onAppThemeChange = actions.onAppThemeChange,
        onUseDynamicColorChange = actions.onUseDynamicColorChange,
        onSeedColorChange = actions.onSeedColorChange,
        onBack = onBack,
        modifier = Modifier.padding(state.innerPadding),
    )
}

@Composable
internal fun RulesRoute(
    state: SettingsGraphState,
    actions: SettingsGraphActions,
    onBack: () -> Unit,
) {
    RulesScreen(
        uiState = state.uiState,
        onSaveHourlyRate = actions.onSaveHourlyRate,
        onSaveMultipliers = actions.onSaveMultipliers,
        onReverseEngineer = actions.onReverseEngineer,
        onModeSwitch = actions.onModeSwitch,
        onBack = onBack,
        modifier = Modifier.padding(state.innerPadding),
    )
}

@Composable
internal fun PreferencesRoute(
    state: SettingsGraphState,
    actions: SettingsGraphActions,
    onBack: () -> Unit,
) {
    PreferencesScreen(
        uiState = state.uiState,
        onCalendarStartDayChange = actions.onCalendarStartDayChange,
        onBack = onBack,
        modifier = Modifier.padding(state.innerPadding),
    )
}

@Composable
internal fun DataManagementRoute(
    state: SettingsGraphState,
    actions: SettingsGraphActions,
    onBack: () -> Unit,
) {
    DataManagementScreen(
        onBackupClick = actions.onBackupClick,
        onRestoreClick = actions.onRestoreClick,
        onExportDataClick = actions.onExportDataClick,
        onBack = onBack,
        modifier = Modifier.padding(state.innerPadding),
    )
}

@Composable
internal fun AboutRoute(
    state: SettingsGraphState,
    actions: SettingsGraphActions,
    onBack: () -> Unit,
) {
    AboutScreen(
        updateUiState = state.updateUiState,
        onCheckForUpdates = actions.onCheckForUpdates,
        onBack = onBack,
        modifier = Modifier.padding(state.innerPadding),
    )
}
