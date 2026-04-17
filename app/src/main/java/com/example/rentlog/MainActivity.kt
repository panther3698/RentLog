package com.example.rentlog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.rentlog.ui.navigation.NavGraph
import com.example.rentlog.ui.theme.RentLogTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.rentlog.data.local.PreferencesManager
import com.example.rentlog.ui.screens.settings.BiometricHelper
import androidx.fragment.app.FragmentActivity
import com.example.rentlog.ui.screens.splash.SplashScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.example.rentlog.data.worker.RentReminderWorker
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() { // Changed to FragmentActivity for Biometric
    
    @Inject lateinit var preferencesManager: PreferencesManager
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        // Request notification permission for Android 13+ (one-time)
        requestNotificationPermissionIfNeeded()
        
        // Biometric Check
        lifecycleScope.launch {
            val biometricEnabled = preferencesManager.isBiometricEnabled.first()
            if (biometricEnabled && BiometricHelper.canAuthenticate(this@MainActivity)) {
                BiometricHelper.showBiometricPrompt(
                    this@MainActivity,
                    onSuccess = { viewModel.setUnlocked(true) },
                    onError = { finish() }
                )
            } else {
                viewModel.setUnlocked(true)
            }
        }

        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState(initial = "SYSTEM")
            val isDark = when(themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            RentLogTheme(darkTheme = isDark) {
                val startDestination by viewModel.startDestination.collectAsState()
                val isUnlocked by viewModel.isUnlocked.collectAsState()
                val navController = rememberNavController()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { _ ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isUnlocked && startDestination != null) {
                            NavGraph(
                                navController = navController,
                                startDestination = startDestination!!
                            )
                        } else {
                            SplashScreen()
                        }
                    }
                }
            }
        }
        
        // Schedule monthly reminder
        RentReminderWorker.scheduleMonthlyReminder(this)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lifecycleScope.launch {
                val alreadyAsked = preferencesManager.notificationPermissionAsked.first()
                if (!alreadyAsked) {
                    preferencesManager.setNotificationPermissionAsked(true)
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            NOTIFICATION_PERMISSION_REQUEST_CODE
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}
