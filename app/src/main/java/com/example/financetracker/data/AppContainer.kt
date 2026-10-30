package com.example.financetracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

interface AppContainer {
    val recordsRepository: RecordsRepository
    val settingsRepository: SettingsRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val recordsRepository: RecordsRepository by lazy {
        OfflineRecordsRepository(FinanceDatabase.getDatabase(context).recordDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context.dataStore)
    }
}
