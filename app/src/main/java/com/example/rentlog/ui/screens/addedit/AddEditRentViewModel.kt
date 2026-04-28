package com.devchiradhi.rentlog.ui.screens.addedit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devchiradhi.rentlog.domain.model.RentEntry
import com.devchiradhi.rentlog.domain.repository.LandlordRepository
import com.devchiradhi.rentlog.domain.repository.RentEntryRepository
import com.devchiradhi.rentlog.ui.util.FiscalYearHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddEditRentViewModel @Inject constructor(
    private val rentRepository: RentEntryRepository,
    private val landlordRepository: LandlordRepository,
    private val accessManager: com.devchiradhi.rentlog.data.manager.AccessManager,
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val month: Int = checkNotNull(savedStateHandle["month"])
    private val fiscalStartYear: Int = checkNotNull(savedStateHandle["fiscalYear"])
    private val calendarYear = FiscalYearHelper.getCalendarYearForMonth(month, fiscalStartYear)

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState = _uiState.asStateFlow()

    val hasFullAccess: StateFlow<Boolean> = accessManager.hasFullAccess
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

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
                    landlordId = landlord?.id ?: 0,
                    paymentMode = if (existingEntry != null) {
                        when {
                            existingEntry.transactionId.startsWith("UPI", ignoreCase = true) -> "UPI"
                            existingEntry.transactionId.startsWith("NEFT", ignoreCase = true) || 
                            existingEntry.transactionId.startsWith("IMPS", ignoreCase = true) ||
                            existingEntry.transactionId.startsWith("RTGS", ignoreCase = true) ||
                            existingEntry.transactionId.contains("Transfer", ignoreCase = true) -> "Transfer"
                            existingEntry.transactionId.isBlank() -> "Cash"
                            else -> "Transfer"
                        }
                    } else "UPI"
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

    fun onPaymentModeChange(mode: String) {
        _uiState.update { it.copy(paymentMode = mode) }
    }

    fun viewAttachment() {
        val uriString = _uiState.value.attachmentUri
        if (uriString.isBlank()) return

        try {
            val uri = Uri.parse(uriString)
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, appContext.contentResolver.getType(uri) ?: "*/*")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
        } catch (e: Exception) {
            _uiState.update { it.copy(attachmentError = "Could not open attachment. It may have been moved or deleted.") }
        }
    }

    fun clearAttachmentError() {
        _uiState.update { it.copy(attachmentError = null) }
    }

    fun saveEntry() {
        val state = _uiState.value
        if (state.amount.isBlank() || state.landlordId == 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            // Logic to prepend payment mode to transactionId if it's not already there
            val finalTxnId = if (state.transactionId.isBlank() && state.paymentMode != "Cash") {
                state.paymentMode // Use mode as txnId if txnId is empty but mode is specified
            } else if (state.transactionId.isNotBlank() && 
                !state.transactionId.startsWith(state.paymentMode, ignoreCase = true) && 
                state.paymentMode != "Cash" && state.paymentMode != "Transfer") {
                "${state.paymentMode}: ${state.transactionId}"
            } else {
                state.transactionId
            }

            rentRepository.insertOrUpdateRentEntry(
                RentEntry(
                    id = state.existingEntryId,
                    month = state.month,
                    year = fiscalStartYear,
                    amount = state.amount.toDoubleOrNull() ?: 0.0,
                    paymentDate = state.paymentDate,
                    transactionId = if (state.paymentMode == "Cash" && state.transactionId.isBlank()) "" else finalTxnId,
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

    /** Persist URI permission so the attachment survives app restarts. */
    fun persistAttachmentUri(uri: Uri) {
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        try {
            appContext.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (_: SecurityException) { /* provider may not support persistable permissions */ }
    }
}

data class AddEditUiState(
    val month: Int = 1,
    val year: Int = 0,
    val amount: String = "",
    val transactionId: String = "",
    val paymentMode: String = "UPI",
    val attachmentUri: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val isEdit: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val showSuccess: Boolean = false,
    val existingEntryId: Int = 0,
    val landlordId: Int = 0,
    val attachmentError: String? = null
)
