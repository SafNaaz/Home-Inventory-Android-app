package com.homeinventory.app.ui.screens.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.homeinventory.app.data.manager.InventoryStats
import com.homeinventory.app.data.models.SmartRecommendation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    navController: NavController,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights & Analytics") }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error!!)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Statistics Cards
                    item {
                        uiState.stats?.let { stats ->
                            StatisticsSection(stats = stats)
                        }
                    }
                    
                    // Smart Recommendations
                    item {
                        Text(
                            text = "Smart Recommendations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(uiState.recommendations) { recommendation ->
                        RecommendationCard(recommendation = recommendation)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsSection(stats: InventoryStats) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Inventory Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.Inventory2,
                    label = "Total Items",
                    value = stats.totalItems.toString()
                )
                StatItem(
                    icon = Icons.Default.Warning,
                    label = "Low Stock",
                    value = stats.lowStockItems.toString()
                )
                StatItem(
                    icon = Icons.Default.ShowChart,
                    label = "Avg. Stock",
                    value = "${stats.averageStockLevel}%"
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(
                    icon = Icons.Default.Schedule,
                    label = "Shopping Frequency:",
                    value = stats.estimatedShoppingFrequency
                )
                InfoRow(
                    icon = Icons.Default.ShoppingCart,
                    label = "Next Shopping Trip:",
                    value = stats.estimatedNextShoppingTrip
                )
                InfoRow(
                    icon = Icons.Default.TrendingUp,
                    label = "Efficiency Tip:",
                    value = stats.shoppingEfficiencyTip
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommendationCard(recommendation: SmartRecommendation) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (recommendation.icon) {
                        "warning" -> Icons.Default.Warning
                        "access_time" -> Icons.Default.AccessTime
                        "update" -> Icons.Default.Update
                        "inventory_2" -> Icons.Default.Inventory2
                        "add_circle" -> Icons.Default.AddCircle
                        "route" -> Icons.Default.Route
                        "shopping_cart" -> Icons.Default.ShoppingCart
                        "check_circle" -> Icons.Default.CheckCircle
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = recommendation.color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}