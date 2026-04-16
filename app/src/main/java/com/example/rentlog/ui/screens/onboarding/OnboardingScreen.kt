package com.example.rentlog.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

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

    val landlord by viewModel.landlord.collectAsState(initial = null)
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
        viewModel.onboardingCompleted.collect {
            onNavigateToDashboard()
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Decorative background element
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
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
                .navigationBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(if (isEditMode) 40.dp else 60.dp))
            
            // Fixed Header: Premium Logo/Icon
            Surface(
                modifier = Modifier
                    .size(90.dp)
                    .shadow(20.dp, RoundedCornerShape(28.dp), spotColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Rent Log",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "PROFESSIONAL TRACKING",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Scrollable Form Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Identity Details", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        OnboardingField("Landlord Name", name, { name = it }, Icons.Default.Person)
                        OnboardingField("Your Name (Tenant)", tenantName, { tenantName = it }, Icons.Default.Badge)
                        OnboardingField("Landlord PAN Number", pan, { pan = it.uppercase() }, Icons.Default.AccountBalanceWallet)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Address Details", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        OnboardingField("Property Address (Tenant)", tenantAddress, { tenantAddress = it }, Icons.Default.Home)
                        OnboardingField("Landlord Address", landlordAddress, { landlordAddress = it }, Icons.Default.LocationOn)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Rent Details", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        OnboardingField("Default Monthly Rent", rent, { rent = it }, Icons.Default.ReceiptLong, KeyboardType.Number)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Fixed Footer Button
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    if (name.isNotBlank() && pan.isNotBlank() && tenantName.isNotBlank()) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (landlord == null) "GET STARTED" else "SAVE CHANGES", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (isEditMode && onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun OnboardingField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}
