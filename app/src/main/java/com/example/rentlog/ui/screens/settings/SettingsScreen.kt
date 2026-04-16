package com.example.rentlog.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isReminderEnabled by viewModel.isReminderEnabled.collectAsState()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // In a real app, we'd copy the URI content to a temp file then import
            // For now, providing a placeholder message as path resolution is complex
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
                        modifier = Modifier.padding(start = 8.dp).size(40.dp).shadow(4.dp, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { SettingsHeader("Profile") }
            item {
                PremiumSettingsCard {
                    SettingsClickableItem(
                        title = "Landlord & Tenant Details",
                        subtitle = "Edit names, PAN, and default rent",
                        icon = Icons.Default.Person
                    ) {
                        onEditProfile()
                    }
                }
            }

            item { SettingsHeader("Appearance") }
            item {
                PremiumSettingsCard {
                    SettingsClickableItem(
                        title = "Theme Mode",
                        subtitle = themeMode,
                        icon = Icons.Default.Palette
                    ) {
                        val nextMode = when (themeMode) {
                            "SYSTEM" -> "LIGHT"
                            "LIGHT" -> "DARK"
                            else -> "SYSTEM"
                        }
                        viewModel.setThemeMode(nextMode)
                    }
                }
            }

            item { SettingsHeader("Security & Reminders") }
            item {
                PremiumSettingsCard {
                    Column {
                        SettingsSwitchItem(
                            title = "Biometric Lock",
                            subtitle = "Lock app with fingerprint/face",
                            icon = Icons.Default.Fingerprint,
                            checked = isBiometricEnabled,
                            onCheckedChange = { viewModel.setBiometricEnabled(it) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        SettingsSwitchItem(
                            title = "Monthly Reminders",
                            subtitle = "Notify on 1st of every month",
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
                            subtitle = "Save database to internal storage",
                            icon = Icons.Default.CloudUpload
                        ) {
                            viewModel.exportBackup { success, message ->
                                Toast.makeText(context, if (success) "Backup saved: $message" else "Error: $message", Toast.LENGTH_LONG).show()
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        SettingsClickableItem(
                            title = "Import Backup",
                            subtitle = "Restore data from file",
                            icon = Icons.Default.CloudDownload
                        ) {
                            filePickerLauncher.launch("*/*")
                        }
                    }
                }
            }

            item { SettingsHeader("Support") }
            item {
                PremiumSettingsCard {
                    Column {
                        SettingsClickableItem(
                            title = "About RentLog",
                            subtitle = "v1.0.0 Premium Edition",
                            icon = Icons.Default.Verified
                        ) {
                            Toast.makeText(context, "RentLog Premium", Toast.LENGTH_SHORT).show()
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        SettingsClickableItem(
                            title = "Privacy Policy",
                            subtitle = "Your data is stored locally",
                            icon = Icons.Default.Security
                        ) {
                            Toast.makeText(context, "Data stays on your device.", Toast.LENGTH_SHORT).show()
                        }
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
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(24.dp),
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
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}
