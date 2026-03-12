package com.peter.overtimecalculator.ui.settings

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.domain.SeedColor
import com.peter.overtimecalculator.ui.AppUiState
import com.peter.overtimecalculator.ui.AppUpdateUiState
import com.peter.overtimecalculator.ui.CalendarStartDay

object SettingsDestinations {
    const val GRAPH_ROUTE = "settings_graph"
    const val MAIN = "settings/main"
    const val RULES = "settings/rules"
    const val PREFERENCES = "settings/preferences"
    const val DATA = "settings/data"
    const val ABOUT = "settings/about"
}

fun NavGraphBuilder.settingsGraph(
    navController: NavController,
    uiStateState: State<AppUiState>,
    updateUiStateState: State<AppUpdateUiState>,
    innerPadding: PaddingValues,
    onSaveHourlyRate: (String) -> Unit,
    onSaveMultipliers: (String, String, String) -> Unit,
    onReverseEngineer: (String) -> Unit,
    onCheckForUpdates: () -> Unit,
    onCalendarStartDayChange: (CalendarStartDay) -> Unit,
    onAppThemeChange: (AppTheme) -> Unit,
    onUseDynamicColorChange: (Boolean) -> Unit,
    onSeedColorChange: (SeedColor) -> Unit,
    onExportDataClick: () -> Unit,
    onModeSwitch: () -> Unit,
) {
    navigation(
        startDestination = SettingsDestinations.MAIN,
        route = SettingsDestinations.GRAPH_ROUTE,
    ) {
        composable(
            route = SettingsDestinations.MAIN,
            enterTransition = { settingsForwardEnter() },
            exitTransition = { settingsForwardExit() },
            popEnterTransition = { settingsBackEnter() },
            popExitTransition = { settingsBackExit() },
        ) {
            val uiState = uiStateState.value
            val updateUiState = updateUiStateState.value
            SettingsMainScreen(
                uiState = uiState,
                updateUiState = updateUiState,
                onBack = { navController.popBackStack() },
                onNavigateToRules = { navController.navigate(SettingsDestinations.RULES) },
                onNavigateToPreferences = { navController.navigate(SettingsDestinations.PREFERENCES) },
                onNavigateToData = { navController.navigate(SettingsDestinations.DATA) },
                onNavigateToAbout = { navController.navigate(SettingsDestinations.ABOUT) },
                modifier = Modifier.padding(innerPadding),
            )
        }

        composable(
            route = SettingsDestinations.RULES,
            enterTransition = { settingsForwardEnter() },
            exitTransition = { settingsForwardExit() },
            popEnterTransition = { settingsBackEnter() },
            popExitTransition = { settingsBackExit() },
        ) {
            val uiState = uiStateState.value
            RulesScreen(
                uiState = uiState,
                onSaveHourlyRate = onSaveHourlyRate,
                onSaveMultipliers = onSaveMultipliers,
                onReverseEngineer = onReverseEngineer,
                onModeSwitch = onModeSwitch,
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(innerPadding),
            )
        }

        composable(
            route = SettingsDestinations.PREFERENCES,
            enterTransition = { settingsForwardEnter() },
            exitTransition = { settingsForwardExit() },
            popEnterTransition = { settingsBackEnter() },
            popExitTransition = { settingsBackExit() },
        ) {
            val uiState = uiStateState.value
            PreferencesScreen(
                uiState = uiState,
                onCalendarStartDayChange = onCalendarStartDayChange,
                onAppThemeChange = onAppThemeChange,
                onUseDynamicColorChange = onUseDynamicColorChange,
                onSeedColorChange = onSeedColorChange,
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(innerPadding),
            )
        }

        composable(
            route = SettingsDestinations.DATA,
            enterTransition = { settingsForwardEnter() },
            exitTransition = { settingsForwardExit() },
            popEnterTransition = { settingsBackEnter() },
            popExitTransition = { settingsBackExit() },
        ) {
            DataManagementScreen(
                onExportDataClick = onExportDataClick,
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(innerPadding),
            )
        }

        composable(
            route = SettingsDestinations.ABOUT,
            enterTransition = { settingsForwardEnter() },
            exitTransition = { settingsForwardExit() },
            popEnterTransition = { settingsBackEnter() },
            popExitTransition = { settingsBackExit() },
        ) {
            val updateUiState = updateUiStateState.value
            AboutScreen(
                updateUiState = updateUiState,
                onCheckForUpdates = onCheckForUpdates,
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

private fun AnimatedContentTransitionScope<*>.settingsForwardEnter() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))

private fun AnimatedContentTransitionScope<*>.settingsForwardExit() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))

private fun AnimatedContentTransitionScope<*>.settingsBackEnter() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))

private fun AnimatedContentTransitionScope<*>.settingsBackExit() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
