package com.kingstudio.spendwise.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CurrencyPreference @Inject constructor(
    private val dataStore: DataStore<Preferences>) {

    companion object {
        private val CURRENCY_CODE_KEY = stringPreferencesKey("currency_code")
        const val DEFAULT_CURRENCY_CODE = "INR"
    }

    val currencyCode: Flow<String> = dataStore.data.map { prefs ->
        prefs[CURRENCY_CODE_KEY] ?: DEFAULT_CURRENCY_CODE
    }

    suspend fun setCurrencyCode(code: String) {
        dataStore.edit { prefs -> prefs[CURRENCY_CODE_KEY] = code  }
    }
}