package com.peter.overtimecalculator.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.DayCellUiState
import com.peter.overtimecalculator.domain.HourlyRateSource
import com.peter.overtimecalculator.domain.ZeroDecimal
import com.peter.overtimecalculator.domain.toDisplayString
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val monthFormatter = DateTimeFormatter.ofPattern("yyyy 年 M 月", Locale.CHINA)

@Composable
internal fun SummaryCard(uiState: AppUiState, dayCells: List<DayCellUiState>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.testTag("summary_card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
            Text(
                "¥${uiState.summary.totalPay.toDisplayString()}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.testTag("summary_total_pay"),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryMetric("累计时长", formatMinutes(uiState.summary.totalMinutes))
                SummaryMetric(
                    label = when (uiState.config.rateSource) {
                        HourlyRateSource.MANUAL -> "当前时薪·手动"
                        HourlyRateSource.REVERSE_ENGINEERED -> "当前时薪·反推"
                    },
                    value = "¥${uiState.config.hourlyRate.toDisplayString()}",
                )
            }
            if (uiState.summary.uncoveredCompMinutes > 0) {
                Text(
                    text = "本月调休已超过加班余额，超出 ${formatStepperDuration(uiState.summary.uncoveredCompMinutes)} 暂未计入金额，请按公司规则处理。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (uiState.config.hourlyRate <= ZeroDecimal) {
                Text(
                    text = "时薪未设置，请到设置页录入或反推。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
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
private fun SummaryMetric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun MonthSwitcher(
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Surface(
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
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
            }
            Text(
                selectedMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
            }
        }
    }
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

    val barColor = MaterialTheme.colorScheme.onPrimaryContainer

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
