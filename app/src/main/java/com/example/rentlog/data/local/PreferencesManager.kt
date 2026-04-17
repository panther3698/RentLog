package com.example.rentlog.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

    val activeLandlordId: Flow<Int> = dataStore.data.map { preferences ->
        preferences[ACTIVE_LANDLORD_ID] ?: -1
    }

    val notificationPermissionAsked: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATION_PERMISSION_ASKED] ?: false
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

    suspend fun setActiveLandlordId(id: Int) {
        dataStore.edit { preferences ->
            preferences[ACTIVE_LANDLORD_ID] = id
        }
    }

    suspend fun setNotificationPermissionAsked(asked: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION_ASKED] = asked
        }
    }

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        private val ACTIVE_LANDLORD_ID = intPreferencesKey("active_landlord_id")
        private val NOTIFICATION_PERMISSION_ASKED = booleanPreferencesKey("notification_permission_asked")
    }
}
