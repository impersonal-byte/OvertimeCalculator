package com.peter.overtimecalculator.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.DayCellUiState
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.ZeroDecimal
import com.peter.overtimecalculator.domain.toDisplayString
import com.peter.overtimecalculator.ui.theme.OvertimeTheme
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val monthFormatter = DateTimeFormatter.ofPattern("yyyy 年 M 月", Locale.CHINA)

@Composable
internal fun SummaryCard(uiState: AppUiState, dayCells: List<DayCellUiState>) {
    val defaults = OvertimeTheme.defaults
    val isDark = defaults.pageBackground.luminance() < 0.5f
    var privacyEnabled by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    val cardBrush = remember(defaults) {
        Brush.verticalGradient(
            colors = listOf(
                defaults.cardElevatedContainer,
                lerp(defaults.cardContainer, defaults.sectionContainer, 0.45f),
            ),
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = defaults.pageForeground,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 1.dp else 4.dp),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.testTag("summary_card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Text(
                        uiState.selectedMonth.format(monthFormatter),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                IconButton(
                    onClick = { privacyEnabled = !privacyEnabled },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = defaults.navigationContainer.copy(alpha = 0.76f),
                        contentColor = defaults.pageForeground,
                    ),
                    modifier = Modifier.testTag("summary_privacy_toggle"),
                ) {
                    Icon(
                        imageVector = if (privacyEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (privacyEnabled) "显示隐私金额" else "隐藏隐私金额",
                    )
                }
            }
            SensitiveSummaryValue(
                value = "¥${uiState.summary.totalPay.toDisplayString()}",
                hidden = privacyEnabled,
                visibleTag = "summary_total_pay",
                hiddenTag = "summary_total_pay_mask",
                style = SensitiveValueStyle.Total,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryMetric("累计时长", formatMinutes(uiState.summary.totalMinutes))
                SummaryMetric(
                    label = when (uiState.config.rateSource) {
                        HourlyRateSource.MANUAL -> "当前时薪·手动"
                        HourlyRateSource.REVERSE_ENGINEERED -> "当前时薪·反推"
                    },
                    value = "¥${uiState.config.hourlyRate.toDisplayString()}",
                    valueTag = "summary_hourly_rate",
                    hiddenTag = "summary_hourly_rate_mask",
                    hidden = privacyEnabled,
                )
            }
            if (uiState.summary.uncoveredCompMinutes > 0) {
                Text(
                    text = "本月调休已超过加班余额，超出 ${formatStepperDuration(uiState.summary.uncoveredCompMinutes)} 暂未计入金额，请按公司规则处理。",
                    style = MaterialTheme.typography.bodySmall,
                    color = defaults.warningTint,
                )
            }
            if (uiState.config.hourlyRate <= ZeroDecimal) {
                Text(
                    text = "时薪未设置，请到设置页录入或反推。",
                    style = MaterialTheme.typography.bodySmall,
                    color = defaults.warningTint,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            OvertimeTrendChart(
                dayCells = dayCells,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    valueTag: String? = null,
    hiddenTag: String? = null,
    hidden: Boolean = false,
) {
    val defaults = OvertimeTheme.defaults

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = defaults.pageForeground.copy(alpha = 0.72f),
        )
        SensitiveSummaryValue(
            value = value,
            hidden = hidden,
            visibleTag = valueTag,
            hiddenTag = hiddenTag,
            style = SensitiveValueStyle.Metric,
        )
    }
}

private enum class SensitiveValueStyle {
    Total,
    Metric,
}

@Composable
private fun SensitiveSummaryValue(
    value: String,
    hidden: Boolean,
    visibleTag: String?,
    style: SensitiveValueStyle,
    hiddenTag: String? = null,
) {
    val defaults = OvertimeTheme.defaults
    val textStyle = when (style) {
        SensitiveValueStyle.Total -> MaterialTheme.typography.headlineLarge
        SensitiveValueStyle.Metric -> MaterialTheme.typography.titleSmall
    }
    val textWeight = when (style) {
        SensitiveValueStyle.Total -> FontWeight.Black
        SensitiveValueStyle.Metric -> FontWeight.Bold
    }

    Box(
        modifier = if (visibleTag != null) Modifier.testTag(visibleTag) else Modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            value,
            style = textStyle,
            fontWeight = textWeight,
        )
        if (hidden) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(999.dp))
                    .then(if (hiddenTag != null) Modifier.testTag(hiddenTag) else Modifier),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            color = defaults.cardElevatedContainer.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(999.dp),
                        ),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    defaults.cardElevatedContainer.copy(alpha = 0.72f),
                                    defaults.accent.copy(alpha = 0.08f),
                                    defaults.cardElevatedContainer.copy(alpha = 0.78f),
                                    defaults.pageForeground.copy(alpha = 0.04f),
                                ),
                            ),
                            shape = RoundedCornerShape(999.dp),
                        ),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(8.dp)
                        .background(
                            color = defaults.cardElevatedContainer.copy(alpha = 0.56f),
                            shape = RoundedCornerShape(999.dp),
                        ),
                )
            }
        }
    }
}

@Composable
internal fun MonthSwitcher(
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val defaults = OvertimeTheme.defaults

    Surface(
        color = defaults.sectionContainer,
        contentColor = defaults.pageForeground,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MonthSwitchButton(onClick = onPreviousMonth, defaults = defaults) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
            }
            Text(
                selectedMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            MonthSwitchButton(onClick = onNextMonth, defaults = defaults) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
            }
        }
    }
}

@Composable
private fun MonthSwitchButton(
    onClick: () -> Unit,
    defaults: com.peter.overtimecalculator.ui.theme.ThemeDefaults,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = defaults.navigationContainer,
            contentColor = defaults.pageForeground,
        ),
        content = content,
    )
}

@Composable
private fun OvertimeTrendChart(
    dayCells: List<DayCellUiState>,
    modifier: Modifier = Modifier,
) {
    if (dayCells.isEmpty()) return

    val chartBars = remember(dayCells) {
        val maxMinutes = dayCells.maxOfOrNull { kotlin.math.abs(it.overtimeMinutes) }
            ?.coerceAtLeast(60)
            ?: 60
        dayCells.map { cell ->
            val absoluteMinutes = kotlin.math.abs(cell.overtimeMinutes)
            TrendBarData(
                heightRatio = absoluteMinutes.toFloat() / maxMinutes.toFloat(),
                alpha = when {
                    absoluteMinutes == 0 -> 0f
                    cell.overtimeMinutes < 0 -> 0.3f
                    else -> 0.85f
                },
            )
        }
    }

    if (chartBars.none { it.heightRatio > 0f }) return

    val barColor = OvertimeTheme.defaults.accent

    Canvas(
        modifier = modifier.testTag("overtime_trend_chart"),
    ) {
        val barWidth = size.width / chartBars.size.coerceAtLeast(1)
        val maxBarHeight = size.height
        val barCorner = 2.dp.toPx()

        chartBars.forEachIndexed { index, bar ->
            if (bar.heightRatio <= 0f) return@forEachIndexed

            val barHeight = maxBarHeight * bar.heightRatio
            val topY = size.height - barHeight
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x = index * barWidth + barWidth * 0.1f, y = topY),
                size = Size(width = barWidth * 0.8f, height = barHeight),
                cornerRadius = CornerRadius(barCorner, barCorner),
                alpha = bar.alpha,
            )
        }
    }
}

private data class TrendBarData(
    val heightRatio: Float,
    val alpha: Float,
)
