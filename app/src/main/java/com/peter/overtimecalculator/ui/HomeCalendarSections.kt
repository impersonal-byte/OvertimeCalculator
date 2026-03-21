package com.peter.overtimecalculator.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.DayCellUiState
import com.peter.overtimecalculator.ui.theme.OvertimeTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
internal fun CalendarGrid(
    selectedMonth: YearMonth,
    dayCells: List<DayCellUiState>,
    calendarStartDay: CalendarStartDay,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaults = OvertimeTheme.defaults
    val dayMap = remember(dayCells) { dayCells.associateBy { it.date } }
    val presentationMap = remember(dayCells) { buildCalendarCellPresentations(dayCells) }
    val today = remember { LocalDate.now() }
    val leadingBlanks = remember(selectedMonth, calendarStartDay) {
        selectedMonth.atDay(1).dayOfWeek.toCalendarOffset(calendarStartDay)
    }
    val totalCells = leadingBlanks + selectedMonth.lengthOfMonth()
    val trailingBlanks = if (totalCells % 7 == 0) 0 else 7 - (totalCells % 7)
    val slots = remember(selectedMonth, dayCells, leadingBlanks, trailingBlanks) {
        buildList<LocalDate?> {
            repeat(leadingBlanks) { add(null) }
            dayCells.forEach { add(it.date) }
            repeat(trailingBlanks) { add(null) }
        }
    }
    val weeks = remember(slots) { slots.chunked(7) }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.testTag("calendar_grid"),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            dayOfWeekLabels(calendarStartDay).forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = defaults.pageForeground.copy(alpha = 0.62f),
                )
            }
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        week.forEach { date ->
                            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                                if (date == null) {
                                    Spacer(modifier = Modifier.fillMaxSize())
                                } else {
                                    DayCard(
                                        cell = dayMap.getValue(date),
                                        presentation = presentationMap.getValue(date),
                                        isToday = date == today,
                                        isFuture = date.isAfter(today),
                                        onClick = { onDayClick(date) },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCard(
    cell: DayCellUiState,
    presentation: CalendarCellPresentation,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaults = OvertimeTheme.defaults
    val colors = dayCardColors(presentation)
    val borderColor = if (isToday) defaults.accent else colors.border
    val borderWidth = if (isToday) 2.dp else 1.dp

    Surface(
        color = colors.container,
        contentColor = colors.content,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = if (presentation.tier == CalendarCellIntensityTier.NONE) 1.dp else 3.dp,
        modifier = modifier
            .alpha(if (isFuture) 0.56f else 1f)
            .testTag("day_card_${cell.date}")
            .clickable(enabled = !isFuture, onClick = onClick)
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .semantics(mergeDescendants = true) {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = cell.date.dayOfMonth.toString(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = if (presentation.tier == CalendarCellIntensityTier.NONE) {
                    defaults.pageForeground
                } else {
                    colors.content
                },
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                if (presentation.hoursLabel.isNotBlank()) {
                    Text(
                        text = presentation.hoursLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.content,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun dayCardColors(presentation: CalendarCellPresentation): DayCardColors {
    val defaults = OvertimeTheme.defaults
    return when (presentation.colorRole) {
        CalendarCellColorRole.DEFAULT -> DayCardColors(
            container = defaults.cardContainer,
            content = defaults.pageForeground,
            border = defaults.outline,
        )
        CalendarCellColorRole.COMP_TIME -> layeredColors(
            tier = presentation.tier,
            base = defaults.cardContainer,
            target = defaults.sectionContainer,
            content = defaults.pageForeground,
            border = defaults.outline,
            fallbackContent = defaults.pageForeground,
        )
        CalendarCellColorRole.WORKDAY_OVERTIME -> layeredColors(
            tier = presentation.tier,
            base = defaults.cardContainer,
            target = lerp(defaults.sectionContainer, defaults.warningTint, 0.32f),
            content = defaults.pageForeground,
            border = defaults.warningTint,
            fallbackContent = defaults.pageForeground,
        )
        CalendarCellColorRole.REST_DAY_OVERTIME -> layeredColors(
            tier = presentation.tier,
            base = defaults.cardContainer,
            target = lerp(defaults.sectionContainer, defaults.accent, 0.28f),
            content = defaults.pageForeground,
            border = defaults.accent,
            fallbackContent = defaults.pageForeground,
        )
        CalendarCellColorRole.HOLIDAY_OVERTIME -> layeredColors(
            tier = presentation.tier,
            base = defaults.cardContainer,
            target = lerp(defaults.sectionContainer, defaults.warningTint, 0.24f),
            content = defaults.pageForeground,
            border = defaults.warningTint,
            fallbackContent = defaults.pageForeground,
        )
        CalendarCellColorRole.HOLIDAY_OVERTIME_HIGH -> DayCardColors(
            container = lerp(defaults.cardElevatedContainer, defaults.warningTint, 0.86f),
            content = defaults.accentOn,
            border = defaults.warningTint,
        )
    }
}

@Composable
private fun layeredColors(
    tier: CalendarCellIntensityTier,
    base: Color,
    target: Color,
    content: Color,
    border: Color,
    fallbackContent: Color,
): DayCardColors {
    val amount = when (tier) {
        CalendarCellIntensityTier.NONE -> 0f
        CalendarCellIntensityTier.LOW -> 0.35f
        CalendarCellIntensityTier.MID -> 0.70f
        CalendarCellIntensityTier.HIGH -> 1.0f
    }
    val borderAmount = when (tier) {
        CalendarCellIntensityTier.NONE -> 0f
        CalendarCellIntensityTier.LOW -> 0.45f
        CalendarCellIntensityTier.MID -> 0.75f
        CalendarCellIntensityTier.HIGH -> 1.0f
    }
    return DayCardColors(
        container = lerp(base, target, amount),
        content = if (amount >= 0.6f) content else fallbackContent,
        border = lerp(OvertimeTheme.defaults.outline, border, borderAmount),
    )
}

private data class DayCardColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

private fun DayOfWeek.toCalendarOffset(calendarStartDay: CalendarStartDay): Int = when (calendarStartDay) {
    CalendarStartDay.MONDAY -> when (this) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }
    CalendarStartDay.SUNDAY -> when (this) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }
}

private fun dayOfWeekLabels(calendarStartDay: CalendarStartDay): List<String> {
    return if (calendarStartDay == CalendarStartDay.MONDAY) {
        listOf("一", "二", "三", "四", "五", "六", "日")
    } else {
        listOf("日", "一", "二", "三", "四", "五", "六")
    }
}
