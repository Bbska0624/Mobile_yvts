package com.example.financetracker.ui.entry

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.R
import com.example.financetracker.broadcast.RecordBroadcast
import com.example.financetracker.data.FinanceRecord
import com.example.financetracker.data.RecordType
import com.example.financetracker.data.RecordsRepository
import com.example.financetracker.data.dateToMillis
import com.example.financetracker.data.todayDate
import com.example.financetracker.ui.navigation.FinanceDestinations
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class RecordDetails(
    val id: Int = 0,
    val type: RecordType = RecordType.EXPENSE,
    val name: String = "",
    val amount: String = "",
    val date: String = todayDate()
)

data class EntryUiState(
    val recordDetails: RecordDetails = RecordDetails(),
    val isEntryValid: Boolean = false,
    val isEditMode: Boolean = false
)

class EntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val recordsRepository: RecordsRepository,
    private val application: Application
) : ViewModel() {

    private val recordId: Int =
        savedStateHandle[FinanceDestinations.RECORD_ID_ARG] ?: FinanceDestinations.NEW_RECORD_ID

    private val recordType: RecordType =
        RecordType.entries.firstOrNull {
            it.name == savedStateHandle.get<String>(FinanceDestinations.RECORD_TYPE_ARG)
        } ?: RecordType.EXPENSE

    var uiState by mutableStateOf(
        EntryUiState(
            recordDetails = RecordDetails(type = recordType),
            isEditMode = recordId != FinanceDestinations.NEW_RECORD_ID
        )
    )
        private set

    init {
        if (recordId != FinanceDestinations.NEW_RECORD_ID) {
            viewModelScope.launch {
                val record = recordsRepository.getRecordStream(recordId).filterNotNull().first()
                uiState = EntryUiState(
                    recordDetails = record.toRecordDetails(),
                    isEntryValid = true,
                    isEditMode = true
                )
            }
        }
    }

    fun updateUiState(recordDetails: RecordDetails) {
        uiState = uiState.copy(
            recordDetails = recordDetails,
            isEntryValid = validateInput(recordDetails)
        )
    }

    suspend fun saveRecord(): Boolean {
        val details = uiState.recordDetails
        if (!validateInput(details)) return false

        if (uiState.isEditMode) {
            recordsRepository.updateRecord(details.toFinanceRecord())
            RecordBroadcast.send(application, application.getString(R.string.record_updated))
        } else {
            recordsRepository.insertRecord(details.toFinanceRecord())
            RecordBroadcast.send(application, application.getString(R.string.record_added))
        }
        return true
    }

    private fun validateInput(details: RecordDetails): Boolean =
        details.name.isNotBlank() &&
            (details.amount.toLongOrNull() ?: 0L) > 0L &&
            dateToMillis(details.date) != null
}

fun RecordDetails.toFinanceRecord(): FinanceRecord = FinanceRecord(
    id = id,
    type = type,
    name = name.trim(),
    amount = amount.toLongOrNull() ?: 0L,
    date = date
)

fun FinanceRecord.toRecordDetails(): RecordDetails = RecordDetails(
    id = id,
    type = type,
    name = name,
    amount = amount.toString(),
    date = date
)
