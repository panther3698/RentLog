package com.devchiradhi.rentlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devchiradhi.rentlog.data.local.PreferencesManager
import com.devchiradhi.rentlog.domain.repository.LandlordRepository
import com.devchiradhi.rentlog.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository: LandlordRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked = _isUnlocked.asStateFlow()

    init {
        viewModelScope.launch { preferencesManager.initFirstLaunchTimestamp() }
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500) // Minimum splash screen time for branding
            try {
                val landlords = repository.getAllLandlords().first()
                val hasSeenWelcome = preferencesManager.hasSeenWelcome.first()
                _startDestination.value = when {
                    landlords.isNotEmpty() -> Screen.Dashboard.route
                    hasSeenWelcome -> Screen.Onboarding.route
                    else -> Screen.Welcome.route
                }
            } catch (e: Exception) {
                _startDestination.value = Screen.Welcome.route
            }
        }
    }

    fun setUnlocked(unlocked: Boolean) {
        _isUnlocked.value = unlocked
    }
}
