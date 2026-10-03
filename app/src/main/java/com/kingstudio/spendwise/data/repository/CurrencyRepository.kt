package com.kingstudio.spendwise.data.repository

import com.kingstudio.spendwise.data.local.datastore.CurrencyPreference
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface CurrencyRepository {
    val selectedCurrencyCode: Flow<String>
    suspend fun setCurrencyCode(code: String)
}

class CurrencyRepositoryImpl @Inject constructor(
    private val currencyPreference: CurrencyPreference
) : CurrencyRepository {

    override val selectedCurrencyCode: Flow<String> =
        currencyPreference.currencyCode

    override suspend fun setCurrencyCode(code: String) {
        currencyPreference.setCurrencyCode(code)
    }
}