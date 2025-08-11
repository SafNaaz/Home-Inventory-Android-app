package com.homeinventory.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeinventory.app.data.model.*
import com.homeinventory.app.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    val repository: InventoryRepository
) : ViewModel() {
    
    // MARK: - State Flows
    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems.asStateFlow()
    
    private val _shoppingItems = MutableStateFlow<List<ShoppingListItem>>(emptyList())
    val shoppingItems: StateFlow<List<ShoppingListItem>> = _shoppingItems.asStateFlow()
    
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // MARK: - Computed Properties
    val totalItems: StateFlow<Int> = inventoryItems.map { it.size }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    val lowStockItemsCount: StateFlow<Int> = inventoryItems.map { items ->
        items.count { it.needsRestocking }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    val averageStockLevel: StateFlow<Double> = inventoryItems.map { items ->
        if (items.isEmpty()) 0.0 else items.map { it.quantity }.average()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    val activeCategoriesCount: StateFlow<Int> = inventoryItems.map { items ->
        items.map { it.category }.distinct().size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    val urgentAttentionItems: StateFlow<List<InventoryItem>> = inventoryItems.map { items ->
        items.filter { item ->
            val daysSince = repository.daysSinceLastUpdate(item)
            val threshold = repository.getExpiryThreshold(item)
            daysSince >= (threshold * 0.8).toInt() // 80% of threshold or more
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        // Lazy initialization to prevent startup delays
        loadSettings()
        // Defer data loading until actually needed
        viewModelScope.launch {
            try {
                observeData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize data: ${e.message}"
            }
        }
    }
    
    private fun observeData() {
        viewModelScope.launch {
            try {
                repository.getAllItems().collect { items ->
                    _inventoryItems.value = items
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load inventory items: ${e.message}"
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getAllShoppingItems().collect { items ->
                    _shoppingItems.value = items
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load shopping items: ${e.message}"
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getAllNotes().collect { notes ->
                    _notes.value = notes
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load notes: ${e.message}"
            }
        }
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = repository.getSettings()
                _settings.value = settings
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load settings: ${e.message}"
            }
        }
    }
    
    // MARK: - Inventory Management
    fun getItemsForCategory(category: InventoryCategory): List<InventoryItem> {
        return _inventoryItems.value.filter { it.category == category }
    }
    
    fun getItemsForSubcategory(subcategory: InventorySubcategory): List<InventoryItem> {
        return _inventoryItems.value.filter { it.subcategory == subcategory }
    }
    
    fun addCustomItem(name: String, subcategory: InventorySubcategory) {
        viewModelScope.launch {
            try {
                val newItem = InventoryItem(
                    name = name.trim(),
                    quantity = 0.0,
                    subcategory = subcategory,
                    isCustom = true
                )
                repository.insertItem(newItem)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add item: ${e.message}"
            }
        }
    }
    
    fun removeItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                repository.deleteItem(item)
                // Also remove any shopping list items that reference this inventory item
                val relatedShoppingItems = _shoppingItems.value.filter { 
                    it.inventoryItemId == item.id 
                }
                relatedShoppingItems.forEach { shoppingItem ->
                    repository.deleteShoppingItem(shoppingItem)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove item: ${e.message}"
            }
        }
    }
    
    fun updateItemQuantity(item: InventoryItem, quantity: Double) {
        viewModelScope.launch {
            try {
                val updatedItem = item.updateQuantity(quantity)
                repository.updateItem(updatedItem)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update quantity: ${e.message}"
            }
        }
    }
    
    fun updateItemName(item: InventoryItem, newName: String) {
        viewModelScope.launch {
            try {
                val trimmedName = newName.trim()
                if (trimmedName.isNotEmpty()) {
                    val updatedItem = item.copy(
                        name = trimmedName,
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.updateItem(updatedItem)
                    
                    // Update any shopping list items that reference this inventory item
                    val relatedShoppingItems = _shoppingItems.value.filter { 
                        it.inventoryItemId == item.id 
                    }
                    relatedShoppingItems.forEach { shoppingItem ->
                        val updatedShoppingItem = shoppingItem.copy(name = trimmedName)
                        repository.updateShoppingItem(updatedShoppingItem)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update item name: ${e.message}"
            }
        }
    }
    
    // MARK: - Shopping List Management
    fun startGeneratingShoppingList() {
        viewModelScope.launch {
            try {
                // Clear existing shopping list
                repository.deleteAllShoppingItems()
                
                // Add items that need attention (≤25%) - sorted by urgency
                val attentionItems = repository.getLowStockItems().sortedBy { it.quantity }
                
                val shoppingItems = attentionItems.map { item ->
                    ShoppingListItem(
                        name = item.name,
                        inventoryItemId = item.id,
                        isTemporary = false
                    )
                }
                
                repository.insertShoppingItems(shoppingItems)
                
                // Update shopping state
                val updatedSettings = _settings.value.copy(shoppingState = ShoppingState.GENERATING)
                repository.saveSettings(updatedSettings)
                _settings.value = updatedSettings
            } catch (e: Exception) {
                _errorMessage.value = "Failed to generate shopping list: ${e.message}"
            }
        }
    }
    
    fun finalizeShoppingList() {
        viewModelScope.launch {
            try {
                val updatedSettings = _settings.value.copy(shoppingState = ShoppingState.LIST_READY)
                repository.saveSettings(updatedSettings)
                _settings.value = updatedSettings
            } catch (e: Exception) {
                _errorMessage.value = "Failed to finalize shopping list: ${e.message}"
            }
        }
    }
    
    fun startShopping() {
        viewModelScope.launch {
            try {
                val updatedSettings = _settings.value.copy(shoppingState = ShoppingState.SHOPPING)
                repository.saveSettings(updatedSettings)
                _settings.value = updatedSettings
            } catch (e: Exception) {
                _errorMessage.value = "Failed to start shopping: ${e.message}"
            }
        }
    }
    
    fun completeAndRestoreShopping() {
        viewModelScope.launch {
            try {
                // Update inventory items that were purchased
                val checkedItems = _shoppingItems.value.filter { it.isChecked && !it.isTemporary }
                
                checkedItems.forEach { shoppingItem ->
                    shoppingItem.inventoryItemId?.let { itemId ->
                        val inventoryItem = _inventoryItems.value.find { it.id == itemId }
                        inventoryItem?.let { item ->
                            val restoredItem = item.restockToFull()
                            repository.updateItem(restoredItem)
                        }
                    }
                }
                
                // Clear shopping list and reset state
                repository.deleteAllShoppingItems()
                val updatedSettings = _settings.value.copy(shoppingState = ShoppingState.EMPTY)
                repository.saveSettings(updatedSettings)
                _settings.value = updatedSettings
            } catch (e: Exception) {
                _errorMessage.value = "Failed to complete shopping: ${e.message}"
            }
        }
    }
    
    fun cancelShopping() {
        viewModelScope.launch {
            try {
                repository.deleteAllShoppingItems()
                val updatedSettings = _settings.value.copy(shoppingState = ShoppingState.EMPTY)
                repository.saveSettings(updatedSettings)
                _settings.value = updatedSettings
            } catch (e: Exception) {
                _errorMessage.value = "Failed to cancel shopping: ${e.message}"
            }
        }
    }
    
    fun addTemporaryItemToShoppingList(name: String) {
        viewModelScope.launch {
            try {
                val trimmedName = name.trim()
                if (trimmedName.isNotEmpty()) {
                    val tempItem = ShoppingListItem(
                        name = trimmedName,
                        isTemporary = true
                    )
                    repository.insertShoppingItem(tempItem)
                    
                    // Add to misc item history
                    val updatedSettings = _settings.value.addMiscItemToHistory(trimmedName)
                    repository.saveSettings(updatedSettings)
                    _settings.value = updatedSettings
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add temporary item: ${e.message}"
            }
        }
    }
    
    fun addInventoryItemToShoppingList(item: InventoryItem) {
        viewModelScope.launch {
            try {
                val shoppingItem = ShoppingListItem(
                    name = item.name,
                    inventoryItemId = item.id,
                    isTemporary = false
                )
                repository.insertShoppingItem(shoppingItem)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add inventory item to shopping list: ${e.message}"
            }
        }
    }
    
    fun removeItemFromShoppingList(item: ShoppingListItem) {
        viewModelScope.launch {
            try {
                repository.deleteShoppingItem(item)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove item from shopping list: ${e.message}"
            }
        }
    }
    
    fun toggleShoppingItemChecked(item: ShoppingListItem) {
        viewModelScope.launch {
            try {
                val updatedItem = item.toggle()
                repository.updateShoppingItem(updatedItem)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to toggle shopping item: ${e.message}"
            }
        }
    }
    
    // MARK: - Notes Management
    fun addNote() {
        viewModelScope.launch {
            try {
                println("DEBUG: Adding note...")
                if (repository.canAddNote()) {
                    val newNote = Note()
                    println("DEBUG: Creating note with ID: ${newNote.id}")
                    repository.insertNote(newNote)
                    println("DEBUG: Note inserted successfully")
                } else {
                    println("DEBUG: Cannot add note - limit reached")
                    _errorMessage.value = "Maximum of 6 notes allowed"
                }
            } catch (e: Exception) {
                println("DEBUG: Error adding note: ${e.message}")
                _errorMessage.value = "Failed to add note: ${e.message}"
            }
        }
    }
    
    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            try {
                println("DEBUG: Adding note with title: '$title' and content length: ${content.length}")
                if (repository.canAddNote()) {
                    val newNote = Note(
                        title = title,
                        content = content,
                        lastModified = System.currentTimeMillis()
                    )
                    println("DEBUG: Creating note with ID: ${newNote.id}")
                    repository.insertNote(newNote)
                    println("DEBUG: Note inserted successfully")
                } else {
                    println("DEBUG: Cannot add note - limit reached")
                    _errorMessage.value = "Maximum of 6 notes allowed"
                }
            } catch (e: Exception) {
                println("DEBUG: Error adding note: ${e.message}")
                _errorMessage.value = "Failed to add note: ${e.message}"
            }
        }
    }
    
    fun updateNote(note: Note, title: String, content: String) {
        viewModelScope.launch {
            try {
                val updatedNote = note.updateContent(title, content)
                repository.updateNote(updatedNote)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update note: ${e.message}"
            }
        }
    }
    
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.deleteNote(note)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete note: ${e.message}"
            }
        }
    }
    
    // MARK: - Settings Management
    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            try {
                println("DEBUG: Updating settings - isDarkMode: ${newSettings.isDarkMode}")
                repository.saveSettings(newSettings)
                _settings.value = newSettings
                println("DEBUG: Settings updated successfully")
            } catch (e: Exception) {
                println("DEBUG: Failed to update settings: ${e.message}")
                _errorMessage.value = "Failed to update settings: ${e.message}"
            }
        }
    }
    
    fun toggleDarkMode() {
        println("DEBUG: Toggling dark mode from ${_settings.value.isDarkMode} to ${!_settings.value.isDarkMode}")
        val updatedSettings = _settings.value.copy(isDarkMode = !_settings.value.isDarkMode)
        _settings.value = updatedSettings // Update immediately for UI
        viewModelScope.launch {
            try {
                repository.saveSettings(updatedSettings) // Save to database asynchronously
            } catch (e: Exception) {
                // Revert on error
                _settings.value = _settings.value.copy(isDarkMode = !updatedSettings.isDarkMode)
                _errorMessage.value = "Failed to save theme preference: ${e.message}"
            }
        }
    }
    
    fun toggleSecurity() {
        val updatedSettings = _settings.value.copy(isSecurityEnabled = !_settings.value.isSecurityEnabled)
        updateSettings(updatedSettings)
    }
    
    fun toggleInventoryReminder() {
        val updatedSettings = _settings.value.copy(isInventoryReminderEnabled = !_settings.value.isInventoryReminderEnabled)
        updateSettings(updatedSettings)
    }
    
    fun updateReminderTimes(time1: String, time2: String? = null) {
        val updatedSettings = _settings.value.copy(
            reminderTime1 = time1,
            reminderTime2 = time2 ?: _settings.value.reminderTime2,
            isSecondReminderEnabled = time2 != null
        )
        updateSettings(updatedSettings)
    }
    
    // MARK: - Analytics Methods
    suspend fun getSmartRecommendations(): List<SmartRecommendation> {
        return try {
            repository.getSmartRecommendations()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to get recommendations: ${e.message}"
            emptyList()
        }
    }
    
    suspend fun getEstimatedShoppingFrequency(): String {
        return try {
            repository.getEstimatedShoppingFrequency()
        } catch (e: Exception) {
            "No data yet"
        }
    }
    
    suspend fun getEstimatedNextShoppingTrip(): String {
        return try {
            repository.getEstimatedNextShoppingTrip()
        } catch (e: Exception) {
            "No rush"
        }
    }
    
    suspend fun getShoppingEfficiencyTip(): String {
        return try {
            repository.getShoppingEfficiencyTip()
        } catch (e: Exception) {
            "Keep tracking for insights"
        }
    }
    
    // MARK: - Data Management
    fun clearAllData() {
        viewModelScope.launch {
            try {
                repository.clearAllData()
                val defaultSettings = AppSettings()
                repository.saveSettings(defaultSettings)
                _settings.value = defaultSettings
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear data: ${e.message}"
            }
        }
    }
    
    // MARK: - Error Handling
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
