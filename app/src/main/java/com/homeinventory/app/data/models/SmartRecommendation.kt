package com.homeinventory.app.data.models

import androidx.compose.ui.graphics.Color

data class SmartRecommendation(
    val title: String,
    val description: String,
    val icon: String,
    val color: Color,
    val priority: RecommendationPriority
)

enum class RecommendationPriority {
    LOW, MEDIUM, HIGH
}
