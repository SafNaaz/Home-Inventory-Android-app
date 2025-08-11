package com.homeinventory.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.ui.graphics.Color
import java.util.*

// MARK: - Inventory Categories
enum class InventoryCategory(
    val displayName: String,
    val icon: String,
    val color: androidx.compose.ui.graphics.Color
) {
    FRIDGE("Fridge", "kitchen", Color(0xFF007AFF)),
    GROCERY("Grocery", "local_grocery_store", Color(0xFF34C759)),
    HYGIENE("Hygiene", "cleaning_services", Color(0xFF00C7BE)),
    PERSONAL_CARE("Personal Care", "face_retouching_natural", Color(0xFFFF2D92));

    fun getSubcategories(): List<InventorySubcategory> {
        return when (this) {
            FRIDGE -> listOf(
                InventorySubcategory.DOOR_BOTTLES,
                InventorySubcategory.TRAY,
                InventorySubcategory.MAIN,
                InventorySubcategory.VEGETABLE,
                InventorySubcategory.FREEZER,
                InventorySubcategory.MINI_COOLER
            )
            GROCERY -> listOf(
                InventorySubcategory.RICE,
                InventorySubcategory.PULSES,
                InventorySubcategory.CEREALS,
                InventorySubcategory.CONDIMENTS,
                InventorySubcategory.OILS
            )
            HYGIENE -> listOf(
                InventorySubcategory.WASHING,
                InventorySubcategory.DISHWASHING,
                InventorySubcategory.TOILET_CLEANING,
                InventorySubcategory.KIDS,
                InventorySubcategory.GENERAL_CLEANING
            )
            PERSONAL_CARE -> listOf(
                InventorySubcategory.FACE,
                InventorySubcategory.BODY,
                InventorySubcategory.HEAD
            )
        }
    }
}

// MARK: - Inventory Subcategories
enum class InventorySubcategory(
    val displayName: String,
    val icon: String,
    val color: androidx.compose.ui.graphics.Color,
    val category: InventoryCategory
) {
    // Fridge subcategories
    DOOR_BOTTLES("Door Bottles", "water_bottle", Color(0xFF007AFF), InventoryCategory.FRIDGE),
    TRAY("Tray Section", "breakfast_dining", Color(0xFFFF9500), InventoryCategory.FRIDGE),
    MAIN("Main Section", "kitchen", Color(0xFF34C759), InventoryCategory.FRIDGE),
    VEGETABLE("Vegetable Section", "eco", Color(0xFF30D158), InventoryCategory.FRIDGE),
    FREEZER("Freezer", "ac_unit", Color(0xFF00C7BE), InventoryCategory.FRIDGE),
    MINI_COOLER("Mini Cooler", "icecream", Color(0xFF5856D6), InventoryCategory.FRIDGE),

    // Grocery subcategories
    RICE("Rice Items", "rice_bowl", Color(0xFF8E6C42), InventoryCategory.GROCERY),
    PULSES("Pulses", "grain", Color(0xFFFFD60A), InventoryCategory.GROCERY),
    CEREALS("Cereals", "bakery_dining", Color(0xFFFF9500), InventoryCategory.GROCERY),
    CONDIMENTS("Condiments", "local_dining", Color(0xFFFF3B30), InventoryCategory.GROCERY),
    OILS("Oils", "opacity", Color(0xFFFFD60A), InventoryCategory.GROCERY),

    // Hygiene subcategories
    WASHING("Washing", "local_laundry_service", Color(0xFF007AFF), InventoryCategory.HYGIENE),
    DISHWASHING("Dishwashing", "restaurant", Color(0xFF34C759), InventoryCategory.HYGIENE),
    TOILET_CLEANING("Toilet Cleaning", "wc", Color(0xFF00C7BE), InventoryCategory.HYGIENE),
    KIDS("Kids", "child_care", Color(0xFFFF2D92), InventoryCategory.HYGIENE),
    GENERAL_CLEANING("General Cleaning", "auto_awesome", Color(0xFF5856D6), InventoryCategory.HYGIENE),

    // Personal Care subcategories
    FACE("Face", "face", Color(0xFFFF2D92), InventoryCategory.PERSONAL_CARE),
    BODY("Body", "accessibility_new", Color(0xFF30D158), InventoryCategory.PERSONAL_CARE),
    HEAD("Head", "psychology", Color(0xFF5856D6), InventoryCategory.PERSONAL_CARE);
}

// MARK: - Shopping Workflow States
enum class ShoppingState {
    EMPTY,           // No shopping list
    GENERATING,      // Generating/editing list
    LIST_READY,      // List created, not editable
    SHOPPING         // Shopping in progress, checklist unlocked
}

// MARK: - Recommendation Priority
enum class RecommendationPriority {
    LOW, MEDIUM, HIGH
}

// MARK: - Inventory Item Model
@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: Double = 1.0, // 0.0 to 1.0 (0% to 100%)
    val subcategory: InventorySubcategory,
    val isCustom: Boolean = false,
    val purchaseHistory: List<Long> = emptyList(), // Timestamps
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val category: InventoryCategory
        get() = subcategory.category

    val quantityPercentage: Int
        get() = (quantity * 100).toInt()

    val needsRestocking: Boolean
        get() = quantity <= 0.25

    fun updateQuantity(newQuantity: Double): InventoryItem {
        return copy(
            quantity = newQuantity.coerceIn(0.0, 1.0),
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun restockToFull(): InventoryItem {
        return copy(
            quantity = 1.0,
            purchaseHistory = purchaseHistory + System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    }
}

// MARK: - Shopping List Item
@Entity(tableName = "shopping_items")
data class ShoppingListItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isChecked: Boolean = false,
    val isTemporary: Boolean = false, // For misc items that don't update inventory
    val inventoryItemId: String? = null // Reference to inventory item if not temporary
) {
    fun toggle(): ShoppingListItem {
        return copy(isChecked = !isChecked)
    }
}

// MARK: - Note Model
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
) {
    fun updateContent(newTitle: String, newContent: String): Note {
        return copy(
            title = newTitle,
            content = newContent,
            lastModified = System.currentTimeMillis()
        )
    }
}

// MARK: - Smart Recommendation Model
data class SmartRecommendation(
    val title: String,
    val description: String,
    val icon: String,
    val color: androidx.compose.ui.graphics.Color,
    val priority: RecommendationPriority
)

// MARK: - Settings Model
data class AppSettings(
    val isDarkMode: Boolean = false,
    val isSecurityEnabled: Boolean = false,
    val isInventoryReminderEnabled: Boolean = false,
    val isSecondReminderEnabled: Boolean = false,
    val reminderTime1: String = "09:00", // HH:mm format
    val reminderTime2: String = "18:00", // HH:mm format
    val shoppingState: ShoppingState = ShoppingState.EMPTY,
    val miscItemHistory: List<String> = emptyList()
) {
    fun getMiscItemSuggestions(): List<String> {
        return miscItemHistory.distinct().take(10)
    }

    fun addMiscItemToHistory(item: String): AppSettings {
        val newHistory = (miscItemHistory + item).distinct().takeLast(20)
        return copy(miscItemHistory = newHistory)
    }
}
