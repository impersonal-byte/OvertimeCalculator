package com.peter.overtimecalculator.domain

import java.math.BigDecimal
import java.math.RoundingMode

const val INTERNAL_SCALE = 8
const val MONEY_SCALE = 2

val ZeroDecimal: BigDecimal = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP)
val ZeroWeightedHours: BigDecimal = BigDecimal.ZERO.setScale(INTERNAL_SCALE, RoundingMode.HALF_UP)
private val MinutesPerHour = BigDecimal("60")

fun decimal(value: String): BigDecimal = BigDecimal(value)

fun BigDecimal.toMoneyScale(): BigDecimal = setScale(MONEY_SCALE, RoundingMode.HALF_UP)

fun BigDecimal.toInternalScale(): BigDecimal = setScale(INTERNAL_SCALE, RoundingMode.HALF_UP)

fun BigDecimal.isPositive(): Boolean = compareTo(BigDecimal.ZERO) > 0

fun BigDecimal.isNonNegative(): Boolean = compareTo(BigDecimal.ZERO) >= 0

fun BigDecimal.isZeroOrNegative(): Boolean = compareTo(BigDecimal.ZERO) <= 0

fun Int.toDecimalHours(): BigDecimal {
    return toBigDecimal().divide(MinutesPerHour, INTERNAL_SCALE, RoundingMode.HALF_UP)
}

fun BigDecimal.toDisplayString(scale: Int = MONEY_SCALE): String {
    return setScale(scale, RoundingMode.HALF_UP).toPlainString()
}
