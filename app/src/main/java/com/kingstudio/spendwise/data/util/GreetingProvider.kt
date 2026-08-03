package com.kingstudio.spendwise.data.util

import java.time.LocalTime

object GreetingProvider {
    fun currentGreeting(): String = when(LocalTime.now().hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good evening"
    }
}