package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val startDay by viewModel.startDay.collectAsStateWithLifecycle()
    val endDay by viewModel.endDay.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    
    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<com.example.data.Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<com.example.data.Category?>(null) }
    
    var showCycleDialog by remember { mutableStateOf(false) }
    var showCategoriesListDialog by remember { mutableStateOf(false) }

    val icons = listOf(
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "Restaurant" to Icons.Default.Restaurant,
        "DirectionsCar" to Icons.Default.DirectionsCar,
        "HealthAndSafety" to Icons.Default.HealthAndSafety,
        "Home" to Icons.Default.Home,
        "School" to Icons.Default.School,
        "Pets" to Icons.Default.Pets,
        "Checkroom" to Icons.Default.Checkroom,
        "LocalHospital" to Icons.Default.LocalHospital,
        "Category" to Icons.Default.Category,
        "Airlines" to Icons.Default.Airlines,
        "AccountBalance" to Icons.Default.AccountBalance,
        "AccountBalanceWallet" to Icons.Default.AccountBalanceWallet,
        "CardGiftcard" to Icons.Default.CardGiftcard,
        "ChildCare" to Icons.Default.ChildCare,
        "ColorLens" to Icons.Default.ColorLens,
        "Computer" to Icons.Default.Computer,
        "DirectionsBus" to Icons.Default.DirectionsBus,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "Flight" to Icons.Default.Flight,
        "LocalCafe" to Icons.Default.LocalCafe,
        "LocalDining" to Icons.Default.LocalDining,
        "LocalGasStation" to Icons.Default.LocalGasStation,
        "LocalGroceryStore" to Icons.Default.LocalGroceryStore,
        "LocalLibrary" to Icons.Default.LocalLibrary,
        "LocalMall" to Icons.Default.LocalMall,
        "LocalShipping" to Icons.Default.LocalShipping,
        "Movie" to Icons.Default.Movie,
        "MusicNote" to Icons.Default.MusicNote,
        "SelfImprovement" to Icons.Default.SelfImprovement,
        "SportsEsports" to Icons.Default.SportsEsports,
        "Train" to Icons.Default.Train
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Cycle Configuration
        Button(
            onClick = { showCycleDialog = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Período do Ciclo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.DateRange, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Theme Configuration
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Tema Escuro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { viewModel.setIsDarkTheme(it) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { showCategoriesListDialog = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Gerenciar Categorias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.Category, contentDescription = null)
            }
        }
    }
    
    if (showCycleDialog) {
        AlertDialog(
            onDismissRequest = { showCycleDialog = false },
            title = { Text("Período do Ciclo") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = startDay.toString(),
                        onValueChange = { it.toIntOrNull()?.let { day -> viewModel.setStartDay(day) } },
                        label = { Text("Dia Inicial") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDay.toString(),
                        onValueChange = { it.toIntOrNull()?.let { day -> viewModel.setEndDay(day) } },
                        label = { Text("Dia Final") },
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCycleDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showCategoriesListDialog) {
        AlertDialog(
            onDismissRequest = { showCategoriesListDialog = false },
            title = { Text("Categorias") },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(allCategories) { cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(32.dp).background(Color(cat.color), CircleShape), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                        Icon(
                                            imageVector = icons.firstOrNull { it.first == cat.iconName }?.second ?: Icons.Default.Category,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(cat.name)
                                }
                                Row {
                                    IconButton(onClick = {
                                        categoryToEdit = cat
                                        showCategoryDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { categoryToDelete = cat }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            categoryToEdit = null
                            showCategoryDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Criar Nova Categoria")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoriesListDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
    
    if (showCategoryDialog) {
        CategoryDialog(
            initialCategory = categoryToEdit,
            onDismiss = { showCategoryDialog = false },
            onSave = { name, color, icon -> 
                if (categoryToEdit != null) {
                    viewModel.updateCategory(categoryToEdit!!.name, name, color, icon)
                } else {
                    viewModel.addCategory(name, color, icon)
                }
                showCategoryDialog = false
            }
        )
    }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Excluir Categoria") },
            text = { Text("Tem certeza que deseja excluir a categoria '${categoryToDelete?.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    categoryToDelete?.let { viewModel.deleteCategory(it.name) }
                    categoryToDelete = null
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDialog(
    initialCategory: com.example.data.Category?,
    onDismiss: () -> Unit,
    onSave: (String, Long, String) -> Unit
) {
    var name by remember(initialCategory) { mutableStateOf(initialCategory?.name ?: "") }
    
    val colors = listOf(
        0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF3949AB, 0xFF1E88E5,
        0xFF00ACC1, 0xFF43A047, 0xFFFDD835, 0xFFFB8C00, 0xFF6D4C41
    )
    var selectedColor by remember(initialCategory) { mutableStateOf(initialCategory?.color ?: colors[0]) }
    
    val icons = listOf(
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "Restaurant" to Icons.Default.Restaurant,
        "DirectionsCar" to Icons.Default.DirectionsCar,
        "HealthAndSafety" to Icons.Default.HealthAndSafety,
        "Home" to Icons.Default.Home,
        "School" to Icons.Default.School,
        "Pets" to Icons.Default.Pets,
        "Checkroom" to Icons.Default.Checkroom,
        "LocalHospital" to Icons.Default.LocalHospital,
        "Category" to Icons.Default.Category,
        "Airlines" to Icons.Default.Airlines,
        "AccountBalance" to Icons.Default.AccountBalance,
        "AccountBalanceWallet" to Icons.Default.AccountBalanceWallet,
        "CardGiftcard" to Icons.Default.CardGiftcard,
        "ChildCare" to Icons.Default.ChildCare,
        "ColorLens" to Icons.Default.ColorLens,
        "Computer" to Icons.Default.Computer,
        "DirectionsBus" to Icons.Default.DirectionsBus,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "Flight" to Icons.Default.Flight,
        "LocalCafe" to Icons.Default.LocalCafe,
        "LocalDining" to Icons.Default.LocalDining,
        "LocalGasStation" to Icons.Default.LocalGasStation,
        "LocalGroceryStore" to Icons.Default.LocalGroceryStore,
        "LocalLibrary" to Icons.Default.LocalLibrary,
        "LocalMall" to Icons.Default.LocalMall,
        "LocalShipping" to Icons.Default.LocalShipping,
        "Movie" to Icons.Default.Movie,
        "MusicNote" to Icons.Default.MusicNote,
        "SelfImprovement" to Icons.Default.SelfImprovement,
        "SportsEsports" to Icons.Default.SportsEsports,
        "Train" to Icons.Default.Train
    )
    var selectedIcon by remember(initialCategory) { mutableStateOf(initialCategory?.iconName ?: icons[0].first) }

    var expandedIconMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCategory == null) "Nova Categoria" else "Editar Categoria") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Categoria") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Cor", style = MaterialTheme.typography.labelLarge)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(100.dp)
                ) {
                    items(colors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(color), CircleShape)
                                .clickable { selectedColor = color }
                        ) {
                            if (selectedColor == color) {
                                Icon(Icons.Default.Check, "Selected", tint = Color.White, modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                            }
                        }
                    }
                }
                
                Text("Ícone", style = MaterialTheme.typography.labelLarge)
                Box {
                    IconButton(onClick = { expandedIconMenu = true }) {
                        Icon(
                            icons.firstOrNull { it.first == selectedIcon }?.second ?: Icons.Default.Category,
                            contentDescription = selectedIcon,
                            tint = Color(selectedColor),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expandedIconMenu,
                        onDismissRequest = { expandedIconMenu = false },
                        modifier = Modifier.height(300.dp).width(250.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(icons) { iconData ->
                                IconButton(onClick = {
                                    selectedIcon = iconData.first
                                    expandedIconMenu = false
                                }) {
                                    Icon(
                                        iconData.second,
                                        contentDescription = iconData.first,
                                        tint = if (selectedIcon == iconData.first) Color(selectedColor) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, selectedColor, selectedIcon)
                    }
                }
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
