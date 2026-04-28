package com.devchiradhi.rentlog.ui.screens.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devchiradhi.rentlog.data.billing.BillingManager
import com.devchiradhi.rentlog.data.billing.BillingState
import com.devchiradhi.rentlog.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val billingState: StateFlow<BillingState> = billingManager.billingState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BillingState.Idle)

    val isPremium: StateFlow<Boolean> = preferencesManager.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        billingManager.startConnection()
    }

    fun purchase(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }

    fun resetBillingState() {
        billingManager.resetState()
    }
}
