package com.together.utils

import com.together.base.UiState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

private const val time = "HH:mm"
private val format = SimpleDateFormat(time, Locale.GERMANY)

fun getDays(market: UiState.Market): Array<Date> {
    val calendar  = Calendar.getInstance()
    val b = format.parse(market.begin)
    val e = format.parse(market.end)
    calendar.time = Date()
    val midTime = (b.hours+e.hours) *0.5
    calendar.set(Calendar.HOUR_OF_DAY,midTime.toInt())
    calendar.set(Calendar.MINUTE,0)
    while (calendar.get(Calendar.DAY_OF_WEEK) != market.dayIndicator) {
        calendar.add(Calendar.DATE, 1)
    }
    val result = ArrayList<Date>(3)
    (0 until 3).forEach { index ->
        val plusDays = index * 7
        if (index != 0) calendar.add(Calendar.DATE, plusDays)
        result.add(calendar.time)
    }
    return result.toTypedArray()
}

fun alterPickUptime(days: Array<Date>, hour: Int, minute: Int): ArrayList<Date> {
    val newArray = ArrayList<Date>(3)
    val calendar  = Calendar.getInstance()
    days.forEachIndexed { index, day ->
        calendar.clear()
        calendar.time = day
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        newArray.add(index, calendar.time)
    }
    return newArray
}