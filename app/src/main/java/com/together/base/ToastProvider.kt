package com.together.base

import android.os.Handler
import android.os.Looper
import android.widget.Toast

class ToastProvider(private val toastData: UiEvent.ShowToast)  {

    private fun  getRunnable() : Runnable  {
        return Runnable {
            val t = Toast.makeText(toastData.context, toastData.msg, toastData.length)
            t.setGravity(toastData.gravity,0,0)
            t.show()
        }
    }

    fun show(){
        Handler(Looper.getMainLooper()).post(getRunnable())
    }
}