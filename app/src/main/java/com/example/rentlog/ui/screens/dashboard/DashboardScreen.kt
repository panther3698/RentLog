package com.example.rentlog.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rentlog.domain.model.RentEntry
import com.example.rentlog.ui.util.FiscalYearHelper
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    onMonthClick: (Int) -> Unit,
    onSummaryClick: () -> Unit,
    onSettingsClick: () -> Unit,
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
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    FiscalYearHelper.getFiscalYearLabel(year),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showYearSelector = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp),
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
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rent Log",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1.5).sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                onClick = { showYearSelector = true },
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
                            Spacer(Modifier.width(8.dp))
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
                        modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            }

            // High-End Reports CTA
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSummaryClick() }
                        .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Background Accent Gradient
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
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(18.dp),
                                shadowElevation = 12.dp
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(14.dp).size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Tax Reports & PDFs",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    "Generate HRA receipts & logs",
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

            // Grid Title
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Track Payments",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                )
            }

            items(fiscalMonths) { month ->
                val entry = entries.find { it.month == month }
                val calendarYear = FiscalYearHelper.getCalendarYearForMonth(month, fiscalStartYear)
                
                var isPressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                    label = "scale"
                )

                PremiumMonthCard(
                    modifier = Modifier
                        .scale(scale)
                        .clickable { onMonthClick(month) },
                    month = month,
                    year = calendarYear,
                    entry = entry
                )
            }
        }
    }
}

@Composable
fun PremiumMonthCard(
    modifier: Modifier = Modifier,
    month: Int,
    year: Int,
    entry: RentEntry?
) {
    val monthName = DateFormatSymbols().months[month - 1]
    val isPaid = entry != null
    
    // Vibrant Professional Colors
    val statusColor = if (isPaid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
    val cardBg = if (isPaid) statusColor.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(185.dp)
            .shadow(
                elevation = if (isPaid) 16.dp else 4.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = statusColor.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            1.5.dp, 
            if (isPaid) statusColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                }

                if (isPaid) {
                    Column {
                        Text(
                            text = "₹${entry.amount.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            fontSize = 22.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "PAID ON ${SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(Date(entry.paymentDate))}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor.copy(alpha = 0.7f),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                } else {
                    // Empty state indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "LOG RENT", 
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // High-visibility checkmark
            if (isPaid) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(32.dp),
                    color = statusColor,
                    shape = CircleShape,
                    shadowElevation = 8.dp
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
