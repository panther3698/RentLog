package com.example.rentlog.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentlog.domain.model.RentEntry
import com.example.rentlog.domain.repository.RentEntryRepository
import com.example.rentlog.ui.util.FiscalYearHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RentEntryRepository
) : ViewModel() {

    private val _selectedFiscalYear = MutableStateFlow(FiscalYearHelper.getCurrentFiscalYear())
    val selectedFiscalYear = _selectedFiscalYear.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val rentEntries: StateFlow<List<RentEntry>> = _selectedFiscalYear
        .flatMapLatest { year ->
            repository.getEntriesForYear(year)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectFiscalYear(year: Int) {
        _selectedFiscalYear.value = year
    }
}
