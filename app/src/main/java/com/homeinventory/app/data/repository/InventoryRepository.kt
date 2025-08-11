package com.homeinventory.app.data.repository

import com.homeinventory.app.data.database.*
import com.homeinventory.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryItemDao: InventoryItemDao,
    private val shoppingListItemDao: ShoppingListItemDao,
    private val notesDao: NotesDao,
    private val settingsDao: SettingsDao
) {
    
    // MARK: - Inventory Items
    fun getAllItems(): Flow<List<InventoryItem>> = inventoryItemDao.getAllItems()
    
    fun getItemsBySubcategory(subcategory: InventorySubcategory): Flow<List<InventoryItem>> = 
        inventoryItemDao.getItemsBySubcategory(subcategory)
    
    fun getItemsByCategory(category: InventoryCategory): Flow<List<InventoryItem>> = 
        inventoryItemDao.getAllItems().map { items ->
            items.filter { it.category == category }
        }
    
    suspend fun getItemById(id: String): InventoryItem? = inventoryItemDao.getItemById(id)
    
    suspend fun getLowStockItems(): List<InventoryItem> = inventoryItemDao.getLowStockItems()
    
    suspend fun insertItem(item: InventoryItem) = inventoryItemDao.insertItem(item)
    
    suspend fun insertItems(items: List<InventoryItem>) = inventoryItemDao.insertItems(items)
    
    suspend fun updateItem(item: InventoryItem) = inventoryItemDao.updateItem(item)
    
    suspend fun deleteItem(item: InventoryItem) = inventoryItemDao.deleteItem(item)
    
    suspend fun deleteAllItems() = inventoryItemDao.deleteAllItems()
    
    suspend fun updateItemQuantity(id: String, quantity: Double) = 
        inventoryItemDao.updateItemQuantity(id, quantity, System.currentTimeMillis())
    
    suspend fun updateItemName(id: String, name: String) = 
        inventoryItemDao.updateItemName(id, name, System.currentTimeMillis())
    
    // MARK: - Shopping List Items
    fun getAllShoppingItems(): Flow<List<ShoppingListItem>> = shoppingListItemDao.getAllShoppingItems()
    
    suspend fun getShoppingItemById(id: String): ShoppingListItem? = 
        shoppingListItemDao.getShoppingItemById(id)
    
    suspend fun insertShoppingItem(item: ShoppingListItem) = 
        shoppingListItemDao.insertShoppingItem(item)
    
    suspend fun insertShoppingItems(items: List<ShoppingListItem>) = 
        shoppingListItemDao.insertShoppingItems(items)
    
    suspend fun updateShoppingItem(item: ShoppingListItem) = 
        shoppingListItemDao.updateShoppingItem(item)
    
    suspend fun deleteShoppingItem(item: ShoppingListItem) = 
        shoppingListItemDao.deleteShoppingItem(item)
    
    suspend fun deleteAllShoppingItems() = shoppingListItemDao.deleteAllShoppingItems()
    
    suspend fun updateShoppingItemChecked(id: String, isChecked: Boolean) = 
        shoppingListItemDao.updateShoppingItemChecked(id, isChecked)
    
    // MARK: - Notes
    fun getAllNotes(): Flow<List<Note>> = notesDao.getAllNotes()
    
    suspend fun getNoteById(id: String): Note? = notesDao.getNoteById(id)
    
    suspend fun insertNote(note: Note) = notesDao.insertNote(note)
    
    suspend fun updateNote(note: Note) = notesDao.updateNote(note)
    
    suspend fun deleteNote(note: Note) = notesDao.deleteNote(note)
    
    suspend fun deleteAllNotes() = notesDao.deleteAllNotes()
    
    suspend fun getNotesCount(): Int = notesDao.getNotesCount()
    
    suspend fun canAddNote(): Boolean = getNotesCount() < 6
    
    // MARK: - Settings
    suspend fun getSettings(): AppSettings {
        return settingsDao.getSettings()?.toAppSettings() ?: AppSettings()
    }
    
    suspend fun saveSettings(settings: AppSettings) {
        val entity = AppSettingsEntity.fromAppSettings(settings)
        settingsDao.insertSettings(entity)
    }
    
    suspend fun updateShoppingState(state: ShoppingState) {
        settingsDao.updateShoppingState(state)
    }
    
    // MARK: - Analytics and Computed Properties
    suspend fun getTotalItemsCount(): Int {
        return getAllItems().first().size
    }
    
    suspend fun getLowStockItemsCount(): Int {
        return getLowStockItems().size
    }
    
    suspend fun getAverageStockLevel(): Double {
        val items = getAllItems().first()
        return if (items.isEmpty()) 0.0 else items.map { it.quantity }.average()
    }
    
    suspend fun getItemsNeedingAttention(): List<InventoryItem> {
        return getLowStockItems().sortedBy { it.quantity }
    }
    
    suspend fun getActiveCategoriesCount(): Int {
        val items = getAllItems().first()
        return items.map { it.category }.distinct().size
    }
    
    suspend fun getMostFrequentlyRestockedItem(): InventoryItem? {
        val items = getAllItems().first()
        return items.maxByOrNull { it.purchaseHistory.size }
    }
    
    suspend fun getLeastUsedItem(): InventoryItem? {
        val items = getAllItems().first()
        return items.minByOrNull { it.lastUpdated }
    }
    
    fun daysSinceLastUpdate(item: InventoryItem): Int {
        val now = System.currentTimeMillis()
        val diffInMs = now - item.lastUpdated
        return (diffInMs / (1000 * 60 * 60 * 24)).toInt()
    }
    
    fun getExpiryThreshold(item: InventoryItem): Int {
        // Kitchen items: 14 days, Others: 60 days
        return if (item.category == InventoryCategory.FRIDGE) 14 else 60
    }
    
    suspend fun isItemExpired(item: InventoryItem): Boolean {
        val daysSince = daysSinceLastUpdate(item)
        val threshold = getExpiryThreshold(item)
        return daysSince >= threshold
    }
    
    suspend fun isItemNearExpiry(item: InventoryItem): Boolean {
        val daysSince = daysSinceLastUpdate(item)
        val threshold = getExpiryThreshold(item)
        val warningThreshold = (threshold * 0.8).toInt() // 80% of threshold
        return daysSince >= warningThreshold && daysSince < threshold
    }
    
    suspend fun getExpiredItems(): List<InventoryItem> {
        val items = getAllItems().first()
        return items.filter { isItemExpired(it) }
    }
    
    suspend fun getNearExpiryItems(): List<InventoryItem> {
        val items = getAllItems().first()
        return items.filter { isItemNearExpiry(it) }
    }
    
    suspend fun getCriticalKitchenItems(): List<InventoryItem> {
        val items = getAllItems().first()
        return items.filter { item ->
            item.category == InventoryCategory.FRIDGE && daysSinceLastUpdate(item) >= 14
        }
    }
    
    suspend fun getStaleOtherItems(): List<InventoryItem> {
        val items = getAllItems().first()
        return items.filter { item ->
            item.category != InventoryCategory.FRIDGE && daysSinceLastUpdate(item) >= 60
        }
    }
    
    suspend fun getUrgentAttentionItems(): List<InventoryItem> {
        return getExpiredItems() + getNearExpiryItems()
    }
    
    suspend fun getEstimatedShoppingFrequency(): String {
        val items = getAllItems().first()
        val totalPurchases = items.sumOf { it.purchaseHistory.size }
        if (totalPurchases == 0) return "No data yet"
        
        // Calculate average days between purchases
        val itemsWithHistory = items.filter { it.purchaseHistory.size > 1 }
        if (itemsWithHistory.isEmpty()) return "Weekly"
        
        val totalDays = itemsWithHistory.map { item ->
            val sortedHistory = item.purchaseHistory.sorted()
            var totalDaysBetween = 0
            for (i in 1 until sortedHistory.size) {
                val days = ((sortedHistory[i] - sortedHistory[i-1]) / (1000 * 60 * 60 * 24)).toInt()
                totalDaysBetween += days
            }
            totalDaysBetween / (sortedHistory.size - 1)
        }.sum()
        
        val avgDays = totalDays / itemsWithHistory.size
        
        return when {
            avgDays <= 7 -> "Weekly"
            avgDays <= 14 -> "Bi-weekly"
            avgDays <= 30 -> "Monthly"
            else -> "Rarely"
        }
    }
    
    suspend fun getEstimatedNextShoppingTrip(): String {
        val lowStockItems = getLowStockItems()
        val criticalItems = getAllItems().first().filter { it.quantity <= 0.1 }
        
        return when {
            criticalItems.isNotEmpty() -> "Now (critical items)"
            lowStockItems.size >= 5 -> "This week"
            lowStockItems.isNotEmpty() -> "Next week"
            else -> "No rush"
        }
    }
    
    suspend fun getShoppingEfficiencyTip(): String {
        val lowStockItems = getLowStockItems()
        val categoryGroups = lowStockItems.groupBy { it.category }
        val maxCategory = categoryGroups.maxByOrNull { it.value.size }
        
        return if (maxCategory != null && maxCategory.value.size > 1) {
            "Focus on ${maxCategory.key.displayName} section"
        } else {
            "Spread across categories"
        }
    }
    
    suspend fun getSmartRecommendations(): List<SmartRecommendation> {
        val recommendations = mutableListOf<SmartRecommendation>()
        
        // HIGHEST PRIORITY: Expired Kitchen Items (14+ days)
        val expiredKitchenItems = getCriticalKitchenItems()
        if (expiredKitchenItems.isNotEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "🚨 URGENT: Kitchen Items Expired",
                description = "${expiredKitchenItems.size} kitchen items haven't been updated in 2+ weeks. Check for spoilage immediately!",
                icon = "error",
                color = androidx.compose.ui.graphics.Color.Red,
                priority = RecommendationPriority.HIGH
            ))
        }
        
        // HIGH PRIORITY: Expired Other Items (60+ days)
        val expiredOtherItems = getStaleOtherItems()
        if (expiredOtherItems.isNotEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "⚠️ Stale Items Alert",
                description = "${expiredOtherItems.size} items haven't been updated in 2+ months. Time to review and update!",
                icon = "schedule",
                color = androidx.compose.ui.graphics.Color(0xFFFF9500),
                priority = RecommendationPriority.HIGH
            ))
        }
        
        // MEDIUM PRIORITY: Near Expiry Items
        val nearExpiryItems = getNearExpiryItems()
        if (nearExpiryItems.isNotEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "Items Need Attention Soon",
                description = "${nearExpiryItems.size} items are approaching their update deadline. Check them this week.",
                icon = "update",
                color = androidx.compose.ui.graphics.Color(0xFFFFD60A),
                priority = RecommendationPriority.MEDIUM
            ))
        }
        
        // Critical stock recommendation
        val items = getAllItems().first()
        val criticalItems = items.filter { it.quantity <= 0.1 }
        if (criticalItems.isNotEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "Critical Stock Alert",
                description = "${criticalItems.size} items are critically low (≤10%). Consider shopping soon.",
                icon = "warning",
                color = androidx.compose.ui.graphics.Color.Red,
                priority = RecommendationPriority.HIGH
            ))
        }
        
        // Category balance recommendation
        val categoryDistribution = items.groupBy { it.category }
        val imbalancedCategories = categoryDistribution.filter { it.value.size < 2 }
        if (imbalancedCategories.isNotEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "Expand Your Inventory",
                description = "Some categories have very few items. Consider adding more items for better tracking.",
                icon = "add_circle",
                color = androidx.compose.ui.graphics.Color(0xFF007AFF),
                priority = RecommendationPriority.LOW
            ))
        }
        
        // Default recommendation if no specific insights
        if (recommendations.isEmpty()) {
            recommendations.add(SmartRecommendation(
                title = "Great Job!",
                description = "Your inventory is well-maintained. Keep tracking your items for better insights.",
                icon = "check_circle",
                color = androidx.compose.ui.graphics.Color(0xFF34C759),
                priority = RecommendationPriority.LOW
            ))
        }
        
        return recommendations.sortedWith(compareBy<SmartRecommendation> { it.priority != RecommendationPriority.HIGH }.thenBy { it.priority != RecommendationPriority.MEDIUM })
    }
    
    // MARK: - Data Management
    suspend fun clearAllData() {
        deleteAllItems()
        deleteAllShoppingItems()
        deleteAllNotes()
    }
    
    suspend fun resetToDefaults() {
        clearAllData()
        val sampleItems = DefaultItemsHelper.createSampleItems()
        insertItems(sampleItems)
    }
}
