package com.example.rentlog.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentlog.domain.model.Landlord
import com.example.rentlog.domain.repository.LandlordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val name: String = "",
    val tenantName: String = "",
    val tenantAddress: String = "",
    val landlordAddress: String = "",
    val panNumber: String = "",
    val defaultRentAmount: String = ""
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: LandlordRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun onTenantNameChange(name: String) {
        _state.value = _state.value.copy(tenantName = name)
    }

    fun onTenantAddressChange(address: String) {
        _state.value = _state.value.copy(tenantAddress = address)
    }

    fun onLandlordAddressChange(address: String) {
        _state.value = _state.value.copy(landlordAddress = address)
    }

    fun onPanChange(pan: String) {
        _state.value = _state.value.copy(panNumber = pan)
    }

    fun onRentAmountChange(amount: String) {
        _state.value = _state.value.copy(defaultRentAmount = amount)
    }

    fun saveLandlord(onComplete: () -> Unit) {
        viewModelScope.launch {
            val landlord = Landlord(
                name = _state.value.name,
                tenantName = _state.value.tenantName,
                tenantAddress = _state.value.tenantAddress,
                landlordAddress = _state.value.landlordAddress,
                panNumber = _state.value.panNumber,
                defaultRentAmount = _state.value.defaultRentAmount.toDoubleOrNull() ?: 0.0
            )
            repository.insertOrUpdateLandlord(landlord)
            onComplete()
        }
    }
}
