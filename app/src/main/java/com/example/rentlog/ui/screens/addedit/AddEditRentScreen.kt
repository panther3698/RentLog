package com.example.rentlog.ui.screens.addedit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rentlog.ui.components.PrimaryButton
import com.example.rentlog.ui.theme.Elevation
import com.example.rentlog.ui.theme.Radius
import com.example.rentlog.ui.theme.Spacing
import com.example.rentlog.ui.theme.extendedColors
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRentScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditRentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val monthName = DateFormatSymbols().months[state.month - 1]

    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.onAttachmentChange(it.toString()) } }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.paymentDate,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= System.currentTimeMillis()
        }
    )

    // Navigate back after save
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    // Show success snackbar briefly before navigating
    LaunchedEffect(state.showSuccess) {
        if (state.showSuccess) {
            snackbarHostState.showSnackbar(
                message = if (state.isEdit) "Payment updated" else "Payment saved",
                duration = SnackbarDuration.Short
            )
            viewModel.onSuccessShown()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateChange(it) }
                    showDatePicker = false
                }) { Text("Confirm", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
            shape = Radius.xxl,
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            monthName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            if (state.isEdit) "Editing saved payment" else "Recording new payment",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .padding(start = Spacing.sm)
                            .size(40.dp)
                            .shadow(Elevation.low, Radius.md)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            // Edit mode banner
            if (state.isEdit) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Radius.md,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm + Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(Spacing.sm + Spacing.xs))
                        Text(
                            "You're updating an existing payment. Changes are saved immediately.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Amount Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        Elevation.high,
                        Radius.xxl,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                shape = Radius.xxl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Monthly Rent Amount",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    TextField(
                        value = state.amount,
                        onValueChange = { viewModel.onAmountChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        placeholder = {
                            Text(
                                "0",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            )
                        }
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = Radius.sm
                    ) {
                        Text(
                            "INR (₹)",
                            modifier = Modifier.padding(horizontal = Spacing.sm + Spacing.xs, vertical = Spacing.xs),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Date & Notes
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Surface(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = Radius.xl,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(Spacing.md))
                        Column {
                            Text(
                                "Payment Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                                    .format(Date(state.paymentDate)),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                }

                OutlinedTextField(
                    value = state.transactionId,
                    onValueChange = { viewModel.onTransactionIdChange(it) },
                    label = { Text("Reference / Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    shape = Radius.xl,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Proof of payment attachment
                val successColor = MaterialTheme.extendedColors.success
                val hasAttachment = state.attachmentUri.isNotBlank()
                Surface(
                    onClick = { attachmentLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = Radius.xl,
                    color = if (hasAttachment) successColor.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (hasAttachment) successColor.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (hasAttachment) Icons.Default.CheckCircle else Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = if (hasAttachment) successColor else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Proof of Payment",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                if (hasAttachment) "Proof attached ✓" else "Attach screenshot or receipt",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasAttachment) successColor
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        if (hasAttachment) {
                            IconButton(
                                onClick = { viewModel.onAttachmentChange("") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove attachment",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        } else {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = if (state.isEdit) "SAVE CHANGES" else "LOG PAYMENT",
                onClick = { viewModel.saveEntry() },
                enabled = state.amount.isNotBlank(),
                isLoading = state.isSaving
            )

            Spacer(Modifier.height(Spacing.md))
        }
    }
}
