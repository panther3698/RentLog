package com.devchiradhi.rentlog.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.devchiradhi.rentlog.data.local.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class BillingState {
    object Idle : BillingState()
    object Loading : BillingState()
    object Success : BillingState()
    object Pending : BillingState()
    object NotOwned : BillingState()
    data class Error(val message: String) : BillingState()
}

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) : PurchasesUpdatedListener {

    private val billingScope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val PREMIUM_PRODUCT_ID = "rentlog_premium"
    }

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    // ── Connection ────────────────────────────────────────────────────────────

    fun startConnection(onReady: (() -> Unit)? = null) {
        if (billingClient.isReady) { onReady?.invoke(); return }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryExistingPurchases()
                    onReady?.invoke()
                }
            }
            override fun onBillingServiceDisconnected() {
                // Will reconnect on next purchase attempt
            }
        })
    }

    fun endConnection() {
        if (billingClient.isReady) billingClient.endConnection()
    }

    fun refreshPurchases() {
        if (!billingClient.isReady) {
            startConnection(onReady = { queryExistingPurchases(markMissingAsNotOwned = false, emitSuccess = false) })
            return
        }
        queryExistingPurchases(markMissingAsNotOwned = false, emitSuccess = false)
    }

    // ── Purchase flow ─────────────────────────────────────────────────────────

    fun launchPurchaseFlow(activity: Activity) {
        if (!billingClient.isReady) {
            startConnection(onReady = { launchPurchaseFlow(activity) })
            _billingState.value = BillingState.Loading
            return
        }
        _billingState.value = BillingState.Loading

        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PREMIUM_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            ).build()

        billingClient.queryProductDetailsAsync(queryParams) { result, productList ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK || productList.isEmpty()) {
                _billingState.value = BillingState.Error(
                    "Product unavailable. Make sure you are signed in to Google Play."
                )
                return@queryProductDetailsAsync
            }
            val productDetails = productList[0]
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                ).build()
            billingClient.launchBillingFlow(activity, billingFlowParams)
        }
    }

    // ── Restore ───────────────────────────────────────────────────────────────

    fun restorePurchases() {
        if (!billingClient.isReady) {
            startConnection(onReady = { restorePurchases() })
            _billingState.value = BillingState.Loading
            return
        }
        _billingState.value = BillingState.Loading
        queryExistingPurchases(markMissingAsNotOwned = true, emitSuccess = true)
    }

    private fun queryExistingPurchases(
        markMissingAsNotOwned: Boolean = false,
        emitSuccess: Boolean = false
    ) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val premium = purchases.firstOrNull { it.products.contains(PREMIUM_PRODUCT_ID) }
                if (premium != null) {
                    handlePurchase(premium, emitSuccess)
                } else {
                    revokePremium()
                    if (markMissingAsNotOwned) {
                        _billingState.value = BillingState.NotOwned
                    }
                }
            }
        }
    }

    // ── PurchasesUpdatedListener ──────────────────────────────────────────────

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.products.contains(PREMIUM_PRODUCT_ID)) {
                        handlePurchase(purchase, emitSuccess = true)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingState.value = BillingState.Idle
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                restorePurchases()
            }
            else -> {
                _billingState.value = BillingState.Error("Purchase failed (code ${result.responseCode}). Please try again.")
            }
        }
    }

    // ── Acknowledgement + grant ───────────────────────────────────────────────

    private fun handlePurchase(purchase: Purchase, emitSuccess: Boolean) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(ackParams) { ackResult ->
                    if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        grantPremium(emitSuccess)
                    } else {
                        _billingState.value = BillingState.Error("Could not confirm purchase. Please restore purchases.")
                    }
                }
            } else {
                grantPremium(emitSuccess)
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _billingState.value = BillingState.Pending
        }
    }

    private fun grantPremium(emitSuccess: Boolean) {
        billingScope.launch {
            preferencesManager.setPremium(true)
            if (emitSuccess) {
                _billingState.value = BillingState.Success
            }
        }
    }

    private fun revokePremium() {
        billingScope.launch {
            preferencesManager.setPremium(false)
        }
    }

    fun resetState() {
        _billingState.value = BillingState.Idle
    }
}
