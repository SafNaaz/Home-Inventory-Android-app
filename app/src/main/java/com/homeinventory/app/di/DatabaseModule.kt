package com.homeinventory.app.di

import android.content.Context
import androidx.room.Room
import com.homeinventory.app.data.database.*
import com.homeinventory.app.data.repository.InventoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideHomeInventoryDatabase(@ApplicationContext context: Context): HomeInventoryDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            HomeInventoryDatabase::class.java,
            "home_inventory_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideInventoryItemDao(database: HomeInventoryDatabase): InventoryItemDao {
        return database.inventoryItemDao()
    }
    
    @Provides
    fun provideShoppingListItemDao(database: HomeInventoryDatabase): ShoppingListItemDao {
        return database.shoppingListItemDao()
    }
    
    @Provides
    fun provideNotesDao(database: HomeInventoryDatabase): NotesDao {
        return database.notesDao()
    }
    
    @Provides
    fun provideSettingsDao(database: HomeInventoryDatabase): SettingsDao {
        return database.settingsDao()
    }
    
    @Provides
    @Singleton
    fun provideInventoryRepository(
        inventoryItemDao: InventoryItemDao,
        shoppingListItemDao: ShoppingListItemDao,
        notesDao: NotesDao,
        settingsDao: SettingsDao
    ): InventoryRepository {
        return InventoryRepository(inventoryItemDao, shoppingListItemDao, notesDao, settingsDao)
    }
}