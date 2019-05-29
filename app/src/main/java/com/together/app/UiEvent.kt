package com.together.app

import android.content.Context

sealed class UiEvent {

    class LogIn(val context: Context) : UiEvent()

    object LogOut : UiEvent()



//    data class OrderNow(val )

}