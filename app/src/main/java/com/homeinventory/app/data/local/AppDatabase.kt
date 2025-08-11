package com.homeinventory.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.homeinventory.app.data.models.*

@Database(
    entities = [
        InventoryItem::class,
        ShoppingListItem::class,
        Note::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    PurchaseHistoryConverter::class,
    DateConverter::class,
    InventoryCategoryConverter::class,
    InventorySubcategoryConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun notesDao(): NotesDao
}
