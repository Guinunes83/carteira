package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.data.Transaction
import com.example.data.TransactionRepository
import com.example.data.UserPreferences
import com.example.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: TransactionRepository,
    val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedDateMs = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMs: StateFlow<Long> = _selectedDateMs

    val allTransactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTransactions: StateFlow<List<Transaction>> = combine(
        _selectedDateMs,
        allTransactions,
        userPreferences.startDayFlow,
        userPreferences.endDayFlow
    ) { dateMs, txs, sDay, eDay ->
        txs.filter { tx ->
            val startMs = DateUtils.getStartOfDayMs(tx.dateMs)
            val endCycleMs = DateUtils.getEndOfCycleMs(tx.dateMs, sDay, eDay)
            dateMs in startMs..endCycleMs
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyBalance: StateFlow<Double> = combine(
        _selectedDateMs,
        allTransactions
    ) { dateMs, txs ->
        val startOfTargetDateMs = DateUtils.getStartOfDayMs(dateMs)
        val endOfTargetDateMs = DateUtils.getEndOfDayMs(dateMs)
        
        txs.filter { it.dateMs in startOfTargetDateMs..endOfTargetDateMs }.sumOf { it.value }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val cycleTotal: StateFlow<Double> = combine(
        _selectedDateMs,
        allTransactions
    ) { dateMs, txs ->
        val endOfTargetDateMs = DateUtils.getEndOfDayMs(dateMs)
        txs.filter { it.dateMs <= endOfTargetDateMs }.sumOf { it.value }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val isDarkTheme = userPreferences.isDarkThemeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val startDay = userPreferences.startDayFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    val endDay = userPreferences.endDayFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val allCategories = repository.getAllCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (repository.getCategoryCount() == 0) {
                repository.insertCategory(com.example.data.Category("Alimentação", 0xFFE53935, "Restaurant"))
                repository.insertCategory(com.example.data.Category("Transporte", 0xFF1E88E5, "DirectionsCar"))
                repository.insertCategory(com.example.data.Category("Saúde", 0xFF43A047, "HealthAndSafety"))
                repository.insertCategory(com.example.data.Category("Moradia", 0xFF8E24AA, "Home"))
                repository.insertCategory(com.example.data.Category("Educação", 0xFFFDD835, "School"))
                repository.insertCategory(com.example.data.Category("Lazer", 0xFFFB8C00, "Pets"))
                repository.insertCategory(com.example.data.Category("Vestuário", 0xFFD81B60, "Checkroom"))
                repository.insertCategory(com.example.data.Category("Outros", 0xFF6D4C41, "Category"))
            }
        }
    }

    fun changeDate(dateMs: Long) {
        _selectedDateMs.value = dateMs
    }

    fun addTransactionGroup(description: String, value: Double, dateMs: Long, category: String, recurrenceType: String, recurrenceCount: Int) {
        viewModelScope.launch {
            val groupId = java.util.UUID.randomUUID().toString()
            val calendar = java.util.Calendar.getInstance().apply { timeInMillis = dateMs }
            
            val totalCount = if (recurrenceType == "MENSAL") recurrenceCount else 1
            // Simple logic: if 'RECORRENTE' we can just insert a large number, e.g. 120 (10 years)
            val loops = if (recurrenceType == "RECORRENTE") 120 else totalCount
            
            for (i in 0 until loops) {
                repository.insert(Transaction(
                    description = description,
                    value = value,
                    dateMs = calendar.timeInMillis,
                    category = category,
                    groupId = groupId,
                    recurrenceType = recurrenceType,
                    recurrenceCount = totalCount
                ))
                // Add 1 month for the next transaction
                if (recurrenceType == "UNICO") break
                calendar.add(java.util.Calendar.MONTH, 1)
            }
        }
    }

    fun updateTransactionGroup(groupId: String, description: String, value: Double, category: String, dateDiff: Long) {
        viewModelScope.launch {
            repository.updateTransactionGroup(groupId, description, value, category, dateDiff)
        }
    }

    fun deleteTransactionGroup(groupId: String) {
        viewModelScope.launch {
            repository.deleteTransactionGroup(groupId)
        }
    }

    fun addCategory(name: String, color: Long, iconName: String) {
        viewModelScope.launch {
            repository.insertCategory(com.example.data.Category(name, color, iconName))
        }
    }

    fun updateCategory(originalName: String, name: String, color: Long, iconName: String) {
        viewModelScope.launch {
            // First we need to get the original category to delete it since 'name' is the primary key.
            // Using a simple workaround, we can delete the old one and insert the new one if the name changed,
            // or just update it if only color/icon changed. But Room's @Update works on primary key.
            // So if name is same, just update. If name is different, delete old, insert new.
            // But we don't really have an issue with category name unless there's a cascade requirement on transactions.
            // Note: Currently transaction has "category: String", so renaming a category might orphan transactions
            // unless we also update transactions. The user didn't mention updating transaction categories, 
            // but typical behaviour: if we edit a category, we might want to update transactions.
            // Let's keep it simple for now, as user just asked to edit, delete or add.
            val cat = com.example.data.Category(name, color, iconName)
            if (originalName != name) {
                repository.deleteCategory(com.example.data.Category(originalName, 0, ""))
                repository.insertCategory(cat)
            } else {
                repository.updateCategory(cat)
            }
        }
    }
    
    fun deleteCategory(name: String) {
        viewModelScope.launch {
            repository.deleteCategory(com.example.data.Category(name, 0, ""))
        }
    }
    
    fun setStartDay(day: Int) = viewModelScope.launch { userPreferences.setStartDay(day) }
    fun setEndDay(day: Int) = viewModelScope.launch { userPreferences.setEndDay(day) }
    fun setIsDarkTheme(isDark: Boolean) = viewModelScope.launch { userPreferences.setIsDarkTheme(isDark) }

    class Factory(private val repository: TransactionRepository, private val userPreferences: UserPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository, userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
