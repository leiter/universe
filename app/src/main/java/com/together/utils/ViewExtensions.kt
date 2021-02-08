package com.together.utils

import android.os.Build
import android.widget.TimePicker


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