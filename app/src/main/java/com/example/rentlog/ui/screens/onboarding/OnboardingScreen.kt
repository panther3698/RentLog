package com.devchiradhi.rentlog.ui.screens.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.devchiradhi.rentlog.ui.components.AppBackButton
import com.devchiradhi.rentlog.ui.components.FormField
import com.devchiradhi.rentlog.ui.components.PrimaryButton
import com.devchiradhi.rentlog.ui.theme.Elevation
import com.devchiradhi.rentlog.ui.theme.Radius
import com.devchiradhi.rentlog.ui.theme.Spacing

@Composable
fun OnboardingScreen(
    onNavigateToDashboard: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var tenantName by remember { mutableStateOf("") }
    var tenantAddress by remember { mutableStateOf("") }
    var landlordAddress by remember { mutableStateOf("") }
    var pan by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }

    // Validation error flags — shown only after first submit attempt
    var attempted by remember { mutableStateOf(false) }

    val landlord by viewModel.landlord.collectAsState(initial = null)
    val isSaving by viewModel.isSaving.collectAsState()
    val isEditMode = landlord != null

    LaunchedEffect(landlord) {
        landlord?.let {
            name = it.name
            tenantName = it.tenantName
            tenantAddress = it.tenantAddress
            landlordAddress = it.landlordAddress
            pan = it.panNumber
            rent = it.defaultRentAmount.let { r -> if (r == 0.0) "" else r.toString() }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onboardingCompleted.collect { onNavigateToDashboard() }
    }

    val panRegex = remember { Regex("[A-Z]{5}[0-9]{4}[A-Z]") }
    val isPanValid = pan.length == 10 && panRegex.matches(pan)

    val nameError = attempted && name.isBlank()
    val tenantError = attempted && tenantName.isBlank()
    val panError = attempted && !isPanValid
    val isFormValid = name.isNotBlank() && tenantName.isNotBlank() && isPanValid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Spacing.xxxl * 4)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(if (isEditMode) Spacing.xxl else Spacing.xxxl - Spacing.md))

            // Logo
            Surface(
                modifier = Modifier
                    .size(90.dp)
                    .shadow(Elevation.premium, Radius.xxl, spotColor = MaterialTheme.colorScheme.primary),
                shape = Radius.xxl,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(Spacing.lg)
                        .fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = "Rent Log",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "HRA RECEIPT TRACKER",
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 3.sp),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Radius.xxl,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        SectionLabel("Identity Details")
                        FormField(
                            label = "Landlord Name",
                            value = name,
                            onValueChange = { name = it },
                            icon = Icons.Default.Person,
                            isError = nameError,
                            errorMessage = if (nameError) "Landlord name is required" else null
                        )
                        FormField(
                            label = "Tenant Name",
                            value = tenantName,
                            onValueChange = { tenantName = it },
                            icon = Icons.Default.Badge,
                            isError = tenantError,
                            errorMessage = if (tenantError) "Tenant name is required" else null
                        )
                        FormField(
                            label = "Landlord PAN",
                            value = pan,
                            onValueChange = { pan = it.uppercase() },
                            icon = Icons.Default.AccountBalanceWallet,
                            hint = "e.g. ABCDE1234F",
                            isError = panError,
                            errorMessage = if (panError) {
                                if (pan.isBlank()) "PAN number is required"
                                else "Invalid PAN — must be 5 letters, 4 digits, 1 letter (e.g. ABCDE1234F)"
                            } else null
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))
                        SectionLabel("Address Details")
                        FormField(
                            label = "Rental Property Address",
                            value = tenantAddress,
                            onValueChange = { tenantAddress = it },
                            icon = Icons.Default.Home
                        )
                        FormField(
                            label = "Landlord Address",
                            value = landlordAddress,
                            onValueChange = { landlordAddress = it },
                            icon = Icons.Default.LocationOn
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))
                        SectionLabel("Rent Details")
                        FormField(
                            label = "Monthly Rent Amount",
                            value = rent,
                            onValueChange = { rent = it },
                            icon = Icons.Default.ReceiptLong,
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            Spacer(modifier = Modifier.height(Spacing.md))
            PrimaryButton(
                text = if (landlord == null) "GET STARTED" else "SAVE CHANGES",
                onClick = {
                    attempted = true
                    if (isFormValid) {
                        viewModel.saveLandlord(
                            name = name,
                            tenantName = tenantName,
                            tenantAddress = tenantAddress,
                            landlordAddress = landlordAddress,
                            pan = pan,
                            defaultRent = rent.toDoubleOrNull() ?: 0.0
                        )
                    }
                },
                enabled = true,
                isLoading = isSaving
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }

        if (isEditMode && onBack != null) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(Spacing.sm)
            ) {
                AppBackButton(onClick = onBack)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = Spacing.xs)
    )
}
