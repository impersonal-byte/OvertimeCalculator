package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.SeedColor
import com.peter.overtimecalculator.ui.AppUiState
import com.peter.overtimecalculator.ui.AppUpdateUiState
import com.peter.overtimecalculator.ui.CalendarStartDay

data class SettingsGraphState(
    val uiState: AppUiState,
    val updateUiState: AppUpdateUiState,
    val innerPadding: PaddingValues = PaddingValues(0.dp),
)

data class SettingsGraphActions(
    val onSaveHourlyRate: (String) -> Unit,
    val onSaveMultipliers: (String, String, String) -> Unit,
    val onReverseEngineer: (String) -> Unit,
    val onCheckForUpdates: () -> Unit,
    val onCalendarStartDayChange: (CalendarStartDay) -> Unit,
    val onAppThemeChange: (AppTheme) -> Unit,
    val onUseDynamicColorChange: (Boolean) -> Unit,
    val onSeedColorChange: (SeedColor) -> Unit,
    val onBackupClick: () -> Unit,
    val onRestoreClick: () -> Unit,
    val onExportDataClick: () -> Unit,
    val onModeSwitch: () -> Unit,
)
