package com.homeinventory.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.homeinventory.app.data.model.InventoryCategory
import com.homeinventory.app.data.model.InventorySubcategory
import com.homeinventory.app.presentation.ui.components.SubcategoryRow
import com.homeinventory.app.presentation.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    category: InventoryCategory,
    onSubcategoryClick: (InventorySubcategory) -> Unit,
    onBackClick: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        category.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(category.getSubcategories()) { subcategory ->
                val subcategoryItems = inventoryItems.filter { it.subcategory == subcategory }
                val lowStockCount = subcategoryItems.count { it.needsRestocking }
                
                SubcategoryRow(
                    subcategory = subcategory,
                    itemsCount = subcategoryItems.size,
                    lowStockCount = lowStockCount,
                    onClick = { onSubcategoryClick(subcategory) }
                )
                
                HorizontalDivider()
            }
        }
    }
}
