package com.devchiradhi.rentlog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.activity.viewModels
import com.devchiradhi.rentlog.ui.navigation.NavGraph
import com.devchiradhi.rentlog.ui.theme.RentLogTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.devchiradhi.rentlog.data.billing.BillingManager
import com.devchiradhi.rentlog.data.local.PreferencesManager
import com.devchiradhi.rentlog.ui.screens.settings.BiometricHelper
import com.devchiradhi.rentlog.ui.screens.splash.SplashScreen
import com.devchiradhi.rentlog.worker.ReminderWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() { // Changed to FragmentActivity for Biometric
    
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var billingManager: BillingManager
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Install the Android 12+ splash screen — must be called before setContent
        val splashScreen = installSplashScreen()
        // Keep the splash visible until the start destination is determined
        splashScreen.setKeepOnScreenCondition {
            viewModel.startDestination.value == null
        }

        enableEdgeToEdge()
        
        // DETERMINING START DESTINATION
        // DETERMINING START DESTINATION
        // determinación de destino de inicio
        // determinations of start destination
        // determinations of start destination
        
        // Remove the biometric check from onCreate and put it in onStart
        // so it triggers on every resume from background.
        
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
                ) { _ -> // Removed innerPadding to handle edge-to-edge properly in screens
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
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

        syncReminderSchedule()
    }

    override fun onStart() {
        super.onStart()
        checkSecurity()
    }

    override fun onStop() {
        super.onStop()
        // Lock the app when it goes to background, but NOT on rotation
        if (!isChangingConfigurations) {
            viewModel.setUnlocked(false)
        }
    }

    private fun checkSecurity() {
        lifecycleScope.launch {
            val biometricEnabled = preferencesManager.isBiometricEnabled.first()
            if (biometricEnabled && BiometricHelper.canAuthenticate(this@MainActivity)) {
                // If already unlocked (e.g., during rotation), don't show prompt again
                if (viewModel.isUnlocked.value) return@launch

                BiometricHelper.showBiometricPrompt(
                    this@MainActivity,
                    onSuccess = { viewModel.setUnlocked(true) },
                    onError = { finish() }
                )
            } else {
                viewModel.setUnlocked(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        billingManager.refreshPurchases()
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

    private fun syncReminderSchedule() {
        lifecycleScope.launch {
            val remindersEnabled = preferencesManager.isReminderEnabled.first()
            ReminderWorker.sync(this@MainActivity, remindersEnabled)
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}
