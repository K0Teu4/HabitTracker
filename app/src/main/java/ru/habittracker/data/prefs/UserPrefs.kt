package ru.habittracker.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class UserPrefs(private val context: Context) {

    private val onboardingDoneKey = booleanPreferencesKey("onboarding_done")
    private val themeModeKey = stringPreferencesKey("theme_mode")

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[onboardingDoneKey] ?: false
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[themeModeKey]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun setOnboardingDone(done: Boolean = true) {
        context.dataStore.edit { it[onboardingDoneKey] = done }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[themeModeKey] = mode.name }
    }
}
