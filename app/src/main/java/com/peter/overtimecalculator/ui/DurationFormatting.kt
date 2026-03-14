package com.peter.overtimecalculator.ui

import java.util.Locale

internal fun formatMinutes(totalMinutes: Int): String {
    val sign = if (totalMinutes < 0) "-" else ""
    val absoluteMinutes = kotlin.math.abs(totalMinutes)
    return "$sign${absoluteMinutes / 60}h ${absoluteMinutes % 60}m"
}

internal fun formatStepperDuration(totalMinutes: Int): String {
    return String.format(Locale.US, "%.1fh", totalMinutes / 60.0)
}
