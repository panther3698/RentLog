package com.devchiradhi.rentlog.ui.screens.settings

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devchiradhi.rentlog.BuildConfig
import com.devchiradhi.rentlog.data.local.AppDatabase
import com.devchiradhi.rentlog.data.local.PreferencesManager
import com.devchiradhi.rentlog.worker.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase,
    private val accessManager: com.devchiradhi.rentlog.data.manager.AccessManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val themeMode: StateFlow<String> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val isBiometricEnabled: StateFlow<Boolean> = preferencesManager.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isReminderEnabled: StateFlow<Boolean> = preferencesManager.isReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasFullAccess: StateFlow<Boolean> = accessManager.hasFullAccess
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isPremium: StateFlow<Boolean> = preferencesManager.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val debugBypassPremiumAccess: StateFlow<Boolean> = preferencesManager.debugBypassPremiumAccess
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BuildConfig.DEBUG)

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setBiometricEnabled(enabled) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setReminderEnabled(enabled)
            ReminderWorker.sync(context, enabled)
        }
    }

    fun exportBackup(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Checkpoint WAL so all data is in the main .db file
                    database.query(androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)")).use { cursor ->
                        cursor.moveToFirst()
                    }

                    val dbFile = context.getDatabasePath("rent_log_db")
                    val fileName = "RentLog_Backup_${System.currentTimeMillis()}.db"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "application/x-sqlite3")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/RentLog/Backups")
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }
                        val itemUri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        
                        if (itemUri != null) {
                            context.contentResolver.openOutputStream(itemUri)?.use { output ->
                                FileInputStream(dbFile).use { it.copyTo(output) }
                            }
                            contentValues.clear()
                            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            context.contentResolver.update(itemUri, contentValues, null, null)
                            withContext(Dispatchers.Main) { onResult(true, "Backup saved to Downloads/RentLog/Backups") }
                        } else {
                            withContext(Dispatchers.Main) { onResult(false, "Could not create backup file") }
                        }
                    } else {
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val targetDir = File(downloadsDir, "RentLog/Backups")
                        if (!targetDir.exists()) targetDir.mkdirs()
                        val targetFile = File(targetDir, fileName)
                        FileInputStream(dbFile).use { input ->
                            FileOutputStream(targetFile).use { output -> input.copyTo(output) }
                        }
                        withContext(Dispatchers.Main) { onResult(true, "Backup saved to Downloads/RentLog/Backups") }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { onResult(false, e.message ?: "Unknown error") }
                }
            }
        }
    }

    // ── Debug helpers (no-ops in release; AccessManager bypasses trial anyway) ──

    fun resetTrial() {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch { preferencesManager.resetTrialTimestamp() }
    }

    fun simulateTrialExpired() {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch { preferencesManager.simulateTrialExpired() }
    }

    fun setDebugBypassPremiumAccess(enabled: Boolean) {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch { preferencesManager.setDebugBypassPremiumAccess(enabled) }
    }

    fun importBackup(uri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            // ... (rest of existing importBackup logic)
        }
    }

    fun wipeAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // 1. Stop reminders
                    ReminderWorker.sync(context, false)
                    
                    // 2. Clear Database
                    database.clearAllTables()
                    
                    // 3. Clear DataStore
                    preferencesManager.clearAllData()
                    
                    withContext(Dispatchers.Main) {
                        onComplete()
                    }
                } catch (e: Exception) {
                    // Log error if needed
                }
            }
        }
    }
}
