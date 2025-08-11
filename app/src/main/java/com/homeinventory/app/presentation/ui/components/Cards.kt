package com.homeinventory.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeinventory.app.data.model.InventoryCategory
import com.homeinventory.app.data.model.InventorySubcategory
import com.homeinventory.app.data.model.SmartRecommendation
import com.homeinventory.app.data.model.RecommendationPriority

// MARK: - Category Card
@Composable
fun CategoryCard(
    category: InventoryCategory,
    itemsCount: Int,
    lowStockCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            // Large Icon
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = category.displayName,
                modifier = Modifier.size(50.dp),
                tint = category.color
            )
            
            // Category Name
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Stats
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$itemsCount items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (lowStockCount > 0) {
                    Text(
                        text = "$lowStockCount need restocking",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

// MARK: - Subcategory Row
@Composable
fun SubcategoryRow(
    subcategory: InventorySubcategory,
    itemsCount: Int,
    lowStockCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = getSubcategoryIcon(subcategory),
            contentDescription = subcategory.displayName,
            modifier = Modifier.size(30.dp),
            tint = subcategory.color
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = subcategory.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "$itemsCount items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (lowStockCount > 0) {
                Text(
                    text = "$lowStockCount need restocking",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// MARK: - Insight Card
@Composable
fun InsightCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// MARK: - Insight Row Card
@Composable
fun InsightRowCard(
    title: String,
    subtitle: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                tint = color
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
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

// MARK: - Recommendation Card
@Composable
fun RecommendationCard(
    recommendation: SmartRecommendation,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (recommendation.priority) {
        RecommendationPriority.HIGH -> Color.Red.copy(alpha = 0.1f)
        RecommendationPriority.MEDIUM -> Color(0xFFFF9500).copy(alpha = 0.1f)
        RecommendationPriority.LOW -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when (recommendation.priority) {
        RecommendationPriority.HIGH -> Color.Red.copy(alpha = 0.3f)
        RecommendationPriority.MEDIUM -> Color(0xFFFF9500).copy(alpha = 0.3f)
        RecommendationPriority.LOW -> Color.Transparent
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = if (borderColor != Color.Transparent) {
            androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = getRecommendationIcon(recommendation.icon),
                contentDescription = recommendation.title,
                modifier = Modifier.size(24.dp),
                tint = recommendation.color
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = recommendation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (recommendation.priority == RecommendationPriority.HIGH) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "High Priority",
                    tint = Color.Red
                )
            }
        }
    }
}

// MARK: - Urgent Alert Banner
@Composable
fun UrgentAlertBanner(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                tint = color
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = color
            )
        }
    }
}

// MARK: - Helper Functions for Icons
@Composable
private fun getCategoryIcon(category: InventoryCategory): ImageVector {
    return when (category) {
        InventoryCategory.FRIDGE -> Icons.Default.Kitchen
        InventoryCategory.GROCERY -> Icons.Default.LocalGroceryStore
        InventoryCategory.HYGIENE -> Icons.Default.CleaningServices
        InventoryCategory.PERSONAL_CARE -> Icons.Default.Face
    }
}

@Composable
private fun getSubcategoryIcon(subcategory: InventorySubcategory): ImageVector {
    return when (subcategory) {
        // Fridge subcategories
        InventorySubcategory.DOOR_BOTTLES -> Icons.Default.LocalDrink
        InventorySubcategory.TRAY -> Icons.Default.Dining
        InventorySubcategory.MAIN -> Icons.Default.Kitchen
        InventorySubcategory.VEGETABLE -> Icons.Default.Eco
        InventorySubcategory.FREEZER -> Icons.Default.AcUnit
        InventorySubcategory.MINI_COOLER -> Icons.Default.Icecream
        
        // Grocery subcategories
        InventorySubcategory.RICE -> Icons.Default.RiceBowl
        InventorySubcategory.PULSES -> Icons.Default.Grain
        InventorySubcategory.CEREALS -> Icons.Default.Dining
        InventorySubcategory.CONDIMENTS -> Icons.Default.Restaurant
        InventorySubcategory.OILS -> Icons.Default.Opacity
        
        // Hygiene subcategories
        InventorySubcategory.WASHING -> Icons.Default.LocalLaundryService
        InventorySubcategory.DISHWASHING -> Icons.Default.Restaurant
        InventorySubcategory.TOILET_CLEANING -> Icons.Default.Wc
        InventorySubcategory.KIDS -> Icons.Default.ChildCare
        InventorySubcategory.GENERAL_CLEANING -> Icons.Default.AutoAwesome
        
        // Personal Care subcategories
        InventorySubcategory.FACE -> Icons.Default.Face
        InventorySubcategory.BODY -> Icons.Default.AccessibilityNew
        InventorySubcategory.HEAD -> Icons.Default.Psychology
    }
}

@Composable
private fun getRecommendationIcon(iconName: String): ImageVector {
    return when (iconName) {
        "error" -> Icons.Default.Error
        "schedule" -> Icons.Default.Schedule
        "update" -> Icons.Default.Update
        "warning" -> Icons.Default.Warning
        "add_circle" -> Icons.Default.AddCircle
        "check_circle" -> Icons.Default.CheckCircle
        else -> Icons.Default.Info
    }
}
