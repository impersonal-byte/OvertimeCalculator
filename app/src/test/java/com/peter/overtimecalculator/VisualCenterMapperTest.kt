package com.peter.overtimecalculator

import com.peter.overtimecalculator.ui.components.VisualCenterMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class VisualCenterMapperTest {
    @Test
    fun centeredMapping_placesZeroAtVisualCenter() {
        assertFloatEquals(0f, VisualCenterMapper.minutesToSliderFraction(-480, -480, 960, centeredVisual = true))
        assertFloatEquals(0.5f, VisualCenterMapper.minutesToSliderFraction(0, -480, 960, centeredVisual = true))
        assertFloatEquals(1f, VisualCenterMapper.minutesToSliderFraction(960, -480, 960, centeredVisual = true))
    }

    @Test
    fun centeredMapping_reversesBackToSignedMinutes() {
        assertEquals(-480, VisualCenterMapper.sliderFractionToMinutes(0f, -480, 960, centeredVisual = true))
        assertEquals(-240, VisualCenterMapper.sliderFractionToMinutes(0.25f, -480, 960, centeredVisual = true))
        assertEquals(0, VisualCenterMapper.sliderFractionToMinutes(0.5f, -480, 960, centeredVisual = true))
        assertEquals(480, VisualCenterMapper.sliderFractionToMinutes(0.75f, -480, 960, centeredVisual = true))
        assertEquals(960, VisualCenterMapper.sliderFractionToMinutes(1f, -480, 960, centeredVisual = true))
    }

    @Test
    fun nonCenteredMapping_usesStraightPositiveRange() {
        assertFloatEquals(0f, VisualCenterMapper.minutesToSliderFraction(0, 0, 960, centeredVisual = false))
        assertFloatEquals(0.5f, VisualCenterMapper.minutesToSliderFraction(480, 0, 960, centeredVisual = false))
        assertFloatEquals(1f, VisualCenterMapper.minutesToSliderFraction(960, 0, 960, centeredVisual = false))
    }

    private fun assertFloatEquals(expected: Float, actual: Float) {
        assertEquals(expected, actual, 0.0001f)
    }
}
