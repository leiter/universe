package com.together.app

import android.content.Context
import android.net.Uri
import android.view.Gravity
import androidx.drawerlayout.widget.DrawerLayout


sealed class UiEvent {

    class LogIn(val context: Context) : UiEvent()

    object LogOut : UiEvent()

    object LoadProducts : UiEvent()

    data class ShowToast(val context: Context, val msg: Int,
                         var gravity: Int = Gravity.TOP) : UiEvent()

    data class PostProduct(var product: UiState.Article)

    data class DrawerState(val drawerLayout: DrawerLayout, val gravity: Int) : UiEvent()

    data class PostChatMessage(var creatorId: String,
                               var name: String,
                               var text: String,
                               var photoUrl: String) : UiEvent()

    data class NewImageCreated(val uri:Uri) : UiEvent()

    data class PostAnyMessage(var creatorId: String,
                              var name: String,
                              var text: String,
                              var photoUrl: String) : UiEvent()


    class ShowFragment()


}