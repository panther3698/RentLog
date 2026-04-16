package com.example.rentlog.ui.screens.summary

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentlog.data.pdf.PdfGenerator
import com.example.rentlog.domain.model.RentEntry
import com.example.rentlog.domain.repository.LandlordRepository
import com.example.rentlog.domain.repository.RentEntryRepository
import com.example.rentlog.ui.util.FiscalYearHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val rentRepository: RentEntryRepository,
    private val landlordRepository: LandlordRepository,
    private val pdfGenerator: PdfGenerator
) : ViewModel() {

    private val fiscalStartYear = FiscalYearHelper.getCurrentFiscalYear()

    private val _exportUri = MutableStateFlow<Uri?>(null)
    val exportUri = _exportUri.asStateFlow()

    private val _shouldShare = MutableStateFlow(false)
    val shouldShare = _shouldShare.asStateFlow()

    private val _uiState = MutableStateFlow(SummaryState(fiscalLabel = FiscalYearHelper.getFiscalYearLabel(fiscalStartYear)))
    val uiState = _uiState.asStateFlow()

    init {
        rentRepository.getEntriesForYear(fiscalStartYear)
            .onEach { entries ->
                val totalPaid = entries.sumOf { it.amount }
                val paidMonths = entries.map { it.month }.toSet()
                val fiscalMonths = FiscalYearHelper.getFiscalMonths()
                val missingMonths = fiscalMonths.filter { it !in paidMonths }
                
                _uiState.update { 
                    it.copy(
                        totalPaid = totalPaid,
                        missingMonthsCount = missingMonths.size,
                        missingMonthsNames = missingMonths.map { DateFormatSymbols().months[it - 1] },
                        entries = entries
                    )
                }
            }.launchIn(viewModelScope)
    }

    fun exportReport(type: ExportType, selection: Int? = null, shareDirectly: Boolean = false) {
        if (_uiState.value.isLoading) return
        
        _uiState.update { it.copy(isLoading = true) }
        _shouldShare.value = shareDirectly
        
        viewModelScope.launch {
            try {
                val landlord = landlordRepository.getAllLandlords().firstOrNull()?.firstOrNull() ?: return@launch
                val allEntries = _uiState.value.entries
                
                val (filteredEntries, title, fileName) = when (type) {
                    ExportType.ANNUAL -> {
                        Triple(allEntries, "Annual Rent Report - ${FiscalYearHelper.getFiscalYearLabel(fiscalStartYear)}", "Annual_Report_${fiscalStartYear}.pdf")
                    }
                    ExportType.QUARTERLY -> {
                        val quarter = selection ?: ((Calendar.getInstance().get(Calendar.MONTH) + 1).let { 
                            when (it) { in 4..6 -> 1; in 7..9 -> 2; in 10..12 -> 3; else -> 4 }
                        })
                        val qMonths = when (quarter) {
                            1 -> listOf(4, 5, 6)
                            2 -> listOf(7, 8, 9)
                            3 -> listOf(10, 11, 12)
                            else -> listOf(1, 2, 3)
                        }
                        Triple(allEntries.filter { it.month in qMonths }, "Quarterly Rent Report (Q$quarter)", "Quarterly_Report_Q$quarter.pdf")
                    }
                    ExportType.MONTHLY -> {
                        val month = selection ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
                        val monthName = DateFormatSymbols().months[month - 1]
                        Triple(allEntries.filter { it.month == month }, "Monthly Rent Receipt - $monthName", "Monthly_Receipt_$monthName.pdf")
                    }
                }

                if (filteredEntries.isNotEmpty() || type == ExportType.ANNUAL) {
                    val uri = pdfGenerator.generateCustomReport(
                        landlord = landlord, 
                        entries = filteredEntries, 
                        title = title, 
                        fileName = fileName,
                        fiscalStartYear = fiscalStartYear
                    )
                    _exportUri.value = uri
                }
            } catch (e: Exception) {
                // Error handling
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetExportUri() {
        _exportUri.value = null
        _shouldShare.value = false
    }
}

enum class ExportType {
    MONTHLY, QUARTERLY, ANNUAL
}

data class SummaryState(
    val totalPaid: Double = 0.0,
    val missingMonthsCount: Int = 12,
    val missingMonthsNames: List<String> = emptyList(),
    val entries: List<RentEntry> = emptyList(),
    val isLoading: Boolean = false,
    val fiscalLabel: String = ""
)
