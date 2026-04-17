package com.example.rentlog.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentlog.domain.model.RentEntry
import com.example.rentlog.ui.theme.Elevation
import com.example.rentlog.ui.theme.Radius
import com.example.rentlog.ui.theme.Spacing
import com.example.rentlog.ui.theme.extendedColors
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonthCard(
    modifier: Modifier = Modifier,
    month: Int,
    entry: RentEntry?
) {
    val monthName = DateFormatSymbols().months[month - 1]
    val isPaid = entry != null
    val successColor = MaterialTheme.extendedColors.success

    val statusColor = if (isPaid) successColor else MaterialTheme.colorScheme.primary
    val cardBg = if (isPaid) statusColor.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(185.dp)
            .shadow(
                elevation = if (isPaid) Elevation.high else Elevation.low,
                shape = Radius.xxl,
                spotColor = statusColor.copy(alpha = 0.3f)
            ),
        shape = Radius.xxl,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            1.5.dp,
            if (isPaid) statusColor.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )

                if (isPaid) {
                    Column {
                        Text(
                            text = "₹${entry.amount.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                text = "PAID ${SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(Date(entry.paymentDate))}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                Radius.md
                            )
                            .padding(vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(Spacing.xs))
                        Text(
                            "LOG RENT",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (isPaid) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.md)
                        .size(32.dp),
                    color = statusColor,
                    shape = CircleShape,
                    shadowElevation = Elevation.medium
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Paid",
                        modifier = Modifier.padding(Spacing.sm),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
