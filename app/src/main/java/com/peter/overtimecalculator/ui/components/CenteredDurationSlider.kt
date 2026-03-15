package com.peter.overtimecalculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.OvertimeEntryValidator
import com.peter.overtimecalculator.ui.rememberTickHapticFeedback
@Composable
internal fun CenteredDurationSlider(
    valueMinutes: Int,
    onValueMinutesChange: (Int) -> Unit,
    minMinutes: Int,
    maxMinutes: Int = OvertimeEntryValidator.MAX_OVERTIME_MINUTES,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    centeredVisual: Boolean = minMinutes < 0,
) {
    val tickHaptics = rememberTickHapticFeedback()
    val isCentered = centeredVisual && minMinutes < 0
    val sliderFraction = VisualCenterMapper.minutesToSliderFraction(
        minutes = valueMinutes,
        minMinutes = minMinutes,
        maxMinutes = maxMinutes,
        centeredVisual = isCentered,
    )
    val majorTicks = buildMajorTickAnchors(minMinutes, maxMinutes, centeredVisual = isCentered)

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = sliderFraction,
            onValueChange = { newFraction ->
                val mappedMinutes = VisualCenterMapper.sliderFractionToMinutes(
                    fraction = newFraction,
                    minMinutes = minMinutes,
                    maxMinutes = maxMinutes,
                    centeredVisual = isCentered,
                )
                val snappedMinutes = DurationMapper.clampAndSnap(
                    rawMinutes = mappedMinutes,
                    minMinutes = minMinutes,
                    maxMinutes = maxMinutes,
                )
                if (snappedMinutes != valueMinutes) {
                    tickHaptics.performTick()
                }
                onValueMinutesChange(snappedMinutes)
            },
            valueRange = 0f..1f,
            steps = 0,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("duration_slider")
                .semantics {
                    contentDescription = "工时调整滑块"
                    stateDescription = DurationMapper.formatDuration(valueMinutes)
                },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .testTag("center_marker_container"),
        ) {
            Box(
                modifier = Modifier
                    .align(if (isCentered) Alignment.Center else Alignment.CenterStart)
                    .padding(start = if (isCentered) 0.dp else 2.dp)
                    .height(8.dp)
                    .testTag("center_marker"),
            ) {
                Text(
                    text = "|",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
        TickLabelRow(
            ticks = majorTicks,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .testTag("major_ticks"),
        )
    }
}

@Composable
private fun TickLabelRow(
    ticks: List<MajorTickAnchor>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val maxWidthDp = maxWidth
        Layout(
            content = {
                ticks.forEach { tick ->
                    Text(
                        text = DurationMapper.formatDuration(tick.minutes),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .testTag("tick_${tick.minutes}")
                            .widthIn(min = 24.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            },
        ) { measurables, constraints ->
            val placeables = measurables.map { measurable ->
                measurable.measure(
                    Constraints(
                        minWidth = 0,
                        maxWidth = constraints.maxWidth,
                        minHeight = 0,
                        maxHeight = constraints.maxHeight,
                    ),
                )
            }
            val height = placeables.maxOfOrNull { it.height } ?: 0
            val width = constraints.maxWidth

            layout(width, height) {
                placeables.forEachIndexed { index, placeable ->
                    val anchor = ticks[index]
                    val centeredX = (width * anchor.fraction).toInt() - placeable.width / 2
                    val x = centeredX.coerceIn(0, (width - placeable.width).coerceAtLeast(0))
                    placeable.placeRelative(x, 0)
                }
            }
        }
    }
}
