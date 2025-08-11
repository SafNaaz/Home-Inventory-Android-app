package com.homeinventory.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import com.homeinventory.app.data.model.*
import com.homeinventory.app.presentation.ui.components.*
import com.homeinventory.app.presentation.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val totalItems by viewModel.totalItems.collectAsState()
    val lowStockItemsCount by viewModel.lowStockItemsCount.collectAsState()
    val averageStockLevel by viewModel.averageStockLevel.collectAsState()
    val activeCategoriesCount by viewModel.activeCategoriesCount.collectAsState()
    val urgentAttentionItems by viewModel.urgentAttentionItems.collectAsState()
    
    var smartRecommendations by remember { mutableStateOf<List<SmartRecommendation>>(emptyList()) }
    var shoppingFrequency by remember { mutableStateOf("Loading...") }
    var nextShoppingTrip by remember { mutableStateOf("Loading...") }
    var shoppingTip by remember { mutableStateOf("Loading...") }
    
    LaunchedEffect(inventoryItems) {
        smartRecommendations = viewModel.getSmartRecommendations()
        shoppingFrequency = viewModel.getEstimatedShoppingFrequency()
        nextShoppingTrip = viewModel.getEstimatedNextShoppingTrip()
        shoppingTip = viewModel.getShoppingEfficiencyTip()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Insights",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Urgent Alerts Section (if any)
            if (urgentAttentionItems.isNotEmpty()) {
                item {
                    UrgentItemsSection(viewModel, urgentAttentionItems)
                }
            }
            
            // Overview Cards
            item {
                OverviewCardsSection(
                    totalItems = totalItems,
                    lowStockItemsCount = lowStockItemsCount,
                    averageStockLevel = averageStockLevel,
                    activeCategoriesCount = activeCategoriesCount
                )
            }
            
            // Usage Patterns
            item {
                UsagePatternsSection(viewModel, inventoryItems)
            }
            
            // Category Analysis
            item {
                CategoryAnalysisSection(inventoryItems)
            }
            
            // Shopping Insights
            item {
                ShoppingInsightsSection(
                    shoppingFrequency = shoppingFrequency,
                    nextShoppingTrip = nextShoppingTrip,
                    shoppingTip = shoppingTip
                )
            }
            
            // Recommendations
            item {
                RecommendationsSection(smartRecommendations)
            }
        }
    }
}

@Composable
private fun UrgentItemsSection(
    viewModel: InventoryViewModel,
    urgentAttentionItems: List<InventoryItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "🚨 Urgent Attention Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )
        
        // Critical Kitchen Items (14+ days)
        val criticalKitchenItems = urgentAttentionItems.filter { item ->
            item.category == InventoryCategory.FRIDGE && 
            viewModel.repository.daysSinceLastUpdate(item) >= 14
        }
        
        if (criticalKitchenItems.isNotEmpty()) {
            UrgentAlertBanner(
                title = "🚨 URGENT: Kitchen Items Expired",
                subtitle = "${criticalKitchenItems.size} kitchen items haven't been updated in 2+ weeks. Check for spoilage immediately!",
                icon = Icons.Filled.Error,
                color = Color.Red,
                onClick = { /* TODO: Navigate to items */ }
            )
        }
        
        // Stale Other Items (60+ days)
        val staleOtherItems = urgentAttentionItems.filter { item ->
            item.category != InventoryCategory.FRIDGE && 
            viewModel.repository.daysSinceLastUpdate(item) >= 60
        }
        
        if (staleOtherItems.isNotEmpty()) {
            UrgentAlertBanner(
                title = "⚠️ Stale Items Alert",
                subtitle = "${staleOtherItems.size} items haven't been updated in 2+ months. Time to review and update!",
                icon = Icons.Filled.Schedule,
                color = Color(0xFFFF9500),
                onClick = { /* TODO: Navigate to items */ }
            )
        }
        
        // Near Expiry Items
        val nearExpiryItems = urgentAttentionItems.filter { item ->
            val daysSince = viewModel.repository.daysSinceLastUpdate(item)
            val threshold = viewModel.repository.getExpiryThreshold(item)
            val warningThreshold = (threshold * 0.8).toInt()
            daysSince >= warningThreshold && daysSince < threshold
        }
        
        if (nearExpiryItems.isNotEmpty()) {
            UrgentAlertBanner(
                title = "Items Need Attention Soon",
                subtitle = "${nearExpiryItems.size} items are approaching their update deadline. Check them this week.",
                icon = Icons.Filled.Update,
                color = Color(0xFFFFD60A),
                onClick = { /* TODO: Navigate to items */ }
            )
        }
    }
}

@Composable
private fun OverviewCardsSection(
    totalItems: Int,
    lowStockItemsCount: Int,
    averageStockLevel: Double,
    activeCategoriesCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(220.dp)
        ) {
            items(
                listOf(
                    Triple("Total Items", totalItems.toString(), Icons.Filled.Inventory),
                    Triple("Low Stock", lowStockItemsCount.toString(), Icons.Filled.Warning),
                    Triple("Avg Stock", "${(averageStockLevel * 100).toInt()}%", Icons.Filled.Analytics),
                    Triple("Categories", activeCategoriesCount.toString(), Icons.Filled.Category)
                )
            ) { (title, value, icon) ->
                val color = when (title) {
                    "Total Items" -> Color(0xFF007AFF)
                    "Low Stock" -> if (lowStockItemsCount > 0) Color.Red else Color(0xFF34C759)
                    "Avg Stock" -> Color(0xFFFF9500)
                    "Categories" -> Color(0xFF5856D6)
                    else -> MaterialTheme.colorScheme.primary
                }
                
                InsightCard(
                    title = title,
                    value = value,
                    icon = icon,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun UsagePatternsSection(
    viewModel: InventoryViewModel,
    inventoryItems: List<InventoryItem>
) {
    val mostFrequentlyRestockedItem = inventoryItems.maxByOrNull { it.purchaseHistory.size }
    val leastUsedItem = inventoryItems.minByOrNull { it.lastUpdated }
    val lowStockItems = inventoryItems.filter { it.needsRestocking }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Usage Patterns",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightRowCard(
                title = "Most Restocked",
                subtitle = mostFrequentlyRestockedItem?.name ?: "No data yet",
                value = mostFrequentlyRestockedItem?.let { "${it.purchaseHistory.size} times" } ?: "",
                icon = Icons.Filled.Refresh,
                color = Color(0xFF34C759)
            )
            
            InsightRowCard(
                title = "Least Used",
                subtitle = leastUsedItem?.name ?: "No data yet",
                value = leastUsedItem?.let { 
                    "Last updated ${viewModel.repository.daysSinceLastUpdate(it)} days ago" 
                } ?: "",
                icon = Icons.Filled.Schedule,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            InsightRowCard(
                title = "Need Attention",
                subtitle = "${lowStockItems.size} items below 25%",
                value = if (lowStockItems.isEmpty()) "All good!" else "Check inventory",
                icon = Icons.Filled.Visibility,
                color = if (lowStockItems.isEmpty()) Color(0xFF34C759) else Color(0xFFFF9500)
            )
        }
    }
}

@Composable
private fun CategoryAnalysisSection(inventoryItems: List<InventoryItem>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Category Analysis",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InventoryCategory.values().forEach { category ->
                val items = inventoryItems.filter { it.category == category }
                if (items.isNotEmpty()) {
                    CategoryInsightRow(category, items)
                }
            }
        }
    }
}

@Composable
private fun CategoryInsightRow(
    category: InventoryCategory,
    items: List<InventoryItem>
) {
    val lowStockCount = items.count { it.needsRestocking }
    val averageStock = if (items.isEmpty()) 0.0 else items.map { it.quantity }.average()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = when (category) {
                    InventoryCategory.FRIDGE -> Icons.Filled.Kitchen
                    InventoryCategory.GROCERY -> Icons.Filled.LocalGroceryStore
                    InventoryCategory.HYGIENE -> Icons.Filled.CleaningServices
                    InventoryCategory.PERSONAL_CARE -> Icons.Filled.Face
                },
                contentDescription = category.displayName,
                tint = category.color,
                modifier = Modifier.size(30.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "${items.size} items • ${(averageStock * 100).toInt()}% avg stock",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (lowStockCount > 0) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = lowStockCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    
                    Text(
                        text = "low stock",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "All Good",
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Text(
                        text = "all good",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF34C759)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingInsightsSection(
    shoppingFrequency: String,
    nextShoppingTrip: String,
    shoppingTip: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Shopping Insights",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightRowCard(
                title = "Shopping Frequency",
                subtitle = "Based on restock patterns",
                value = shoppingFrequency,
                icon = Icons.Filled.ShoppingCart,
                color = Color(0xFF007AFF)
            )
            
            InsightRowCard(
                title = "Next Shopping Trip",
                subtitle = "Estimated based on current stock levels",
                value = nextShoppingTrip,
                icon = Icons.Filled.CalendarToday,
                color = Color(0xFF5856D6)
            )
            
            InsightRowCard(
                title = "Shopping Efficiency",
                subtitle = "Items typically bought together",
                value = shoppingTip,
                icon = Icons.Filled.Lightbulb,
                color = Color(0xFFFFD60A)
            )
        }
    }
}

@Composable
private fun RecommendationsSection(smartRecommendations: List<SmartRecommendation>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Smart Recommendations",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            smartRecommendations.forEach { recommendation ->
                RecommendationCard(recommendation = recommendation)
            }
        }
    }
}
