package com.homeinventory.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

@Entity(tableName = "inventory_items")
@TypeConverters(PurchaseHistoryConverter::class)
data class InventoryItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var quantity: Double, // 0.0 to 1.0 (0% to 100%)
    var subcategory: InventorySubcategory,
    var isCustom: Boolean = false,
    var purchaseHistory: List<Date> = listOf(),
    var lastUpdated: Date = Date()
) {
    val category: InventoryCategory
        get() = subcategory.category

    val quantityPercentage: Int
        get() = (quantity * 100).toInt()

    val needsRestocking: Boolean
        get() = quantity <= 0.25

    fun updateQuantity(newQuantity: Double) {
        quantity = newQuantity.coerceIn(0.0, 1.0)
        lastUpdated = Date()
    }

    fun restockToFull() {
        quantity = 1.0
        purchaseHistory = purchaseHistory + Date()
        lastUpdated = Date()
    }
}

class PurchaseHistoryConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): List<Date> {
        val type = object : TypeToken<List<Date>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<Date>): String {
        return gson.toJson(list)
    }
}
