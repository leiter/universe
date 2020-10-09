package com.together.base

import android.os.Handler
import android.widget.Toast

class ToastProvider(private val toastData: UiEvent.ShowToast)  {

    private fun  getRunnable() : Runnable  {
        return Runnable {
            val t = Toast.makeText(toastData.context, toastData.msg, toastData.length)
            t.setGravity(toastData.gravity,0,0)
            t.show()
            //                val i =  Toast(toastData.context)
            //                i.apply {
            //                    setGravity(toastData.gravity,0,0)
            //
            //                    duration = Toast.LENGTH_SHORT
            //                }
        }
    }

    fun show(){
        Handler().post(getRunnable())
    }
}