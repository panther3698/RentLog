package com.devchiradhi.rentlog.ui.screens.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.devchiradhi.rentlog.ui.components.AppBackButton
import com.devchiradhi.rentlog.ui.theme.Elevation
import com.devchiradhi.rentlog.ui.theme.Radius
import com.devchiradhi.rentlog.ui.theme.Spacing
import com.devchiradhi.rentlog.ui.theme.extendedColors
import java.util.Locale
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HraCalculatorScreen(
    onNavigateBack: () -> Unit,
    onGoPremium: () -> Unit = {},
    viewModel: HraCalculatorViewModel = hiltViewModel()
) {
    val hasFullAccess by viewModel.hasFullAccess.collectAsState()
    var showTrialExpiredSheet by remember { mutableStateOf(false) }

    if (showTrialExpiredSheet) {
        com.devchiradhi.rentlog.ui.components.TrialExpiredSheet(
            onDismiss = { showTrialExpiredSheet = false },
            onGoPremium = onGoPremium
        )
    }

    var basicSalary by remember { mutableStateOf("") }
    var daAmount   by remember { mutableStateOf("") }
    var hraReceived by remember { mutableStateOf("") }
    var rentPaid by remember { mutableStateOf("") }
    var isMetro by remember { mutableStateOf(true) }

    val result = remember(basicSalary, daAmount, hraReceived, rentPaid, isMetro) {
        calculateHraExemption(
            basicSalary = (basicSalary.toDoubleOrNull() ?: 0.0) + (daAmount.toDoubleOrNull() ?: 0.0),
            hraReceived = hraReceived.toDoubleOrNull() ?: 0.0,
            rentPaid = rentPaid.toDoubleOrNull() ?: 0.0,
            isMetro = isMetro
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "HRA Calculator",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Section 10(13A) Exemption",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = Spacing.md)) {
                        AppBackButton(onClick = onNavigateBack)
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
                .padding(horizontal = Spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            // Inputs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = Radius.xl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text(
                        "Monthly Figures",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(Spacing.sm))

                    CalculatorField(
                        label = "Basic Salary",
                        value = basicSalary,
                        onValueChange = { basicSalary = it },
                        helper = "Monthly basic salary (from payslip)"
                    )
                    CalculatorField(
                        label = "DA (forming part of retirement benefits)",
                        value = daAmount,
                        onValueChange = { daAmount = it },
                        helper = "Govt employees only — DA treated as basic for HRA. Leave ₹0 if private sector."
                    )
                    CalculatorField(
                        label = "HRA Received",
                        value = hraReceived,
                        onValueChange = { hraReceived = it },
                        helper = "Monthly HRA from employer (check payslip)"
                    )
                    CalculatorField(
                        label = "Rent Paid",
                        value = rentPaid,
                        onValueChange = { rentPaid = it },
                        helper = "Actual monthly rent you pay"
                    )
                }
            }

            // Metro toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = Radius.xl,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Metro City",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Delhi, Mumbai, Chennai, Kolkata (50% rule)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Switch(
                        checked = isMetro,
                        onCheckedChange = { isMetro = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Result card — below inputs so it's visible after filling fields
            val inputsFilled = basicSalary.isNotBlank() && hraReceived.isNotBlank() && rentPaid.isNotBlank()
            if (!hasFullAccess && inputsFilled) {
                // Trial expired — blur/replace result with upgrade prompt
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Radius.xl,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "Trial ended",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Upgrade to see your HRA exemption result",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = { showTrialExpiredSheet = true },
                            shape = Radius.lg
                        ) {
                            Text("Upgrade to Premium — ₹99", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                HraResultCard(result = result, inputsFilled = inputsFilled)

                // Breakdown card
                if (result.exemption > 0) {
                    val hasDA = (daAmount.toDoubleOrNull() ?: 0.0) > 0.0
                    BreakdownCard(result = result, hasDA = hasDA)
                }
            }

            // Info note
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = Radius.lg,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        "HRA exemption = minimum of: (1) actual HRA received, (2) rent paid minus 10% of basic, (3) 50%/40% of basic salary. Old tax regime only.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xl))
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun HraResultCard(result: HraResult, inputsFilled: Boolean) {
    val hasResult = result.exemption > 0
    val isZeroResult = inputsFilled && !hasResult

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                if (hasResult) Elevation.premium else Elevation.low,
                Radius.xxl,
                spotColor = if (hasResult) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                else Color.Transparent
            ),
        shape = Radius.xxl,
        colors = CardDefaults.cardColors(
            containerColor = when {
                hasResult -> MaterialTheme.colorScheme.primary
                isZeroResult -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.lg, horizontal = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Annual HRA Exemption",
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    hasResult -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    isZeroResult -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                },
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                when {
                    hasResult -> "₹${formatAmount(result.exemption)}"
                    isZeroResult -> "₹0"
                    else -> "Fill in your salary details"
                },
                style = if (!inputsFilled) MaterialTheme.typography.titleMedium
                else MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = when {
                    hasResult -> MaterialTheme.colorScheme.onPrimary
                    isZeroResult -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                },
                textAlign = TextAlign.Center
            )
            if (hasResult) {
                Spacer(Modifier.height(Spacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    TaxSavedChip(modifier = Modifier.weight(1f), label = "At 30%", amount = result.taxSavedAt30)
                    TaxSavedChip(modifier = Modifier.weight(1f), label = "At 20%", amount = result.taxSavedAt20)
                    TaxSavedChip(modifier = Modifier.weight(1f), label = "At 10%", amount = result.taxSavedAt10)
                }
            }
            if (isZeroResult) {
                Spacer(Modifier.height(Spacing.sm))
                val reason = when {
                    result.component1 == 0.0 -> "No HRA received from employer."
                    result.component2 == 0.0 -> "Your rent is less than 10% of your basic salary — the excess over 10% is what qualifies for exemption."
                    else -> "Exemption works out to zero with these figures."
                }
                Text(
                    reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun TaxSavedChip(modifier: Modifier = Modifier, label: String, amount: Double) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
        shape = Radius.md
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            Text(
                "₹${formatAmount(amount)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1
            )
            Text(
                "saved",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun BreakdownCard(result: HraResult, hasDA: Boolean = false) {
    val baseLbl = if (hasDA) "Basic+DA" else "Basic"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Radius.xl,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                "How it's calculated",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.sm))
            BreakdownRow("1. Actual HRA received", result.component1, result.exemption == result.component1)
            BreakdownRow("2. Rent paid − 10% of $baseLbl", result.component2, result.exemption == result.component2)
            BreakdownRow("3. ${result.basicPct}% of $baseLbl", result.component3, result.exemption == result.component3)
            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Exemption (minimum)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("₹${formatAmount(result.exemption)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, amount: Double, isMinimum: Boolean) {
    val successColor = MaterialTheme.extendedColors.success
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isMinimum) successColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isMinimum) {
                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(12.dp), tint = successColor)
                Spacer(Modifier.width(2.dp))
            }
            Text(
                "₹${formatAmount(amount)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isMinimum) successColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun CalculatorField(label: String, value: String, onValueChange: (String) -> Unit, helper: String) {
    Column(modifier = Modifier.padding(vertical = Spacing.xs)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("₹", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = Radius.lg,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            supportingText = {
                Text(helper, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        )
    }
}

// ─── Domain logic ────────────────────────────────────────────────────────────

data class HraResult(
    val exemption: Double,
    val component1: Double,  // Actual HRA received (annual)
    val component2: Double,  // Rent - 10% of (basic+DA) (annual)
    val component3: Double,  // % of (basic+DA) (annual)
    val basicPct: Int,
    val taxSavedAt30: Double,
    val taxSavedAt20: Double,
    val taxSavedAt10: Double,
    val daIncluded: Boolean = false  // true when DA was added to base
)

fun calculateHraExemption(
    basicSalary: Double,   // Already = Basic + DA (caller adds them)
    hraReceived: Double,
    rentPaid: Double,
    isMetro: Boolean
): HraResult {
    // Per Rule 2A, the base is Basic + DA forming part of retirement benefits
    val annualBase = basicSalary * 12
    val annualHra  = hraReceived * 12
    val annualRent = rentPaid * 12
    val basicPct   = if (isMetro) 50 else 40

    val c1 = annualHra
    val c2 = maxOf(0.0, annualRent - (0.10 * annualBase))
    val c3 = (basicPct / 100.0) * annualBase

    val exemption = min(c1, min(c2, c3))
    return HraResult(
        exemption    = exemption,
        component1   = c1,
        component2   = c2,
        component3   = c3,
        basicPct     = basicPct,
        taxSavedAt30 = exemption * 0.30,
        taxSavedAt20 = exemption * 0.20,
        taxSavedAt10 = exemption * 0.10
    )
}

private fun formatAmount(amount: Double): String =
    String.format(Locale.getDefault(), "%,.0f", amount)
