package com.example.rentlog.ui.screens.summary

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rentlog.ui.util.FiscalYearHelper
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.google.android.play.core.review.ReviewManagerFactory
import android.app.Activity

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
    val haptic = LocalHapticFeedback.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }

    var showMonthPicker by remember { mutableStateOf(false) }
    var showQuarterPicker by remember { mutableStateOf(false) }
    var pendingShareByChoice by remember { mutableStateOf(false) }
    var previewData by remember { mutableStateOf<PreviewData?>(null) }
    var pendingExportType by remember { mutableStateOf<ExportType?>(null) }
    var pendingSelection by remember { mutableStateOf<Int?>(null) }

    // Show snackbar on error
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }

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
            
            // Subtly prompt for app review after successful PDF generation
            activity?.let { act ->
                val reviewManager = ReviewManagerFactory.create(act)
                val request = reviewManager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reviewManager.launchReviewFlow(act, task.result)
                    }
                }
            }
            
            viewModel.resetExportUri()
        }
    }

    // After month/quarter is picked, show preview instead of directly exporting
    fun initiatePreview(type: ExportType, selection: Int?) {
        val preview = viewModel.preparePreview(type, selection)
        if (preview != null && preview.entries.isNotEmpty()) {
            previewData = preview
            pendingExportType = type
            pendingSelection = selection
        } else if (preview != null && preview.entries.isEmpty() && type != ExportType.ANNUAL) {
            // No entries — show error via normal flow
            viewModel.exportReport(type, selection, pendingShareByChoice)
        } else if (preview == null) {
            viewModel.exportReport(type, selection, pendingShareByChoice)
        } else {
            // Annual with no entries — still show preview
            previewData = preview
            pendingExportType = type
            pendingSelection = selection
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { month ->
                showMonthPicker = false
                initiatePreview(ExportType.MONTHLY, month)
            }
        )
    }

    if (showQuarterPicker) {
        QuarterPickerDialog(
            onDismiss = { showQuarterPicker = false },
            onQuarterSelected = { quarter ->
                showQuarterPicker = false
                initiatePreview(ExportType.QUARTERLY, quarter)
            }
        )
    }

    // Receipt Preview Dialog
    previewData?.let { preview ->
        ReceiptPreviewDialog(
            previewData = preview,
            onDismiss = { previewData = null },
            onDownload = {
                previewData = null
                viewModel.exportReport(pendingExportType ?: preview.type, pendingSelection ?: preview.selection, false)
            },
            onShare = {
                previewData = null
                viewModel.exportReport(pendingExportType ?: preview.type, pendingSelection ?: preview.selection, true)
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Tax Reports", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateBack()
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(start = 8.dp).size(40.dp).shadow(4.dp, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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

            // HRA Tax Saving Estimate
            item {
                HraSavingCard(totalPaid = state.totalPaid)
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
                        onDownload = {
                            pendingShareByChoice = false
                            initiatePreview(ExportType.ANNUAL, null)
                        },
                        onShare = {
                            pendingShareByChoice = true
                            initiatePreview(ExportType.ANNUAL, null)
                        },
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

// ================================================
// Receipt Preview Dialog
// ================================================

@Composable
fun ReceiptPreviewDialog(
    previewData: PreviewData,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "PREVIEW",
                            style = MaterialTheme.typography.labelLarge,
                            letterSpacing = 3.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                        Text(
                            previewData.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            previewData.fiscalLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                        )
                    }
                }

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tenant & Landlord details
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "TENANT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        previewData.landlord.tenantName.ifBlank { "—" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (previewData.landlord.tenantAddress.isNotBlank()) {
                                        Text(
                                            previewData.landlord.tenantAddress,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "LANDLORD",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        previewData.landlord.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "PAN: ${previewData.landlord.panNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    // Entries table
                    if (previewData.entries.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Table header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                ) {
                                    Text("Month", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("Amount", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End)
                                    Text("Mode", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End)
                                    Text("Date", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                                previewData.entries.forEach { entry ->
                                    val monthName = DateFormatSymbols().months[entry.month - 1]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(monthName, modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                        Text("₹${entry.amount.toInt()}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                                        Text(entry.paymentMode, modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.End)
                                        Text(
                                            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(entry.paymentDate)),
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 1.5.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "TOTAL",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "₹${String.format(Locale.getDefault(), "%,.0f", previewData.totalAmount)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                        ) {
                            Text(
                                "No entries found for this period.",
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Note
                    Text(
                        "Note: This is a computer-generated statement for income tax (HRA) declaration purposes.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        lineHeight = 16.sp
                    )
                }

                // Action buttons
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onDownload,
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = previewData.entries.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onShare,
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            enabled = previewData.entries.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ================================================
// Existing components
// ================================================

@Composable
fun HraSavingCard(totalPaid: Double) {
    if (totalPaid <= 0.0) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "HRA Tax Exemption",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Total paid: ₹${String.format(Locale.getDefault(), "%,.0f", totalPaid)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Claim exemption under Sec 10(13A).",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(14.dp), tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
            } else {
                IconButton(
                    onClick = onDownload, 
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onShare,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        contentColor = Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
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
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Total Rent Paid ($label)",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "₹${String.format(Locale.getDefault(), "%,.0f", totalPaid)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary,
                letterSpacing = (-1.5).sp
            )
        }
    }
}
