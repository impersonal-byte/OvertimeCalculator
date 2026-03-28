package com.peter.overtimecalculator.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.ui.theme.OvertimeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThemeModeChooser(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
) {
    val defaults = OvertimeTheme.defaults
    val entries = listOf(
        ThemeModeOption(AppTheme.LIGHT, "浅色", "light"),
        ThemeModeOption(AppTheme.DARK, "深色", "dark"),
        ThemeModeOption(AppTheme.SYSTEM, "自动", "system"),
    )

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_mode_chooser"),
    ) {
        entries.forEachIndexed { index, option ->
            val selected = selectedTheme == option.theme
            val tag = "theme_mode_${option.testTagSuffix}"

            SegmentedButton(
                selected = selected,
                onClick = { onThemeSelected(option.theme) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = entries.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = defaults.navigationContainer,
                    activeContentColor = defaults.pageForeground,
                    activeBorderColor = defaults.accent.copy(alpha = 0.62f),
                    inactiveContainerColor = defaults.sectionContainer.copy(alpha = 0.78f),
                    inactiveContentColor = defaults.pageForeground.copy(alpha = 0.76f),
                    inactiveBorderColor = defaults.outline.copy(alpha = 0.46f),
                ),
                modifier = Modifier.testTag(tag),
                icon = {},
                label = {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    )
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(1.dp)
                                .testTag("${tag}_selected"),
                        )
                    }
                },
            )
        }
    }
}

private data class ThemeModeOption(
    val theme: AppTheme,
    val label: String,
    val testTagSuffix: String,
)
