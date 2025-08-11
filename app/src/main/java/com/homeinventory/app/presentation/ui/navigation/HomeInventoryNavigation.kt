package com.homeinventory.app.presentation.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.homeinventory.app.data.model.InventoryCategory
import com.homeinventory.app.data.model.InventorySubcategory
import com.homeinventory.app.presentation.ui.screens.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Shopping : Screen("shopping", "Shopping", Icons.Filled.ShoppingCart)
    object Insights : Screen("insights", "Insights", Icons.Filled.Analytics)
    object Notes : Screen("notes", "Notes", Icons.Filled.Note)
    object CategoryDetail : Screen("category_detail/{categoryName}", "Category", Icons.Filled.Category)
    object SubcategoryDetail : Screen("subcategory_detail/{subcategoryName}", "Items", Icons.Filled.Inventory)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeInventoryNavigation() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Shopping, Screen.Insights, Screen.Notes)
    
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Only show bottom bar on main screens
            if (currentDestination?.route in items.map { it.route }) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                if (screen.route == Screen.Home.route) {
                                    // Always navigate to home, clearing the back stack
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false // Don't restore state for home to ensure fresh navigation
                                    }
                                } else {
                                    navController.navigate(screen.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        // on the back stack as users select items
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onCategoryClick = { category ->
                        navController.navigate("category_detail/${category.name}")
                    },
                    onShoppingClick = {
                        navController.navigate(Screen.Shopping.route)
                    },
                    onInsightsClick = {
                        navController.navigate(Screen.Insights.route)
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            
            composable(Screen.Shopping.route) {
                ShoppingScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            
            composable(Screen.Insights.route) {
                InsightsScreen()
            }
            
            composable(Screen.Notes.route) {
                NotesScreen(
                    onNavigateToNoteDetail = { noteId ->
                        navController.navigate("note_detail/$noteId")
                    },
                    onNavigateToNewNote = {
                        navController.navigate("note_detail/new?edit=true")
                    }
                )
            }
            
            composable(
                route = "note_detail/{noteId}?edit={edit}",
                arguments = listOf(
                    navArgument("noteId") { type = NavType.StringType },
                    navArgument("edit") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")
                val startInEditMode = backStackEntry.arguments?.getBoolean("edit") ?: false
                NoteDetailScreen(
                    noteId = noteId,
                    startInEditMode = startInEditMode,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("category_detail/{categoryName}") { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName")
                val category = InventoryCategory.values().find { it.name == categoryName }
                
                if (category != null) {
                    CategoryDetailScreen(
                        category = category,
                        onSubcategoryClick = { subcategory ->
                            navController.navigate("subcategory_detail/${subcategory.name}")
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
            
            composable("subcategory_detail/{subcategoryName}") { backStackEntry ->
                val subcategoryName = backStackEntry.arguments?.getString("subcategoryName")
                val subcategory = InventorySubcategory.values().find { it.name == subcategoryName }
                
                if (subcategory != null) {
                    SubcategoryDetailScreen(
                        subcategory = subcategory,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
