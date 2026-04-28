package com.devchiradhi.rentlog.data.pdf

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.devchiradhi.rentlog.domain.model.Landlord
import com.devchiradhi.rentlog.domain.model.RentEntry
import com.devchiradhi.rentlog.ui.util.FiscalYearHelper
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // ── Color palette ──────────────────────────────────────────────────────────
    private val colorPrimary  = DeviceRgb(45, 106, 79)   // dark green
    private val colorGrey     = DeviceRgb(100, 100, 100)
    private val colorLightGrey= DeviceRgb(245, 245, 245)
    private val colorWarning  = DeviceRgb(180, 80, 0)
    private val colorRed      = DeviceRgb(180, 30, 30)

    fun generateCustomReport(
        landlord: Landlord,
        entries: List<RentEntry>,
        title: String,
        fileName: String,
        fiscalStartYear: Int? = null
    ): Uri? {
        return createPdf(fileName) { document ->
            addReportContent(document, landlord, entries, title, fiscalStartYear)
        }
    }

    private fun addReportContent(
        document: Document,
        landlord: Landlord,
        entries: List<RentEntry>,
        title: String,
        fiscalStartYear: Int?
    ) {
        // ── Title block ───────────────────────────────────────────────────────
        document.add(
            Paragraph("RENT PAYMENT STATEMENT")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18f)
                .setBold()
                .setFontColor(colorPrimary)
        )
        document.add(
            Paragraph("For HRA Exemption under Section 10(13A) of the Income Tax Act, 1961")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(9f)
                .setFontColor(colorGrey)
        )
        document.add(
            Paragraph(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(13f)
                .setBold()
                .setMarginTop(4f)
        )

        if (fiscalStartYear != null) {
            val fyLabel = FiscalYearHelper.getFiscalYearLabel(fiscalStartYear)
            val ayEndYY = (fiscalStartYear + 2) % 100
            val ayLabel = "AY ${fiscalStartYear + 1}-${ayEndYY.toString().padStart(2, '0')}"
            document.add(
                Paragraph("$fyLabel  |  $ayLabel")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10f)
                    .setItalic()
                    .setFontColor(colorGrey)
            )
        }

        // ── Old Tax Regime Warning ─────────────────────────────────────────────
        val warningTable = Table(UnitValue.createPercentArray(floatArrayOf(1f))).useAllAvailableWidth()
            .setMarginTop(10f).setMarginBottom(6f)
        warningTable.addCell(
            Cell().add(
                Paragraph("⚠  IMPORTANT: HRA exemption under Section 10(13A) is available ONLY under the OLD Tax Regime. " +
                        "The new tax regime (Section 115BAC) — which is the DEFAULT from FY 2023-24 — does NOT allow this exemption. " +
                        "Ensure you have opted for the old regime with your employer (Form 10-IEA for self-filing).")
                    .setFontSize(8f)
                    .setFontColor(colorWarning)
            )
                .setBackgroundColor(DeviceRgb(255, 243, 224))
                .setBorder(com.itextpdf.layout.borders.SolidBorder(colorWarning, 0.75f))
                .setPadding(7f)
        )
        document.add(warningTable)

        document.add(Paragraph("\n"))

        // ── Parties table ─────────────────────────────────────────────────────
        val infoTable = Table(UnitValue.createPointArray(floatArrayOf(1f, 1f))).useAllAvailableWidth()

        infoTable.addCell(Cell().add(Paragraph("TENANT / EMPLOYEE DETAILS").setBold().setFontSize(9f).setFontColor(colorPrimary)).setBorder(null))
        infoTable.addCell(Cell().add(Paragraph("LANDLORD / LESSOR DETAILS").setBold().setFontSize(9f).setFontColor(colorPrimary)).setBorder(null))

        infoTable.addCell(Cell().add(
            Paragraph()
                .add(Paragraph("Name: ").setBold().setFontSize(9f))
                .add(Paragraph(landlord.tenantName.ifBlank { "—" }).setFontSize(9f))
        ).setBorder(null))
        infoTable.addCell(Cell().add(
            Paragraph()
                .add(Paragraph("Name: ").setBold().setFontSize(9f))
                .add(Paragraph(landlord.name.ifBlank { "—" }).setFontSize(9f))
        ).setBorder(null))

        infoTable.addCell(Cell().add(
            Paragraph()
                .add(Paragraph("Rented Property Address:\n").setBold().setFontSize(9f))
                .add(Paragraph(landlord.tenantAddress.ifBlank { "Not provided" }).setFontSize(9f))
        ).setBorder(null))
        infoTable.addCell(Cell().add(
            Paragraph()
                .add(Paragraph("Address: ").setBold().setFontSize(9f))
                .add(Paragraph(landlord.landlordAddress.ifBlank { "—" }).setFontSize(9f))
        ).setBorder(null))

        infoTable.addCell(Cell().add(Paragraph("")).setBorder(null))
        infoTable.addCell(Cell().add(
            Paragraph()
                .add(Paragraph("PAN: ").setBold().setFontSize(9f))
                .add(Paragraph(
                    if (landlord.panNumber.isBlank()) "Not provided (submit Form 60)"
                    else landlord.panNumber
                ).setFontSize(9f)
                    .setFontColor(if (landlord.panNumber.isBlank()) colorRed else DeviceRgb(0,0,0)))
        ).setBorder(null))

        document.add(infoTable)
        document.add(Paragraph("\n"))

        // ── Payment table ─────────────────────────────────────────────────────
        val table = Table(UnitValue.createPointArray(floatArrayOf(90f, 55f, 110f, 80f, 115f, 90f)))
            .useAllAvailableWidth()

        listOf("Month", "Year", "Amount (₹)", "Mode", "Transaction / Ref No.", "Payment Date").forEach { header ->
            table.addHeaderCell(
                Cell().add(Paragraph(header).setBold().setFontSize(8.5f).setFontColor(colorPrimary))
                    .setBackgroundColor(colorLightGrey)
            )
        }

        val sortedEntries = entries.sortedWith(
            compareBy(
                { if (it.month >= 4) it.year else it.year + 1 },
                { if (it.month >= 4) it.month else it.month + 12 }
            )
        )

        sortedEntries.forEach { entry ->
            val monthName = DateFormatSymbols().months[entry.month - 1]
            val calendarYear = FiscalYearHelper.getCalendarYearForMonth(entry.month, entry.year)
            val mode = derivePaymentMode(entry.transactionId)
            table.addCell(Cell().add(Paragraph(monthName).setFontSize(9f)))
            table.addCell(Cell().add(Paragraph("$calendarYear").setFontSize(9f)))
            table.addCell(Cell().add(Paragraph(formatAmount(entry.amount)).setFontSize(9f)))
            table.addCell(Cell().add(Paragraph(mode).setFontSize(9f)))
            table.addCell(Cell().add(Paragraph(entry.transactionId.ifBlank { "—" }).setFontSize(9f)))
            table.addCell(Cell().add(Paragraph(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(entry.paymentDate))).setFontSize(9f)))
        }

        document.add(table)

        // ── Total ─────────────────────────────────────────────────────────────
        val total = entries.sumOf { it.amount }
        document.add(
            Paragraph("TOTAL RENT PAID: ${formatAmount(total)}")
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(11f)
                .setFontColor(colorPrimary)
                .setMarginTop(6f)
        )

        // ── TDS Advisory ──────────────────────────────────────────────────────
        val monthlyAvg = if (entries.isNotEmpty()) total / entries.size else 0.0
        if (monthlyAvg > 50000) {
            document.add(
                Paragraph("⚠  TDS NOTICE (Section 194-IB): Monthly rent exceeds ₹50,000. " +
                        "Tenant is required to deduct TDS @ 5% on rent paid. " +
                        "File Form 26QC within 30 days from end of financial year and issue Form 16C to landlord.")
                    .setFontSize(8f)
                    .setBold()
                    .setFontColor(colorRed)
                    .setMarginTop(8f)
            )
        }

        // ── PAN advisory ──────────────────────────────────────────────────────
        if (total > 100000) {
            document.add(
                Paragraph("ℹ  PAN MANDATORY: Total rent paid exceeds ₹1,00,000 for this period. " +
                        "Landlord's PAN is mandatory for HRA exemption (Circular No. 8/2013). " +
                        (if (landlord.panNumber.isBlank()) "PAN not provided — obtain Form 60 declaration from landlord." else "PAN recorded: ${landlord.panNumber}"))
                    .setFontSize(8f)
                    .setFontColor(if (landlord.panNumber.isBlank()) colorRed else colorGrey)
                    .setMarginTop(4f)
            )
        }

        // ── Signature & declaration ────────────────────────────────────────────
        document.add(Paragraph("\n\n"))
        val sigTable = Table(UnitValue.createPointArray(floatArrayOf(1f, 1f))).useAllAvailableWidth()
        sigTable.addCell(
            Cell().add(
                Paragraph("Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}")
                    .setFontSize(9f)
            ).setBorder(null)
        )
        sigTable.addCell(
            Cell().add(
                Paragraph("____________________________\nSignature of Landlord\n(${landlord.name.ifBlank { "Landlord Name" }})")
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(9f)
            ).setBorder(null)
        )
        document.add(sigTable)

        // ── Footer notes ──────────────────────────────────────────────────────
        document.add(
            Paragraph("\nFor use with Form 12BB (Rule 26C) — submit to employer for TDS adjustment on salary.")
                .setFontSize(7.5f)
                .setFontColor(colorGrey)
        )
        document.add(
            Paragraph("This is a computer-generated statement. HRA exemption is subject to conditions under Section 10(13A) read with Rule 2A of the Income Tax Rules, 1962. Applicable under OLD tax regime only.")
                .setFontSize(7.5f)
                .setItalic()
                .setFontColor(colorGrey)
        )
    }

    // ── Monthly Receipt ────────────────────────────────────────────────────────

    fun generateMonthlyReceipt(landlord: Landlord, entry: RentEntry): Uri? {
        val monthName = DateFormatSymbols().months[entry.month - 1]
        val calendarYear = FiscalYearHelper.getCalendarYearForMonth(entry.month, entry.year)
        val fyLabel = FiscalYearHelper.getFiscalYearLabel(entry.year)
        val fileName = "Rent_Receipt_${monthName}_${calendarYear}.pdf"

        return createPdf(fileName) { document ->
            addReceiptContent(document, landlord, entry, calendarYear, fyLabel)
        }
    }

    private fun addReceiptContent(
        document: Document,
        landlord: Landlord,
        entry: RentEntry,
        calendarYear: Int,
        fyLabel: String
    ) {
        val monthName = DateFormatSymbols().months[entry.month - 1]
        val isCashAbove5k = entry.transactionId.isBlank() && entry.amount > 5000.0
        val isHighRent = entry.amount > 50000.0

        // ── Header ────────────────────────────────────────────────────────────
        val headerTable = Table(UnitValue.createPointArray(floatArrayOf(4f, 1f))).useAllAvailableWidth()
        headerTable.addCell(
            Cell().add(
                Paragraph("RENT RECEIPT")
                    .setFontSize(22f)
                    .setBold()
                    .setFontColor(colorPrimary)
            ).setBorder(null).setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
        )
        headerTable.addCell(
            Cell().add(
                Paragraph(if (isCashAbove5k) "Affix ₹1\nRevenue\nStamp" else "Affix\nRevenue\nStamp")
                    .setFontSize(8f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(colorPrimary)
                    .also { if (isCashAbove5k) it.setBold() }
            )
                .setHeight(60f)
                .setWidth(60f)
                .setBorder(com.itextpdf.layout.borders.SolidBorder(colorPrimary, 1f))
                .setBackgroundColor(DeviceRgb(212, 226, 221))
        )
        document.add(headerTable)

        document.add(
            Paragraph("Under Section 10(13A), Income Tax Act, 1961  |  $fyLabel")
                .setFontSize(8f)
                .setFontColor(colorGrey)
                .setItalic()
        )
        document.add(
            Table(UnitValue.createPercentArray(floatArrayOf(1f))).useAllAvailableWidth()
                .addCell(Cell().setBorder(com.itextpdf.layout.borders.SolidBorder(colorPrimary, 0.5f)))
        )
        document.add(Paragraph("\n"))

        // ── Body ──────────────────────────────────────────────────────────────
        val formattedAmount = formatAmount(entry.amount)
        val content = Paragraph()
            .add("Received with thanks from ")
            .add(Paragraph(landlord.tenantName.ifBlank { "—" }).setBold().setFontColor(colorPrimary))
            .add(" a sum of ")
            .add(Paragraph("₹$formattedAmount").setBold().setFontColor(colorPrimary))
            .add(" (Rupees ${AmountToWords.convert(entry.amount.toLong())} only)")
            .add(" towards rent for the month of ")
            .add(Paragraph("$monthName $calendarYear").setBold().setFontColor(colorPrimary))
            .add(" for the premises at ")
            .add(Paragraph(landlord.tenantAddress.ifBlank { "[Rented Property Address]" }).setBold().setFontColor(colorPrimary))
            .add(".")

        document.add(content.setFontSize(11f).setMultipliedLeading(1.6f))
        document.add(Paragraph("\n"))

        // ── Details table ─────────────────────────────────────────────────────
        val table = Table(UnitValue.createPointArray(floatArrayOf(150f, 350f))).useAllAvailableWidth()

        fun addRow(label: String, value: String, valueColor: DeviceRgb? = null) {
            table.addCell(Cell().add(Paragraph(label).setBold().setFontColor(colorPrimary).setFontSize(9f)).setBorder(null))
            val valueP = Paragraph(": $value").setFontSize(9f)
            if (valueColor != null) valueP.setFontColor(valueColor)
            table.addCell(Cell().add(valueP).setBorder(null))
        }

        addRow("Landlord Name", landlord.name.ifBlank { "—" })
        addRow("Landlord Address", landlord.landlordAddress.ifBlank { "—" })
        addRow("Rented Premises", landlord.tenantAddress.ifBlank { "Not provided" })
        addRow("Landlord PAN",
            if (landlord.panNumber.isBlank()) "Not provided (Form 60 required)" else landlord.panNumber,
            if (landlord.panNumber.isBlank()) colorRed else null
        )
        addRow("Payment Mode", derivePaymentMode(entry.transactionId))
        addRow("Payment Date", SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(entry.paymentDate)))
        addRow("Transaction / Ref No.", entry.transactionId.ifBlank { "N/A (Cash)" })

        document.add(table)
        document.add(Paragraph("\n\n\n"))

        // ── Signature ─────────────────────────────────────────────────────────
        val footerTable = Table(UnitValue.createPointArray(floatArrayOf(1f, 1f))).useAllAvailableWidth()
        footerTable.addCell(
            Cell().add(
                Paragraph("Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}")
                    .setFontSize(9f)
            ).setBorder(null)
        )
        footerTable.addCell(
            Cell().add(
                Paragraph("__________________________\n(Signature of Landlord)\n${landlord.name.ifBlank { "" }}")
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(9f)
            ).setBorder(null)
        )
        document.add(footerTable)

        // ── Notices ───────────────────────────────────────────────────────────
        if (isCashAbove5k) {
            document.add(
                Paragraph("\n⚠  Cash payment exceeds ₹5,000 — affix a ₹1 revenue stamp and sign across it on the physical copy (as per the Indian Stamp Act).")
                    .setFontSize(8f)
                    .setBold()
                    .setFontColor(colorWarning)
            )
        }

        if (isHighRent) {
            document.add(
                Paragraph("⚠  TDS NOTICE (Section 194-IB): Monthly rent exceeds ₹50,000. Tenant must deduct TDS @ 5%, file Form 26QC and issue Form 16C to landlord.")
                    .setFontSize(8f)
                    .setBold()
                    .setFontColor(colorRed)
            )
        }

        if (entry.amount * 12 > 100000 && landlord.panNumber.isBlank()) {
            document.add(
                Paragraph("⚠  PAN of landlord is mandatory when annual rent exceeds ₹1,00,000 (CBDT Circular No. 8/2013). Obtain Form 60 if PAN is unavailable.")
                    .setFontSize(8f)
                    .setFontColor(colorRed)
            )
        }

        document.add(
            Paragraph("\nNote: This receipt is valid for HRA exemption claim under Section 10(13A) read with Rule 2A. Applicable under OLD tax regime only. Under the new tax regime (default from FY 2023-24), HRA is fully taxable.")
                .setFontSize(7.5f)
                .setItalic()
                .setFontColor(colorGrey)
        )
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun derivePaymentMode(transactionId: String): String = when {
        transactionId.startsWith("UPI",  ignoreCase = true) -> "UPI"
        transactionId.startsWith("NEFT", ignoreCase = true) -> "NEFT"
        transactionId.startsWith("IMPS", ignoreCase = true) -> "IMPS"
        transactionId.startsWith("RTGS", ignoreCase = true) -> "RTGS"
        transactionId.startsWith("CHEQUE", ignoreCase = true) -> "Cheque"
        transactionId.isBlank() -> "Cash"
        else -> "Online Transfer"
    }

    private fun formatAmount(amount: Double): String =
        "₹${String.format(Locale.getDefault(), "%,.2f", amount)}"

    private fun createPdf(fileName: String, block: (Document) -> Unit): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/RentLog")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val uri = resolver.insert(collection, contentValues) ?: return null

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                val writer = PdfWriter(outputStream)
                val pdf = PdfDocument(writer)
                val document = Document(pdf)
                block(document)
                document.close()
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            uri
        } catch (e: Exception) {
            try { resolver.delete(uri, null, null) } catch (_: Exception) {}
            null
        }
    }
}
