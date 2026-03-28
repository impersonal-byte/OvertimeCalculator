package com.peter.overtimecalculator

import com.peter.overtimecalculator.ui.components.DurationMapper
import com.peter.overtimecalculator.ui.components.buildMajorTickAnchors
import com.peter.overtimecalculator.ui.components.buildMajorTickMinutes
import org.junit.Assert.assertEquals
import org.junit.Test

class CenteredDurationSliderTest {
    @Test
    fun workdayTicks_areSparseAndCenteredAroundZero() {
        assertEquals(
            listOf(-480, -240, 0, 120, 240, 360),
            buildMajorTickMinutes(minMinutes = -480, maxMinutes = 360),
        )
    }

    @Test
    fun legacyWorkdayTicks_keepFocusedMarkersBeforeExtendedPositiveMax() {
        assertEquals(
            listOf(-480, -240, 0, 120, 240, 360, 480),
            buildMajorTickMinutes(minMinutes = -480, maxMinutes = 480),
        )
    }

    @Test
    fun nonWorkdayTicks_startAtZeroOnly() {
        assertEquals(
            listOf(0, 240, 600, 960),
            buildMajorTickMinutes(minMinutes = 0, maxMinutes = 960),
        )
    }

    @Test
    fun workdayTickAnchors_followCenteredVisualFractions() {
        val anchors = buildMajorTickAnchors(minMinutes = -480, maxMinutes = 360, centeredVisual = true)

        assertEquals(0f, anchors[0].fraction)
        assertEquals(0.25f, anchors[1].fraction)
        assertEquals(0.5f, anchors[2].fraction)
        assertEquals(0.6666667f, anchors[3].fraction, 0.0001f)
        assertEquals(0.8333333f, anchors[4].fraction, 0.0001f)
        assertEquals(1f, anchors[5].fraction)
    }

    @Test
    fun nonWorkdayTickAnchors_startAtZeroAndUseLinearFractions() {
        val anchors = buildMajorTickAnchors(minMinutes = 0, maxMinutes = 960, centeredVisual = false)

        assertEquals(0f, anchors[0].fraction)
        assertEquals(0.25f, anchors[1].fraction)
        assertEquals(0.625f, anchors[2].fraction)
        assertEquals(1f, anchors[3].fraction)
    }

    @Test
    fun sliderStateDescription_usesFormattedHours() {
        assertEquals("2.0h", DurationMapper.formatDuration(120))
        assertEquals("-2.0h", DurationMapper.formatDuration(-120))
    }
}
