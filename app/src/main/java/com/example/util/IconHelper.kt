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
            "Airlines" -> Icons.Default.Airlines
            "AccountBalance" -> Icons.Default.AccountBalance
            "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
            "CardGiftcard" -> Icons.Default.CardGiftcard
            "ChildCare" -> Icons.Default.ChildCare
            "ColorLens" -> Icons.Default.ColorLens
            "Computer" -> Icons.Default.Computer
            "DirectionsBus" -> Icons.Default.DirectionsBus
            "FitnessCenter" -> Icons.Default.FitnessCenter
            "Flight" -> Icons.Default.Flight
            "LocalCafe" -> Icons.Default.LocalCafe
            "LocalDining" -> Icons.Default.LocalDining
            "LocalGasStation" -> Icons.Default.LocalGasStation
            "LocalGroceryStore" -> Icons.Default.LocalGroceryStore
            "LocalLibrary" -> Icons.Default.LocalLibrary
            "LocalMall" -> Icons.Default.LocalMall
            "LocalShipping" -> Icons.Default.LocalShipping
            "Movie" -> Icons.Default.Movie
            "MusicNote" -> Icons.Default.MusicNote
            "SelfImprovement" -> Icons.Default.SelfImprovement
            "SportsEsports" -> Icons.Default.SportsEsports
            "Train" -> Icons.Default.Train
            "Category" -> Icons.Default.Category
            else -> Icons.Default.Category
        }
    }
}
