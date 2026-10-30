package com.example.financetracker.data

import kotlinx.coroutines.flow.Flow

interface RecordsRepository {
    fun getRecordsStream(filter: RecordFilter): Flow<List<FinanceRecord>>

    fun getRecordStream(id: Int): Flow<FinanceRecord?>

    suspend fun insertRecord(record: FinanceRecord)

    suspend fun updateRecord(record: FinanceRecord)

    suspend fun deleteRecord(record: FinanceRecord)
}

class OfflineRecordsRepository(private val recordDao: RecordDao) : RecordsRepository {
    override fun getRecordsStream(filter: RecordFilter): Flow<List<FinanceRecord>> =
        when (filter) {
            RecordFilter.ALL -> recordDao.getAllRecords()
            RecordFilter.INCOME -> recordDao.getRecordsByType(RecordType.INCOME)
            RecordFilter.EXPENSE -> recordDao.getRecordsByType(RecordType.EXPENSE)
        }

    override fun getRecordStream(id: Int): Flow<FinanceRecord?> = recordDao.getRecord(id)

    override suspend fun insertRecord(record: FinanceRecord) = recordDao.insert(record)

    override suspend fun updateRecord(record: FinanceRecord) = recordDao.update(record)

    override suspend fun deleteRecord(record: FinanceRecord) = recordDao.delete(record)
}
