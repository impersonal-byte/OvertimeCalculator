package com.peter.overtimecalculator.data

import android.content.Context
import android.content.SharedPreferences
import com.peter.overtimecalculator.domain.AppTheme
import com.peter.overtimecalculator.domain.SeedColor
import com.peter.overtimecalculator.ui.CalendarStartDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppearancePreferencesSnapshot(
    val calendarStartDay: CalendarStartDay,
    val appTheme: AppTheme,
    val useDynamicColor: Boolean,
    val seedColor: SeedColor,
)

interface AppearancePreferencesRepository {
    val snapshot: StateFlow<AppearancePreferencesSnapshot>

    fun saveCalendarStartDay(startDay: CalendarStartDay)

    fun saveAppTheme(theme: AppTheme)

    fun saveUseDynamicColor(useDynamic: Boolean)

    fun saveSeedColor(seedColor: SeedColor)
}

class SharedPreferencesAppearancePreferencesRepository(
    private val sharedPreferences: SharedPreferences,
) : AppearancePreferencesRepository {
    private val _snapshot = MutableStateFlow(readSnapshot())

    override val snapshot: StateFlow<AppearancePreferencesSnapshot> = _snapshot.asStateFlow()

    private fun readSnapshot(): AppearancePreferencesSnapshot {
        return AppearancePreferencesSnapshot(
            calendarStartDay = loadCalendarStartDay(),
            appTheme = loadAppTheme(),
            useDynamicColor = loadUseDynamicColor(),
            seedColor = loadSeedColor(),
        )
    }

    override fun saveCalendarStartDay(startDay: CalendarStartDay) {
        sharedPreferences.edit()
            .putString(KEY_CALENDAR_START_DAY, startDay.name)
            .apply()
        _snapshot.value = _snapshot.value.copy(calendarStartDay = startDay)
    }

    override fun saveAppTheme(theme: AppTheme) {
        sharedPreferences.edit()
            .putString(KEY_APP_THEME, theme.name)
            .apply()
        _snapshot.value = _snapshot.value.copy(appTheme = theme)
    }

    override fun saveUseDynamicColor(useDynamic: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_USE_DYNAMIC_COLOR, useDynamic)
            .apply()
        _snapshot.value = _snapshot.value.copy(useDynamicColor = useDynamic)
    }

    override fun saveSeedColor(seedColor: SeedColor) {
        sharedPreferences.edit()
            .putString(KEY_SEED_COLOR, seedColor.name)
            .apply()
        _snapshot.value = _snapshot.value.copy(seedColor = seedColor)
    }

    private fun loadCalendarStartDay(): CalendarStartDay {
        return sharedPreferences.getString(KEY_CALENDAR_START_DAY, CalendarStartDay.MONDAY.name)
            ?.let { stored -> CalendarStartDay.entries.firstOrNull { it.name == stored } }
            ?: CalendarStartDay.MONDAY
    }

    private fun loadAppTheme(): AppTheme {
        return sharedPreferences.getString(KEY_APP_THEME, AppTheme.SYSTEM.name)
            ?.let { stored -> AppTheme.entries.firstOrNull { it.name == stored } }
            ?: AppTheme.SYSTEM
    }

    private fun loadUseDynamicColor(): Boolean {
        return sharedPreferences.getBoolean(KEY_USE_DYNAMIC_COLOR, true)
    }

    private fun loadSeedColor(): SeedColor {
        return sharedPreferences.getString(KEY_SEED_COLOR, SeedColor.CLAY.name)
            ?.let { stored ->
                when (stored) {
                    "GEEK_BLUE" -> SeedColor.SKY_BLUE
                    "DEEP_PURPLE" -> SeedColor.ORCHID
                    else -> SeedColor.entries.firstOrNull { it.name == stored }
                }
            }
            ?: SeedColor.CLAY
    }

    companion object {
        private const val KEY_CALENDAR_START_DAY = "calendar_start_day"
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_USE_DYNAMIC_COLOR = "use_dynamic_color"
        private const val KEY_SEED_COLOR = "seed_color"

        fun create(context: Context): SharedPreferencesAppearancePreferencesRepository {
            return SharedPreferencesAppearancePreferencesRepository(
                sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
            )
        }

        private const val PREFS_NAME = "overtime-preferences"
    }
}
