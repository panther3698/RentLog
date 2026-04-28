package com.devchiradhi.rentlog.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devchiradhi.rentlog.domain.model.Landlord
import com.devchiradhi.rentlog.domain.model.RentEntry
import com.devchiradhi.rentlog.data.manager.AccessManager
import com.devchiradhi.rentlog.domain.repository.LandlordRepository
import com.devchiradhi.rentlog.domain.repository.RentEntryRepository
import com.devchiradhi.rentlog.ui.util.FiscalYearHelper
import com.devchiradhi.rentlog.ui.util.TrialHelper
import com.devchiradhi.rentlog.ui.util.TrialStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RentEntryRepository,
    private val landlordRepository: LandlordRepository,
    private val accessManager: AccessManager
) : ViewModel() {

    val trialStatus: StateFlow<TrialStatus> = accessManager.trialStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TrialStatus.InTrial(TrialHelper.TRIAL_DAYS))

    private val _selectedFiscalYear = MutableStateFlow(FiscalYearHelper.getCurrentFiscalYear())
    val selectedFiscalYear = _selectedFiscalYear.asStateFlow()

    val landlords: StateFlow<List<Landlord>> = landlordRepository.getAllLandlords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeLandlord: StateFlow<Landlord?> = landlordRepository.getAllLandlords()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val rentEntries: StateFlow<List<RentEntry>> = combine(
        _selectedFiscalYear, activeLandlord
    ) { year, landlord ->
        Pair(year, landlord)
    }.flatMapLatest { (year, landlord) ->
        if (landlord != null) {
            repository.getEntriesForYearAndLandlord(year, landlord.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectFiscalYear(year: Int) {
        _selectedFiscalYear.value = year
    }

    fun deleteEntry(entry: RentEntry) {
        viewModelScope.launch {
            repository.deleteRentEntry(entry)
        }
    }
}
