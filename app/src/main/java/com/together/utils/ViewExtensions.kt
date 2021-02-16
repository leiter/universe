package com.together.utils

import android.os.Build
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.together.data.UiEvent
import com.together.repository.NoInternetConnection


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