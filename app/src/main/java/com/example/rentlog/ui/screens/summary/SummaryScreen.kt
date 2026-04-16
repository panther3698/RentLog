package com.example.rentlog.ui.screens.summary

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rentlog.ui.util.FiscalYearHelper
import java.text.DateFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val exportUri by viewModel.exportUri.collectAsState()
    val shouldShare by viewModel.shouldShare.collectAsState()
    val context = LocalContext.current

    var showMonthPicker by remember { mutableStateOf(false) }
    var showQuarterPicker by remember { mutableStateOf(false) }
    var pendingShareByChoice by remember { mutableStateOf(false) }

    LaunchedEffect(exportUri) {
        exportUri?.let { uri ->
            if (shouldShare) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Report via..."))
            } else {
                Toast.makeText(context, "Report saved to Documents/RentLog", Toast.LENGTH_LONG).show()
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                try {
                    context.startActivity(Intent.createChooser(intent, "Open PDF"))
                } catch (e: Exception) {
                    Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.resetExportUri()
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { month ->
                viewModel.exportReport(ExportType.MONTHLY, month, pendingShareByChoice)
                showMonthPicker = false
            }
        )
    }

    if (showQuarterPicker) {
        QuarterPickerDialog(
            onDismiss = { showQuarterPicker = false },
            onQuarterSelected = { quarter ->
                viewModel.exportReport(ExportType.QUARTERLY, quarter, pendingShareByChoice)
                showQuarterPicker = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Tax Reports", 
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(start = 8.dp).size(40.dp).shadow(4.dp, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            item {
                SummaryHeader(totalPaid = state.totalPaid, label = state.fiscalLabel)
            }

            item {
                Text(
                    text = "Generate Reports",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ReportOptionCard(
                        title = "Annual Report (Full FY)",
                        subtitle = "Complete log for ${state.fiscalLabel}",
                        icon = Icons.Outlined.Summarize,
                        onDownload = { viewModel.exportReport(ExportType.ANNUAL, null, false) },
                        onShare = { viewModel.exportReport(ExportType.ANNUAL, null, true) },
                        isLoading = state.isLoading
                    )
                    
                    ReportOptionCard(
                        title = "Quarterly Report",
                        subtitle = "Select a specific quarter",
                        icon = Icons.Outlined.Summarize,
                        onDownload = { 
                            pendingShareByChoice = false
                            showQuarterPicker = true 
                        },
                        onShare = { 
                            pendingShareByChoice = true
                            showQuarterPicker = true 
                        },
                        isLoading = state.isLoading
                    )

                    ReportOptionCard(
                        title = "Monthly Receipt",
                        subtitle = "Select a specific month",
                        icon = Icons.Outlined.Summarize,
                        onDownload = { 
                            pendingShareByChoice = false
                            showMonthPicker = true 
                        },
                        onShare = { 
                            pendingShareByChoice = true
                            showMonthPicker = true 
                        },
                        isLoading = state.isLoading
                    )
                }
            }

            if (state.missingMonthsNames.isNotEmpty()) {
                item {
                    Text(
                        text = "Payment Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PriorityHigh, 
                                        contentDescription = null, 
                                        modifier = Modifier.padding(4.dp).size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Missing Months (${state.missingMonthsCount})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = state.missingMonthsNames.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthPickerDialog(onDismiss: () -> Unit, onMonthSelected: (Int) -> Unit) {
    val months = FiscalYearHelper.getFiscalMonths()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Month") },
        text = {
            LazyColumn {
                items(months) { month ->
                    val monthName = DateFormatSymbols().months[month - 1]
                    Text(
                        text = monthName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMonthSelected(month) }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun QuarterPickerDialog(onDismiss: () -> Unit, onQuarterSelected: (Int) -> Unit) {
    val quarters = listOf(
        "Q1 (Apr - Jun)" to 1,
        "Q2 (Jul - Sep)" to 2,
        "Q3 (Oct - Dec)" to 3,
        "Q4 (Jan - Mar)" to 4
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Quarter") },
        text = {
            Column {
                quarters.forEach { (label, value) ->
                    Text(
                        text = label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQuarterSelected(value) }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ReportOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
            } else {
                IconButton(
                    onClick = onDownload,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Download")
                }
                IconButton(
                    onClick = onShare,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
        }
    }
}

@Composable
fun SummaryHeader(totalPaid: Double, label: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Total Rent Paid ($label)", 
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "₹${String.format(Locale.getDefault(), "%,.0f", totalPaid)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary,
                letterSpacing = (-1).sp
            )
        }
    }
}
