package com.devchiradhi.rentlog.ui.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devchiradhi.rentlog.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    fun markWelcomeSeen() {
        viewModelScope.launch { preferencesManager.setHasSeenWelcome(true) }
    }
}
