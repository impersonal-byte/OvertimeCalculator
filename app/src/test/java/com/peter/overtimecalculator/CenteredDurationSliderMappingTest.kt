package com.peter.overtimecalculator

import com.peter.overtimecalculator.ui.components.DurationMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class CenteredDurationSliderMappingTest {
    @Test
    fun clampAndSnap_roundsToNearestStepInsideRange() {
        assertEquals(120, DurationMapper.clampAndSnap(120, -480, 960))
        assertEquals(120, DurationMapper.clampAndSnap(115, -480, 960))
        assertEquals(120, DurationMapper.clampAndSnap(125, -480, 960))
        assertEquals(-30, DurationMapper.clampAndSnap(-30, -480, 960))
        assertEquals(0, DurationMapper.clampAndSnap(0, -480, 960))
    }

    @Test
    fun clampAndSnap_appliesBoundsBeforeReturning() {
        assertEquals(0, DurationMapper.clampAndSnap(-15, 0, 960))
        assertEquals(960, DurationMapper.clampAndSnap(965, -480, 960))
        assertEquals(-480, DurationMapper.clampAndSnap(-999, -480, 960))
        assertEquals(360, DurationMapper.clampAndSnap(965, -480, 360))
    }

    @Test
    fun formatDuration_returnsConsistentDecimalHours() {
        assertEquals("2.0h", DurationMapper.formatDuration(120))
        assertEquals("-2.0h", DurationMapper.formatDuration(-120))
        assertEquals("0.0h", DurationMapper.formatDuration(0))
    }
}
