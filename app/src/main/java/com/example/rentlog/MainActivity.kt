package com.example.rentlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.rentlog.ui.navigation.NavGraph
import com.example.rentlog.ui.theme.RentLogTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.lifecycle.lifecycleScope
import com.example.rentlog.data.local.PreferencesManager
import com.example.rentlog.ui.screens.settings.BiometricHelper
import androidx.fragment.app.FragmentActivity
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
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) { _ ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isUnlocked && startDestination != null) {
                            NavGraph(
                                navController = navController,
                                startDestination = startDestination!!
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
        
        // Schedule monthly reminder
        RentReminderWorker.scheduleMonthlyReminder(this)
    }
}
