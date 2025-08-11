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
                        var showTimePickerDialog1 by remember { mutableStateOf(false) }
                        
                        SettingsItem(
                            icon = Icons.Filled.Schedule,
                            title = "First Reminder",
                            subtitle = "Daily reminder at ${settings.reminderTime1}",
                            iconTint = Color(0xFFFF9500)
                        ) {
                            TextButton(onClick = { showTimePickerDialog1 = true }) {
                                Text(settings.reminderTime1)
                            }
                        }
                        
                        if (showTimePickerDialog1) {
                            TimePickerDialog(
                                initialTime = settings.reminderTime1,
                                onDismiss = { showTimePickerDialog1 = false },
                                onTimeSelected = { selectedTime ->
                                    viewModel.updateReminderTimes(selectedTime, if (settings.isSecondReminderEnabled) settings.reminderTime2 else null)
                                    showTimePickerDialog1 = false
                                }
                            )
                        }
                        
                        var showTimePickerDialog2 by remember { mutableStateOf(false) }
                        
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
                                TextButton(onClick = { showTimePickerDialog2 = true }) {
                                    Text(settings.reminderTime2)
                                }
                            }
                            
                            if (showTimePickerDialog2) {
                                TimePickerDialog(
                                    initialTime = settings.reminderTime2,
                                    onDismiss = { showTimePickerDialog2 = false },
                                    onTimeSelected = { selectedTime ->
                                        viewModel.updateReminderTimes(settings.reminderTime1, selectedTime)
                                        showTimePickerDialog2 = false
                                    }
                                )
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

@Composable
private fun TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialTime.split(":")[0].toInt()) }
    var selectedMinute by remember { mutableStateOf(initialTime.split(":")[1].toInt()) }
    var is24HourFormat by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Hour selection
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = { 
                            selectedHour = if (is24HourFormat) {
                                if (selectedHour >= 23) 0 else selectedHour + 1
                            } else {
                                if (selectedHour >= 12) 1 else selectedHour + 1
                            }
                        }) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increase hour")
                        }
                        
                        Text(
                            text = String.format("%02d", selectedHour),
                            style = MaterialTheme.typography.headlineLarge
                        )
                        
                        IconButton(onClick = { 
                            selectedHour = if (is24HourFormat) {
                                if (selectedHour <= 0) 23 else selectedHour - 1
                            } else {
                                if (selectedHour <= 1) 12 else selectedHour - 1
                            }
                        }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease hour")
                        }
                    }
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    
                    // Minute selection
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = { 
                            selectedMinute = if (selectedMinute >= 59) 0 else selectedMinute + 1
                        }) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increase minute")
                        }
                        
                        Text(
                            text = String.format("%02d", selectedMinute),
                            style = MaterialTheme.typography.headlineLarge
                        )
                        
                        IconButton(onClick = { 
                            selectedMinute = if (selectedMinute <= 0) 59 else selectedMinute - 1
                        }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease minute")
                        }
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("24-hour format")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = is24HourFormat,
                        onCheckedChange = { 
                            is24HourFormat = it
                            if (!is24HourFormat && selectedHour > 12) {
                                // Convert 24h to 12h
                                selectedHour -= 12
                            } else if (is24HourFormat && !it && selectedHour == 0) {
                                selectedHour = 12
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    onTimeSelected(formattedTime)
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
