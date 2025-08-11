package com.homeinventory.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "shopping_list_items")
data class ShoppingListItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var isChecked: Boolean = false,
    var isTemporary: Boolean = false,
    var inventoryItemId: String? = null // Reference to inventory item if not temporary
)
