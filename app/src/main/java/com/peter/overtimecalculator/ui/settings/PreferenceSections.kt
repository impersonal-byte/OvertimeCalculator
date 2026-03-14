package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.peter.overtimecalculator.ui.CalendarStartDay

@Composable
internal fun CalendarStartDaySection(
    selectedStartDay: CalendarStartDay,
    onCalendarStartDayChange: (CalendarStartDay) -> Unit,
) {
    SettingCard("日历显示", "选择日历每周的起始日，修改后会立即同步到首页。") {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calendar_start_day_row"),
        ) {
            CalendarStartDay.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = selectedStartDay == option,
                    onClick = { onCalendarStartDayChange(option) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = CalendarStartDay.entries.size,
                    ),
                ) {
                    Text(if (option == CalendarStartDay.MONDAY) "周一为首日" else "周日为首日")
                }
            }
        }
    }
}
