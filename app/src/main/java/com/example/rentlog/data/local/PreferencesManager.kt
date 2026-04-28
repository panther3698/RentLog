package com.devchiradhi.rentlog.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "SYSTEM"
    }

    val isBiometricEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BIOMETRIC_ENABLED] ?: false
    }

    val isReminderEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[REMINDER_ENABLED] ?: false
    }

    val hasSeenWelcome: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HAS_SEEN_WELCOME] ?: false
    }

    val isPremium: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_PREMIUM] ?: false
    }

    val notificationPermissionAsked: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATION_PERMISSION_ASKED] ?: false
    }

    val debugBypassPremiumAccess: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DEBUG_BYPASS_PREMIUM_ACCESS] ?: true
    }

    /** Epoch-ms of the very first launch. 0L = never set yet. */
    val firstLaunchTimestamp: Flow<Long> = dataStore.data.map { preferences ->
        preferences[FIRST_LAUNCH_TIMESTAMP] ?: 0L
    }

    /** Idempotent — only writes if the key is missing. */
    suspend fun initFirstLaunchTimestamp() {
        dataStore.edit { preferences ->
            if (!preferences.contains(FIRST_LAUNCH_TIMESTAMP)) {
                preferences[FIRST_LAUNCH_TIMESTAMP] = System.currentTimeMillis()
            }
        }
    }

    /**
     * DEBUG ONLY — resets the trial as if the app was just installed.
     * Call from Settings or a test to get a fresh 21-day window.
     */
    suspend fun resetTrialTimestamp() {
        dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    /**
     * DEBUG ONLY — backdates the first-launch timestamp by 15 days
     * so you can see the expired state without waiting.
     */
    suspend fun simulateTrialExpired() {
        val fifteenDaysAgo = System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000)
        dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_TIMESTAMP] = fifteenDaysAgo
        }
    }

    suspend fun setHasSeenWelcome(seen: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_SEEN_WELCOME] = seen
        }
    }

    suspend fun setPremium(premium: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_PREMIUM] = premium
        }
    }

    suspend fun setNotificationPermissionAsked(asked: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION_ASKED] = asked
        }
    }

    suspend fun setDebugBypassPremiumAccess(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DEBUG_BYPASS_PREMIUM_ACCESS] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[REMINDER_ENABLED] = enabled
        }
    }

    suspend fun clearAllData() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        private val HAS_SEEN_WELCOME = booleanPreferencesKey("has_seen_welcome")
        private val IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val NOTIFICATION_PERMISSION_ASKED = booleanPreferencesKey("notification_permission_asked")
        private val FIRST_LAUNCH_TIMESTAMP = longPreferencesKey("first_launch_timestamp")
        private val DEBUG_BYPASS_PREMIUM_ACCESS = booleanPreferencesKey("debug_bypass_premium_access")
    }
}
