package com.example.financetracker.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.R
import com.example.financetracker.broadcast.RecordBroadcast
import com.example.financetracker.data.FinanceRecord
import com.example.financetracker.data.RecordFilter
import com.example.financetracker.data.RecordType
import com.example.financetracker.data.RecordsRepository
import com.example.financetracker.data.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class SelectedMonth(val year: Int, val month: Int) {
    val datePrefix: String get() = String.format(Locale.US, "%04d.%02d", year, month)
    val label: String get() = String.format(Locale.US, "%04d-%02d", year, month)

    fun previous(): SelectedMonth =
        if (month == 1) SelectedMonth(year - 1, 12) else SelectedMonth(year, month - 1)

    fun next(): SelectedMonth =
        if (month == 12) SelectedMonth(year + 1, 1) else SelectedMonth(year, month + 1)

    companion object {
        fun current(): SelectedMonth {
            val calendar = Calendar.getInstance()
            return SelectedMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
        }
    }
}

data class HomeUiState(
    val records: List<FinanceRecord> = emptyList(),
    val selectedMonth: SelectedMonth = SelectedMonth.current(),
    val filter: RecordFilter = RecordFilter.ALL,
    val hasAnyRecords: Boolean = false,
    val areAmountsVisible: Boolean = false
) {
    val incomeTotal: Long get() = records.filter { it.type == RecordType.INCOME }.sumOf { it.amount }
    val expenseTotal: Long get() = records.filter { it.type == RecordType.EXPENSE }.sumOf { it.amount }
}

class HomeViewModel(
    private val recordsRepository: RecordsRepository,
    settingsRepository: SettingsRepository,
    private val application: Application
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(SelectedMonth.current())
    private val areAmountsVisible = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredRecords = settingsRepository.recordFilter
        .flatMapLatest { filter ->
            recordsRepository.getRecordsStream(filter).map { records -> filter to records }
        }

    private val hasAnyRecords = recordsRepository.getRecordsStream(RecordFilter.ALL)
        .map { it.isNotEmpty() }

    val uiState: StateFlow<HomeUiState> =
        combine(
            filteredRecords,
            selectedMonth,
            hasAnyRecords,
            areAmountsVisible
        ) { (filter, records), month, hasAny, amountsVisible ->
            HomeUiState(
                records = records
                    .filter { it.date.startsWith(month.datePrefix) }
                    .sortedWith(compareByDescending<FinanceRecord> { it.date }.thenByDescending { it.id }),
                selectedMonth = month,
                filter = filter,
                hasAnyRecords = hasAny,
                areAmountsVisible = amountsVisible
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = HomeUiState()
        )

    fun showPreviousMonth() {
        selectedMonth.update { it.previous() }
    }

    fun showNextMonth() {
        selectedMonth.update { it.next() }
    }

    fun toggleAmountVisibility() {
        areAmountsVisible.update { !it }
    }

    fun deleteRecord(record: FinanceRecord) {
        viewModelScope.launch {
            recordsRepository.deleteRecord(record)
            RecordBroadcast.send(application, application.getString(R.string.record_deleted))
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
