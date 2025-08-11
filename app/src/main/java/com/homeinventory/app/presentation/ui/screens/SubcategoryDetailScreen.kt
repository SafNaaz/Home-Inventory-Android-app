package com.homeinventory.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.homeinventory.app.data.model.InventoryItem
import com.homeinventory.app.data.model.InventorySubcategory
import com.homeinventory.app.presentation.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcategoryDetailScreen(
    subcategory: InventorySubcategory,
    onBackClick: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val subcategoryItems = inventoryItems.filter { it.subcategory == subcategory }
    
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<InventoryItem?>(null) }
    var showEditDialog by remember { mutableStateOf<InventoryItem?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        subcategory.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddItemDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Item")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(subcategoryItems) { item ->
                ItemCard(
                    item = item,
                    onQuantityChange = { newQuantity ->
                        viewModel.updateItemQuantity(item, newQuantity)
                    },
                    onEditClick = { showEditDialog = item },
                    onDeleteClick = { showDeleteDialog = item }
                )
            }
            
            if (subcategoryItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Inventory2,
                                contentDescription = "No items",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = "No items in ${subcategory.displayName}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = "Tap the + button to add your first item",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Add Item Dialog
    if (showAddItemDialog) {
        AddItemDialog(
            subcategory = subcategory,
            onDismiss = { showAddItemDialog = false },
            onConfirm = { itemNames ->
                itemNames.forEach { name ->
                    if (name.isNotBlank()) {
                        viewModel.addCustomItem(name, subcategory)
                    }
                }
                showAddItemDialog = false
            }
        )
    }
    
    // Edit Item Dialog
    showEditDialog?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { showEditDialog = null },
            onConfirm = { newName ->
                viewModel.updateItemName(item, newName)
                showEditDialog = null
            }
        )
    }
    
    // Delete Confirmation Dialog
    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete \"${item.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeItem(item)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ItemCard(
    item: InventoryItem,
    onQuantityChange: (Double) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var sliderValue by remember(item.quantity) { mutableStateOf(item.quantity.toFloat()) }
    var isSliderBeingDragged by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
                
                Text(
                    text = "${item.quantityPercentage}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.needsRestocking) Color.Red else MaterialTheme.colorScheme.primary
                )
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Stock Level",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Slider(
                    value = if (isSliderBeingDragged) sliderValue else item.quantity.toFloat(),
                    onValueChange = { newValue ->
                        sliderValue = newValue
                        isSliderBeingDragged = true
                    },
                    onValueChangeFinished = {
                        isSliderBeingDragged = false
                        onQuantityChange(sliderValue.toDouble())
                    },
                    valueRange = 0f..1f,
                    steps = 19, // 20 steps (5% increments)
                    colors = SliderDefaults.colors(
                        thumbColor = if (item.needsRestocking) Color.Red else MaterialTheme.colorScheme.primary,
                        activeTrackColor = if (item.needsRestocking) Color.Red else MaterialTheme.colorScheme.primary
                    )
                )
            }
            
            if (item.needsRestocking) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFFF9500),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Needs restocking",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9500)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddItemDialog(
    subcategory: InventorySubcategory,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var itemNames by remember { mutableStateOf(listOf("")) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Items to ${subcategory.displayName}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Add up to 5 items at once")
                
                itemNames.forEachIndexed { index, name ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("${index + 1}.")
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { newName ->
                                itemNames = itemNames.toMutableList().apply {
                                    this[index] = newName
                                }
                            },
                            label = { Text("Item name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (itemNames.size > 1) {
                            IconButton(
                                onClick = {
                                    itemNames = itemNames.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Remove,
                                    contentDescription = "Remove",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
                
                if (itemNames.size < 5) {
                    TextButton(
                        onClick = {
                            itemNames = itemNames + ""
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add More")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(itemNames) },
                enabled = itemNames.any { it.isNotBlank() }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditItemDialog(
    item: InventoryItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var editedName by remember { mutableStateOf(item.name) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item Name") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Update the name of this inventory item")
                
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Item name") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(editedName) },
                enabled = editedName.trim().isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
