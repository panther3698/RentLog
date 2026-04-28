package com.devchiradhi.rentlog.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.devchiradhi.rentlog.BuildConfig
import com.devchiradhi.rentlog.ui.components.AppBackButton
import com.devchiradhi.rentlog.ui.theme.Elevation
import com.devchiradhi.rentlog.ui.theme.Radius
import com.devchiradhi.rentlog.ui.theme.Spacing

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
    val hasFullAccess by viewModel.hasFullAccess.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val debugBypassPremiumAccess by viewModel.debugBypassPremiumAccess.collectAsState()
    val context = LocalContext.current

    val effectivePremium = isPremium || (BuildConfig.DEBUG && debugBypassPremiumAccess)

    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTrialExpiredSheet by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset all data?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will permanently delete all your payments, landlord info, and settings. " +
                    "This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    viewModel.wipeAllData {
                        // Restart app or navigate back
                        onNavigateBack()
                        Toast.makeText(context, "App reset successfully", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Wipe Everything", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            },
            shape = Radius.xl,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About RentLog", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        "Version 1.6",
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

    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Confirm before overwriting â€” import destroys existing data
    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Restore backup?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will replace all current data with the selected backup. " +
                    "The app will need to restart after restore. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingImportUri = null
                    viewModel.importBackup(uri) { success, message ->
                        Toast.makeText(
                            context,
                            message ?: if (success) "Restore complete" else "Restore failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) { Text("Restore", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) { Text("Cancel") }
            },
            shape = Radius.xl,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { pendingImportUri = it }
    }

    if (showTrialExpiredSheet) {
        com.devchiradhi.rentlog.ui.components.TrialExpiredSheet(
            onDismiss = { showTrialExpiredSheet = false },
            onGoPremium = onGoPremium
        )
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
                    Box(modifier = Modifier.padding(start = Spacing.md)) {
                        AppBackButton(onClick = onNavigateBack)
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
            contentPadding = PaddingValues(bottom = Spacing.xl + 40.dp)
        ) {
            // Go Premium banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGoPremium() }
                        .shadow(
                            if (effectivePremium) Elevation.low else Elevation.premium,
                            Radius.xxl,
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                    shape = Radius.xxl,
                    colors = CardDefaults.cardColors(
                        containerColor = if (effectivePremium) 
                            MaterialTheme.colorScheme.secondaryContainer 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (effectivePremium) Icons.Default.TaskAlt else Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = if (effectivePremium) 
                                MaterialTheme.colorScheme.onSecondaryContainer 
                            else 
                                MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (effectivePremium) "You're on Premium" else "Unlock Premium",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (effectivePremium) 
                                    MaterialTheme.colorScheme.onSecondaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                if (effectivePremium) 
                                    "All features are unlocked — Lifetime Access" 
                                else 
                                    "PDFs, HRA calculator, attachments & more — ₹99 once",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (effectivePremium) 
                                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f) 
                                else 
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (effectivePremium) 
                                MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f) 
                            else 
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
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
                            onCheckedChange = {
                                if (hasFullAccess) viewModel.setBiometricEnabled(it)
                                else showTrialExpiredSheet = true
                            }
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
                            onCheckedChange = { enabled ->
                                if (hasFullAccess) {
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        
                                        if (!hasPermission) {
                                            Toast.makeText(
                                                context,
                                                "Please allow notifications in system settings to receive reminders.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            // Optional: Open settings automatically
                                            try {
                                                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                    data = Uri.fromParts("package", context.packageName, null)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {}
                                            return@SettingsSwitchItem
                                        }
                                    }
                                    viewModel.setReminderEnabled(enabled)
                                } else {
                                    showTrialExpiredSheet = true
                                }
                            }
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
                                if (!hasFullAccess) { showTrialExpiredSheet = true; return@SettingsClickableItem }
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
                            onClick = {
                                if (!hasFullAccess) { showTrialExpiredSheet = true; return@SettingsClickableItem }
                                filePickerLauncher.launch(arrayOf("application/x-sqlite3", "application/octet-stream", "*/*"))
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.md),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        SettingsClickableItem(
                            title = "Reset App",
                            subtitle = "Wipe all data and start fresh",
                            icon = Icons.Default.DeleteForever,
                            onClick = { showResetDialog = true },
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // â”€â”€ Developer Tools (debug builds only) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            if (BuildConfig.DEBUG) {
                item { SettingsHeader("🛠 Developer Tools") }
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(Elevation.medium, Radius.xl),
                        shape = Radius.xl,
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A2E)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f))
                    ) {
                        Column {
                            SettingsSwitchItem(
                                title = "Bypass Premium Locks",
                                subtitle = if (debugBypassPremiumAccess) {
                                    "All premium features stay unlocked in debug builds"
                                } else {
                                    "Use the real trial and premium gating rules"
                                },
                                icon = Icons.Default.BugReport,
                                checked = debugBypassPremiumAccess,
                                onCheckedChange = viewModel::setDebugBypassPremiumAccess
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Spacing.md),
                                color = Color(0xFF7C4DFF).copy(alpha = 0.2f)
                            )
                            SettingsClickableItem(
                                title = "Reset Trial (Fresh 21 days)",
                                subtitle = "Simulate a new install timestamp",
                                icon = Icons.Default.Refresh,
                                onClick = {
                                    viewModel.setDebugBypassPremiumAccess(false)
                                    viewModel.resetTrial()
                                    Toast.makeText(
                                        context,
                                        "Trial reset. Premium bypass disabled so the change is visible.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Spacing.md),
                                color = Color(0xFF7C4DFF).copy(alpha = 0.2f)
                            )
                            SettingsClickableItem(
                                title = "Simulate Trial Expired",
                                subtitle = "Backdate launch to 15 days ago",
                                icon = Icons.Default.Timer,
                                onClick = {
                                    viewModel.setDebugBypassPremiumAccess(false)
                                    viewModel.simulateTrialExpired()
                                    Toast.makeText(
                                        context,
                                        "Trial expired simulation applied. Premium bypass disabled.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }

            item { SettingsHeader("Support") }
            item {
                PremiumSettingsCard {
                    Column {
                        SettingsClickableItem(
                            title = "Share App",
                            subtitle = "Recommend RentLog to friends",
                            icon = Icons.Default.Share,
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "RentLog — Track rent & claim HRA")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Track monthly rent payments & generate HRA receipts for tax filing — 100% offline.\n\nhttps://play.google.com/store/apps/details?id=com.devchiradhi.rentlog"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.md),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        SettingsClickableItem(
                            title = "Rate App",
                            subtitle = "Love RentLog? Leave a review",
                            icon = Icons.Default.Star,
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=com.devchiradhi.rentlog")
                                )
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google.com/store/apps/details?id=com.devchiradhi.rentlog")
                                        )
                                    )
                                }
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.md),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        SettingsClickableItem(
                            title = "Contact Support",
                            subtitle = "Get help or report an issue",
                            icon = Icons.Default.Email,
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@devchiradhi.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "RentLog Support — v1.6")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.md),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        SettingsClickableItem(
                            title = "About RentLog",
                            subtitle = "v1.6",
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
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium, color = if(color == MaterialTheme.colorScheme.error) color else Color.Unspecified) },
        supportingContent = {
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = color)
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
