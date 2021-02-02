package com.together.utils

import java.text.SimpleDateFormat
import java.util.*

private val dateFormatWithDash = SimpleDateFormat("dd_MM_yyyy", Locale.GERMANY)
private val dateFormatWithDayName = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
private val dateFormatTime = SimpleDateFormat("HH:mm", Locale.GERMAN)
private val dayFormatDay = SimpleDateFormat("E, dd.MM.yyyy ", Locale.GERMAN)

fun Date.toDateString() :String {
    return dateFormatWithDayName.format(this.time) + "\n" + "Ca. ${dateFormatTime.format(this.time)} Uhr"
}

fun Date.toAppointmentTime() : String {
    return dayFormatDay.format(this.time) + "gegen " + dateFormatTime.format(this.time) + " Uhr"
}

fun Long.toOrderTime() : String {
    val c = Calendar.getInstance()
    c.timeInMillis = this
    return "Bestellung vom " + dateFormatWithDayName.format(c.time)
}

fun Long.toOrderId() : String {
    val c = Calendar.getInstance()
    c.timeInMillis = this
    return dateFormatWithDash.format(c.time)
}