package com.devchiradhi.rentlog.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.devchiradhi.rentlog.ui.components.MonthCard
import com.devchiradhi.rentlog.ui.components.StatCard
import com.devchiradhi.rentlog.ui.theme.*
import com.devchiradhi.rentlog.ui.util.FiscalYearHelper
import com.devchiradhi.rentlog.ui.util.TrialStatus
import java.util.*

@Composable
fun DashboardScreen(
    onMonthClick: (month: Int, fiscalYear: Int) -> Unit,
    onSummaryClick: (fiscalYear: Int) -> Unit,
    onSettingsClick: () -> Unit,
    onCalculatorClick: () -> Unit,
    onGoPremium: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val entries by viewModel.rentEntries.collectAsState()
    val fiscalStartYear by viewModel.selectedFiscalYear.collectAsState()
    val activeLandlord by viewModel.activeLandlord.collectAsState()
    val trialStatus by viewModel.trialStatus.collectAsState()
    val fiscalMonths = FiscalYearHelper.getFiscalMonths()
    val scrollState = rememberScrollState()

    var showYearSelector by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<com.devchiradhi.rentlog.domain.model.RentEntry?>(null) }

    // Delete confirmation dialog
    entryToDelete?.let { entry ->
        val monthName = java.text.DateFormatSymbols().months[entry.month - 1]
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete payment?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Remove the ₹${entry.amount.toInt()} payment for $monthName? This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry)
                    entryToDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) { Text("Cancel") }
            },
            shape = Radius.xl,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = Spacing.md)
                .verticalScroll(scrollState)
        ) {

            // ── Fixed header section ──────────────────────────────────────────

            Spacer(Modifier.height(Spacing.md))

            // Title row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md, bottom = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rent Log",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = { showYearSelector = true },
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            shape = Radius.sm
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = FiscalYearHelper.getFiscalYearLabel(fiscalStartYear),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = "Financial Year",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(28.dp))
                }
            }

            // ── Trial status badge ────────────────────────────────────────────
            when (val status = trialStatus) {
                is TrialStatus.InTrial -> {
                    val isLastDays = status.daysRemaining <= 3
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = Radius.md,
                        color = if (isLastDays)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.md, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isLastDays)
                                    "⚡ Trial ends in ${status.daysRemaining} days"
                                else
                                    "🎉 Free trial — ${status.daysRemaining} days left",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isLastDays)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                            TextButton(
                                onClick = onGoPremium,
                                contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = 0.dp)
                            ) {
                                Text(
                                    "Upgrade",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (isLastDays)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.xs))
                }
                is TrialStatus.Expired -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = Radius.lg,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trial ended — Features locked",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            TextButton(
                                onClick = onGoPremium,
                                contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = 0.dp)
                            ) {
                                Text(
                                    "GO PREMIUM",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.xs))
                }
                is TrialStatus.Premium -> { /* silent — no badge needed */ }
            }

            // Tax Reports CTA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSummaryClick(fiscalStartYear) }
                    .shadow(
                        Elevation.medium,
                        Radius.lg,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                shape = Radius.lg,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = Radius.md,
                            shadowElevation = Elevation.low
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Tax Reports & PDFs",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Create HRA receipts",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // HRA Calculator card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCalculatorClick() },
                shape = Radius.lg,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = Radius.md
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "HRA Calculator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // Stats row
            val paidCount = fiscalMonths.count { month -> entries.any { it.month == month } }
            val totalPaid = entries.sumOf { it.amount }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                StatCard(
                    modifier = Modifier.weight(0.4f),
                    label = "Paid",
                    value = "$paidCount / 12"
                )
                StatCard(
                    modifier = Modifier.weight(0.6f),
                    label = "Total This FY",
                    value = "₹${String.format(Locale.getDefault(), "%,.0f", totalPaid)}"
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            // Section label
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = Spacing.xs, bottom = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Payments",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    if (entries.isEmpty()) {
                        Text(
                            text = "Tap any month to record a payment",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = Spacing.xs)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xs))

            // ── Month grid (non-lazy to allow parent scrolling) ─────────────────

            if (activeLandlord == null) {
                NoProfileEmptyState(onSetupClick = onSettingsClick)
            } else if (entries.isEmpty() && fiscalStartYear > FiscalYearHelper.getCurrentFiscalYear()) {
                FutureYearEmptyState(fiscalLabel = FiscalYearHelper.getFiscalYearLabel(fiscalStartYear))
            } else if (entries.isEmpty()) {
                EmptyDashboardState(onMonthClick = { onMonthClick(FiscalYearHelper.getFiscalMonths().first(), fiscalStartYear) })
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    val rows = fiscalMonths.chunked(2)
                    rows.forEach { rowMonths ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            rowMonths.forEach { month ->
                                val entry = entries.find { it.month == month }
                                var isPressed by remember { mutableStateOf(false) }
                                val scale by animateFloatAsState(
                                    targetValue = if (isPressed) 0.94f else 1f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                                    label = "scale"
                                )

                                MonthCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .scale(scale)
                                        .clickable { onMonthClick(month, fiscalStartYear) },
                                    month = month,
                                    entry = entry,
                                    onDeleteClick = if (entry != null) {
                                        { entryToDelete = entry }
                                    } else null
                                )
                            }
                            // If row has only 1 item, add a spacer to maintain alignment
                            if (rowMonths.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun EmptyDashboardState(onMonthClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            shape = Radius.xxl
        ) {
            Icon(
                Icons.Default.PostAdd,
                contentDescription = null,
                modifier = Modifier.padding(Spacing.lg).size(40.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        Spacer(Modifier.height(Spacing.md))
        Text(
            text = "No payments recorded",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = "Your dashboard is empty. Start by logging your first rent payment.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.xl)
        )
        Spacer(Modifier.height(Spacing.lg))
        Button(
            onClick = onMonthClick,
            shape = Radius.lg,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = Elevation.low)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text("Log First Payment", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NoProfileEmptyState(onSetupClick: () -> Unit) {
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
                Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.padding(Spacing.lg).size(40.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        Spacer(Modifier.height(Spacing.md))
        Text(
            text = "Set up your profile first",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = "Add your landlord details to start tracking rent payments",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.lg)
        )
        Spacer(Modifier.height(Spacing.lg))
        Button(
            onClick = onSetupClick,
            shape = Radius.lg
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(Spacing.sm))
            Text("Set Up Profile", fontWeight = FontWeight.Bold)
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
