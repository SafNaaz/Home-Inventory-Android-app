package com.homeinventory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.homeinventory.app.presentation.ui.navigation.HomeInventoryNavigation
import com.homeinventory.app.presentation.ui.theme.HomeInventoryTheme
import com.homeinventory.app.presentation.viewmodel.InventoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Defer edge-to-edge to prevent early UI issues
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            // Ignore edge-to-edge errors for now
        }
        
        setContent {
            AppWithErrorHandling()
        }
    }
}

@Composable
fun AppWithErrorHandling() {
    var isLoading by remember { mutableStateOf(true) }
    
    // Brief loading screen
    LaunchedEffect(Unit) {
        delay(300) // Quick initialization delay
        isLoading = false
    }
    
    if (isLoading) {
        // Use default theme for loading screen
        HomeInventoryTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Home Inventory",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    } else {
        MainAppContent()
    }
}

@Composable
fun MainAppContent() {
    val viewModel: InventoryViewModel = hiltViewModel()
    val settings by viewModel.settings.collectAsState()
    
    HomeInventoryTheme(darkTheme = settings.isDarkMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeInventoryNavigation()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HomeInventoryTheme {
        AppWithErrorHandling()
    }
}