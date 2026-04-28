package com.devchiradhi.rentlog.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.devchiradhi.rentlog.domain.model.RentEntry
import com.devchiradhi.rentlog.ui.theme.Elevation
import com.devchiradhi.rentlog.ui.theme.Radius
import com.devchiradhi.rentlog.ui.theme.Spacing
import com.devchiradhi.rentlog.ui.theme.extendedColors
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

private fun derivePaymentMode(txnId: String): String = when {
    txnId.startsWith("UPI",    ignoreCase = true) -> "UPI"
    txnId.startsWith("NEFT",   ignoreCase = true) -> "NEFT"
    txnId.startsWith("IMPS",   ignoreCase = true) -> "IMPS"
    txnId.startsWith("RTGS",   ignoreCase = true) -> "RTGS"
    txnId.startsWith("CHEQUE", ignoreCase = true) -> "Cheque"
    txnId.isBlank()                               -> "Cash"
    else                                           -> "Transfer"
}

@Composable
fun MonthCard(
    modifier: Modifier = Modifier,
    month: Int,
    entry: RentEntry?,
    onDeleteClick: (() -> Unit)? = null
) {
    val monthName = DateFormatSymbols().months[month - 1]
    val isPaid = entry != null
    val successColor = MaterialTheme.extendedColors.success

    val statusColor = if (isPaid) successColor else MaterialTheme.colorScheme.primary
    val cardBg = if (isPaid) statusColor.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .shadow(
                elevation = if (isPaid) Elevation.low else 0.dp,
                shape = Radius.lg,
                spotColor = statusColor.copy(alpha = 0.1f)
            ),
        shape = Radius.lg,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            1.dp,
            if (isPaid) statusColor.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.06f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                verticalArrangement = Arrangement.Center
            ) {
                // ── Month name ──────────────────────────────────────────────
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1
                )

                if (isPaid) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val mode = derivePaymentMode(entry.transactionId)
                            Text(
                                text = mode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor.copy(alpha = 0.7f),
                                fontSize = 8.sp
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor.copy(alpha = 0.3f),
                                fontSize = 8.sp
                            )
                            Text(
                                text = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(entry.paymentDate)),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = statusColor.copy(alpha = 0.5f),
                                fontSize = 8.sp
                            )
                        }

                        // ── Amount ──────────────────────────────────────────
                        Text(
                            text = "₹${entry.amount.toInt()}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor,
                            maxLines = 1
                        )
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                                Radius.sm
                            )
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            "LOG",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            if (isPaid) {
                // ── Paid checkmark ────────────────────────────────
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.xs)
                        .size(16.dp),
                    color = statusColor,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Paid",
                        modifier = Modifier.padding(3.dp),
                        tint = Color.White
                    )
                }

                // ── Delete — bottom end ─────────────────────────────────────
                if (onDeleteClick != null) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(Spacing.xs)
                            .size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete payment",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}
