package com.devchiradhi.rentlog.ui.screens.summary

import android.app.Activity
import android.content.ClipData
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
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.devchiradhi.rentlog.ui.components.AppBackButton
import com.devchiradhi.rentlog.ui.components.ReportCard
import com.devchiradhi.rentlog.ui.theme.Elevation
import com.devchiradhi.rentlog.ui.theme.Radius
import com.devchiradhi.rentlog.ui.theme.Spacing
import com.devchiradhi.rentlog.ui.util.FiscalYearHelper
import com.google.android.play.core.review.ReviewManagerFactory
import java.text.DateFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    onNavigateBack: () -> Unit,
    onGoPremium: () -> Unit = {},
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val exportUri by viewModel.exportUri.collectAsState()
    val exportFileName by viewModel.exportFileName.collectAsState()
    val shouldShare by viewModel.shouldShare.collectAsState()
    val hasFullAccess by viewModel.hasFullAccess.collectAsState()

    var showTrialExpiredSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity

    var showMonthPicker by remember { mutableStateOf(false) }
    var showQuarterPicker by remember { mutableStateOf(false) }
    var pendingShareByChoice by remember { mutableStateOf(false) }

    LaunchedEffect(exportUri) {
        exportUri?.let { uri ->
            if (shouldShare) {
                val name = exportFileName ?: "RentLog_Report.pdf"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, name.removeSuffix(".pdf").replace("_", " "))
                    // ClipData ensures receiving apps see the correct display name
                    clipData = ClipData.newUri(context.contentResolver, name, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Report via..."))
            } else {
                Toast.makeText(context, "Saved to Documents/RentLog", Toast.LENGTH_LONG).show()
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                try {
                    context.startActivity(Intent.createChooser(intent, "Open PDF"))
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "No PDF app found. Install a PDF viewer to open this file.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            viewModel.resetExportUri()

            // Trigger in-app review after a successful PDF export
            activity?.let { act ->
                try {
                    val reviewManager = ReviewManagerFactory.create(act)
                    reviewManager.requestReviewFlow().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            reviewManager.launchReviewFlow(act, task.result)
                        }
                        // Failures are silent — Google throttles review prompts anyway
                    }
                } catch (_: Exception) { /* review not available */ }
            }
        }
    }

    // Show error messages as a snackbar/toast
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    if (showTrialExpiredSheet) {
        com.devchiradhi.rentlog.ui.components.TrialExpiredSheet(
            onDismiss = { showTrialExpiredSheet = false },
            onGoPremium = onGoPremium
        )
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
                    Box(modifier = Modifier.padding(start = Spacing.md)) {
                        AppBackButton(onClick = onNavigateBack)
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
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            contentPadding = PaddingValues(top = Spacing.md, bottom = Spacing.xxl + 40.dp)
        ) {
            item {
                SummaryHeader(totalPaid = state.totalPaid, label = state.fiscalLabel)
            }

            item {
                Text(
                    text = "Download Reports",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = Spacing.xs)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    ReportCard(
                        title = "Annual Report (Full FY)",
                        subtitle = "All 12 months for ${state.fiscalLabel}",
                        icon = Icons.Outlined.Summarize,
                        onDownload = {
                            if (hasFullAccess) viewModel.exportReport(ExportType.ANNUAL, null, false)
                            else showTrialExpiredSheet = true
                        },
                        onShare = {
                            if (hasFullAccess) viewModel.exportReport(ExportType.ANNUAL, null, true)
                            else showTrialExpiredSheet = true
                        },
                        isLoading = state.isLoading
                    )

                    ReportCard(
                        title = "Quarterly Report",
                        subtitle = "Choose a quarter to download",
                        icon = Icons.Outlined.Summarize,
                        onDownload = {
                            if (!hasFullAccess) { showTrialExpiredSheet = true; return@ReportCard }
                            pendingShareByChoice = false
                            showQuarterPicker = true
                        },
                        onShare = {
                            if (!hasFullAccess) { showTrialExpiredSheet = true; return@ReportCard }
                            pendingShareByChoice = true
                            showQuarterPicker = true
                        },
                        isLoading = state.isLoading
                    )

                    ReportCard(
                        title = "Monthly Receipt",
                        subtitle = "Choose a month to download",
                        icon = Icons.Outlined.Summarize,
                        onDownload = {
                            if (!hasFullAccess) { showTrialExpiredSheet = true; return@ReportCard }
                            pendingShareByChoice = false
                            showMonthPicker = true
                        },
                        onShare = {
                            if (!hasFullAccess) { showTrialExpiredSheet = true; return@ReportCard }
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
                        modifier = Modifier.padding(top = Spacing.sm, start = Spacing.xs)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = Radius.xl,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(Spacing.lg)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    shape = Radius.sm
                                ) {
                                    Icon(
                                        Icons.Default.PriorityHigh,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(Spacing.xs)
                                            .size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(Modifier.width(Spacing.sm2))
                                Text(
                                    text = "Missing Months (${state.missingMonthsCount})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(Modifier.height(Spacing.sm2))
                            Text(
                                text = state.missingMonthsNames.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                lineHeight = 20.sp
                            )
                            Spacer(Modifier.height(Spacing.sm))
                            Text(
                                text = "Missing months won't appear in your annual report.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                lineHeight = 16.sp
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
        title = { Text("Choose a month", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn {
                items(months) { month ->
                    val monthName = DateFormatSymbols().months[month - 1]
                    Text(
                        text = monthName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMonthSelected(month) }
                            .padding(horizontal = Spacing.sm2, vertical = 14.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = Radius.xl,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun QuarterPickerDialog(onDismiss: () -> Unit, onQuarterSelected: (Int) -> Unit) {
    val quarters = listOf(
        "Q1 (Apr – Jun)" to 1,
        "Q2 (Jul – Sep)" to 2,
        "Q3 (Oct – Dec)" to 3,
        "Q4 (Jan – Mar)" to 4
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a quarter", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                quarters.forEach { (label, value) ->
                    Text(
                        text = label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQuarterSelected(value) }
                            .padding(horizontal = Spacing.sm2, vertical = 14.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = Radius.xl,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun SummaryHeader(totalPaid: Double, label: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(Elevation.premium, Radius.xxl, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        shape = Radius.xxl,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Total Rent Paid ($label)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.sm))
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
