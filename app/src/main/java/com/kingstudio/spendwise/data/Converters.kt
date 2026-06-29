package com.kingstudio.spendwise.data

import androidx.room.TypeConverter
import com.kingstudio.spendwise.data.local.entity.IncomeFrequency
import com.kingstudio.spendwise.data.local.entity.IncomeSource

class Converters {

    @TypeConverter
    fun fromIncomeFrequency(value: IncomeFrequency): String = value.name

    @TypeConverter
    fun toIncomeFrequency(value: String): IncomeFrequency = IncomeFrequency.valueOf(value)

    @TypeConverter
    fun fromIncomeSource(value: IncomeSource): String = value.name

    @TypeConverter
    fun toIncomeSource(value: String): IncomeSource = IncomeSource.valueOf(value)

}