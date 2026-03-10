package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.ZeroDecimal
import com.peter.overtimecalculator.domain.toDisplayString
import com.peter.overtimecalculator.ui.AppUiState

private enum class HourlyRateInputMode(val label: String) {
    MANUAL("手动输入"),
    REVERSE("总额反推"),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RulesScreen(
    uiState: AppUiState,
    onSaveHourlyRate: (String) -> Unit,
    onSaveMultipliers: (String, String, String) -> Unit,
    onReverseEngineer: (String) -> Unit,
    onModeSwitch: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedModeName by rememberSaveable(uiState.config.yearMonth, uiState.config.rateSource) {
        mutableStateOf(uiState.config.rateSource.toInputMode().name)
    }
    val selectedMode = HourlyRateInputMode.valueOf(selectedModeName)

    var hourlyRateText by rememberSaveable(uiState.config.yearMonth, uiState.config.hourlyRate) {
        mutableStateOf(if (uiState.config.hourlyRate == ZeroDecimal) "" else uiState.config.hourlyRate.toDisplayString())
    }
    var weekdayRateText by rememberSaveable(uiState.config.yearMonth, uiState.config.weekdayRate) {
        mutableStateOf(uiState.config.weekdayRate.toDisplayString())
    }
    var restDayRateText by rememberSaveable(uiState.config.yearMonth, uiState.config.restDayRate) {
        mutableStateOf(uiState.config.restDayRate.toDisplayString())
    }
    var holidayRateText by rememberSaveable(uiState.config.yearMonth, uiState.config.holidayRate) {
        mutableStateOf(uiState.config.holidayRate.toDisplayString())
    }
    var reversePayText by rememberSaveable(uiState.config.yearMonth) { mutableStateOf("") }
    var showMultiplierSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SettingsTopBar(
                title = "薪酬与规则",
                onBack = onBack,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                SettingCard("时薪控制中心", "在手动输入和总额反推之间切换，本月配置会随保存结果更新。") {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hourly_mode_row"),
                    ) {
                        HourlyRateInputMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = selectedMode == mode,
                                onClick = {
                                    if (selectedMode != mode) {
                                        selectedModeName = mode.name
                                        onModeSwitch()
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = HourlyRateInputMode.entries.size,
                                ),
                            ) {
                                Text(mode.label)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    when (selectedMode) {
                        HourlyRateInputMode.MANUAL -> {
                            Column(
                                modifier = Modifier.testTag("manual_hourly_panel"),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                OutlinedTextField(
                                    value = hourlyRateText,
                                    onValueChange = { hourlyRateText = it.filterAllowedDecimal() },
                                    label = { Text("时薪（元/小时）") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("hourly_rate_input"),
                                )
                                Button(
                                    onClick = { onSaveHourlyRate(hourlyRateText) },
                                    modifier = Modifier.testTag("save_hourly_rate"),
                                ) {
                                    Text("保存时薪")
                                }
                            }
                        }

                        HourlyRateInputMode.REVERSE -> {
                            Column(
                                modifier = Modifier.testTag("reverse_panel"),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                OutlinedTextField(
                                    value = reversePayText,
                                    onValueChange = { reversePayText = it.filterAllowedDecimal() },
                                    label = { Text("已发加班工资总额") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reverse_pay_input"),
                                )
                                Text(
                                    "时薪 = 总额 / Σ(每日工时 × 当日倍率)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.testTag("hourly_formula"),
                                )
                                Text(
                                    "当前月已录入 ${formatMinutes(uiState.summary.totalMinutes)}。若没有每日明细，反推会被阻止。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Button(
                                    onClick = { onReverseEngineer(reversePayText) },
                                    modifier = Modifier.testTag("start_reverse"),
                                ) {
                                    Text("保存并反推")
                                }
                            }
                        }
                    }
                }
            }
            item {
                SettingCard("倍率设置", "工作日、休息日、节假日的加班工资倍率。") {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("multiplier_summary_card"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MultiplierBadge("工作日", weekdayRateText)
                        MultiplierBadge("休息日", restDayRateText)
                        MultiplierBadge("节假日", holidayRateText)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = { showMultiplierSheet = true },
                            modifier = Modifier.testTag("edit_multipliers_button"),
                        ) {
                            Text("修改倍率")
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        if (showMultiplierSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMultiplierSheet = false },
                modifier = Modifier.testTag("multiplier_editor_sheet"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("修改倍率", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "默认中国常规值为 1.5 / 2.0 / 3.0。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = weekdayRateText,
                        onValueChange = { weekdayRateText = it.filterAllowedDecimal() },
                        label = { Text("工作日倍率") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("weekday_rate_input"),
                    )
                    OutlinedTextField(
                        value = restDayRateText,
                        onValueChange = { restDayRateText = it.filterAllowedDecimal() },
                        label = { Text("休息日倍率") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("restday_rate_input"),
                    )
                    OutlinedTextField(
                        value = holidayRateText,
                        onValueChange = { holidayRateText = it.filterAllowedDecimal() },
                        label = { Text("节假日倍率") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("holiday_rate_input"),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = {
                                weekdayRateText = "1.50"
                                restDayRateText = "2.00"
                                holidayRateText = "3.00"
                            },
                            modifier = Modifier.testTag("reset_multipliers_button"),
                        ) {
                            Text("恢复默认值")
                        }
                        Button(
                            onClick = {
                                onSaveMultipliers(weekdayRateText, restDayRateText, holidayRateText)
                                showMultiplierSheet = false
                            },
                            modifier = Modifier.testTag("save_multipliers_button"),
                        ) {
                            Text("保存倍率")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun HourlyRateSource.toInputMode(): HourlyRateInputMode = when (this) {
    HourlyRateSource.MANUAL -> HourlyRateInputMode.MANUAL
    HourlyRateSource.REVERSE_ENGINEERED -> HourlyRateInputMode.REVERSE
}

private fun String.filterAllowedDecimal(): String {
    val raw = filter { it.isDigit() || it == '.' }
    val firstDot = raw.indexOf('.')
    return if (firstDot == -1) raw else raw.substring(0, firstDot + 1) + raw.substring(firstDot + 1).replace(".", "")
}

private fun formatMinutes(totalMinutes: Int): String {
    val sign = if (totalMinutes < 0) "-" else ""
    val absoluteMinutes = kotlin.math.abs(totalMinutes)
    return "$sign${absoluteMinutes / 60}h ${absoluteMinutes % 60}m"
}
