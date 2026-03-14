package com.peter.overtimecalculator

import com.peter.overtimecalculator.ui.components.DurationMapper
import com.peter.overtimecalculator.ui.components.buildMajorTickMinutes
import org.junit.Assert.assertEquals
import org.junit.Test

class CenteredDurationSliderTest {
    @Test
    fun workdayTicks_areSparseAndCenteredAroundZero() {
        assertEquals(
            listOf(-480, -240, 0, 240, 480, 720, 960),
            buildMajorTickMinutes(minMinutes = -480, maxMinutes = 960),
        )
    }

    @Test
    fun nonWorkdayTicks_startAtZeroOnly() {
        assertEquals(
            listOf(0, 240, 480, 720, 960),
            buildMajorTickMinutes(minMinutes = 0, maxMinutes = 960),
        )
    }

    @Test
    fun sliderStateDescription_usesFormattedHours() {
        assertEquals("2.0h", DurationMapper.formatDuration(120))
        assertEquals("-2.0h", DurationMapper.formatDuration(-120))
    }
}
