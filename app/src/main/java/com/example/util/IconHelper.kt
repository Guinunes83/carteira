package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    fun getIcon(name: String): ImageVector {
        return when (name) {
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "Restaurant" -> Icons.Default.Restaurant
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "HealthAndSafety" -> Icons.Default.HealthAndSafety
            "Home" -> Icons.Default.Home
            "School" -> Icons.Default.School
            "Pets" -> Icons.Default.Pets
            "Checkroom" -> Icons.Default.Checkroom
            "LocalHospital" -> Icons.Default.LocalHospital
            "Category" -> Icons.Default.Category
            else -> Icons.Default.Category
        }
    }
}
