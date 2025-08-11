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
import com.homeinventory.app.data.model.*
import com.homeinventory.app.presentation.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit = {}
) {
    val shoppingItems by viewModel.shoppingItems.collectAsState()
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val settings by viewModel.settings.collectAsState()
    
    when (settings.shoppingState) {
        ShoppingState.EMPTY -> EmptyShoppingView(viewModel)
        ShoppingState.GENERATING -> GeneratingShoppingView(viewModel, shoppingItems, inventoryItems, onNavigateToHome)
        ShoppingState.LIST_READY -> ReadyShoppingView(viewModel, shoppingItems, onNavigateToHome)
        ShoppingState.SHOPPING -> ActiveShoppingView(viewModel, shoppingItems, onNavigateToHome)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptyShoppingView(viewModel: InventoryViewModel) {
    val lowStockItemsCount by viewModel.lowStockItemsCount.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = "Shopping Cart",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Ready to Shop?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Generate a shopping list based on items that need attention in your household inventory.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { viewModel.startGeneratingShoppingList() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Shopping List")
                }
                
                if (lowStockItemsCount > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFFF9500)
                        )
                        Text(
                            text = "$lowStockItemsCount items need attention",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF9500)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeneratingShoppingView(
    viewModel: InventoryViewModel,
    shoppingItems: List<ShoppingListItem>,
    inventoryItems: List<InventoryItem>,
    onNavigateToHome: () -> Unit
) {
    var showAddMiscDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Your Shopping List") },
                navigationIcon = {
                    IconButton(onClick = {
                        println("🏠 Navigating to Home from Generating Shopping View")
                        onNavigateToHome()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Go to Home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            viewModel.cancelShopping()
                            onNavigateToHome()
                        }
                    ) {
                        Text("Cancel", color = Color.Red)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Shopping List Items
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (shoppingItems.isNotEmpty()) {
                    // Group items by category
                    val categoryItems = shoppingItems.filter { !it.isTemporary }
                        .groupBy { item ->
                            inventoryItems.find { it.id == item.inventoryItemId }?.category
                        }
                    
                    categoryItems.forEach { (category, items) ->
                        if (category != null && items.isNotEmpty()) {
                            item {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = category.color,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            items(items) { item ->
                                GeneratingItemRow(item, inventoryItems.find { it.id == item.inventoryItemId }, viewModel)
                            }
                        }
                    }
                    
                    // Misc items
                    val miscItems = shoppingItems.filter { it.isTemporary }
                    if (miscItems.isNotEmpty()) {
                        item {
                            Text(
                                text = "Misc Items",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9500),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(miscItems) { item ->
                            GeneratingItemRow(item, null, viewModel)
                        }
                    }
                } else {
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
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No items need attention",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Your inventory is well-stocked!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showAddMiscDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Misc")
                }
                
                Button(
                    onClick = { viewModel.finalizeShoppingList() },
                    modifier = Modifier.weight(1f),
                    enabled = shoppingItems.isNotEmpty()
                ) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Continue")
                }
            }
        }
    }
    
    if (showAddMiscDialog) {
        AddMiscItemDialog(
            onDismiss = { showAddMiscDialog = false },
            onConfirm = { itemNames ->
                itemNames.forEach { name ->
                    if (name.isNotBlank()) {
                        viewModel.addTemporaryItemToShoppingList(name)
                    }
                }
                showAddMiscDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadyShoppingView(
    viewModel: InventoryViewModel,
    shoppingItems: List<ShoppingListItem>,
    onNavigateToHome: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Checklist Ready") },
                navigationIcon = {
                    IconButton(onClick = {
                        println("🏠 Navigating to Home from Ready Shopping View")
                        onNavigateToHome()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Go to Home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Read-only Checklist
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shoppingItems) { item ->
                    ReadOnlyItemRow(item)
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.cancelShopping() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = { viewModel.startShopping() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start Shopping")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveShoppingView(
    viewModel: InventoryViewModel,
    shoppingItems: List<ShoppingListItem>,
    onNavigateToHome: () -> Unit
) {
    val checkedCount = shoppingItems.count { it.isChecked }
    val totalCount = shoppingItems.size
    var showCompleteDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Shopping in Progress")
                        Text(
                            text = "$checkedCount/$totalCount completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        println("🏠 Navigating to Home from Active Shopping View")
                        onNavigateToHome()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Go to Home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Active Checklist
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shoppingItems) { item ->
                    ActiveItemRow(item, viewModel)
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Plan change options */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Plan Change")
                }
                
                Button(
                    onClick = { showCompleteDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Complete")
                }
            }
        }
    }
    
    if (showCompleteDialog) {
        val restorableCount = shoppingItems.count { it.isChecked && !it.isTemporary }
        
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Complete Shopping Trip") },
            text = { 
                if (restorableCount > 0) {
                    Text("This will restore $restorableCount checked items to 100% stock and clear the shopping list.")
                } else {
                    Text("This will clear the shopping list. No inventory will be restored.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.completeAndRestoreShopping()
                        showCompleteDialog = false
                        onNavigateToHome() // Navigate to home after completing shopping
                    }
                ) {
                    Text("Complete & Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GeneratingItemRow(
    item: ShoppingListItem,
    inventoryItem: InventoryItem?,
    viewModel: InventoryViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (item.isTemporary) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalOffer,
                            contentDescription = "Misc Item",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFF9500)
                        )
                        Text(
                            text = "Misc Item",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9500)
                        )
                    }
                } else if (inventoryItem != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = inventoryItem.subcategory.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = inventoryItem.subcategory.color
                        )
                        Text(
                            text = "• ${inventoryItem.quantityPercentage}% left",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            }
            
            IconButton(
                onClick = { viewModel.removeItemFromShoppingList(item) }
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

@Composable
private fun ReadOnlyItemRow(item: ShoppingListItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActiveItemRow(
    item: ShoppingListItem,
    viewModel: InventoryViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { viewModel.toggleShoppingItemChecked(item) }
            )
            
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AddMiscItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var itemNames by remember { mutableStateOf(listOf("")) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Misc Items") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Add items that aren't tracked in your household inventory")
                
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
