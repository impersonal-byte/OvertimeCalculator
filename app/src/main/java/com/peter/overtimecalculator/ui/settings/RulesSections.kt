package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.ui.formatMinutes

internal enum class HourlyRateInputMode(val label: String) {
    MANUAL("手动输入"),
    REVERSE("总额反推"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HourlyRateControlSection(
    selectedMode: HourlyRateInputMode,
    hourlyRateText: String,
    reversePayText: String,
    totalMinutes: Int,
    onModeSelected: (HourlyRateInputMode) -> Unit,
    onHourlyRateTextChange: (String) -> Unit,
    onSaveHourlyRate: () -> Unit,
    onReversePayTextChange: (String) -> Unit,
    onReverseEngineer: () -> Unit,
) {
    SettingCard("时薪控制中心", "在手动输入和总额反推之间切换，本月配置会随保存结果更新。") {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hourly_mode_row"),
        ) {
            HourlyRateInputMode.entries.forEachIndexed { index, mode ->
                val modeTag = if (mode == HourlyRateInputMode.MANUAL) {
                    "hourly_mode_manual"
                } else {
                    "hourly_mode_reverse"
                }
                SegmentedButton(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = HourlyRateInputMode.entries.size,
                    ),
                    modifier = Modifier.testTag(modeTag),
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
                        onValueChange = onHourlyRateTextChange,
                        label = { Text("时薪（元/小时）") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hourly_rate_input"),
                    )
                    Button(
                        onClick = onSaveHourlyRate,
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
                        onValueChange = onReversePayTextChange,
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
                        "当前月已录入 ${formatMinutes(totalMinutes)}。若没有每日明细，反推会被阻止。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = onReverseEngineer,
                        modifier = Modifier.testTag("start_reverse"),
                    ) {
                        Text("保存并反推")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MultiplierSection(
    weekdayRateText: String,
    restDayRateText: String,
    holidayRateText: String,
    onEditMultipliers: () -> Unit,
) {
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
                onClick = onEditMultipliers,
                modifier = Modifier.testTag("edit_multipliers_button"),
            ) {
                Text("修改倍率")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MultiplierEditorSheet(
    weekdayRateText: String,
    restDayRateText: String,
    holidayRateText: String,
    onDismiss: () -> Unit,
    onWeekdayRateChange: (String) -> Unit,
    onRestDayRateChange: (String) -> Unit,
    onHolidayRateChange: (String) -> Unit,
    onResetDefaults: () -> Unit,
    onSave: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                onValueChange = onWeekdayRateChange,
                label = { Text("工作日倍率") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekday_rate_input"),
            )
            OutlinedTextField(
                value = restDayRateText,
                onValueChange = onRestDayRateChange,
                label = { Text("休息日倍率") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("restday_rate_input"),
            )
            OutlinedTextField(
                value = holidayRateText,
                onValueChange = onHolidayRateChange,
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
                    onClick = onResetDefaults,
                    modifier = Modifier.testTag("reset_multipliers_button"),
                ) {
                    Text("恢复默认值")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.testTag("save_multipliers_button"),
                ) {
                    Text("保存倍率")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

internal fun String.filterAllowedDecimal(): String {
    val raw = filter { it.isDigit() || it == '.' }
    val firstDot = raw.indexOf('.')
    return if (firstDot == -1) raw else raw.substring(0, firstDot + 1) + raw.substring(firstDot + 1).replace(".", "")
}
