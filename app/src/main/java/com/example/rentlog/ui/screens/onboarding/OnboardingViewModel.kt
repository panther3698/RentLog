package com.devchiradhi.rentlog.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devchiradhi.rentlog.domain.model.Landlord
import com.devchiradhi.rentlog.domain.repository.LandlordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: LandlordRepository
) : ViewModel() {

    private val _onboardingCompleted = MutableSharedFlow<Unit>()
    val onboardingCompleted = _onboardingCompleted.asSharedFlow()

    private val _landlord = MutableStateFlow<Landlord?>(null)
    val landlord: StateFlow<Landlord?> = _landlord.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadLandlord()
    }

    private fun loadLandlord() {
        viewModelScope.launch {
            repository.getAllLandlords().collect { landlords ->
                if (landlords.isNotEmpty()) {
                    _landlord.value = landlords.first()
                }
            }
        }
    }

    fun saveLandlord(
        name: String,
        tenantName: String,
        tenantAddress: String,
        landlordAddress: String,
        pan: String,
        defaultRent: Double
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val currentLandlord = _landlord.value ?: Landlord(name = "", panNumber = "")
            repository.insertOrUpdateLandlord(
                currentLandlord.copy(
                    name = name,
                    tenantName = tenantName,
                    tenantAddress = tenantAddress,
                    landlordAddress = landlordAddress,
                    panNumber = pan,
                    defaultRentAmount = defaultRent
                )
            )
            _isSaving.value = false
            _onboardingCompleted.emit(Unit)
        }
    }
}
