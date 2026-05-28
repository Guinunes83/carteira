package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import java.text.NumberFormat
import java.util.Locale
import java.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Size

@Composable
fun ReportsScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    
    var selectedPeriod by remember { mutableStateOf("Mês") }
    val periods = listOf("Mês", "Ano", "Todo")

    val months = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
    
    var selectedMonthIndex by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    var showMonthDropdown by remember { mutableStateOf(false) }
    var showYearDropdown by remember { mutableStateOf(false) }
    
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    // Get available years from transactions
    val availableYears = remember(allTransactions) {
        if (allTransactions.isEmpty()) {
            listOf(Calendar.getInstance().get(Calendar.YEAR))
        } else {
            allTransactions.map {
                val cal = Calendar.getInstance().apply { timeInMillis = it.dateMs }
                cal.get(Calendar.YEAR)
            }.distinct().sortedDescending()
        }
    }
    
    // Ensure selectedYear is within availableYears, or keep the current one
    LaunchedEffect(availableYears) {
        if (!availableYears.contains(selectedYear)) {
            selectedYear = availableYears.firstOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
        }
    }

    // Sum negative values (expenses) per category
    val expenseStats = remember(allTransactions, selectedPeriod, selectedMonthIndex, selectedYear) {
        if (allTransactions.isEmpty()) return@remember emptyMap<String, Double>()
        
        val filtered = when (selectedPeriod) {
            "Mês" -> allTransactions.filter { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.dateMs }
                cal.get(Calendar.MONTH) == selectedMonthIndex && cal.get(Calendar.YEAR) == selectedYear
            }
            "Ano" -> allTransactions.filter { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.dateMs }
                cal.get(Calendar.YEAR) == selectedYear
            }
            else -> allTransactions
        }
        
        filtered.filter { it.value < 0 }.groupBy { it.category }.mapValues { (_, txs) ->
            kotlin.math.abs(txs.sumOf { it.value })
        }
    }
    
    val incomeStats = remember(allTransactions, selectedPeriod, selectedMonthIndex, selectedYear) {
        if (allTransactions.isEmpty()) return@remember emptyMap<String, Double>()
        
        val filtered = when (selectedPeriod) {
            "Mês" -> allTransactions.filter { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.dateMs }
                cal.get(Calendar.MONTH) == selectedMonthIndex && cal.get(Calendar.YEAR) == selectedYear
            }
            "Ano" -> allTransactions.filter { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.dateMs }
                cal.get(Calendar.YEAR) == selectedYear
            }
            else -> allTransactions
        }
        
        filtered.filter { it.value > 0 }.groupBy { it.category }.mapValues { (_, txs) ->
            txs.sumOf { it.value }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 24.dp)
    ) {
        Text(
            text = "Relatórios",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            periods.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { selectedPeriod = period },
                    label = { Text(period) }
                )
            }
        }
        
        if (selectedPeriod == "Mês" || selectedPeriod == "Ano") {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedPeriod == "Mês") {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { showMonthDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(months[selectedMonthIndex])
                        }
                        DropdownMenu(expanded = showMonthDropdown, onDismissRequest = { showMonthDropdown = false }) {
                            months.forEachIndexed { index, monthName ->
                                DropdownMenuItem(
                                    text = { Text(monthName) },
                                    onClick = {
                                        selectedMonthIndex = index
                                        showMonthDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { showYearDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedYear.toString())
                    }
                    DropdownMenu(expanded = showYearDropdown, onDismissRequest = { showYearDropdown = false }) {
                        availableYears.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year.toString()) },
                                onClick = {
                                    selectedYear = year
                                    showYearDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val totalExpense = expenseStats.values.sum()
        val totalIncome = incomeStats.values.sum()
        
        if (totalExpense > 0 || totalIncome > 0) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (totalExpense > 0) {
                    item {
                        Text("Despesas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        // Pie Chart Despesas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(160.dp)) {
                                var startAngle = -90f
                                expenseStats.forEach { (catName, sum) ->
                                    val sweepAngle = ((sum / totalExpense) * 360f).toFloat()
                                    val colorLong = allCategories.find { it.name == catName }?.color ?: 0xFF9E9E9E
                                    drawArc(
                                        color = Color(colorLong),
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = true,
                                        size = Size(size.width, size.height)
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(MaterialTheme.colorScheme.background, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currencyFormat.format(totalExpense),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    val sortedExpenses = expenseStats.entries.sortedByDescending { it.value }
                    items(sortedExpenses) { entry ->
                        val colorLong = allCategories.find { it.name == entry.key }?.color ?: 0xFF9E9E9E
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(16.dp).background(Color(colorLong), CircleShape))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(entry.key, style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(
                                text = currencyFormat.format(entry.value),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
                
                if (totalIncome > 0) {
                    item {
                        Text("Receitas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        // Pie Chart Receitas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(160.dp)) {
                                var startAngle = -90f
                                incomeStats.forEach { (catName, sum) ->
                                    val sweepAngle = ((sum / totalIncome) * 360f).toFloat()
                                    val colorLong = allCategories.find { it.name == catName }?.color ?: 0xFF9E9E9E
                                    drawArc(
                                        color = Color(colorLong),
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = true,
                                        size = Size(size.width, size.height)
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(MaterialTheme.colorScheme.background, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currencyFormat.format(totalIncome),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.GreenIncome
                                )
                            }
                        }
                    }
                    
                    val sortedIncomes = incomeStats.entries.sortedByDescending { it.value }
                    items(sortedIncomes) { entry ->
                        val colorLong = allCategories.find { it.name == entry.key }?.color ?: 0xFF9E9E9E
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(16.dp).background(Color(colorLong), CircleShape))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(entry.key, style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(
                                text = currencyFormat.format(entry.value),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.GreenIncome
                            )
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Sem dados no período")
            }
        }
    }
}
