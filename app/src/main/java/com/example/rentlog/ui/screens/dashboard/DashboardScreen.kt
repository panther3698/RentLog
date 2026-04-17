package com.example.rentlog.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rentlog.ui.components.MonthCard
import com.example.rentlog.ui.components.StatCard
import com.example.rentlog.ui.theme.*
import com.example.rentlog.ui.util.FiscalYearHelper
import java.util.*

@Composable
fun DashboardScreen(
    onMonthClick: (Int) -> Unit,
    onSummaryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCalculatorClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val entries by viewModel.rentEntries.collectAsState()
    val fiscalStartYear by viewModel.selectedFiscalYear.collectAsState()
    val fiscalMonths = FiscalYearHelper.getFiscalMonths()

    var showYearSelector by remember { mutableStateOf(false) }

    if (showYearSelector) {
        val currentYear = FiscalYearHelper.getCurrentFiscalYear()
        val years = (currentYear - 3..currentYear + 1).toList().reversed()

        AlertDialog(
            onDismissRequest = { showYearSelector = false },
            title = { Text("Select Financial Year", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    years.forEach { year ->
                        val isSelected = year == fiscalStartYear
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectFiscalYear(year)
                                    showYearSelector = false
                                },
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else Color.Transparent,
                            shape = Radius.md
                        ) {
                            Row(
                                modifier = Modifier.padding(Spacing.md),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    FiscalYearHelper.getFiscalYearLabel(year),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showYearSelector = false }) { Text("Cancel") }
            },
            shape = Radius.xxl,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + Spacing.md,
                bottom = Spacing.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Header
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md, bottom = Spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rent Log",
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                onClick = { showYearSelector = true },
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = Radius.sm
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                                ) {
                                    Text(
                                        text = FiscalYearHelper.getFiscalYearLabel(fiscalStartYear),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                text = "Financial Year",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }
                    IconButton(
                        onClick = onSettingsClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.shadow(Elevation.medium, Radius.md)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            }

            // Reports CTA Card
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSummaryClick() }
                        .shadow(
                            Elevation.premium,
                            Radius.xl,
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                    shape = Radius.xl,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.lg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = Radius.lg,
                                shadowElevation = Elevation.medium
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(Spacing.lg))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Tax Reports & PDFs",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Create HRA receipts for tax filing",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // HRA Calculator card
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCalculatorClick() },
                    shape = Radius.xl,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = Radius.lg
                        ) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "HRA Calculator",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Section 10(13A) — how much you save",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Stats Row
            item(span = { GridItemSpan(2) }) {
                val paidCount = fiscalMonths.count { month -> entries.any { it.month == month } }
                val totalPaid = entries.sumOf { it.amount }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Paid",
                        value = "$paidCount / 12 months"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Total This FY",
                        value = "₹${String.format(Locale.getDefault(), "%,.0f", totalPaid)}"
                    )
                }
            }

            // Section header + first-use hint
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier.padding(
                        top = Spacing.sm,
                        start = Spacing.xs
                    )
                ) {
                    Text(
                        text = "Monthly Payments",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    if (entries.isEmpty()) {
                        Text(
                            text = "Tap any month to record a payment",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = Spacing.xs)
                        )
                    }
                }
            }

            // Month grid or empty state
            if (entries.isEmpty() && fiscalStartYear > FiscalYearHelper.getCurrentFiscalYear()) {
                item(span = { GridItemSpan(2) }) {
                    FutureYearEmptyState(fiscalLabel = FiscalYearHelper.getFiscalYearLabel(fiscalStartYear))
                }
            } else {
                items(fiscalMonths) { month ->
                    val entry = entries.find { it.month == month }
                    var isPressed by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                        label = "scale"
                    )

                    MonthCard(
                        modifier = Modifier
                            .scale(scale)
                            .clickable { onMonthClick(month) },
                        month = month,
                        entry = entry
                    )
                }
            }
        }
    }
}

@Composable
private fun FutureYearEmptyState(fiscalLabel: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            shape = Radius.xxl
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.padding(Spacing.lg).size(40.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
        Spacer(Modifier.height(Spacing.md))
        Text(
            text = "No payments yet for $fiscalLabel",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = "Tap any month above to start recording",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
            textAlign = TextAlign.Center
        )
    }
}
