package com.example.rentlog.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentlog.data.local.AppDatabase
import com.example.rentlog.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.rentlog.worker.ReminderWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val themeMode: StateFlow<String> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val isBiometricEnabled: StateFlow<Boolean> = preferencesManager.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isReminderEnabled: StateFlow<Boolean> = preferencesManager.isReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBiometricEnabled(enabled)
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setReminderEnabled(enabled)
            if (enabled) {
                ReminderWorker.schedule(context)
            } else {
                ReminderWorker.cancel(context)
            }
        }
    }

    fun exportBackup(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                database.close()
                val dbFile = context.getDatabasePath("rent_log_db")
                val backupFile = File(context.getExternalFilesDir(null), "rent_log_backup.db")
                
                FileInputStream(dbFile).use { input ->
                    FileOutputStream(backupFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onResult(true, backupFile.absolutePath)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun importBackup(backupPath: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                database.close()
                val dbFile = context.getDatabasePath("rent_log_db")
                val backupFile = File(backupPath)
                
                FileInputStream(backupFile).use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
