package com.together.app

import android.content.Context
import android.view.Gravity
import androidx.appcompat.app.ActionBarDrawerToggle


sealed class UiEvent {

    class LogIn(val context: Context) : UiEvent()

    object LogOut : UiEvent()

    object LoadProducts : UiEvent()

    data class ShowToast(val context: Context, val msg: Int,
                         var gravity: Int = Gravity.TOP) : UiEvent()


    data class DrawerState(val drawerToggle: ActionBarDrawerToggle)

    data class PostChatMessage(var creatorId: String,
                               var name: String,
                               var text: String,
                               var photoUrl: String) : UiEvent()


    data class PostAnyMessage(var creatorId: String,
                              var name: String,
                              var text: String,
                              var photoUrl: String) : UiEvent()


}