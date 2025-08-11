package com.homeinventory.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeinventory.app.data.manager.SmartRecommendationsManager
import com.homeinventory.app.data.manager.InventoryStats
import com.homeinventory.app.data.models.SmartRecommendation
import com.homeinventory.app.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val smartRecommendationsManager: SmartRecommendationsManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Observe inventory items and generate insights
            inventoryRepository.getAllItems()
                .map { items ->
                    val recommendations = smartRecommendationsManager.getSmartRecommendations(items)
                    val stats = smartRecommendationsManager.getInventoryStats(items)
                    InsightsUiState(
                        isLoading = false,
                        recommendations = recommendations,
                        stats = stats
                    )
                }
                .catch { e ->
                    _uiState.value = InsightsUiState(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }
}

data class InsightsUiState(
    val isLoading: Boolean = true,
    val recommendations: List<SmartRecommendation> = emptyList(),
    val stats: InventoryStats? = null,
    val error: String? = null
)
