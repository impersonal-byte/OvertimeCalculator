package com.peter.overtimecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.ui.AppUpdateViewModel
import com.peter.overtimecalculator.ui.OvertimeCalculatorApp
import com.peter.overtimecalculator.ui.OvertimeViewModel
import com.peter.overtimecalculator.ui.theme.OvertimeCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val overtimeViewModel: OvertimeViewModel = viewModel(
                factory = appViewModelFactory { OvertimeViewModel(application) },
            )
            val appUpdateViewModel: AppUpdateViewModel = viewModel(
                factory = appViewModelFactory { AppUpdateViewModel(application) },
            )
            val uiState by overtimeViewModel.uiState.collectAsStateWithLifecycle()
            val isDarkTheme = when (uiState.appTheme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            OvertimeCalculatorTheme(
                darkTheme = isDarkTheme,
                dynamicColor = uiState.useDynamicColor,
                seedColor = uiState.seedColor,
            ) {
                OvertimeCalculatorApp(
                    viewModel = overtimeViewModel,
                    appUpdateViewModel = appUpdateViewModel,
                )
            }
        }
    }
}
