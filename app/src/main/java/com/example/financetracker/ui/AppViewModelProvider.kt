package com.example.financetracker.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financetracker.FinanceApplication
import com.example.financetracker.ui.entry.EntryViewModel
import com.example.financetracker.ui.home.HomeViewModel
import com.example.financetracker.ui.settings.SettingsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                financeApplication().container.recordsRepository,
                financeApplication().container.settingsRepository,
                financeApplication()
            )
        }
        initializer {
            EntryViewModel(
                this.createSavedStateHandle(),
                financeApplication().container.recordsRepository,
                financeApplication()
            )
        }
        initializer {
            SettingsViewModel(financeApplication().container.settingsRepository)
        }
    }
}

fun CreationExtras.financeApplication(): FinanceApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as FinanceApplication)
