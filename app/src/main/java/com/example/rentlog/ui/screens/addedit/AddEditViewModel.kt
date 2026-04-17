package com.example.rentlog.ui.screens.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentlog.data.local.PreferencesManager
import com.example.rentlog.domain.model.RentEntry
import com.example.rentlog.domain.repository.LandlordRepository
import com.example.rentlog.domain.repository.RentEntryRepository
import com.example.rentlog.ui.util.FiscalYearHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val rentRepository: RentEntryRepository,
    private val landlordRepository: LandlordRepository,
    private val preferencesManager: PreferencesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val month: Int = try {
        val m = savedStateHandle.get<String>("month")?.toInt() ?: 1
        m
    } catch (e: Exception) {
        1
    }
    
    private val fiscalStartYear = FiscalYearHelper.getCurrentFiscalYear()

    private val _state = MutableStateFlow(AddEditUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val activeLandlordId = preferencesManager.activeLandlordId.first()
            val allLandlords = landlordRepository.getAllLandlords().first()
            val landlord = allLandlords.find { it.id == activeLandlordId } ?: allLandlords.firstOrNull()

            val existingEntry = landlord?.let { l ->
                rentRepository.getEntriesForYearAndLandlord(fiscalStartYear, l.id)
                    .first().find { it.month == month }
            }

            _state.update {
                it.copy(
                    month = month,
                    amount = existingEntry?.amount?.toInt()?.toString() ?: landlord?.defaultRentAmount?.toInt()?.toString() ?: "",
                    transactionId = existingEntry?.transactionId ?: "",
                    paymentDate = existingEntry?.paymentDate ?: System.currentTimeMillis(),
                    paymentMode = existingEntry?.paymentMode ?: "UPI",
                    landlordId = landlord?.id ?: 0,
                    existingEntryId = existingEntry?.id ?: 0
                )
            }
        }
    }

    fun onAmountChange(amount: String) { _state.update { it.copy(amount = amount) } }
    fun onTransactionIdChange(id: String) { _state.update { it.copy(transactionId = id) } }
    fun onPaymentModeChange(mode: String) { _state.update { it.copy(paymentMode = mode) } }

    fun saveEntry(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.amount.isBlank() || s.landlordId == 0) return

        viewModelScope.launch {
            rentRepository.insertOrUpdateRentEntry(
                RentEntry(
                    id = s.existingEntryId,
                    month = s.month,
                    year = fiscalStartYear,
                    amount = s.amount.toDoubleOrNull() ?: 0.0,
                    paymentDate = s.paymentDate,
                    transactionId = s.transactionId,
                    landlordId = s.landlordId,
                    paymentMode = s.paymentMode
                )
            )
            onSuccess()
        }
    }
}

data class AddEditUiState(
    val month: Int = 1,
    val amount: String = "",
    val transactionId: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMode: String = "UPI",
    val landlordId: Int = 0,
    val existingEntryId: Int = 0
)
