package com.together.app

import android.content.Context


sealed class UiEvent{

    class LogIn(val context: Context) : UiEvent()

    object LogOut : UiEvent()

    object LoadProducts : UiEvent()

    data class PostChatMessage(var creatorId: String,
                               var name: String,
                               var text: String,
                               var photoUrl: String) : UiEvent()

    data class PostAnyMessage(var creatorId: String,
                              var name: String,
                              var text: String,
                              var photoUrl: String) : UiEvent()




}