package com.together.base

import android.content.Context
import android.net.Uri
import android.view.Gravity
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager


sealed class UiEvent {

    companion object {
        //   Drawer State Actions
        const val OPEN_DRAWER = 0
        const val DRAWER_UNLOCKED = 1
        const val LOCK_MODE_LOCKED_CLOSED = 2

        const val UNDEFINED = -1
        const val DELETE_PRODUCT = 1
        const val UPLOAD_PRODUCT = 2
    }


    data class LogIn(val context: Context) : UiEvent()

    object LogOut : UiEvent()

    data class ShowToast(val context: Context, val msg: Int,
                         val gravity: Int = Gravity.TOP,
                         val length: Int = Toast.LENGTH_SHORT) : UiEvent()

    data class DrawerState(val gravity: Int) : UiEvent()

    object Loading : UiEvent()
    data class LoadingDone(val indicator: Int) : UiEvent()
    object ShowCreateFragment : UiEvent()

    object OpenDrawer : UiEvent()

    object CloseDrawer : UiEvent()

    object LockDrawer : UiEvent()

    object UnlockDrawer :  UiEvent()

    data class ShowLicense (val context: Context) : UiEvent()


    data class ReplaceFragment(val fragMange: FragmentManager,
                               val fragment: Fragment,
                               val tag: String): UiEvent()

    data class DialogFragment(val fragMange: FragmentManager,
                               val fragment: Fragment,
                               val tag: String): UiEvent()

    data class AddFragment(val fragMange: FragmentManager,
                           val fragment: Fragment,
                           val tag: String): UiEvent()


    object BasketMinusOne : UiEvent()

    data class RemoveBasketItem(val id: String ): UiEvent()

    data class EditBasketItem(val id: String): UiEvent()

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