package com.example.rentlog.ui.screens.premium

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentlog.ui.components.PrimaryButton
import com.example.rentlog.ui.theme.Elevation
import com.example.rentlog.ui.theme.Radius
import com.example.rentlog.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero section
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
                    .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(Elevation.premium, Radius.xxl, spotColor = MaterialTheme.colorScheme.primary),
                        shape = Radius.xxl,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(Spacing.lg)
                                .fillMaxSize(),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        "Rent Log Premium",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Everything you need for HRA filing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {

                // Price card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            Elevation.premium,
                            Radius.xxl,
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                    shape = Radius.xxl,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.xl),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "One-Time Payment",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "₹199",
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
                            shape = Radius.lg
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "SAVE UP TO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                                Text(
                                    "₹1,500",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    "in taxes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
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

                PrimaryButton(
                    text = "UNLOCK PREMIUM — ₹199",
                    onClick = { /* TODO: wire payment */ }
                )

                TextButton(
                    onClick = { /* TODO: restore purchase */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Already purchased? Restore",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }

                Spacer(Modifier.height(Spacing.xl))
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
