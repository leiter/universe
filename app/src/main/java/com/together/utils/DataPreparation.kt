package com.together.utils

import com.together.base.UiState
import com.together.repository.Result
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val time = "HH:mm"
private val format = SimpleDateFormat(time, Locale.GERMANY)

fun getDays(market: UiState.Market, hourMinute: Date? = null): Array<Date> {
    val calendar = Calendar.getInstance()
    val b = format.parse(market.begin)
    val e = format.parse(market.end)

    val (midHour, midMinute) =
        if (hourMinute != null) {
            calendar.time = hourMinute
            calendar.get(Calendar.HOUR_OF_DAY) to calendar.get(Calendar.MINUTE)
        } else ((b!!.hours + e!!.hours) * 0.5).toInt() to 0

    calendar.time = Date()
    calendar.set(Calendar.HOUR_OF_DAY, midHour)
    calendar.set(Calendar.MINUTE, midMinute)
    while (calendar.get(Calendar.DAY_OF_WEEK) != market.dayIndicator) {
        calendar.add(Calendar.DATE, 1)
    }
    val result = ArrayList<Date>(3)
    (0 until 2).forEach { index ->
        val plusDays = index * 7
        if (index != 0) calendar.add(Calendar.DATE, plusDays)
        result.add(calendar.time)
    }
    return result.toTypedArray()
}

fun alterPickUptime(days: Array<Date>, hour: Int, minute: Int): ArrayList<Date> {
    val newArray = ArrayList<Date>(2)
    val calendar = Calendar.getInstance()
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


fun Result.SellerProfile.getMarketDates() : List<String> {
    val result = ArrayList<String>()
    val days = this.markets.map { it.dayIndex }
    val calendar = Calendar.getInstance()
    val startDay = calendar.get(Calendar.DAY_OF_WEEK)
    val diffList = days.map { it - startDay }
    val nextMarketDay = diffList.minByOrNull { it }
    val firstToGo = diffList.indexOfFirst { it == nextMarketDay }
    val i = days[firstToGo]
    val nf = setOnCalendar(calendar,i)
    result.add(nf.time.time.toOrderId())
    val rest = days.toMutableList()
    rest.removeAt(firstToGo)
    rest.forEach {
        val n = setOnCalendar(calendar,it)
        result.add(n.time.time.toOrderId())
    }
    return result.toList()
}

private fun setOnCalendar(calendar: Calendar, dayIndex: Int): Calendar {
    while (calendar.get(Calendar.DAY_OF_WEEK) != dayIndex) {
        calendar.add(Calendar.DATE, 1)
    }
    return calendar
}