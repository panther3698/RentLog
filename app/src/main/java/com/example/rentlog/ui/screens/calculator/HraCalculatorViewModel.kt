package com.devchiradhi.rentlog.ui.screens.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devchiradhi.rentlog.data.manager.AccessManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HraCalculatorViewModel @Inject constructor(
    accessManager: AccessManager
) : ViewModel() {

    val hasFullAccess: StateFlow<Boolean> = accessManager.hasFullAccess
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
}
