package com.peter.overtimecalculator.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MonthlyConfigEntity::class, OvertimeEntryEntity::class, HolidayOverrideEntity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(BigDecimalConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun overtimeDao(): OvertimeDao

    companion object {
        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS monthly_config_new (
                        yearMonth TEXT NOT NULL PRIMARY KEY,
                        hourlyRate TEXT NOT NULL,
                        rateSource TEXT NOT NULL,
                        weekdayRate TEXT NOT NULL,
                        restDayRate TEXT NOT NULL,
                        holidayRate TEXT NOT NULL,
                        lockedByUser INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO monthly_config_new (
                        yearMonth,
                        hourlyRate,
                        rateSource,
                        weekdayRate,
                        restDayRate,
                        holidayRate,
                        lockedByUser
                    )
                    SELECT
                        yearMonth,
                        CAST(hourlyRate AS TEXT),
                        rateSource,
                        CAST(weekdayRate AS TEXT),
                        CAST(restDayRate AS TEXT),
                        CAST(holidayRate AS TEXT),
                        lockedByUser
                    FROM monthly_config
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE monthly_config")
                db.execSQL("ALTER TABLE monthly_config_new RENAME TO monthly_config")
            }
        }
    }
}
