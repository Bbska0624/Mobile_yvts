package com.example.financetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FinanceRecord::class], version = 1, exportSchema = false)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var Instance: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, FinanceDatabase::class.java, "finance_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
