package com.together.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

private val dateFormatWithDash = SimpleDateFormat("yyyyMMdd", Locale.GERMANY)
private val dateFormatWithDayName = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
private val dateFormatTime = SimpleDateFormat("HH:mm", Locale.GERMAN)
private val dayFormatDay = SimpleDateFormat("E, dd.MM.yyyy ", Locale.GERMAN)

fun Date.toDateString() :String {
    return dateFormatWithDayName.format(this.time) + "\n" + "Ca. ${dateFormatTime.format(this.time)} Uhr"
}

fun Date.toAppointmentTime() : String {
    return dayFormatDay.format(this.time) + "gegen " + dateFormatTime.format(this.time) + " Uhr"
}

fun Long.toPickUpText() : String {
    return dayFormatDay.format(this) + " um " + dateFormatTime.format(this) + " Uhr"
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

fun Date.getHourAndMinute() : String {
    return dateFormatTime.format(this)
}

fun Long.toDate() : Date {
    val c = Calendar.getInstance()
    c.timeInMillis = this
    return c.time
}

fun Context.getQuantityString(
    pluralResId: Int,
    quantity: Int,
    zeroResId: Int? = null
): String {
    return if (zeroResId != null && quantity == 0) {
        resources.getString(zeroResId)
    } else {
        resources.getQuantityString(pluralResId, quantity, quantity)
    }
}