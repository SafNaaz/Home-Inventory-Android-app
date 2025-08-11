package com.homeinventory.app.data.database

import androidx.room.*
import com.homeinventory.app.data.model.*
import kotlinx.coroutines.flow.Flow

// MARK: - Type Converters
class Converters {
    @TypeConverter
    fun fromInventorySubcategory(subcategory: InventorySubcategory): String {
        return subcategory.name
    }

    @TypeConverter
    fun toInventorySubcategory(subcategoryName: String): InventorySubcategory {
        return InventorySubcategory.valueOf(subcategoryName)
    }

    @TypeConverter
    fun fromShoppingState(state: ShoppingState): String {
        return state.name
    }

    @TypeConverter
    fun toShoppingState(stateName: String): ShoppingState {
        return ShoppingState.valueOf(stateName)
    }

    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { it.toLong() }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString("|||")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split("|||")
    }
}

// MARK: - Inventory Item DAO
@Dao
interface InventoryItemDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE subcategory = :subcategory ORDER BY name ASC")
    fun getItemsBySubcategory(subcategory: InventorySubcategory): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: String): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE quantity <= 0.25 ORDER BY quantity ASC")
    suspend fun getLowStockItems(): List<InventoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItem>)

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAllItems()

    @Query("UPDATE inventory_items SET quantity = :quantity, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateItemQuantity(id: String, quantity: Double, timestamp: Long)

    @Query("UPDATE inventory_items SET name = :name, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateItemName(id: String, name: String, timestamp: Long)
}

// MARK: - Shopping List Item DAO
@Dao
interface ShoppingListItemDao {
    @Query("SELECT * FROM shopping_items ORDER BY name ASC")
    fun getAllShoppingItems(): Flow<List<ShoppingListItem>>

    @Query("SELECT * FROM shopping_items WHERE id = :id")
    suspend fun getShoppingItemById(id: String): ShoppingListItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingListItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItems(items: List<ShoppingListItem>)

    @Update
    suspend fun updateShoppingItem(item: ShoppingListItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingListItem)

    @Query("DELETE FROM shopping_items")
    suspend fun deleteAllShoppingItems()

    @Query("UPDATE shopping_items SET isChecked = :isChecked WHERE id = :id")
    suspend fun updateShoppingItemChecked(id: String, isChecked: Boolean)
}

// MARK: - Notes DAO
@Dao
interface NotesDao {
    @Query("SELECT * FROM notes ORDER BY lastModified DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNotesCount(): Int
}

// MARK: - Settings Entity for Room
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isDarkMode: Boolean = false,
    val isSecurityEnabled: Boolean = false,
    val isInventoryReminderEnabled: Boolean = false,
    val isSecondReminderEnabled: Boolean = false,
    val reminderTime1: String = "09:00",
    val reminderTime2: String = "18:00",
    val shoppingState: ShoppingState = ShoppingState.EMPTY,
    val miscItemHistory: List<String> = emptyList()
) {
    fun toAppSettings(): AppSettings {
        return AppSettings(
            isDarkMode = isDarkMode,
            isSecurityEnabled = isSecurityEnabled,
            isInventoryReminderEnabled = isInventoryReminderEnabled,
            isSecondReminderEnabled = isSecondReminderEnabled,
            reminderTime1 = reminderTime1,
            reminderTime2 = reminderTime2,
            shoppingState = shoppingState,
            miscItemHistory = miscItemHistory
        )
    }

    companion object {
        fun fromAppSettings(settings: AppSettings): AppSettingsEntity {
            return AppSettingsEntity(
                isDarkMode = settings.isDarkMode,
                isSecurityEnabled = settings.isSecurityEnabled,
                isInventoryReminderEnabled = settings.isInventoryReminderEnabled,
                isSecondReminderEnabled = settings.isSecondReminderEnabled,
                reminderTime1 = settings.reminderTime1,
                reminderTime2 = settings.reminderTime2,
                shoppingState = settings.shoppingState,
                miscItemHistory = settings.miscItemHistory
            )
        }
    }
}

// MARK: - Settings DAO
@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettingsEntity)

    @Update
    suspend fun updateSettings(settings: AppSettingsEntity)

    @Query("UPDATE app_settings SET shoppingState = :state WHERE id = 1")
    suspend fun updateShoppingState(state: ShoppingState)
}

// MARK: - Database
@Database(
    entities = [InventoryItem::class, ShoppingListItem::class, Note::class, AppSettingsEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HomeInventoryDatabase : RoomDatabase() {
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun shoppingListItemDao(): ShoppingListItemDao
    abstract fun notesDao(): NotesDao
    abstract fun settingsDao(): SettingsDao
}
