package com.example.rentlog.ui.screens.welcome

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rentlog.ui.components.PrimaryButton
import com.example.rentlog.ui.theme.Elevation
import com.example.rentlog.ui.theme.Radius
import com.example.rentlog.ui.theme.Spacing

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 4 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(Spacing.xxl + Spacing.md))

                // Logo
                Surface(
                    modifier = Modifier
                        .size(96.dp)
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

                Spacer(Modifier.height(Spacing.lg))

                Text(
                    text = "Rent Log",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "Track rent. Claim HRA. Save taxes.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Spacing.xs)
                )

                Spacer(Modifier.height(Spacing.xl))

                // Value props
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    ValuePropRow(
                        icon = Icons.Default.CalendarMonth,
                        title = "Track payments month by month",
                        subtitle = "Never lose a payment record again"
                    )
                    ValuePropRow(
                        icon = Icons.Default.Description,
                        title = "Generate HRA receipts instantly",
                        subtitle = "Annual, quarterly, and monthly PDFs"
                    )
                    ValuePropRow(
                        icon = Icons.Default.Calculate,
                        title = "Calculate your HRA tax exemption",
                        subtitle = "See exactly how much you save under Section 10(13A)"
                    )
                    ValuePropRow(
                        icon = Icons.Default.Lock,
                        title = "100% offline — your data stays local",
                        subtitle = "No account, no cloud, no tracking"
                    )
                }

                Spacer(Modifier.height(Spacing.xl))

                // Sample receipt preview
                SampleReceiptCard()

                Spacer(Modifier.height(Spacing.xl))

                PrimaryButton(
                    text = "GET STARTED — IT'S FREE",
                    onClick = {
                        viewModel.markWelcomeSeen()
                        onGetStarted()
                    }
                )

                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = "No sign-up required",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                )

                Spacer(Modifier.height(Spacing.xl))
            }
        }
    }
}

@Composable
private fun ValuePropRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = Radius.md,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(10.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.width(Spacing.md))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SampleReceiptCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(Elevation.high, Radius.xl, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        shape = Radius.xl,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RENT RECEIPT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = Radius.sm
                ) {
                    Text(
                        "SAMPLE",
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(Spacing.sm))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(Modifier.height(Spacing.sm))

            Text(
                "Received with thanks from Rahul Sharma a sum of Rs. 25,000 (Rupees Twenty-Five Thousand only) towards rent for the period of March 2025.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(Spacing.sm))

            ReceiptRow("Landlord", "Suresh Kumar")
            ReceiptRow("PAN", "ABCDE1234F")
            ReceiptRow("Property", "B-204, Sector 18, Noida")
            ReceiptRow("Payment Date", "01 March 2025")
            ReceiptRow("Ref / UPI", "UPI-4782930012")

            Spacer(Modifier.height(Spacing.sm))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(Modifier.height(Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "01/03/2025",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    "Signature of Landlord ___________",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.height(Spacing.sm))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    "Computer-generated for HRA / income tax purposes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            label,
            modifier = Modifier.width(90.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium
        )
        Text(
            ": $value",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            fontWeight = FontWeight.SemiBold
        )
    }
}
