package com.example.financetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: FinanceRecord)

    @Update
    suspend fun update(record: FinanceRecord)

    @Delete
    suspend fun delete(record: FinanceRecord)

    @Query("SELECT * FROM records WHERE id = :id")
    fun getRecord(id: Int): Flow<FinanceRecord?>

    @Query("SELECT * FROM records ORDER BY date ASC, id ASC")
    fun getAllRecords(): Flow<List<FinanceRecord>>

    @Query("SELECT * FROM records WHERE type = :type ORDER BY date ASC, id ASC")
    fun getRecordsByType(type: RecordType): Flow<List<FinanceRecord>>
}
