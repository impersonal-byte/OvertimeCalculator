package com.peter.overtimecalculator.ui.components

import java.util.Locale
import kotlin.math.roundToInt

internal object DurationMapper {
    const val StepSizeMinutes = 30

    fun clampAndSnap(
        rawMinutes: Int,
        minMinutes: Int,
        maxMinutes: Int,
        stepSizeMinutes: Int = StepSizeMinutes,
    ): Int {
        val clamped = rawMinutes.coerceIn(minMinutes, maxMinutes)
        val snapped = (clamped.toDouble() / stepSizeMinutes).roundToInt() * stepSizeMinutes
        return snapped.coerceIn(minMinutes, maxMinutes)
    }

    fun formatDuration(totalMinutes: Int): String {
        return String.format(Locale.US, "%.1fh", totalMinutes / 60.0)
    }
}

internal object VisualCenterMapper {
    fun minutesToSliderFraction(
        minutes: Int,
        minMinutes: Int,
        maxMinutes: Int,
        centeredVisual: Boolean = minMinutes < 0,
    ): Float {
        if (!centeredVisual || minMinutes >= 0) {
            val totalRange = (maxMinutes - minMinutes).coerceAtLeast(1)
            return ((minutes - minMinutes).toFloat() / totalRange).coerceIn(0f, 1f)
        }

        return if (minutes <= 0) {
            0.5f * ((minutes - minMinutes).toFloat() / (0 - minMinutes).toFloat())
        } else {
            0.5f + 0.5f * (minutes.toFloat() / maxMinutes.toFloat())
        }.coerceIn(0f, 1f)
    }

    fun sliderFractionToMinutes(
        fraction: Float,
        minMinutes: Int,
        maxMinutes: Int,
        centeredVisual: Boolean = minMinutes < 0,
    ): Int {
        val normalized = fraction.coerceIn(0f, 1f)
        if (!centeredVisual || minMinutes >= 0) {
            return (minMinutes + (maxMinutes - minMinutes) * normalized).roundToInt()
        }

        return if (normalized <= 0.5f) {
            (minMinutes + (0 - minMinutes) * (normalized / 0.5f)).roundToInt()
        } else {
            (maxMinutes * ((normalized - 0.5f) / 0.5f)).roundToInt()
        }
    }
}

internal data class MajorTickAnchor(
    val minutes: Int,
    val fraction: Float,
)

internal fun buildMajorTickMinutes(minMinutes: Int, maxMinutes: Int): List<Int> {
    return if (minMinutes < 0) {
        listOf(minMinutes, -240, 0, 240, 600, maxMinutes)
    } else {
        listOf(0, 240, 600, maxMinutes)
    }
}

internal fun buildMajorTickAnchors(
    minMinutes: Int,
    maxMinutes: Int,
    centeredVisual: Boolean,
): List<MajorTickAnchor> {
    return buildMajorTickMinutes(minMinutes, maxMinutes).map { minutes ->
        MajorTickAnchor(
            minutes = minutes,
            fraction = VisualCenterMapper.minutesToSliderFraction(
                minutes = minutes,
                minMinutes = minMinutes,
                maxMinutes = maxMinutes,
                centeredVisual = centeredVisual,
            ),
        )
    }
}
