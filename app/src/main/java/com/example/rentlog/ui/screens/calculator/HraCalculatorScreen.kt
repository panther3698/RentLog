package com.example.rentlog.ui.screens.calculator

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
import com.example.rentlog.ui.theme.Elevation
import com.example.rentlog.ui.theme.Radius
import com.example.rentlog.ui.theme.Spacing
import com.example.rentlog.ui.theme.extendedColors
import java.util.Locale
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HraCalculatorScreen(onNavigateBack: () -> Unit) {
    var basicSalary by remember { mutableStateOf("") }
    var hraReceived by remember { mutableStateOf("") }
    var rentPaid by remember { mutableStateOf("") }
    var isMetro by remember { mutableStateOf(true) }

    val result = remember(basicSalary, hraReceived, rentPaid, isMetro) {
        calculateHraExemption(
            basicSalary = basicSalary.toDoubleOrNull() ?: 0.0,
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
                .padding(horizontal = Spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            // Result card at top — shows 0 until inputs filled
            HraResultCard(result = result)

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
                        helper = "Your monthly basic salary"
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

            // Breakdown card
            if (result.exemption > 0) {
                BreakdownCard(result = result)
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
        }
    }
}

@Composable
private fun HraResultCard(result: HraResult) {
    val successColor = MaterialTheme.extendedColors.success
    val hasResult = result.exemption > 0

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
            containerColor = if (hasResult) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Annual HRA Exemption",
                style = MaterialTheme.typography.labelLarge,
                color = if (hasResult) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                if (hasResult) "₹${formatAmount(result.exemption)}"
                else "Fill in your salary details",
                style = if (hasResult) MaterialTheme.typography.displaySmall
                else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (hasResult) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                textAlign = TextAlign.Center
            )
            if (hasResult) {
                Spacer(Modifier.height(Spacing.sm))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    TaxSavedChip(label = "At 30%", amount = result.taxSavedAt30)
                    TaxSavedChip(label = "At 20%", amount = result.taxSavedAt20)
                    TaxSavedChip(label = "At 10%", amount = result.taxSavedAt10)
                }
            }
        }
    }
}

@Composable
private fun TaxSavedChip(label: String, amount: Double) {
    Surface(
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
        shape = Radius.pill
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
            Text(
                "₹${formatAmount(amount)} saved",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun BreakdownCard(result: HraResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Radius.xl,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                "How it's calculated",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.sm))
            BreakdownRow("1. Actual HRA received (annual)", result.component1, result.exemption == result.component1)
            BreakdownRow("2. Rent paid − 10% of basic (annual)", result.component2, result.exemption == result.component2)
            BreakdownRow("3. ${result.basicPct}% of basic salary (annual)", result.component3, result.exemption == result.component3)
            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Exemption (minimum)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("₹${formatAmount(result.exemption)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
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
    val component2: Double,  // Rent - 10% basic (annual)
    val component3: Double,  // % of basic (annual)
    val basicPct: Int,
    val taxSavedAt30: Double,
    val taxSavedAt20: Double,
    val taxSavedAt10: Double
)

fun calculateHraExemption(
    basicSalary: Double,
    hraReceived: Double,
    rentPaid: Double,
    isMetro: Boolean
): HraResult {
    val annualBasic = basicSalary * 12
    val annualHra = hraReceived * 12
    val annualRent = rentPaid * 12
    val basicPct = if (isMetro) 50 else 40

    val c1 = annualHra
    val c2 = maxOf(0.0, annualRent - (0.10 * annualBasic))
    val c3 = (basicPct / 100.0) * annualBasic

    val exemption = min(c1, min(c2, c3))
    return HraResult(
        exemption = exemption,
        component1 = c1,
        component2 = c2,
        component3 = c3,
        basicPct = basicPct,
        taxSavedAt30 = exemption * 0.30,
        taxSavedAt20 = exemption * 0.20,
        taxSavedAt10 = exemption * 0.10
    )
}

private fun formatAmount(amount: Double): String =
    String.format(Locale.getDefault(), "%,.0f", amount)
