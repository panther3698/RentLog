package com.devchiradhi.rentlog.ui.util

object TrialHelper {

    const val TRIAL_DAYS = 21

    private val trialMs get() = TRIAL_DAYS * 24L * 60 * 60 * 1000

    fun isInTrial(firstLaunchMs: Long): Boolean {
        if (firstLaunchMs == 0L) return true          // timestamp not set yet → treat as in-trial
        return System.currentTimeMillis() - firstLaunchMs < trialMs
    }

    /** Returns 0 when expired. */
    fun daysRemaining(firstLaunchMs: Long): Int {
        if (firstLaunchMs == 0L) return TRIAL_DAYS
        val remaining = trialMs - (System.currentTimeMillis() - firstLaunchMs)
        return maxOf(0, (remaining / (24L * 60 * 60 * 1000)).toInt())
    }

    fun hasFullAccess(isPremium: Boolean, firstLaunchMs: Long) =
        isPremium || isInTrial(firstLaunchMs)
}

sealed class TrialStatus {
    object Premium : TrialStatus()
    data class InTrial(val daysRemaining: Int) : TrialStatus()
    object Expired : TrialStatus()
}
