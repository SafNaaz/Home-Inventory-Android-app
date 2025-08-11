package com.homeinventory.app.data.local

import androidx.room.*
import com.homeinventory.app.data.models.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: String): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Query("SELECT * FROM inventory_items WHERE quantity <= 0.25")
    fun getLowStockItems(): Flow<List<InventoryItem>>

    @Query("SELECT COUNT(*) FROM inventory_items")
    fun getTotalItemsCount(): Flow<Int>

    @Query("SELECT AVG(quantity) FROM inventory_items")
    fun getAverageStockLevel(): Flow<Double>
}
