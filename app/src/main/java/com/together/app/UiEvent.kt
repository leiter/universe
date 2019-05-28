package com.together.app

import android.content.Context

sealed class UiEvent {


    class LogIn(val context: Context) : UiEvent()
    class LogOut(val context: Context) : UiEvent()



//    data class OrderNow(val )

}