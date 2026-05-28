package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.data.AppDatabase
import com.example.data.Transaction
import com.example.data.TransactionRepository
import com.example.data.UserPreferences
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ReportsScreen
import com.example.util.DateUtils
import com.example.util.IconHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this)
        val repository = TransactionRepository(db.transactionDao(), db.categoryDao())
        val userPreferences = UserPreferences(this)
        val viewModel by viewModels<MainViewModel> { MainViewModel.Factory(repository, userPreferences) }

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                var showSplash by remember { mutableStateOf(true) }
                if (showSplash) {
                    SplashScreen(onAnimationEnd = { showSplash = false })
                } else {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Início") }
                )
                NavigationBarItem(
                    selected = currentRoute == "reports",
                    onClick = { navController.navigate("reports") { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Relatórios") },
                    label = { Text("Relatórios") }
                )
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Config") },
                    label = { Text("Config") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "home", modifier = Modifier.padding(innerPadding)) {
            composable("home") { FluxoApp(viewModel) }
            composable("reports") { ReportsScreen(viewModel) }
            composable("settings") { SettingsScreen(viewModel) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxoApp(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val dailyBalance by viewModel.dailyBalance.collectAsStateWithLifecycle()
    val cycleTotal by viewModel.cycleTotal.collectAsStateWithLifecycle()
    val todayTransactions by viewModel.todayTransactions.collectAsStateWithLifecycle()
    val selectedDateMs by viewModel.selectedDateMs.collectAsStateWithLifecycle()

    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var showForm by remember { mutableStateOf(false) }
    
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showMonthlyTransactions by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd 'de' MMM, yyyy", Locale("pt", "BR")) }
    val monthFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.localToUtcMidnight(selectedDateMs)
    )


    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                IconButton(onClick = { viewModel.changeDate(selectedDateMs - 86400000L) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Anterior", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = dateFormatter.format(Date(selectedDateMs)).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable { showDatePickerDialog = true }.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                IconButton(onClick = { viewModel.changeDate(selectedDateMs + 86400000L) }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Próximo", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Saldo do Dia".uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val formattedBalance = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(dailyBalance)
                Text(
                    text = formattedBalance,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 42.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = if (dailyBalance >= 0) com.example.ui.theme.GreenIncome else com.example.ui.theme.RedExpense
                )
                
                val formattedTotal = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cycleTotal)
                val currentMonthName = monthFormatter.format(Date(selectedDateMs)).replaceFirstChar { it.uppercase() }
                
                Text(
                    text = "Valor total de $currentMonthName é $formattedTotal",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (cycleTotal >= 0) com.example.ui.theme.GreenIncome else com.example.ui.theme.RedExpense,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
            }

            Button(
                onClick = { 
                    transactionToEdit = null
                    showForm = true 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(64.dp)
                    .testTag("add_value_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(32.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    "+",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "LANÇAR VALOR",
                    style = MaterialTheme.typography.titleSmall,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
            ) {
                Text(
                    text = "Lançamentos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                
                if (todayTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum lançamento nesta data.",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp)
                    ) {
                        items(todayTransactions, key = { it.id }) { transaction ->
                            val categoryObj = viewModel.allCategories.collectAsStateWithLifecycle().value.find { it.name == transaction.category }
                            TransactionItem(
                                transaction = transaction,
                                categoryObj = categoryObj,
                                onClick = {
                                    transactionToEdit = transaction
                                    showForm = true
                                }
                            )
                        }
                    }
                }
                
                TextButton(
                    onClick = { showMonthlyTransactions = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text("LANÇAMENTOS AGRUPADOS")
                }
            }
        }
        
        if (showMonthlyTransactions) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            var groupedFilter by remember { mutableStateOf("Mês") }
            val allTransactionsList by viewModel.allTransactions.collectAsStateWithLifecycle(emptyList())
            val startDay by viewModel.userPreferences.startDayFlow.collectAsStateWithLifecycle(1)
            val endDay by viewModel.userPreferences.endDayFlow.collectAsStateWithLifecycle(31)
            val cycleStart = DateUtils.getStartOfCycleMs(selectedDateMs, startDay, endDay)
            val cycleEnd = DateUtils.getEndOfCycleMs(selectedDateMs, startDay, endDay)
            
            val currentCycleTransactions = if (groupedFilter == "Mês") {
                allTransactionsList.filter { it.dateMs in cycleStart..cycleEnd }.sortedByDescending { it.dateMs }
            } else {
                allTransactionsList.sortedByDescending { it.dateMs }
            }
            
            ModalBottomSheet(
                onDismissRequest = { showMonthlyTransactions = false },
                containerColor = MaterialTheme.colorScheme.surface,
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        text = "Lançamentos Agrupados",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { groupedFilter = "Mês" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (groupedFilter == "Mês") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Text("Mês", color = if (groupedFilter == "Mês") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary)
                        }
                        OutlinedButton(
                            onClick = { groupedFilter = "Total" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (groupedFilter == "Total") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Text("Total", color = if (groupedFilter == "Total") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (currentCycleTransactions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(if (groupedFilter == "Mês") "Nenhum lançamento no mês." else "Nenhum lançamento registrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                            items(currentCycleTransactions, key = { it.id }) { transaction ->
                                val categoryObj = viewModel.allCategories.collectAsStateWithLifecycle().value.find { it.name == transaction.category }
                                TransactionItem(
                                    transaction = transaction,
                                    categoryObj = categoryObj,
                                    onClick = {
                                        transactionToEdit = transaction
                                        showForm = true
                                        showMonthlyTransactions = false
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
        
        if (showForm) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showForm = false },
                containerColor = MaterialTheme.colorScheme.surface,
                sheetState = sheetState
            ) {
                TransactionForm(
                    initialTransaction = transactionToEdit,
                    categories = viewModel.allCategories.collectAsStateWithLifecycle().value,
                    defaultDateMs = selectedDateMs,
                    onAdd = { desc, value, txDateMs, cat, recType, recCount ->
                        if (transactionToEdit != null) {
                            viewModel.updateTransaction(transactionToEdit!!.id, desc, value, cat.name, txDateMs)
                        } else {
                            viewModel.addTransactionGroup(desc, value, txDateMs, cat.name, recType, recCount)
                        }
                        showForm = false
                    },
                    onDelete = {
                        transactionToEdit?.let { viewModel.deleteTransaction(it.id) }
                        showForm = false
                    },
                    onClose = { showForm = false }
                )
            }
        }
        
        if (showDatePickerDialog) {
            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { 
                            viewModel.changeDate(DateUtils.utcMidnightToLocal(it)) 
                        }
                        showDatePickerDialog = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePickerDialog = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionForm(
    initialTransaction: Transaction?,
    categories: List<com.example.data.Category>,
    defaultDateMs: Long,
    onAdd: (String, Double, Long, com.example.data.Category, String, Int) -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    var description by remember { mutableStateOf(initialTransaction?.description ?: "") }
    var category by remember { mutableStateOf(categories.find { it.name == initialTransaction?.category } ?: categories.firstOrNull()) }
    var valueStr by remember { mutableStateOf(if (initialTransaction != null) kotlin.math.abs(initialTransaction.value).toString() else "") }
    var isExpense by remember { mutableStateOf(if (initialTransaction != null) initialTransaction.value < 0 else true) }
    var selectedDateMs by remember { mutableStateOf(initialTransaction?.dateMs ?: defaultDateMs) }
    
    var recurrenceType by remember { mutableStateOf("UNICO") } // UNICO, MENSAL, RECORRENTE
    var recurrenceCount by remember { mutableStateOf("1") }
    
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd 'de' MMM, yyyy", Locale("pt", "BR")) }
    
    val dpState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.localToUtcMidnight(selectedDateMs)
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { selectedDateMs = DateUtils.utcMidnightToLocal(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = dpState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = if (initialTransaction == null) "Novo Lançamento" else "Editar Lançamento",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(java.util.Locale.ROOT) else char.toString() } },
            label = { Text("Descrição (ex: Almoço)") },
            modifier = Modifier.fillMaxWidth().testTag("desc_input"),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = category?.name ?: "",
                onValueChange = { },
                label = { Text("Categoria") },
                modifier = Modifier.fillMaxWidth().clickable { showCategoryDropdown = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            DropdownMenu(
                expanded = showCategoryDropdown,
                onDismissRequest = { showCategoryDropdown = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(androidx.compose.ui.graphics.Color(0xFF000080), androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = com.example.util.IconHelper.getIcon(cat.iconName),
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(cat.name) 
                            }
                        },
                        onClick = { 
                            category = cat
                            showCategoryDropdown = false 
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = valueStr,
            onValueChange = { valueStr = it },
            label = { Text("Valor original total") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().testTag("value_input"),
            singleLine = true,
            prefix = { Text("R$ ") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isExpense,
                        onClick = { isExpense = true },
                        modifier = Modifier.testTag("radio_expense")
                    )
                    Text("Despesa", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !isExpense,
                        onClick = { isExpense = false },
                        modifier = Modifier.testTag("radio_income")
                    )
                    Text("Receita", style = MaterialTheme.typography.bodyMedium)
                }
            }
            // Date Selection
            TextButton(onClick = { showDatePicker = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = "Selecionar Data", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(dateFormatter.format(Date(selectedDateMs)), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        
        if (initialTransaction == null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Repetição", style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = recurrenceType == "UNICO", onClick = { recurrenceType = "UNICO" }, label = { Text("Único") })
                FilterChip(selected = recurrenceType == "MENSAL", onClick = { recurrenceType = "MENSAL" }, label = { Text("Mensal") })
                FilterChip(selected = recurrenceType == "RECORRENTE", onClick = { recurrenceType = "RECORRENTE" }, label = { Text("Recorrente") })
            }
            if (recurrenceType == "MENSAL") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = recurrenceCount,
                    onValueChange = { recurrenceCount = it },
                    label = { Text("Quantos meses?") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val valueDouble = valueStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                val finalValue = if (isExpense) -valueDouble else valueDouble
                val finalCat = category ?: categories.firstOrNull()
                val finalCount = recurrenceCount.toIntOrNull() ?: 1
                if (description.isNotBlank() && valueDouble > 0 && finalCat != null) {
                    onAdd(description, finalValue, selectedDateMs, finalCat, recurrenceType, finalCount)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(if (initialTransaction == null) "Lançar Agora" else "Salvar")
        }
        
        if (initialTransaction != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Deletar Lançamento")
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, categoryObj: com.example.data.Category?, onClick: () -> Unit) {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = formatter.format(Date(transaction.dateMs))
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    val isPositive = transaction.value >= 0
    val dynamicColor = if (isPositive) com.example.ui.theme.GreenIncome else com.example.ui.theme.RedExpense

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = dynamicColor.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(dynamicColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconHelper.getIcon(categoryObj?.iconName ?: "Category"),
                        contentDescription = categoryObj?.name ?: "Categoria",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$formattedTime • ${transaction.category}" + if (transaction.recurrenceType != "UNICO") " 🔄" else "",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = dynamicColor
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if(isPositive) "+ " else "- ") + currencyFormat.format(kotlin.math.abs(transaction.value)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) com.example.ui.theme.GreenIncome else com.example.ui.theme.RedExpense
                )
            }
        }
    }
}

@Composable
fun SplashScreen(onAnimationEnd: () -> Unit) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.toFloat()
    val screenWidthHalf = (configuration.screenWidthDp / 2)
    val screenHeightHalf = (configuration.screenHeightDp / 2)
    
    var isExiting by remember { mutableStateOf(false) }
    
    val dollarSigns = remember {
        List(30) {
            val xOffset = (-screenWidthHalf..screenWidthHalf).random().toFloat()
            val yOffset = (-screenHeightHalf..screenHeightHalf).random().toFloat()
            val baseSize = (24..64).random().toFloat()
            val isPulsing = Math.random() > 0.4
            val isScaling = Math.random() > 0.4
            val phase = (0..2000).random()
            
            DollarSignData(xOffset, yOffset, baseSize, isPulsing, isScaling, phase)
        }
    }
    
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val offsetY = remember { androidx.compose.animation.core.Animatable(screenHeight) }

    LaunchedEffect(Unit) {
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 3500,
                easing = androidx.compose.animation.core.LinearOutSlowInEasing
            )
        )
        kotlinx.coroutines.delay(400)
        isExiting = true
        kotlinx.coroutines.delay(1200)
        onAnimationEnd()
    }
    
    val revealProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExiting) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutLinearInEasing),
        label = "revealProgress"
    )
    val exitAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExiting) 0f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300, delayMillis = 900, easing = androidx.compose.animation.core.LinearEasing),
        label = "exitAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(exitAlpha)
            .background(Color(0xFF003060)),
        contentAlignment = Alignment.Center
    ) {
        // Background Dollar Signs
        dollarSigns.forEach { ds ->
            val alphaState = if (ds.isPulsing) {
                infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(durationMillis = 1500 + ds.phase, easing = androidx.compose.animation.core.LinearEasing),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ),
                    label = "alpha"
                ).value
            } else 0.5f

            val scaleState = if (ds.isScaling) {
                infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.3f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(durationMillis = 2000 + ds.phase, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ),
                    label = "scale"
                ).value
            } else 1f
            
            Text(
                text = "$",
                fontSize = ds.baseSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC0C0C0).copy(alpha = alphaState),
                modifier = Modifier
                    .offset(x = ds.x.dp, y = ds.y.dp)
                    .graphicsLayer(
                        scaleX = scaleState,
                        scaleY = scaleState
                    )
            )
        }
        
        // Central Text
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.zIndex(10f)
        ) {
            val textStyle = MaterialTheme.typography.displayLarge.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                fontSize = 48.sp // Adjusted for prominent display
            )
            
            Text(
                text = "CARTEIRA",
                style = textStyle.copy(
                    drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(
                        miter = 10f,
                        width = 4f,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                ),
                color = Color.Black
            )
            Text(
                text = "CARTEIRA",
                style = textStyle,
                color = Color(0xFFFFD700) // Golden Yellow
            )
        }

        // Wallet Icon with Motion Lines
        Box(
            modifier = Modifier
                .offset(y = offsetY.value.dp)
                .size(160.dp)
                .zIndex(5f),
            contentAlignment = Alignment.TopCenter
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_wallet_logo),
                contentDescription = "Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(120.dp)
            )
            
            val isMoving = Math.abs(offsetY.value) > 10f
            if (isMoving && !isExiting) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineCount = 5
                    val spacing = size.width / (lineCount + 1)
                    val yLineStart = 110.dp.toPx()
                    
                    for (j in 1..lineCount) {
                        val xPos = spacing * j
                        val relativeSpeed = Math.abs(offsetY.value) / screenHeight
                        val baseLength = (20..60).random().toFloat()
                        val length = baseLength + (100f * relativeSpeed)
                        val yStartAdjusted = yLineStart + if (j % 2 == 0) 10.dp.toPx() else 0f
                        
                        drawLine(
                            color = Color.White.copy(alpha = relativeSpeed.coerceIn(0.1f, 0.6f)),
                            start = androidx.compose.ui.geometry.Offset(x = xPos, y = yStartAdjusted),
                            end = androidx.compose.ui.geometry.Offset(x = xPos, y = yStartAdjusted + length),
                            strokeWidth = 3.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
        }
        
        // VCGMotion Style Reveal Canvas
        if (revealProgress > 0f) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().zIndex(20f)) {
                val maxRadius = size.width * 1.5f
                val colors = listOf(
                    Color(0xFFFFD700), // Gold
                    Color(0xFF00BFFF), // Deep Sky Blue
                    Color(0xFFFFEE58), // Yellow
                    Color(0xFF00509E), // Mid Blue
                    Color(0xFF003060)  // Dark Blue
                )
                
                for (i in colors.indices) {
                   val delay = i * 0.12f
                   val p = (revealProgress - delay) / (1f - delay)
                   if (p > 0f) {
                       drawCircle(
                           color = colors[i],
                           radius = maxRadius * androidx.compose.animation.core.CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f).transform(p.coerceIn(0f, 1f))
                       )
                   }
                }
            }
        }
    }
}

data class DollarSignData(
    val x: Float,
    val y: Float,
    val baseSize: Float,
    val isPulsing: Boolean,
    val isScaling: Boolean,
    val phase: Int
)
