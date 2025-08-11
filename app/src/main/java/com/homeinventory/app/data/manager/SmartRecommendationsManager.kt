package com.homeinventory.app.data.manager

import androidx.compose.ui.graphics.Color
import com.homeinventory.app.model.*
import com.homeinventory.app.data.models.RecommendationPriority
import com.homeinventory.app.data.models.SmartRecommendation
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class SmartRecommendationsManager @Inject constructor() {
    
    fun getSmartRecommendations(inventoryItems: List<InventoryItem>): List<SmartRecommendation> {
        val recommendations = mutableListOf<SmartRecommendation>()
        
        // HIGHEST PRIORITY: Critical Kitchen Items (14+ days)
        val expiredKitchenItems = getExpiredKitchenItems(inventoryItems)
        if (expiredKitchenItems.isNotEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "🚨 URGENT: Kitchen Items Need Update",
                description = "${expiredKitchenItems.size} kitchen items haven't been updated in 2+ weeks. Check for spoilage!",
                icon = "warning",
                color = Color.Red,
                priority = RecommendationPriority.HIGH
            ))
        }
        
        // HIGH PRIORITY: Expired Other Items (60+ days)
        val expiredOtherItems = getExpiredOtherItems(inventoryItems)
        if (expiredOtherItems.isNotEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "⚠️ Stale Items Alert",
                description = "${expiredOtherItems.size} items haven't been updated in 2+ months. Time to review!",
                icon = "access_time",
                color = Color(0xFFFF9800), // Orange
                priority = RecommendationPriority.HIGH
            ))
        }
        
        // MEDIUM PRIORITY: Near Expiry Items
        val nearExpiryItems = getNearExpiryItems(inventoryItems)
        if (nearExpiryItems.isNotEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "Items Need Attention Soon",
                description = "${nearExpiryItems.size} items are approaching their update deadline. Check this week.",
                icon = "update",
                color = Color(0xFFFFEB3B), // Yellow
                priority = RecommendationPriority.MEDIUM
            ))
        }
        
        // Critical stock recommendation
        val criticalItems = inventoryItems.filter { it.quantity <= 0.1f }
        if (criticalItems.isNotEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "Critical Stock Alert",
                description = "${criticalItems.size} items are critically low (≤10%). Consider shopping soon.",
                icon = "inventory_2",
                color = Color.Red,
                priority = RecommendationPriority.HIGH
            ))
        }
        
        // Category balance recommendation
        val categoryDistribution = inventoryItems.groupBy { it.category }
        val imbalancedCategories = categoryDistribution.filter { it.value.size < 2 }
        if (imbalancedCategories.isNotEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "Expand Your Inventory",
                description = "Some categories have very few items. Consider adding more for better tracking.",
                icon = "add_circle",
                color = Color.Blue,
                priority = RecommendationPriority.LOW
            ))
        }
        
        // Shopping efficiency recommendation
        val lowStockByCategory = inventoryItems
            .filter { it.needsRestocking }
            .groupBy { it.category }
        if (lowStockByCategory.size > 2) {
            recommendations.add(SmartRecommendation(
                title = "Optimize Shopping Route",
                description = "You have low stock items across ${lowStockByCategory.size} categories. Plan efficiently.",
                icon = "route",
                color = Color.Green,
                priority = RecommendationPriority.MEDIUM
            ))
        }
        
        // Frequent restocking recommendation
        val frequentItems = inventoryItems.filter { it.purchaseHistory.size > 5 }
        if (frequentItems.isNotEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "Consider Bulk Buying",
                description = "${frequentItems.size} items are restocked frequently. Consider buying in bulk.",
                icon = "shopping_cart",
                color = Color(0xFF9C27B0), // Purple
                priority = RecommendationPriority.LOW
            ))
        }
        
        // Default recommendation if no specific insights
        if (recommendations.isEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "Great Job!",
                description = "Your inventory is well-maintained. Keep tracking items for better insights.",
                icon = "check_circle",
                color = Color.Green,
                priority = RecommendationPriority.LOW
            ))
        }
        
        return recommendations.sortedByDescending { it.priority == RecommendationPriority.HIGH }
    }
    
    fun getInventoryStats(inventoryItems: List<InventoryItem>): InventoryStats {
        return InventoryStats(
            totalItems = inventoryItems.size,
            lowStockItems = inventoryItems.count { it.needsRestocking },
            averageStockLevel = getAverageStockLevel(inventoryItems),
            estimatedShoppingFrequency = calculateShoppingFrequency(inventoryItems),
            estimatedNextShoppingTrip = estimateNextShoppingTrip(inventoryItems),
            shoppingEfficiencyTip = generateShoppingEfficiencyTip(inventoryItems)
        )
    }
    
    private fun getExpiredKitchenItems(items: List<InventoryItem>): List<InventoryItem> {
        return items.filter { item ->
            item.category == InventoryCategory.FRIDGE && 
            daysSinceLastUpdate(item) >= 14
        }
    }
    
    private fun getExpiredOtherItems(items: List<InventoryItem>): List<InventoryItem> {
        return items.filter { item ->
            item.category != InventoryCategory.FRIDGE && 
            daysSinceLastUpdate(item) >= 60
        }
    }
    
    private fun getNearExpiryItems(items: List<InventoryItem>): List<InventoryItem> {
        return items.filter { item ->
            val threshold = if (item.category == InventoryCategory.FRIDGE) 14 else 60
            val warningThreshold = (threshold * 0.8).toInt()
            val days = daysSinceLastUpdate(item)
            days >= warningThreshold && days < threshold
        }
    }
    
    private fun daysSinceLastUpdate(item: InventoryItem): Int {
        val diff = Date().time - item.lastUpdated.time
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }
    
    private fun getAverageStockLevel(items: List<InventoryItem>): Int {
        if (items.isEmpty()) return 0
        val average = items.map { it.quantity }.average()
        return (average * 100f).roundToInt()
    }
    
    private fun calculateShoppingFrequency(items: List<InventoryItem>): String {
        val totalPurchases = items.sumOf { it.purchaseHistory.size }
        if (totalPurchases == 0) return "No data yet"
        
        // Calculate average days between purchases
        val averageDays = items
            .filter { it.purchaseHistory.size > 1 }
            .map { item ->
                val sortedHistory = item.purchaseHistory.sorted()
                if (sortedHistory.size < 2) return@map 0
                var totalDays = 0
                for (i in 1 until sortedHistory.size) {
                    val days = (sortedHistory[i].time - sortedHistory[i-1].time) / (24 * 60 * 60 * 1000)
                    totalDays += days.toInt()
                }
                totalDays / (sortedHistory.size - 1)
            }
            .average()
            .toInt()
        
        return when {
            averageDays == 0 -> "Weekly"
            averageDays <= 7 -> "Weekly"
            averageDays <= 14 -> "Bi-weekly"
            averageDays <= 30 -> "Monthly"
            else -> "Rarely"
        }
    }
    
    private fun estimateNextShoppingTrip(items: List<InventoryItem>): String {
        val lowStockItems = items.filter { it.needsRestocking }
        val criticalItems = items.filter { it.quantity <= 0.1 }
        
        return when {
            criticalItems.isNotEmpty() -> "Now (critical items)"
            lowStockItems.size >= 5 -> "This week"
            lowStockItems.isNotEmpty() -> "Next week"
            else -> "No rush"
        }
    }
    
    private fun generateShoppingEfficiencyTip(items: List<InventoryItem>): String {
        val categoryGroups = items
            .filter { it.needsRestocking }
            .groupBy { it.category }
        
        val maxCategory = categoryGroups.maxByOrNull { it.value.size }
        
        return if (maxCategory != null && maxCategory.value.size > 1) {
            "Focus on ${maxCategory.key.displayName} section"
        } else {
            "Spread across categories"
        }
    }
}

data class InventoryStats(
    val totalItems: Int,
    val lowStockItems: Int,
    val averageStockLevel: Int,
    val estimatedShoppingFrequency: String,
    val estimatedNextShoppingTrip: String,
    val shoppingEfficiencyTip: String
)
