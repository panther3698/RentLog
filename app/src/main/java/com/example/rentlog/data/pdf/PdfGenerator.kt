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
import java.io.OutputStream
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
            document.add(Paragraph("RENT PAYMENT LOG")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold())
            
            document.add(Paragraph(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14f))

            fiscalStartYear?.let { startYear ->
                val fyLabel = FiscalYearHelper.getFiscalYearLabel(startYear)
                val ayLabel = "AY ${startYear + 1}-${(startYear + 2) % 100}"
                document.add(Paragraph("$fyLabel | $ayLabel")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10f)
                    .setItalic())
            }
            
            document.add(Paragraph("\n"))
            
            // Tax Compliance Header
            val infoTable = Table(UnitValue.createPointArray(floatArrayOf(1f, 1f))).useAllAvailableWidth()
            infoTable.addCell(Cell().add(Paragraph("TENANT DETAILS").setBold().setFontSize(10f)).setBorder(null))
            infoTable.addCell(Cell().add(Paragraph("LANDLORD DETAILS").setBold().setFontSize(10f)).setBorder(null))
            
            infoTable.addCell(Cell().add(Paragraph("Name: ${landlord.tenantName}\nAddress: ${landlord.tenantAddress}")).setBorder(null))
            infoTable.addCell(Cell().add(Paragraph("Name: ${landlord.name}\nAddress: ${landlord.landlordAddress}")).setBorder(null))
            
            infoTable.addCell(Cell().add(Paragraph("")).setBorder(null))
            infoTable.addCell(Cell().add(Paragraph("PAN: ${landlord.panNumber}")).setBorder(null))
            
            document.add(infoTable)
            document.add(Paragraph("\n"))

            val table = Table(UnitValue.createPointArray(floatArrayOf(100f, 80f, 120f, 150f, 100f)))
                .useAllAvailableWidth()

            table.addHeaderCell(Cell().add(Paragraph("Month").setBold().setFontSize(10f)))
            table.addHeaderCell(Cell().add(Paragraph("Year").setBold().setFontSize(10f)))
            table.addHeaderCell(Cell().add(Paragraph("Amount (INR)").setBold().setFontSize(10f)))
            table.addHeaderCell(Cell().add(Paragraph("Transaction ID").setBold().setFontSize(10f)))
            table.addHeaderCell(Cell().add(Paragraph("Payment Date").setBold().setFontSize(10f)))

            entries.sortedWith(compareBy({ if (it.month >= 4) it.year else it.year + 1 }, { if (it.month >= 4) it.month else it.month + 12 }))
                .forEach { entry ->
                    val monthName = DateFormatSymbols().months[entry.month - 1]
                    val calendarYear = FiscalYearHelper.getCalendarYearForMonth(entry.month, entry.year)
                    table.addCell(Cell().add(Paragraph(monthName).setFontSize(10f)))
                    table.addCell(Cell().add(Paragraph("$calendarYear").setFontSize(10f)))
                    table.addCell(Cell().add(Paragraph("₹${String.format(Locale.getDefault(), "%,.2f", entry.amount)}").setFontSize(10f)))
                    table.addCell(Cell().add(Paragraph(entry.transactionId.ifBlank { "-" }).setFontSize(10f)))
                    table.addCell(Cell().add(Paragraph(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(entry.paymentDate))).setFontSize(10f)))
                }

            document.add(table)
            
            val total = entries.sumOf { it.amount }
            document.add(Paragraph("\nTOTAL RENT PAID: ₹${String.format(Locale.getDefault(), "%,.2f", total)}")
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(12f))

            document.add(Paragraph("\n\nNote: This is a computer-generated statement for income tax (HRA) declaration purposes.")
                .setFontSize(8f)
                .setItalic()
                .setFontColor(DeviceRgb(100, 100, 100)))
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
        
        // Revenue Stamp Box Placeholder
        val headerTable = Table(UnitValue.createPointArray(floatArrayOf(4f, 1f))).useAllAvailableWidth()
        headerTable.addCell(Cell().add(Paragraph("RENT RECEIPT")
            .setFontSize(22f)
            .setBold()
            .setFontColor(DeviceRgb(0, 0, 0))).setBorder(null).setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE))
        
        headerTable.addCell(Cell().add(Paragraph("Affix \nRevenue \nStamp")
            .setFontSize(8f)
            .setTextAlignment(TextAlignment.CENTER))
            .setHeight(50f)
            .setWidth(50f)
            .setBorder(com.itextpdf.layout.borders.DashedBorder(0.5f)))
        
        document.add(headerTable)
        document.add(Paragraph("\n"))

        // Content following standard IT format
        val formattedAmount = String.format(Locale.getDefault(), "%,.2f", entry.amount)
        val content = Paragraph().add("Received with thanks from ").add(Paragraph(landlord.tenantName).setBold())
            .add(" a sum of ").add(Paragraph("Rs. $formattedAmount").setBold())
            .add(" (Rupees ${AmountToWords.convert(entry.amount.toLong())} only) ")
            .add("towards rent for the period of ").add(Paragraph("$monthName $calendarYear").setBold())
            .add(".")
        
        document.add(content.setFontSize(12f).setMultipliedLeading(1.5f))
        document.add(Paragraph("\n"))

        // Details Table
        val table = Table(UnitValue.createPointArray(floatArrayOf(150f, 350f)))
            .useAllAvailableWidth()

        table.addCell(Cell().add(Paragraph("Landlord Name")).setBold().setBorder(null))
        table.addCell(Cell().add(Paragraph(": ${landlord.name}")).setBorder(null))

        table.addCell(Cell().add(Paragraph("Landlord Address")).setBold().setBorder(null))
        table.addCell(Cell().add(Paragraph(": ${landlord.landlordAddress}")).setBorder(null))

        table.addCell(Cell().add(Paragraph("Property Address")).setBold().setBorder(null))
        table.addCell(Cell().add(Paragraph(": ${landlord.tenantAddress}")).setBorder(null))
        
        table.addCell(Cell().add(Paragraph("Landlord PAN")).setBold().setBorder(null))
        table.addCell(Cell().add(Paragraph(": ${landlord.panNumber}")).setBorder(null))
        
        table.addCell(Cell().add(Paragraph("Payment Date")).setBold().setBorder(null))
        table.addCell(Cell().add(Paragraph(": ${SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(entry.paymentDate))}")).setBorder(null))
        
        table.addCell(Cell().add(Paragraph("Transaction ID")).setBold().setBorder(null))
        table.addCell(Cell().add(Paragraph(": ${entry.transactionId.ifBlank { "N/A" }}")).setBorder(null))

        document.add(table)

        document.add(Paragraph("\n\n\n"))
        
        val footerTable = Table(UnitValue.createPointArray(floatArrayOf(1f, 1f))).useAllAvailableWidth()
        footerTable.addCell(Cell().add(Paragraph("Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}")).setBorder(null))
        footerTable.addCell(Cell().add(Paragraph("__________________________\n(Signature of Landlord)")
            .setTextAlignment(TextAlignment.RIGHT)).setBorder(null))
        
        document.add(footerTable)
        
        document.add(Paragraph("\n\nNote: If rent paid exceeds Rs. 1,00,000 per annum, Landlord's PAN is mandatory.")
            .setFontSize(9f)
            .setFontColor(DeviceRgb(100, 100, 100)))
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

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        
        return uri?.also {
            resolver.openOutputStream(it)?.use { outputStream ->
                val writer = PdfWriter(outputStream)
                val pdf = PdfDocument(writer)
                val document = Document(pdf)
                block(document)
                document.close()
            }
        }
    }
}
