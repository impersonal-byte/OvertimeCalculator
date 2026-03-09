package com.peter.overtimecalculator.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MonthlyConfigEntity::class, OvertimeEntryEntity::class, HolidayOverrideEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun overtimeDao(): OvertimeDao
}
