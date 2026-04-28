package com.devchiradhi.rentlog.data.manager

import com.devchiradhi.rentlog.BuildConfig
import com.devchiradhi.rentlog.data.local.PreferencesManager
import com.devchiradhi.rentlog.ui.util.TrialHelper
import com.devchiradhi.rentlog.ui.util.TrialStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessManager @Inject constructor(
    private val prefs: PreferencesManager
) {
    /**
     * True when the user may use all premium features (trial active OR purchased).
     * Debug builds can optionally bypass this through a developer setting.
     */
    val hasFullAccess: Flow<Boolean> = combine(
        prefs.isPremium,
        prefs.firstLaunchTimestamp,
        prefs.debugBypassPremiumAccess
    ) { premium, ts, debugBypass ->
        if (BuildConfig.DEBUG && debugBypass) {
            true
        } else {
            TrialHelper.hasFullAccess(premium, ts)
        }
    }

    /**
     * Richer status used for the trial badge on the Dashboard.
     * Debug builds show Premium only when the bypass is enabled.
     */
    val trialStatus: Flow<TrialStatus> = combine(
        prefs.isPremium,
        prefs.firstLaunchTimestamp,
        prefs.debugBypassPremiumAccess
    ) { premium, ts, debugBypass ->
        when {
            BuildConfig.DEBUG && debugBypass -> TrialStatus.Premium
            premium -> TrialStatus.Premium
            TrialHelper.isInTrial(ts) -> TrialStatus.InTrial(TrialHelper.daysRemaining(ts))
            else -> TrialStatus.Expired
        }
    }
}
