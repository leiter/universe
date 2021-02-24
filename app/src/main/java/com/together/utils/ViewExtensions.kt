package com.together.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.together.R
import com.together.base.UiEvent
import com.together.repository.NoInternetConnection
import java.util.*


fun TimePicker.getTimePair(minuteSteps: Int = 15):Pair<Int,Int>{
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        Pair(
            this.currentHour,
            this.currentMinute * minuteSteps
        )
    } else {
        Pair(
            this.hour,
            this.minute * minuteSteps
        )
    }
}

fun String.getDayTimeDate(): Date {
    val t = this.split(":")
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY,t[0].toInt())
    c.set(Calendar.MINUTE,t[1].toInt())
    return c.time
}

fun Fragment.handleProgress(loading: UiEvent.Loading,
                            progress:View,
                            toastNoInternet:Int,
                            toastUnknown: Int, toastSuccess: Int) {
    if(loading.autoConsumeResult){
        loading.exception = null
        loading.contextId = -1
        return
    }
    progress.visibility = loading.visible
    if (loading.didFail) {
        val t = if (loading.exception is NoInternetConnection){
            toastNoInternet
        } else toastUnknown
        Toast.makeText(requireContext(), t, Toast.LENGTH_SHORT).show()
        loading.exception = null
    } else if(loading.contextId != -1 ) {
        Toast.makeText(requireContext(), toastSuccess, Toast.LENGTH_SHORT).show()
        loading.contextId=-1
    }

}

fun View.show(){
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.remove(){
    this.visibility = View.GONE
}

// Show alert dialog
fun Context.showAlertDialog(positiveButtonLable : String = getString(android.R.string.ok),
                            title : String = getString(R.string.app_name), message : String,
                            actionOnPositveButton : () -> Unit) {
    val builder = AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(positiveButtonLable) { dialog, id ->
            dialog.cancel()
            actionOnPositveButton()
        }
    val alert = builder.create()
    alert?.show()
}

// Toash extensions
fun Context.showShotToast(message : String){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showLongToast(message : String){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// Snackbar Extensions
fun View.showShotSnackbar(message : String){
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}

fun View.showLongSnackbar(message : String){
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
}

fun View.snackBarWithAction(message : String, actionlable : String,
                            block : () -> Unit){
    Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        .setAction(actionlable) {
            block()
        }
}

