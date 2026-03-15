package com.peter.overtimecalculator.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OvertimeDao {
    @Query("SELECT * FROM monthly_config ORDER BY yearMonth ASC")
    fun observeAllConfigs(): Flow<List<MonthlyConfigEntity>>

    @Query("SELECT * FROM overtime_entry WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeEntriesInRange(startDate: String, endDate: String): Flow<List<OvertimeEntryEntity>>

    @Query("SELECT * FROM holiday_override WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeOverridesInRange(startDate: String, endDate: String): Flow<List<HolidayOverrideEntity>>

    @Query("SELECT * FROM monthly_config ORDER BY yearMonth ASC")
    suspend fun getAllConfigs(): List<MonthlyConfigEntity>

    @Query("SELECT * FROM overtime_entry WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getEntriesInRange(startDate: String, endDate: String): List<OvertimeEntryEntity>

    @Query("SELECT * FROM holiday_override WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getOverridesInRange(startDate: String, endDate: String): List<HolidayOverrideEntity>

    @Query("SELECT * FROM overtime_entry ORDER BY date ASC")
    suspend fun getAllEntries(): List<OvertimeEntryEntity>

    @Query("SELECT * FROM holiday_override ORDER BY date ASC")
    suspend fun getAllOverrides(): List<HolidayOverrideEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntries(entities: List<OvertimeEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOverrides(entities: List<HolidayOverrideEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(entity: MonthlyConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfigs(entities: List<MonthlyConfigEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entity: OvertimeEntryEntity)

    @Query("DELETE FROM overtime_entry WHERE date = :date")
    suspend fun deleteEntry(date: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHolidayOverride(entity: HolidayOverrideEntity)

    @Query("DELETE FROM holiday_override WHERE date = :date")
    suspend fun deleteHolidayOverride(date: String)

    @Query("DELETE FROM monthly_config")
    suspend fun deleteAllConfigs()

    @Query("DELETE FROM overtime_entry")
    suspend fun deleteAllEntries()

    @Query("DELETE FROM holiday_override")
    suspend fun deleteAllOverrides()
}

