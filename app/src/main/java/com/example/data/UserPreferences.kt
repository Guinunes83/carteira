package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {
    companion object {
        val START_DAY = intPreferencesKey("start_day")
        val END_DAY = intPreferencesKey("end_day")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }

    val startDayFlow: Flow<Int> = context.dataStore.data.map { it[START_DAY] ?: 1 }
    val endDayFlow: Flow<Int> = context.dataStore.data.map { it[END_DAY] ?: 30 }
    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_DARK_THEME] ?: false }

    suspend fun setStartDay(day: Int) {
        context.dataStore.edit { it[START_DAY] = day }
    }

    suspend fun setEndDay(day: Int) {
        context.dataStore.edit { it[END_DAY] = day }
    }

    suspend fun setIsDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[IS_DARK_THEME] = isDark }
    }
}
