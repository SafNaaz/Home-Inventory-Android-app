package com.homeinventory.app.data.local

import androidx.room.*
import com.homeinventory.app.data.models.ShoppingListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list_items")
    fun getAllItems(): Flow<List<ShoppingListItem>>

    @Query("SELECT * FROM shopping_list_items WHERE isChecked = 0")
    fun getUncheckedItems(): Flow<List<ShoppingListItem>>

    @Query("SELECT * FROM shopping_list_items WHERE isChecked = 1")
    fun getCheckedItems(): Flow<List<ShoppingListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingListItem)

    @Delete
    suspend fun deleteItem(item: ShoppingListItem)

    @Update
    suspend fun updateItem(item: ShoppingListItem)

    @Query("DELETE FROM shopping_list_items")
    suspend fun clearShoppingList()

    @Query("SELECT COUNT(*) FROM shopping_list_items")
    fun getShoppingListSize(): Flow<Int>
}
