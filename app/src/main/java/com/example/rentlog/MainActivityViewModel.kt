package com.example.rentlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentlog.domain.repository.LandlordRepository
import com.example.rentlog.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository: LandlordRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked = _isUnlocked.asStateFlow()

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500) // Minimum splash screen time for branding
            try {
                val landlords = repository.getAllLandlords().first()
                if (landlords.isEmpty()) {
                    _startDestination.value = Screen.Onboarding.route
                } else {
                    _startDestination.value = Screen.Dashboard.route
                }
            } catch (e: Exception) {
                _startDestination.value = Screen.Onboarding.route
            }
        }
    }

    fun setUnlocked(unlocked: Boolean) {
        _isUnlocked.value = unlocked
    }
}
