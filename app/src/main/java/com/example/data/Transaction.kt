package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val value: Double,
    val dateMs: Long = System.currentTimeMillis(),
    val category: String = "Outros",
    val groupId: String = java.util.UUID.randomUUID().toString(),
    val recurrenceType: String = "UNICO",
    val recurrenceCount: Int = 1
)
