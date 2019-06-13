package com.together.app

import android.os.Handler
import android.widget.Toast

class ToastProvider(val toastData: UiEvent.ShowToast)  {

    private fun  getRunnable() : Runnable  {
        return object : Runnable{
            override fun run() {
                val i =  Toast(toastData.context)
                i.apply {
                    setGravity(toastData.gravity,0,0)
                    setText(toastData.msg)
                    duration = Toast.LENGTH_SHORT
                }
            }
        }
    }

    fun show(){
        Handler().post(getRunnable())
    }
}