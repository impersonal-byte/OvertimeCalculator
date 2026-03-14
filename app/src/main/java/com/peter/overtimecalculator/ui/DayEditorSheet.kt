package com.peter.overtimecalculator.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.DayType
import com.peter.overtimecalculator.domain.OvertimeEntryValidator
import com.peter.overtimecalculator.ui.components.CenteredDurationSlider
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.withTimeoutOrNull

private const val DurationStepMinutes = 30
private val PositiveDurationPresetsMinutes = listOf(30, 60, 90, 120, 180, 240, 360, 480, 600, 720, 840, 960)
private val NegativeDurationPresetsMinutes = listOf(-30, -60, -120, -240, -480)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CompTimeDayEditorSheet(
    editor: DayEditorUiState,
    onDismiss: () -> Unit,
    onSave: (Int, DayType?) -> Unit,
) {
    var overrideType by rememberSaveable(editor.date) { mutableStateOf(editor.currentOverride?.name ?: "") }
    val effectiveDayType = overrideType.takeIf { it.isNotBlank() }?.let(DayType::valueOf) ?: editor.resolvedDayType
    val minMinutes = minAllowedMinutes(effectiveDayType)
    var totalMinutes by rememberSaveable(editor.date) {
        mutableStateOf(roundSignedToNearestHalfHour(editor.currentMinutes, minMinutes))
    }

    LaunchedEffect(minMinutes) {
        totalMinutes = totalMinutes.coerceIn(minMinutes, OvertimeEntryValidator.MAX_OVERTIME_MINUTES)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.testTag("day_editor_sheet")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = editor.date.format(DateTimeFormatter.ofPattern("M 月 d 日 EEEE", Locale.CHINA)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("duration_stepper"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("工时调整", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = formatStepperDuration(totalMinutes),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_duration_value"),
                    )
                    CenteredDurationSlider(
                        valueMinutes = totalMinutes,
                        onValueMinutesChange = { totalMinutes = it },
                        minMinutes = minMinutes,
                        maxMinutes = OvertimeEntryValidator.MAX_OVERTIME_MINUTES,
                        centeredVisual = minMinutes < 0,
                    )
                    Text(
                        text = when {
                            totalMinutes < 0 -> "当前为调休申请，将优先抵扣本月工作日加班"
                            effectiveDayType == DayType.WORKDAY -> "工作日支持 -8.0h 到 16.0h，负值表示调休。"
                            else -> "当前类型仅支持 0.0h 到 16.0h。"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = { totalMinutes = 0 },
                            modifier = Modifier.testTag("clear_duration"),
                        ) {
                            Text("清零")
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (effectiveDayType == DayType.WORKDAY) {
                            NegativeDurationPresetsMinutes.forEach { presetMinutes ->
                                PresetDurationChip(
                                    label = formatStepperDuration(presetMinutes),
                                    selected = totalMinutes == presetMinutes,
                                    tag = "preset_${presetMinutes}",
                                ) {
                                    totalMinutes = presetMinutes
                                }
                            }
                        }
                        PositiveDurationPresetsMinutes.forEach { presetMinutes ->
                            PresetDurationChip(
                                label = formatStepperDuration(presetMinutes),
                                selected = totalMinutes == presetMinutes,
                                tag = "preset_${presetMinutes}",
                            ) {
                                totalMinutes = presetMinutes
                            }
                        }
                    }
                }
            }
            Surface(
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "系统判定",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = dayTypeLabel(editor.resolvedDayType),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = "日期类型覆盖",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        OverrideChip("跟随系统", overrideType.isBlank(), "override_system") { overrideType = "" }
                        DayType.entries.forEach { type ->
                            OverrideChip(
                                label = dayTypeLabel(type),
                                selected = overrideType == type.name,
                                tag = "override_${type.name.lowercase(Locale.US)}",
                            ) { overrideType = type.name }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) { Text("取消") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSave(totalMinutes, overrideType.takeIf { it.isNotBlank() }?.let(DayType::valueOf))
                            },
                            modifier = Modifier.testTag("editor_save"),
                        ) {
                            Text("保存")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverrideChip(label: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.testTag(tag),
        label = { Text(label) },
    )
}

@Composable
private fun DurationStepperButton(label: String, enabled: Boolean, tag: String, onStep: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .width(60.dp)
            .height(56.dp)
            .testTag(tag)
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = {
                        if (!enabled) {
                            return@detectTapGestures
                        }
                        onStep()
                        val releasedEarly = withTimeoutOrNull(350L) { tryAwaitRelease() }
                        if (releasedEarly == null) {
                            while (true) {
                                onStep()
                                val releasedDuringRepeat = withTimeoutOrNull(90L) { tryAwaitRelease() }
                                if (releasedDuringRepeat != null) {
                                    break
                                }
                            }
                        }
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
            )
        }
    }
}

@Composable
private fun PresetDurationChip(label: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.testTag(tag),
        label = { Text(label) },
    )
}

private fun dayTypeLabel(type: DayType): String = when (type) {
    DayType.WORKDAY -> "工作日"
    DayType.REST_DAY -> "休息日"
    DayType.HOLIDAY -> "节假日"
}

private fun roundSignedToNearestHalfHour(totalMinutes: Int, minMinutes: Int): Int {
    val rounded = if (totalMinutes >= 0) {
        ((totalMinutes + 15) / DurationStepMinutes) * DurationStepMinutes
    } else {
        -(((kotlin.math.abs(totalMinutes) + 15) / DurationStepMinutes) * DurationStepMinutes)
    }
    return rounded.coerceIn(minMinutes, OvertimeEntryValidator.MAX_OVERTIME_MINUTES)
}

private fun adjustSignedDurationMinutes(currentMinutes: Int, deltaMinutes: Int, minMinutes: Int): Int {
    return (currentMinutes + deltaMinutes).coerceIn(minMinutes, OvertimeEntryValidator.MAX_OVERTIME_MINUTES)
}

private fun minAllowedMinutes(dayType: DayType): Int {
    return if (dayType == DayType.WORKDAY) OvertimeEntryValidator.MIN_COMP_MINUTES else 0
}
