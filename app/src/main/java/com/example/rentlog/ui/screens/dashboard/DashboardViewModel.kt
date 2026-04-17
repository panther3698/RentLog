package com.example.rentlog.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentlog.data.local.PreferencesManager
import com.example.rentlog.domain.model.Landlord
import com.example.rentlog.domain.model.RentEntry
import com.example.rentlog.domain.repository.LandlordRepository
import com.example.rentlog.domain.repository.RentEntryRepository
import com.example.rentlog.ui.util.FiscalYearHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RentEntryRepository,
    private val landlordRepository: LandlordRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _selectedFiscalYear = MutableStateFlow(FiscalYearHelper.getCurrentFiscalYear())
    val selectedFiscalYear = _selectedFiscalYear.asStateFlow()

    val landlords: StateFlow<List<Landlord>> = landlordRepository.getAllLandlords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeLandlordId: StateFlow<Int> = preferencesManager.activeLandlordId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    // Derive active landlord from the list and stored ID
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeLandlord: StateFlow<Landlord?> = combine(landlords, activeLandlordId) { list, activeId ->
        if (list.isEmpty()) null
        else list.find { it.id == activeId } ?: list.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val rentEntries: StateFlow<List<RentEntry>> = combine(
        _selectedFiscalYear, activeLandlord
    ) { year, landlord ->
        Pair(year, landlord)
    }.flatMapLatest { (year, landlord) ->
        if (landlord != null) {
            repository.getEntriesForYearAndLandlord(year, landlord.id)
        } else {
            repository.getEntriesForYear(year)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectFiscalYear(year: Int) {
        _selectedFiscalYear.value = year
    }

    fun switchLandlord(landlordId: Int) {
        viewModelScope.launch {
            preferencesManager.setActiveLandlordId(landlordId)
        }
    }

    fun deleteEntry(entry: RentEntry) {
        viewModelScope.launch {
            repository.deleteRentEntry(entry)
        }
    }
}
