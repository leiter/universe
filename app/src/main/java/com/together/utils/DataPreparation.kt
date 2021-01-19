package com.together.utils

import java.util.*
import kotlin.collections.ArrayList

fun getDays(targetDay: Int): Array<Date> {
    val calendar  = Calendar.getInstance()
    calendar.time = Date()
    while (calendar.get(Calendar.DAY_OF_WEEK) != targetDay) {
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