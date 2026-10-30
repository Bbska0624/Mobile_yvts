package com.example.financetracker.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.RecordFilter
import com.example.financetracker.data.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    var selectedFilter by mutableStateOf(RecordFilter.ALL)
        private set

    init {
        viewModelScope.launch {
            selectedFilter = settingsRepository.recordFilter.first()
        }
    }

    fun selectFilter(filter: RecordFilter) {
        selectedFilter = filter
    }

    suspend fun saveSettings() {
        settingsRepository.saveRecordFilter(selectedFilter)
    }
}
