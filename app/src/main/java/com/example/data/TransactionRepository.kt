package com.example.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao? = null
) {

    fun getTransactionsForMonth(startMs: Long, endMs: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForMonth(startMs, endMs)
    }
    
    fun getTransactionsForDay(startMs: Long, endMs: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForDay(startMs, endMs)
    }
    
    fun getMonthTotalBalance(startMs: Long, endMs: Long): Flow<Double?> {
        return transactionDao.getMonthTotalBalance(startMs, endMs)
    }
    
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao?.getAllCategories() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    suspend fun insertCategory(category: Category) {
        categoryDao?.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao?.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao?.deleteCategory(category)
    }

    suspend fun getCategoryCount(): Int {
        return categoryDao?.getCategoryCount() ?: 0
    }

    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }
    
    suspend fun updateTransaction(id: Int, description: String, value: Double, category: String, dateMs: Long) {
        transactionDao.updateTransaction(id, description, value, category, dateMs)
    }

    suspend fun deleteTransaction(id: Int) {
        transactionDao.deleteTransaction(id)
    }
}
