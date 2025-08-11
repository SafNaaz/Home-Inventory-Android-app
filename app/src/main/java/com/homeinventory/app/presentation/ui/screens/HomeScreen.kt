package com.homeinventory.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.homeinventory.app.data.model.InventoryCategory
import com.homeinventory.app.presentation.ui.components.*
import com.homeinventory.app.presentation.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCategoryClick: (InventoryCategory) -> Unit,
    onShoppingClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val totalItems by viewModel.totalItems.collectAsState()
    val lowStockItemsCount by viewModel.lowStockItemsCount.collectAsState()
    val urgentAttentionItems by viewModel.urgentAttentionItems.collectAsState()
    val settings by viewModel.settings.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Household Inventory",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { /* TODO: Reminder settings */ }) {
                        Icon(
                            imageVector = if (settings.isInventoryReminderEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                            contentDescription = "Notifications",
                            tint = if (settings.isInventoryReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                        Icon(
                            imageVector = if (settings.isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (settings.isDarkMode) Color(0xFFFF9500) else Color(0xFF5856D6)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (settings.shoppingState) {
                        com.homeinventory.app.data.model.ShoppingState.EMPTY -> {
                            viewModel.startGeneratingShoppingList()
                            onShoppingClick()
                        }
                        else -> {
                            // Show alert or go to shopping directly
                            onShoppingClick()
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "Generate Shopping List"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Urgent Alerts Banner (if any)
            if (urgentAttentionItems.isNotEmpty()) {
                LaunchedEffect(urgentAttentionItems) {
                    val criticalKitchenItems = urgentAttentionItems.filter { item ->
                        item.category == InventoryCategory.FRIDGE && 
                        viewModel.repository.daysSinceLastUpdate(item) >= 14
                    }
                    
                    val staleOtherItems = urgentAttentionItems.filter { item ->
                        item.category != InventoryCategory.FRIDGE && 
                        viewModel.repository.daysSinceLastUpdate(item) >= 60
                    }
                    
                    if (criticalKitchenItems.isNotEmpty()) {
                        // Show critical kitchen items alert
                    } else if (staleOtherItems.isNotEmpty()) {
                        // Show stale items alert
                    }
                }
                
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // You can add urgent alert banners here if needed
                    // For now, we'll keep it simple
                }
            }
            
            // Quick Stats Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Total Items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = totalItems.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Low Stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = lowStockItemsCount.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (lowStockItemsCount > 0) Color.Red else Color(0xFF34C759)
                        )
                    }
                }
            }
            
            // Category Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(InventoryCategory.values().toList()) { category ->
                    val categoryItems = inventoryItems.filter { it.category == category }
                    val lowStockCount = categoryItems.count { it.needsRestocking }
                    
                    CategoryCard(
                        category = category,
                        itemsCount = categoryItems.size,
                        lowStockCount = lowStockCount,
                        onClick = { onCategoryClick(category) }
                    )
                }
            }
        }
    }
}
