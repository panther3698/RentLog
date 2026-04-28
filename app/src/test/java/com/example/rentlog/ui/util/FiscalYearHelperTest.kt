package com.devchiradhi.rentlog.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FiscalYearHelperTest {

    @Test
    fun `fiscal months follow indian tax year ordering`() {
        assertEquals(listOf(4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3), FiscalYearHelper.getFiscalMonths())
    }

    @Test
    fun `calendar year is derived from fiscal year and month`() {
        assertEquals(2025, FiscalYearHelper.getCalendarYearForMonth(month = 4, fiscalStartYear = 2025))
        assertEquals(2026, FiscalYearHelper.getCalendarYearForMonth(month = 1, fiscalStartYear = 2025))
    }

    @Test
    fun `fiscal year label keeps two digit suffix`() {
        assertEquals("FY 2025-26", FiscalYearHelper.getFiscalYearLabel(2025))
        assertEquals("FY 2099-00", FiscalYearHelper.getFiscalYearLabel(2099))
    }
}
