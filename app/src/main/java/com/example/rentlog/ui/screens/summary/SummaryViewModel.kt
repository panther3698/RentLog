package com.example.rentlog.ui.screens.summary

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentlog.data.local.PreferencesManager
import com.example.rentlog.data.pdf.PdfGenerator
import com.example.rentlog.domain.model.Landlord
import com.example.rentlog.domain.model.RentEntry
import com.example.rentlog.domain.repository.LandlordRepository
import com.example.rentlog.domain.repository.RentEntryRepository
import com.example.rentlog.ui.util.FiscalYearHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val rentRepository: RentEntryRepository,
    private val landlordRepository: LandlordRepository,
    private val pdfGenerator: PdfGenerator,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val fiscalStartYear = FiscalYearHelper.getCurrentFiscalYear()

    private val _exportUri = MutableStateFlow<Uri?>(null)
    val exportUri = _exportUri.asStateFlow()

    private val _shouldShare = MutableStateFlow(false)
    val shouldShare = _shouldShare.asStateFlow()

    private val _uiState = MutableStateFlow(SummaryState(fiscalLabel = FiscalYearHelper.getFiscalYearLabel(fiscalStartYear)))
    val uiState = _uiState.asStateFlow()

    private val _activeLandlord = MutableStateFlow<Landlord?>(null)
    val activeLandlord = _activeLandlord.asStateFlow()

    init {
        observeActiveLandlordAndEntries()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeActiveLandlordAndEntries() {
        combine(
            preferencesManager.activeLandlordId,
            landlordRepository.getAllLandlords()
        ) { activeId, allLandlords ->
            if (allLandlords.isEmpty()) null
            else allLandlords.find { it.id == activeId } ?: allLandlords.first()
        }.onEach { landlord ->
            _activeLandlord.value = landlord
            if (landlord != null) {
                updateEntriesForLandlord(landlord)
            }
        }.launchIn(viewModelScope)
    }

    private fun updateEntriesForLandlord(landlord: Landlord) {
        rentRepository.getEntriesForYearAndLandlord(fiscalStartYear, landlord.id)
            .onEach { entries ->
                val totalPaid = entries.sumOf { it.amount }
                val paidMonths = entries.map { it.month }.toSet()
                val fiscalMonths = FiscalYearHelper.getFiscalMonths()
                val missingMonths = fiscalMonths.filter { it !in paidMonths }

                _uiState.update {
                    it.copy(
                        totalPaid = totalPaid,
                        missingMonthsCount = missingMonths.size,
                        missingMonthsNames = missingMonths.map { m -> DateFormatSymbols().months[m - 1] },
                        entries = entries
                    )
                }
            }.launchIn(viewModelScope)
    }

    /**
     * Prepare preview data for a given report type without generating the PDF.
     * Returns the filtered entries and title for the preview dialog.
     */
    fun preparePreview(type: ExportType, selection: Int? = null): PreviewData? {
        val allEntries = _uiState.value.entries
        val landlord = _activeLandlord.value ?: return null

        val (filteredEntries, title) = when (type) {
            ExportType.ANNUAL -> Pair(
                allEntries,
                "Annual Rent Report - ${FiscalYearHelper.getFiscalYearLabel(fiscalStartYear)}"
            )
            ExportType.QUARTERLY -> {
                val quarter = selection ?: (Calendar.getInstance().get(Calendar.MONTH) + 1).let {
                    when (it) { in 4..6 -> 1; in 7..9 -> 2; in 10..12 -> 3; else -> 4 }
                }
                val qMonths = when (quarter) { 1 -> listOf(4,5,6); 2 -> listOf(7,8,9); 3 -> listOf(10,11,12); else -> listOf(1,2,3) }
                Pair(allEntries.filter { it.month in qMonths }, "Quarterly Rent Report (Q$quarter)")
            }
            ExportType.MONTHLY -> {
                val month = selection ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
                val monthName = DateFormatSymbols().months[month - 1]
                Pair(allEntries.filter { it.month == month }, "Monthly Rent Receipt - $monthName")
            }
        }

        return PreviewData(
            title = title,
            landlord = landlord,
            entries = filteredEntries.sortedWith(
                compareBy(
                    { if (it.month >= 4) it.year else it.year + 1 },
                    { if (it.month >= 4) it.month else it.month + 12 }
                )
            ),
            totalAmount = filteredEntries.sumOf { it.amount },
            fiscalLabel = FiscalYearHelper.getFiscalYearLabel(fiscalStartYear),
            type = type,
            selection = selection
        )
    }

    fun exportReport(type: ExportType, selection: Int? = null, shareDirectly: Boolean = false) {
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        _shouldShare.value = shareDirectly

        viewModelScope.launch {
            try {
                val landlord = _activeLandlord.value
                if (landlord == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "No landlord profile found. Please complete onboarding first.") }
                    return@launch
                }
                val allEntries = _uiState.value.entries

                val (filteredEntries, title, fileName) = when (type) {
                    ExportType.ANNUAL -> Triple(
                        allEntries,
                        "Annual Rent Report - ${FiscalYearHelper.getFiscalYearLabel(fiscalStartYear)}",
                        "Annual_Report_${fiscalStartYear}.pdf"
                    )
                    ExportType.QUARTERLY -> {
                        val quarter = selection ?: (Calendar.getInstance().get(Calendar.MONTH) + 1).let {
                            when (it) { in 4..6 -> 1; in 7..9 -> 2; in 10..12 -> 3; else -> 4 }
                        }
                        val qMonths = when (quarter) { 1 -> listOf(4,5,6); 2 -> listOf(7,8,9); 3 -> listOf(10,11,12); else -> listOf(1,2,3) }
                        Triple(allEntries.filter { it.month in qMonths }, "Quarterly Rent Report (Q$quarter)", "Quarterly_Report_Q$quarter.pdf")
                    }
                    ExportType.MONTHLY -> {
                        val month = selection ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
                        val monthName = DateFormatSymbols().months[month - 1]
                        Triple(allEntries.filter { it.month == month }, "Monthly Rent Receipt - $monthName", "Monthly_Receipt_$monthName.pdf")
                    }
                }

                if (filteredEntries.isEmpty() && type != ExportType.ANNUAL) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "No entries found for the selected period.") }
                    return@launch
                }

                val uri = pdfGenerator.generateCustomReport(
                    landlord = landlord,
                    entries = filteredEntries,
                    title = title,
                    fileName = fileName,
                    fiscalStartYear = fiscalStartYear
                )

                if (uri != null) {
                    _exportUri.value = uri
                } else {
                    _uiState.update { it.copy(errorMessage = "Failed to generate PDF. Please check storage permissions.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Export failed: ${e.message ?: "Unknown error"}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetExportUri() {
        _exportUri.value = null
        _shouldShare.value = false
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

enum class ExportType { MONTHLY, QUARTERLY, ANNUAL }

data class SummaryState(
    val totalPaid: Double = 0.0,
    val missingMonthsCount: Int = 12,
    val missingMonthsNames: List<String> = emptyList(),
    val entries: List<RentEntry> = emptyList(),
    val isLoading: Boolean = false,
    val fiscalLabel: String = "",
    val errorMessage: String? = null
)

data class PreviewData(
    val title: String,
    val landlord: Landlord,
    val entries: List<RentEntry>,
    val totalAmount: Double,
    val fiscalLabel: String,
    val type: ExportType,
    val selection: Int?
)
