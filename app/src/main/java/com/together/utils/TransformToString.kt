package com.together.utils

import java.text.SimpleDateFormat
import java.util.*

private val dateFormatWithDayName = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
private val dateFormatTime = SimpleDateFormat("HH:mm", Locale.GERMAN)
private val dayFormatDay = SimpleDateFormat("E, dd.MM.yyyy ", Locale.GERMAN)

fun Date.toDateString() :String {
    return dateFormatWithDayName.format(this.time) + "\n" + "Ca. ${dateFormatTime.format(this.time)} Uhr"
}

fun Date.toAppointmentTime() : String {
    return dayFormatDay.format(this.time) + "gegen " + dateFormatTime.format(this.time) + " Uhr"
}