package com.devchiradhi.rentlog.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.room.Room
import com.devchiradhi.rentlog.MainActivity
import com.devchiradhi.rentlog.data.local.AppDatabase
import com.devchiradhi.rentlog.ui.util.FiscalYearHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RentLogWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetData = withContext(Dispatchers.IO) {
            loadWidgetData(context)
        }

        provideContent {
            RentLogWidgetContent(widgetData)
        }
    }

    private fun loadWidgetData(context: Context): WidgetData {
        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "rent_log_db"
        ).addMigrations(AppDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val fiscalYear = FiscalYearHelper.getCurrentFiscalYear()

        val entries = db.rentEntryDao().getEntriesForYearSync(fiscalYear)
        val currentMonthEntry = entries.find { it.month == currentMonth }
        val totalPaid = entries.sumOf { it.amount }
        val paidCount = entries.size

        db.close()

        val paymentMode = currentMonthEntry?.transactionId?.let { txId ->
            when {
                txId.startsWith("UPI", ignoreCase = true) -> "UPI"
                txId.startsWith("NEFT", ignoreCase = true) -> "NEFT"
                txId.startsWith("IMPS", ignoreCase = true) -> "IMPS"
                txId.startsWith("RTGS", ignoreCase = true) -> "RTGS"
                txId.isBlank() -> "Cash"
                else -> "Online"
            }
        }

        return WidgetData(
            currentMonth = currentMonth,
            monthName = DateFormatSymbols().months[currentMonth - 1],
            year = FiscalYearHelper.getCalendarYearForMonth(currentMonth, fiscalYear),
            isPaid = currentMonthEntry != null,
            amount = currentMonthEntry?.amount,
            paymentMode = paymentMode,
            paymentDate = currentMonthEntry?.paymentDate,
            totalPaid = totalPaid,
            paidCount = paidCount,
            fiscalLabel = FiscalYearHelper.getFiscalYearLabel(fiscalYear)
        )
    }
}

data class WidgetData(
    val currentMonth: Int,
    val monthName: String,
    val year: Int,
    val isPaid: Boolean,
    val amount: Double?,
    val paymentMode: String?,
    val paymentDate: Long?,
    val totalPaid: Double,
    val paidCount: Int,
    val fiscalLabel: String
)

@androidx.compose.runtime.Composable
fun RentLogWidgetContent(data: WidgetData) {
    val bgColor = ColorProvider(day = Color(0xFFF9F9F7), night = Color(0xFF161616))
    val primaryColor = ColorProvider(day = Color(0xFF9E7715), night = Color(0xFFFFD700))
    val textColor = ColorProvider(day = Color(0xFF1C1C1C), night = Color(0xFFE0E0E0))
    val subtextColor = ColorProvider(day = Color(0xFF6B6B6B), night = Color(0xFFB0B0B0))
    val greenColor = ColorProvider(day = Color(0xFF4CAF50), night = Color(0xFF66BB6A))
    val cardColor = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF1E1E1E))
    val warningColor = ColorProvider(day = Color(0xFFE65100), night = Color(0xFFFF9800))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(bgColor)
            .clickable(actionStartActivity<MainActivity>())
            .padding(16.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RentLog",
                    style = TextStyle(
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = data.fiscalLabel,
                    style = TextStyle(
                        color = subtextColor,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(GlanceModifier.height(12.dp))

            // Current month status
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .cornerRadius(16.dp)
                    .background(cardColor)
                    .padding(14.dp)
            ) {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        text = "${data.monthName} ${data.year}",
                        style = TextStyle(
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(GlanceModifier.height(6.dp))

                    if (data.isPaid && data.amount != null) {
                        Text(
                            text = "₹${data.amount.toInt()}",
                            style = TextStyle(
                                color = greenColor,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✓ PAID",
                                style = TextStyle(
                                    color = greenColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            if (data.paymentMode != null) {
                                Text(
                                    text = "  •  ${data.paymentMode}",
                                    style = TextStyle(
                                        color = subtextColor,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            if (data.paymentDate != null) {
                                Text(
                                    text = "  •  ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(data.paymentDate))}",
                                    style = TextStyle(
                                        color = subtextColor,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Not logged yet",
                            style = TextStyle(
                                color = warningColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = "Tap to log rent payment",
                            style = TextStyle(
                                color = subtextColor,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(GlanceModifier.height(8.dp))

            // Footer stats
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${data.paidCount}/12 months paid",
                    style = TextStyle(
                        color = subtextColor,
                        fontSize = 10.sp
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.0f", data.totalPaid)} total",
                    style = TextStyle(
                        color = primaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
