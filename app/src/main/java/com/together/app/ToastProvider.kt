package com.together.app

import android.os.Handler
import android.util.Log

class ToastProvider(val toastData: UiEvent.ShowToast)  {

    private fun  getRunnable() : Runnable  {
        return object : Runnable{
            override fun run() {
                Log.e("TTTTT", "For debugging RRRRRRRRrr");
//                val i =  Toast(toastData.context)
//                i.apply {
//                    setGravity(toastData.gravity,0,0)
//
//                    duration = Toast.LENGTH_SHORT
//                }
            }
        }
    }

    fun show(){
        Handler().post(getRunnable())
    }
}