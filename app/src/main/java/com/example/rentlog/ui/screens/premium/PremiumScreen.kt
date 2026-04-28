package com.devchiradhi.rentlog.ui.screens.premium

import android.app.Activity
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.devchiradhi.rentlog.data.billing.BillingState
import com.devchiradhi.rentlog.ui.components.AppBackButton
import com.devchiradhi.rentlog.ui.components.PrimaryButton
import com.devchiradhi.rentlog.ui.theme.Elevation
import com.devchiradhi.rentlog.ui.theme.Radius
import com.devchiradhi.rentlog.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val billingState by viewModel.billingState.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // Handle terminal billing states with a dialog
    var dialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(billingState) {
        when (val state = billingState) {
            is BillingState.Success -> {
                dialogMessage = "🎉 Welcome to Premium!\n\nAll features are now unlocked."
            }
            is BillingState.Pending -> {
                dialogMessage = "Your payment is pending. Premium will be activated once the payment clears."
                viewModel.resetBillingState()
            }
            is BillingState.NotOwned -> {
                dialogMessage = "No active purchase found for this Google account. If you purchased on a different account, switch accounts in the Play Store."
                viewModel.resetBillingState()
            }
            is BillingState.Error -> {
                dialogMessage = state.message
                viewModel.resetBillingState()
            }
            else -> {}
        }
    }

    if (dialogMessage != null) {
        AlertDialog(
            onDismissRequest = {
                dialogMessage = null
                if (isPremium) onNavigateBack()
            },
            title = {
                Text(
                    if (isPremium) "Premium Unlocked" else "Purchase Info",
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(dialogMessage ?: "", lineHeight = 22.sp) },
            confirmButton = {
                TextButton(onClick = {
                    dialogMessage = null
                    if (isPremium) onNavigateBack()
                }) { Text("OK") }
            },
            shape = Radius.xl,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero section with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = Spacing.xl, top = 64.dp, end = Spacing.xl, bottom = Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(Elevation.premium, Radius.xxl, spotColor = MaterialTheme.colorScheme.primary),
                            shape = Radius.xxl,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                if (isPremium) Icons.Default.Verified else Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(Spacing.lg)
                                    .fillMaxSize(),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            if (isPremium) "You're on Premium" else "Rent Log Premium",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            if (isPremium) "All features are unlocked"
                            else "Everything you need for HRA filing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    // ... rest of the code remains same, but I need to include it in the replace block or split it.
                    // Price card — hide amount if already premium
                    if (!isPremium) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    Elevation.premium,
                                    Radius.xl,
                                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                ),
                            shape = Radius.xl,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.lg),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "One-Time Payment",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "₹99",
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        "No subscriptions. Yours forever.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                                    shape = Radius.md
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "HRA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            "READY",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Text(
                                            "Proof",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Features list
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = Radius.xl,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
                    ) {
                        Column(modifier = Modifier.padding(Spacing.lg)) {
                            Text(
                                "What's included",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(Spacing.md))

                            PremiumFeatureRow(
                                icon = Icons.Default.PictureAsPdf,
                                title = "PDF Receipt Generation",
                                subtitle = "Annual, quarterly & monthly receipts for HR"
                            )
                            PremiumFeatureRow(
                                icon = Icons.Default.Calculate,
                                title = "HRA Exemption Calculator",
                                subtitle = "Section 10(13A) tax savings, instant"
                            )
                            PremiumFeatureRow(
                                icon = Icons.Default.AttachFile,
                                title = "Proof of Payment Attachments",
                                subtitle = "Attach screenshots & bank statements"
                            )
                            PremiumFeatureRow(
                                icon = Icons.Default.NotificationsActive,
                                title = "Monthly Payment Reminders",
                                subtitle = "Never miss logging a payment"
                            )
                            PremiumFeatureRow(
                                icon = Icons.Default.Fingerprint,
                                title = "Biometric App Lock",
                                subtitle = "Protect sensitive rent data"
                            )
                            PremiumFeatureRow(
                                icon = Icons.Default.CloudUpload,
                                title = "Backup & Restore",
                                subtitle = "Never lose your payment history"
                            )
                            PremiumFeatureRow(
                                icon = Icons.Default.AllInclusive,
                                title = "Unlimited Payment History",
                                subtitle = "All financial years, forever"
                            )
                        }
                    }

                    // Trust signals
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = Radius.lg,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.md),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TrustPill(icon = Icons.Default.Lock, label = "100% Offline")
                            TrustPill(icon = Icons.Default.Shield, label = "No Subscription")
                            TrustPill(icon = Icons.Default.Devices, label = "One Device")
                        }
                    }

                    Spacer(Modifier.height(Spacing.sm))

                    val isLoading = billingState == BillingState.Loading

                    if (isPremium) {
                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth(),
                            shape = Radius.xl,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(Spacing.sm))
                            Text("Premium Active — All Features Unlocked", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        PrimaryButton(
                            text = if (isLoading) "Processing…" else "UNLOCK PREMIUM — ₹99",
                            onClick = { activity?.let { viewModel.purchase(it) } },
                            isLoading = isLoading,
                            enabled = !isLoading
                        )

                        TextButton(
                            onClick = { viewModel.restorePurchases() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                )
                                Spacer(Modifier.width(Spacing.sm))
                            }
                            Text(
                                "Already purchased? Restore",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.xl))
                    Spacer(Modifier.navigationBarsPadding())
                }
            }

            // Overlay Back Button
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(Spacing.sm)
            ) {
                AppBackButton(onClick = onNavigateBack)
            }
        }
    }
}

@Composable
private fun PremiumFeatureRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = Radius.md,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TrustPill(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
