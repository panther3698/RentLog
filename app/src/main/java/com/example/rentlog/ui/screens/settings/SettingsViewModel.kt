package com.example.rentlog.ui.screens.settings

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentlog.data.local.AppDatabase
import com.example.rentlog.data.local.PreferencesManager
import com.example.rentlog.worker.ReminderWorker
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    val themeMode: StateFlow<String> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val isBiometricEnabled: StateFlow<Boolean> = preferencesManager.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isReminderEnabled: StateFlow<Boolean> = preferencesManager.isReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setBiometricEnabled(enabled) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setReminderEnabled(enabled)
            if (enabled) ReminderWorker.schedule(context) else ReminderWorker.cancel(context)
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

                    val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/RentLog")
                        }
                        context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    } else {
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        File(downloadsDir, fileName).also { it.parentFile?.mkdirs() }
                            .let { Uri.fromFile(it) }
                    }

                    if (uri == null) {
                        withContext(Dispatchers.Main) { onResult(false, "Could not create backup file") }
                        return@withContext
                    }

                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        FileInputStream(dbFile).use { it.copyTo(output) }
                    }

                    withContext(Dispatchers.Main) { onResult(true, "Backup saved to Downloads/RentLog/$fileName") }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { onResult(false, e.message ?: "Unknown error") }
                }
            }
        }
    }

    fun importBackup(uri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val dbFile = context.getDatabasePath("rent_log_db")
                    val tempFile = File(context.cacheDir, "import_temp.db")

                    // Copy from content URI to temp file first
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(tempFile).use { input.copyTo(it) }
                    } ?: run {
                        withContext(Dispatchers.Main) { onResult(false, "Could not read backup file") }
                        return@withContext
                    }

                    // Validate it's a SQLite file
                    val header = ByteArray(16)
                    FileInputStream(tempFile).use { it.read(header) }
                    val sqliteHeader = "SQLite format 3".toByteArray()
                    val isValid = header.take(15).toByteArray().contentEquals(sqliteHeader)

                    if (!isValid) {
                        tempFile.delete()
                        withContext(Dispatchers.Main) { onResult(false, "Invalid backup file. Please select a valid RentLog backup.") }
                        return@withContext
                    }

                    // Close DB before overwriting
                    database.close()
                    FileInputStream(tempFile).use { input ->
                        FileOutputStream(dbFile).use { input.copyTo(it) }
                    }
                    tempFile.delete()

                    withContext(Dispatchers.Main) { onResult(true, "Backup restored successfully. Please restart the app.") }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { onResult(false, e.message ?: "Unknown error") }
                }
            }
        }
    }
}
