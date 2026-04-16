package com.example.rentlog.data.pdf

object AmountToWords {
    private val units = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine")
    private val teens = arrayOf("Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
    private val tens = arrayOf("", "Ten", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

    fun convert(amount: Long): String {
        if (amount == 0L) return "Zero"
        return convertToWords(amount).trim()
    }

    private fun convertToWords(n: Long): String {
        if (n < 0) return "Minus " + convertToWords(-n)
        if (n < 10) return units[n.toInt()]
        if (n < 20) return teens[n.toInt() - 10]
        if (n < 100) return tens[n.toInt() / 10] + (if (n % 10 != 0L) " " + units[(n % 10).toInt()] else "")
        if (n < 1000) return units[n.toInt() / 100] + " Hundred" + (if (n % 100 != 0L) " and " + convertToWords(n % 100) else "")
        if (n < 100000) return convertToWords(n / 1000) + " Thousand" + (if (n % 1000 != 0L) " " + convertToWords(n % 1000) else "")
        if (n < 10000000) return convertToWords(n / 100000) + " Lakh" + (if (n % 100000 != 0L) " " + convertToWords(n % 100000) else "")
        return convertToWords(n / 10000000) + " Crore" + (if (n % 10000000 != 0L) " " + convertToWords(n % 10000000) else "")
    }
}
