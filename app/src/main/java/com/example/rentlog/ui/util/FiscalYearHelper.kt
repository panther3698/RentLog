package com.example.rentlog.ui.util

import java.util.*

object FiscalYearHelper {
    /**
     * In India, the Fiscal Year starts on April 1st and ends on March 31st.
     * Example: FY 2024-25 starts April 2024 and ends March 2025.
     */
    
    fun getCurrentFiscalYear(): Int {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) // 0-indexed (0=Jan, 3=Apr)
        val year = calendar.get(Calendar.YEAR)
        
        return if (month >= Calendar.APRIL) year else year - 1
    }

    fun getFiscalYearLabel(startYear: Int): String {
        return "FY $startYear-${(startYear + 1) % 100}"
    }

    /**
     * Returns months in order: April (4) to March (3) of next year
     */
    fun getFiscalMonths(): List<Int> {
        return listOf(4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3)
    }

    /**
     * Returns the actual calendar year for a given month within a fiscal year starting in [startYear]
     */
    fun getCalendarYearForMonth(month: Int, fiscalStartYear: Int): Int {
        return if (month >= 4) fiscalStartYear else fiscalStartYear + 1
    }
}
