package com.together.base

import android.content.Context
import android.net.Uri
import android.view.Gravity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager


sealed class UiEvent {

    companion object {
        //   Drawer State Actions
        const val OPEN = 0
        const val LOCK_MODE_UNLOCKED = 1
        const val LOCK_MODE_LOCKED_CLOSED = 2
    }


    data class LogIn(val context: Context) : UiEvent()

    object LogOut : UiEvent()

    data class ShowToast(val context: Context, val msg: Int,
                         var gravity: Int = Gravity.TOP) : UiEvent()

    data class DrawerState(val gravity: Int) : UiEvent()



    data class ShowLicense (val context: Context) : UiEvent()



    data class ReplaceFragment(val fragMange: FragmentManager,
                               val fragment: Fragment,
                               val tag: String): UiEvent()

    data class ReplaceFrag(
                               val fragment: Fragment,
                               val tag: String): UiEvent()

    data class AddFragment(val fragMange: FragmentManager,
                           val fragment: Fragment,
                           val tag: String): UiEvent()






    data class PostProduct(var product: UiState.Article)

    object LoadProducts : UiEvent()

    data class PostChatMessage(var creatorId: String,
                               var name: String,
                               var text: String,
                               var photoUrl: String) : UiEvent()

    data class NewImageCreated(val uri:Uri) : UiEvent()

    data class PostAnyMessage(var creatorId: String,
                              var name: String,
                              var text: String,
                              var photoUrl: String) : UiEvent()


}