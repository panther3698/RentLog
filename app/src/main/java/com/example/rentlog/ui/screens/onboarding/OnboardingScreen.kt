package com.example.rentlog.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rentlog.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = { viewModel.saveLandlord(onComplete) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo (Matching Splash)
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Welcome to RentLog",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Setup your landlord profile to begin",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(40.dp))

            // Form Fields in Calm Cards
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OnboardingField(
                    icon = Icons.Default.Person,
                    label = "Landlord Name",
                    value = state.name,
                    onValueChange = { viewModel.onNameChange(it) }
                )
                OnboardingField(
                    icon = Icons.Default.Home,
                    label = "Tenant Name",
                    value = state.tenantName,
                    onValueChange = { viewModel.onTenantNameChange(it) }
                )
                OnboardingField(
                    icon = Icons.Default.Place,
                    label = "Tenant Address",
                    value = state.tenantAddress,
                    onValueChange = { viewModel.onTenantAddressChange(it) }
                )
                OnboardingField(
                    icon = Icons.Default.Business,
                    label = "Landlord Address",
                    value = state.landlordAddress,
                    onValueChange = { viewModel.onLandlordAddressChange(it) }
                )
                OnboardingField(
                    icon = Icons.Default.CurrencyRupee,
                    label = "Monthly Rent Amount",
                    value = state.defaultRentAmount,
                    onValueChange = { viewModel.onRentAmountChange(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OnboardingField(
                    icon = Icons.Default.Assignment,
                    label = "Landlord PAN (Optional)",
                    value = state.panNumber,
                    onValueChange = { viewModel.onPanChange(it) }
                )
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun OnboardingField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = { Text("Enter $label") },
                    keyboardOptions = keyboardOptions,
                    singleLine = true
                )
            }
        }
    }
}
