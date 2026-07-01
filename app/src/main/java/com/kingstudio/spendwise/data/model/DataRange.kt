package com.kingstudio.spendwise.data.model

sealed class DataRange {
    object ThisMonth: DataRange()
    object ThisYear: DataRange()
    data class Custom(val startDate: Long, val endDate: Long): DataRange()
}