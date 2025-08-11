package com.homeinventory.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.homeinventory.app.presentation.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showDataDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            item {
                SettingsSection(title = "Appearance") {
                    SettingsItem(
                        icon = if (settings.isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        title = "Dark Mode",
                        subtitle = if (settings.isDarkMode) "Switch to light theme" else "Switch to dark theme",
                        iconTint = if (settings.isDarkMode) Color(0xFFFF9500) else Color(0xFF5856D6)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Switch(
                                checked = settings.isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode() }
                            )
                        }
                    }
                }
            }
            
            // Security Section
            item {
                SettingsSection(title = "Security") {
                    SettingsItem(
                        icon = if (settings.isSecurityEnabled) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        title = "Biometric Security",
                        subtitle = if (settings.isSecurityEnabled) "App is secured with biometrics" else "Enable biometric protection",
                        iconTint = if (settings.isSecurityEnabled) Color(0xFF007AFF) else MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Switch(
                            checked = settings.isSecurityEnabled,
                            onCheckedChange = { viewModel.toggleSecurity() }
                        )
                    }
                }
            }
            
            // Notifications Section
            item {
                SettingsSection(title = "Notifications") {
                    SettingsItem(
                        icon = if (settings.isInventoryReminderEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                        title = "Inventory Reminders",
                        subtitle = if (settings.isInventoryReminderEnabled) "Daily reminders enabled" else "Enable daily reminders",
                        iconTint = if (settings.isInventoryReminderEnabled) Color(0xFF007AFF) else MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Switch(
                            checked = settings.isInventoryReminderEnabled,
                            onCheckedChange = { viewModel.toggleInventoryReminder() }
                        )
                    }
                    
                    if (settings.isInventoryReminderEnabled) {
                        SettingsItem(
                            icon = Icons.Filled.Schedule,
                            title = "First Reminder",
                            subtitle = "Daily reminder at ${settings.reminderTime1}",
                            iconTint = Color(0xFFFF9500)
                        ) {
                            TextButton(onClick = { /* TODO: Time picker */ }) {
                                Text(settings.reminderTime1)
                            }
                        }
                        
                        SettingsItem(
                            icon = if (settings.isSecondReminderEnabled) Icons.Filled.NotificationAdd else Icons.Filled.NotificationsOff,
                            title = "Second Reminder",
                            subtitle = if (settings.isSecondReminderEnabled) "Daily reminder at ${settings.reminderTime2}" else "Enable second daily reminder",
                            iconTint = if (settings.isSecondReminderEnabled) Color(0xFF5856D6) else MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Switch(
                                checked = settings.isSecondReminderEnabled,
                                onCheckedChange = { 
                                    val newSettings = settings.copy(isSecondReminderEnabled = !settings.isSecondReminderEnabled)
                                    viewModel.updateSettings(newSettings)
                                }
                            )
                        }
                        
                        if (settings.isSecondReminderEnabled) {
                            SettingsItem(
                                icon = Icons.Filled.Schedule,
                                title = "Second Reminder Time",
                                subtitle = "Daily reminder at ${settings.reminderTime2}",
                                iconTint = Color(0xFF5856D6)
                            ) {
                                TextButton(onClick = { /* TODO: Time picker */ }) {
                                    Text(settings.reminderTime2)
                                }
                            }
                        }
                    }
                }
            }
            
            // Data Management Section
            item {
                SettingsSection(title = "Data Management") {
                    SettingsItem(
                        icon = Icons.Filled.Storage,
                        title = "Manage Data",
                        subtitle = "Clear or reset your inventory data",
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        TextButton(onClick = { showDataDialog = true }) {
                            Text("Manage")
                        }
                    }
                }
            }
            
            // About Section
            item {
                SettingsSection(title = "About") {
                    SettingsItem(
                        icon = Icons.Filled.Info,
                        title = "Version",
                        subtitle = "Home Inventory v1.0.0",
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {}
                    
                    SettingsItem(
                        icon = Icons.Filled.Code,
                        title = "Open Source",
                        subtitle = "View source code on GitHub",
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        TextButton(onClick = { /* TODO: Open GitHub */ }) {
                            Text("GitHub")
                        }
                    }
                }
            }
        }
    }
    
    // Data Management Dialog
    if (showDataDialog) {
        AlertDialog(
            onDismissRequest = { showDataDialog = false },
            title = { Text("Manage Data") },
            text = { 
                Text("Choose an action for your inventory data. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showDataDialog = false
                    }
                ) {
                    Text("Clear All Data", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    action: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = iconTint
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        action()
    }
}
