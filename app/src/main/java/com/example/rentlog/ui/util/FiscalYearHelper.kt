package com.devchiradhi.rentlog.ui.util

import java.text.DateFormatSymbols
import java.util.*

object FiscalYearHelper {

    fun getFiscalMonths(): List<Int> {
        return listOf(4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3)
    }

    fun getCurrentFiscalYear(): Int {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1 // 1-based
        val year = calendar.get(Calendar.YEAR)
        return if (month < 4) year - 1 else year
    }

    fun getFiscalYearLabel(startYear: Int): String {
        val endYY = (startYear + 1) % 100
        return "FY $startYear-${endYY.toString().padStart(2, '0')}"
    }

    fun getCalendarYearForMonth(month: Int, fiscalStartYear: Int): Int {
        return if (month >= 4) fiscalStartYear else fiscalStartYear + 1
    }

    fun getMonthName(month: Int): String {
        return DateFormatSymbols().months[month - 1]
    }
}
