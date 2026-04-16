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

    fun saveEntry() {
        val state = _uiState.value
        if (state.amount.isBlank() || state.landlordId == 0) return

        viewModelScope.launch {
            // Note: We store the entry with the 'fiscalStartYear' to keep it grouped in queries, 
            // but the 'calendarYear' is what we display if needed.
            // Actually, looking at the Room query, it filters by 'year'.
            // To support Indian FY, 'year' in the DB should represent the Fiscal Start Year.
            rentRepository.insertOrUpdateRentEntry(
                RentEntry(
                    id = state.existingEntryId,
                    month = state.month,
                    year = fiscalStartYear, 
                    amount = state.amount.toDoubleOrNull() ?: 0.0,
                    paymentDate = state.paymentDate,
                    transactionId = state.transactionId,
                    landlordId = state.landlordId
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}

data class AddEditUiState(
    val month: Int = 1,
    val year: Int = 0,
    val amount: String = "",
    val transactionId: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val isEdit: Boolean = false,
    val isSaved: Boolean = false,
    val existingEntryId: Int = 0,
    val landlordId: Int = 0
)
