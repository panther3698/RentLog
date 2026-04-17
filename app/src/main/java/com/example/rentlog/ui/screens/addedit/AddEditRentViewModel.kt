package com.example.rentlog.ui.screens.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentlog.domain.model.RentEntry
import com.example.rentlog.domain.repository.LandlordRepository
import com.example.rentlog.domain.repository.RentEntryRepository
import com.example.rentlog.ui.util.FiscalYearHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddEditRentViewModel @Inject constructor(
    private val rentRepository: RentEntryRepository,
    private val landlordRepository: LandlordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val month: Int = checkNotNull(savedStateHandle["month"])
    private val fiscalStartYear = FiscalYearHelper.getCurrentFiscalYear()
    private val calendarYear = FiscalYearHelper.getCalendarYearForMonth(month, fiscalStartYear)

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val landlord = landlordRepository.getAllLandlords().firstOrNull()?.firstOrNull()
            val existingEntry = rentRepository.getEntriesForYear(fiscalStartYear)
                .firstOrNull()?.find { it.month == month }

            _uiState.update { 
                it.copy(
                    month = month,
                    year = calendarYear,
                    amount = existingEntry?.amount?.toString() ?: landlord?.defaultRentAmount?.toString() ?: "",
                    transactionId = existingEntry?.transactionId ?: "",
                    attachmentUri = existingEntry?.attachmentUri ?: "",
                    paymentDate = existingEntry?.paymentDate ?: System.currentTimeMillis(),
                    isEdit = existingEntry != null,
                    existingEntryId = existingEntry?.id ?: 0,
                    landlordId = landlord?.id ?: 0
                )
            }
        }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onTransactionIdChange(id: String) {
        _uiState.update { it.copy(transactionId = id) }
    }

    fun onDateChange(date: Long) {
        _uiState.update { it.copy(paymentDate = date) }
    }

    fun onAttachmentChange(uri: String) {
        _uiState.update { it.copy(attachmentUri = uri) }
    }

    fun saveEntry() {
        val state = _uiState.value
        if (state.amount.isBlank() || state.landlordId == 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            rentRepository.insertOrUpdateRentEntry(
                RentEntry(
                    id = state.existingEntryId,
                    month = state.month,
                    year = fiscalStartYear,
                    amount = state.amount.toDoubleOrNull() ?: 0.0,
                    paymentDate = state.paymentDate,
                    transactionId = state.transactionId,
                    landlordId = state.landlordId,
                    attachmentUri = state.attachmentUri
                )
            )
            _uiState.update { it.copy(isSaving = false, isSaved = true, showSuccess = true) }
        }
    }

    fun onSuccessShown() {
        _uiState.update { it.copy(showSuccess = false) }
    }
}

data class AddEditUiState(
    val month: Int = 1,
    val year: Int = 0,
    val amount: String = "",
    val transactionId: String = "",
    val attachmentUri: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val isEdit: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val showSuccess: Boolean = false,
    val existingEntryId: Int = 0,
    val landlordId: Int = 0
)
