package com.example.financetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RecordType {
    INCOME,
    EXPENSE
}

@Entity(tableName = "records")
data class FinanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: RecordType,
    val name: String,
    val amount: Long,
    val date: String
)
