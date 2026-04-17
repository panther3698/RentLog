package com.example.rentlog.data.pdf

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.rentlog.domain.model.Landlord
import com.example.rentlog.domain.model.RentEntry
import com.example.rentlog.ui.util.FiscalYearHelper
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

    fun generateCustomReport(
        landlord: Landlord,
        entries: List<RentEntry>,
        title: String,
        fileName: String,
        fiscalStartYear: Int? = null
    ): Uri? {
        return createPdf(fileName) { document ->
            document.add(
                Paragraph("RENT PAYMENT LOG")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20f)
                    .setBold()
            )

            document.add(
                Paragraph(title)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14f)
            )

            fiscalStartYear?.let { startYear ->
                val fyLabel = FiscalYearHelper.getFiscalYearLabel(startYear)
                val ayLabel = "AY ${startYear + 1}-${(startYear + 2) % 100}"
                document.add(
                    Paragraph("$fyLabel | $ayLabel")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(10f)
                        .setItalic()
                )
            }

            document.add(Paragraph("\n"))

            // Tenant / Landlord details
            val infoTable = Table(UnitValue.createPointArray(floatArrayOf(1f, 1f))).useAllAvailableWidth()
            infoTable.addCell(Cell().add(Paragraph("TENANT DETAILS").setBold().setFontSize(10f)).setBorder(null))
            infoTable.addCell(Cell().add(Paragraph("LANDLORD DETAILS").setBold().setFontSize(10f)).setBorder(null))
            infoTable.addCell(Cell().add(Paragraph("Name: ${landlord.tenantName}\nAddress: ${landlord.tenantAddress}")).setBorder(null))
            infoTable.addCell(Cell().add(Paragraph("Name: ${landlord.name}\nAddress: ${landlord.landlordAddress}")).setBorder(null))
            infoTable.addCell(Cell().add(Paragraph("")).setBorder(null))
            infoTable.addCell(Cell().add(Paragraph("PAN: ${landlord.panNumber}")).setBorder(null))
            document.add(infoTable)
            document.add(Paragraph("\n"))

            // Payment table — now includes Payment Mode column
            val table = Table(UnitValue.createPointArray(floatArrayOf(90f, 65f, 110f, 110f, 120f, 90f)))
                .useAllAvailableWidth()

            listOf("Month", "Year", "Amount (INR)", "Mode", "Transaction ID", "Payment Date").forEach { header ->
                table.addHeaderCell(Cell().add(Paragraph(header).setBold().setFontSize(9f)))
            }

            entries.sortedWith(
                compareBy(
                    { if (it.month >= 4) it.year else it.year + 1 },
                    { if (it.month >= 4) it.month else it.month + 12 }
                )
            ).forEach { entry ->
                val monthName = DateFormatSymbols().months[entry.month - 1]
                val calendarYear = FiscalYearHelper.getCalendarYearForMonth(entry.month, entry.year)
                table.addCell(Cell().add(Paragraph(monthName).setFontSize(9f)))
                table.addCell(Cell().add(Paragraph("$calendarYear").setFontSize(9f)))
                table.addCell(Cell().add(Paragraph("₹${String.format(Locale.getDefault(), "%,.2f", entry.amount)}").setFontSize(9f)))
                table.addCell(Cell().add(Paragraph(entry.paymentMode).setFontSize(9f)))
                table.addCell(Cell().add(Paragraph(entry.transactionId.ifBlank { "-" }).setFontSize(9f)))
                table.addCell(Cell().add(Paragraph(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(entry.paymentDate))).setFontSize(9f)))
            }

            document.add(table)

            val total = entries.sumOf { it.amount }
            document.add(
                Paragraph("\nTOTAL RENT PAID: ₹${String.format(Locale.getDefault(), "%,.2f", total)}")
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(12f)
            )

            document.add(
                Paragraph("\n\nNote: This is a computer-generated statement for income tax (HRA) declaration purposes.")
                    .setFontSize(8f)
                    .setItalic()
                    .setFontColor(DeviceRgb(100, 100, 100))
            )
        }
    }

    fun generateMonthlyReceipt(landlord: Landlord, entry: RentEntry): Uri? {
        val monthName = DateFormatSymbols().months[entry.month - 1]
        val calendarYear = FiscalYearHelper.getCalendarYearForMonth(entry.month, entry.year)
        val fileName = "Rent_Receipt_${monthName}_${calendarYear}.pdf"

        return createPdf(fileName) { document ->
            addReceiptContent(document, landlord, entry, calendarYear)
        }
    }

    private fun addReceiptContent(document: Document, landlord: Landlord, entry: RentEntry, calendarYear: Int) {
        val monthName = DateFormatSymbols().months[entry.month - 1]
        val isCashAbove5k = entry.paymentMode == "Cash" && entry.amount > 5000.0
        val forestGreen = DeviceRgb(45, 106, 79) // CalmPrimary

        // Header with revenue stamp placeholder
        val headerTable = Table(UnitValue.createPointArray(floatArrayOf(4f, 1f))).useAllAvailableWidth()
        headerTable.addCell(
            Cell().add(
                Paragraph("RENT RECEIPT")
                    .setFontSize(24f)
                    .setBold()
                    .setFontColor(forestGreen)
            ).setBorder(null).setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
        )
        headerTable.addCell(
            Cell().add(
                Paragraph(if (isCashAbove5k) "Affix ₹1\nRevenue\nStamp" else "Affix \nRevenue \nStamp")
                    .setFontSize(8f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(forestGreen)
                    .also { if (isCashAbove5k) it.setBold() }
            )
                .setHeight(60f)
                .setWidth(60f)
                .setBorder(com.itextpdf.layout.borders.SolidBorder(forestGreen, 1f))
                .setBackgroundColor(DeviceRgb(212, 226, 221)) // CalmBackground
        )
        document.add(headerTable)
        
        // Horizontal line
        document.add(
            Table(UnitValue.createPercentArray(floatArrayOf(1f))).useAllAvailableWidth()
                .addCell(Cell().setBorder(com.itextpdf.layout.borders.SolidBorder(forestGreen, 0.5f)))
        )
        document.add(Paragraph("\n"))

        val formattedAmount = String.format(Locale.getDefault(), "%,.2f", entry.amount)
        val content = Paragraph()
            .add("Received with thanks from ")
            .add(Paragraph(landlord.tenantName).setBold().setFontColor(forestGreen))
            .add(" a sum of ")
            .add(Paragraph("Rs. $formattedAmount").setBold().setFontColor(forestGreen))
            .add(" (Rupees ${AmountToWords.convert(entry.amount.toLong())} only) ")
            .add("towards rent for the period of ")
            .add(Paragraph("$monthName $calendarYear").setBold().setFontColor(forestGreen))
            .add(".")

        document.add(content.setFontSize(12f).setMultipliedLeading(1.6f))
        document.add(Paragraph("\n"))

        // Details table — includes Payment Mode
        val table = Table(UnitValue.createPointArray(floatArrayOf(150f, 350f))).useAllAvailableWidth()

        fun addRow(label: String, value: String) {
            table.addCell(Cell().add(Paragraph(label)).setBold().setFontColor(forestGreen).setBorder(null))
            table.addCell(Cell().add(Paragraph(": $value")).setBorder(null))
        }

        addRow("Landlord Name", landlord.name)
        addRow("Landlord Address", landlord.landlordAddress)
        addRow("Property Address", landlord.tenantAddress)
        addRow("Landlord PAN", landlord.panNumber)
        addRow("Payment Mode", entry.paymentMode)
        addRow("Payment Date", SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(entry.paymentDate)))
        addRow("Transaction ID", entry.transactionId.ifBlank { "N/A" })

        document.add(table)
        document.add(Paragraph("\n\n\n"))

        val footerTable = Table(UnitValue.createPointArray(floatArrayOf(1f, 1f))).useAllAvailableWidth()
        footerTable.addCell(
            Cell().add(Paragraph("Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}")).setBorder(null)
        )
        footerTable.addCell(
            Cell().add(
                Paragraph("__________________________\n(Signature of Landlord)")
                    .setTextAlignment(TextAlignment.RIGHT)
            ).setBorder(null)
        )
        document.add(footerTable)

        // Revenue stamp notice if cash > 5000
        if (isCashAbove5k) {
            document.add(
                Paragraph("\n⚠ This receipt involves a cash payment exceeding ₹5,000. Please affix a ₹1 revenue stamp and sign across it on the physical copy.")
                    .setFontSize(9f)
                    .setBold()
                    .setFontColor(DeviceRgb(180, 80, 0))
            )
        }

        document.add(
            Paragraph("\nNote: If total rent paid exceeds ₹1,00,000 per annum, Landlord's PAN is mandatory for HRA exemption.")
                .setFontSize(9f)
                .setFontColor(DeviceRgb(100, 100, 100))
        )
    }

    private fun createPdf(fileName: String, block: (Document) -> Unit): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/RentLog")
            }
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues) ?: return null

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                val writer = PdfWriter(outputStream)
                val pdf = PdfDocument(writer)
                val document = Document(pdf)
                block(document)
                document.close()
            }
            uri
        } catch (e: Exception) {
            // Clean up failed entry
            try { resolver.delete(uri, null, null) } catch (_: Exception) {}
            null
        }
    }
}
