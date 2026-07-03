package com.kingstudio.spendwise.data.model

sealed class DateRange {
    object ThisMonth: DateRange()
    object ThisYear: DateRange()
    data class Custom(val startDate: Long, val endDate: Long): DateRange()
}