package com.peter.overtimecalculator.data.db

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalConverters {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal): String = value.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String): BigDecimal = BigDecimal(value)
}
