package com.devchiradhi.rentlog.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrialHelperTest {

    @Test
    fun `has full access when user is premium`() {
        assertTrue(TrialHelper.hasFullAccess(isPremium = true, firstLaunchMs = 0L))
    }

    @Test
    fun `trial is active for recent installs`() {
        val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)

        assertTrue(TrialHelper.isInTrial(sevenDaysAgo))
        assertTrue(TrialHelper.daysRemaining(sevenDaysAgo) in 6..7)
    }

    @Test
    fun `trial expires after fourteen days`() {
        val fifteenDaysAgo = System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000)

        assertFalse(TrialHelper.isInTrial(fifteenDaysAgo))
        assertEquals(0, TrialHelper.daysRemaining(fifteenDaysAgo))
        assertFalse(TrialHelper.hasFullAccess(isPremium = false, firstLaunchMs = fifteenDaysAgo))
    }
}
