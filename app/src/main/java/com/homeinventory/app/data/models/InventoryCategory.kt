package com.homeinventory.app.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class InventoryCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color
) {
    FRIDGE("Fridge", Icons.Default.Kitchen, Color(0xFF2196F3)),
    GROCERY("Grocery", Icons.Default.LocalMall, Color(0xFF4CAF50)),
    HYGIENE("Hygiene", Icons.Default.Sanitizer, Color(0xFF00BCD4)),
    PERSONAL_CARE("Personal Care", Icons.Default.Person, Color(0xFFE91E63));

    companion object {
        fun fromString(value: String): InventoryCategory = valueOf(value.uppercase())
    }
}
