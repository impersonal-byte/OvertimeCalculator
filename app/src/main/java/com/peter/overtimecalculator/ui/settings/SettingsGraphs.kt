package com.peter.overtimecalculator.ui.settings

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

object SettingsDestinations {
    const val GRAPH_ROUTE = "settings_graph"
    const val MAIN = "settings/main"
    const val THEME = "settings/theme"
    const val RULES = "settings/rules"
    const val PREFERENCES = "settings/preferences"
    const val DATA = "settings/data"
    const val ABOUT = "settings/about"
}

fun NavGraphBuilder.settingsGraph(
    navController: NavController,
    state: SettingsGraphState,
    actions: SettingsGraphActions,
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
            SettingsMainRoute(
                state = state,
                onBack = { navController.popBackStack() },
                onNavigateToTheme = { navController.navigate(SettingsDestinations.THEME) },
                onNavigateToRules = { navController.navigate(SettingsDestinations.RULES) },
                onNavigateToPreferences = { navController.navigate(SettingsDestinations.PREFERENCES) },
                onNavigateToData = { navController.navigate(SettingsDestinations.DATA) },
                onNavigateToAbout = { navController.navigate(SettingsDestinations.ABOUT) },
            )
        }

        composable(
            route = SettingsDestinations.THEME,
            enterTransition = { settingsForwardEnter() },
            exitTransition = { settingsForwardExit() },
            popEnterTransition = { settingsBackEnter() },
            popExitTransition = { settingsBackExit() },
        ) {
            ThemeSettingsRoute(
                state = state,
                actions = actions,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = SettingsDestinations.RULES,
            enterTransition = { settingsForwardEnter() },
            exitTransition = { settingsForwardExit() },
            popEnterTransition = { settingsBackEnter() },
            popExitTransition = { settingsBackExit() },
        ) {
            RulesRoute(
                state = state,
                actions = actions,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = SettingsDestinations.PREFERENCES,
            enterTransition = { settingsForwardEnter() },
            exitTransition = { settingsForwardExit() },
            popEnterTransition = { settingsBackEnter() },
            popExitTransition = { settingsBackExit() },
        ) {
            PreferencesRoute(
                state = state,
                actions = actions,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = SettingsDestinations.DATA,
            enterTransition = { settingsForwardEnter() },
            exitTransition = { settingsForwardExit() },
            popEnterTransition = { settingsBackEnter() },
            popExitTransition = { settingsBackExit() },
        ) {
            DataManagementRoute(
                state = state,
                actions = actions,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = SettingsDestinations.ABOUT,
            enterTransition = { settingsForwardEnter() },
            exitTransition = { settingsForwardExit() },
            popEnterTransition = { settingsBackEnter() },
            popExitTransition = { settingsBackExit() },
        ) {
            AboutRoute(
                state = state,
                actions = actions,
                onBack = { navController.popBackStack() },
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
