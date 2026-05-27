package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE dateMs >= :startOfMonthMs AND dateMs <= :endOfMonthMs ORDER BY dateMs DESC")
    fun getTransactionsForMonth(startOfMonthMs: Long, endOfMonthMs: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE dateMs >= :startOfDayMs AND dateMs <= :endOfDayMs ORDER BY dateMs DESC")
    fun getTransactionsForDay(startOfDayMs: Long, endOfDayMs: Long): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)
    
    @Query("SELECT SUM(value) FROM transactions WHERE dateMs >= :startOfMonthMs AND dateMs <= :endOfMonthMs")
    fun getMonthTotalBalance(startOfMonthMs: Long, endOfMonthMs: Long): Flow<Double?>
    
    @Query("SELECT * FROM transactions ORDER BY dateMs ASC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("UPDATE transactions SET description = :description, value = :value, category = :category, dateMs = dateMs + :dateDiff WHERE groupId = :groupId")
    suspend fun updateTransactionGroup(groupId: String, description: String, value: Double, category: String, dateDiff: Long)

    @Query("DELETE FROM transactions WHERE groupId = :groupId")
    suspend fun deleteTransactionGroup(groupId: String)
}
