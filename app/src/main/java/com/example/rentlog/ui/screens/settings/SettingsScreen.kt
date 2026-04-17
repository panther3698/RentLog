package com.example.rentlog.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rentlog.ui.theme.Elevation
import com.example.rentlog.ui.theme.Radius
import com.example.rentlog.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onGoPremium: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isReminderEnabled by viewModel.isReminderEnabled.collectAsState()
    val context = LocalContext.current

    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About RentLog", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        "Version 1.0.0 — Premium Edition",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "RentLog helps Indian tenants track monthly rent payments and generate HRA receipts for income tax filing — fully offline, no account needed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            },
            shape = Radius.xl,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        "Your data never leaves your device.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "RentLog stores all information — tenant names, addresses, PAN, and payment history — locally on your device. No data is sent to any server, cloud, or third party.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Got it") }
            },
            shape = Radius.xl,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            Toast.makeText(context, "Backup import requires file permission handling", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
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
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = PaddingValues(bottom = Spacing.xl)
        ) {
            // Go Premium banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGoPremium() }
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
                            .padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Unlock Premium",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                "PDFs, HRA calculator, attachments & more — ₹199 once",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(Spacing.xs)) }

            item { SettingsHeader("Profile") }
            item {
                PremiumSettingsCard {
                    SettingsClickableItem(
                        title = "Landlord & Tenant Details",
                        subtitle = "Edit tenant info, PAN, and rent amount",
                        icon = Icons.Default.Person,
                        onClick = onEditProfile
                    )
                }
            }

            item { SettingsHeader("Appearance") }
            item {
                PremiumSettingsCard {
                    SettingsClickableItem(
                        title = "Theme Mode",
                        subtitle = themeMode,
                        icon = Icons.Default.Palette,
                        onClick = {
                            val nextMode = when (themeMode) {
                                "SYSTEM" -> "LIGHT"
                                "LIGHT" -> "DARK"
                                else -> "SYSTEM"
                            }
                            viewModel.setThemeMode(nextMode)
                        }
                    )
                }
            }

            item { SettingsHeader("Security & Reminders") }
            item {
                PremiumSettingsCard {
                    Column {
                        SettingsSwitchItem(
                            title = "Biometric Lock",
                            subtitle = "Lock app with fingerprint or face",
                            icon = Icons.Default.Fingerprint,
                            checked = isBiometricEnabled,
                            onCheckedChange = { viewModel.setBiometricEnabled(it) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.md),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        SettingsSwitchItem(
                            title = "Monthly Reminders",
                            subtitle = "Notify on the 1st of every month",
                            icon = Icons.Default.NotificationsActive,
                            checked = isReminderEnabled,
                            onCheckedChange = { viewModel.setReminderEnabled(it) }
                        )
                    }
                }
            }

            item { SettingsHeader("Data Management") }
            item {
                PremiumSettingsCard {
                    Column {
                        SettingsClickableItem(
                            title = "Export Backup",
                            subtitle = "Save a backup to your device",
                            icon = Icons.Default.CloudUpload,
                            onClick = {
                                viewModel.exportBackup { success, message ->
                                    Toast.makeText(
                                        context,
                                        if (success) "Backup saved successfully" else "Error: $message",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.md),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        SettingsClickableItem(
                            title = "Import Backup",
                            subtitle = "Restore from a backup file",
                            icon = Icons.Default.CloudDownload,
                            onClick = { filePickerLauncher.launch("*/*") }
                        )
                    }
                }
            }

            item { SettingsHeader("Support") }
            item {
                PremiumSettingsCard {
                    Column {
                        SettingsClickableItem(
                            title = "About RentLog",
                            subtitle = "v1.0.0 — Premium Edition",
                            icon = Icons.Default.Verified,
                            onClick = { showAboutDialog = true }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.md),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        SettingsClickableItem(
                            title = "Privacy Policy",
                            subtitle = "All data stays on your device",
                            icon = Icons.Default.Security,
                            onClick = { showPrivacyDialog = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumSettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                Elevation.medium,
                Radius.xl,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = Radius.xl,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        content = { content() }
    )
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(
            start = Spacing.md,
            top = Spacing.lg,
            bottom = Spacing.sm
        )
    )
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = {
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = {
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    )
}
