package com.together.base

import android.content.Context
import android.net.Uri
import android.view.Gravity
import android.view.View
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
        const val SEND_ORDER = 3
        const val LOAD_OLD_ORDERS = 4
        const val INVALIDATE_SESSION = 5
        const val UPLOAD_PROFILE = 6
        const val CLEAR_ACCOUNT = 7
        const val SEND_ORDER_FAILED = 8
        const val SEND_ORDER_UPDATED = 9
        const val LOAD_OLD_ORDERS_FAILED = 10


        const val SHOW_LOADING = View.VISIBLE
        const val HIDE_LOADING = View.GONE

    }


    data class LogIn(val context: Context) : UiEvent()

    object LogOut : UiEvent()

    data class ShowToast(
        val context: Context, val msg: Int,
        val gravity: Int = Gravity.TOP,
        val length: Int = Toast.LENGTH_SHORT
    ) : UiEvent()

    data class DrawerState(val gravity: Int) : UiEvent()

    data class LoadingContainer(
        val profileUpload: Loading = Loading(-1, autoConsumeResult = true)

    )

    data class Loading(
        override var contextId: Int = UNDEFINED,
        override val showProgress: Boolean = false,
        override var autoConsumeResult: Boolean = false,
        var exception: Throwable? = null,
    ) : UiEvent(), LoadingIndication {
        val visible: Int
            get() {
                return if (showProgress) View.VISIBLE else View.GONE
            }
        val didFail: Boolean
            get() {
                return exception != null
            }
    }

    data class LoadingNeutral(
        override val contextId: Int,
        override val showProgress: Boolean = false,
        override var autoConsumeResult: Boolean = false
    ) : UiEvent(), LoadingIndication

    data class LoadingDone(
        override val contextId: Int, val exception: Throwable? = null,
        override val showProgress: Boolean = false,
        override var autoConsumeResult: Boolean = false
    ) : UiEvent(), LoadingIndication

    data class LoadingD<T>(
        val indicator: Int,
        val exception: Throwable? = null,
        val payLoad: Class<T>
    ) : UiEvent()

    object ShowCreateFragment : UiEvent()

    object OpenDrawer : UiEvent()

    object CloseDrawer : UiEvent()

    object LockDrawer : UiEvent()

    object UnlockDrawer : UiEvent()

    data class ShowLicense(val context: Context) : UiEvent()


    data class ReplaceFragment(
        val fragMange: FragmentManager,
        val fragment: Fragment,
        val tag: String
    ) : UiEvent()

    data class DialogFragment(
        val fragMange: FragmentManager,
        val fragment: Fragment,
        val tag: String
    ) : UiEvent()

    data class AddFragment(
        val fragMange: FragmentManager,
        val fragment: Fragment,
        val tag: String
    ) : UiEvent()


    object BasketMinusOne : UiEvent()

    data class RemoveBasketItem(val id: String) : UiEvent()

    data class EditBasketItem(val id: String) : UiEvent()

    data class PostProduct(var product: UiState.Article)

    object LoadProducts : UiEvent()

    data class PostChatMessage(
        var creatorId: String,
        var name: String,
        var text: String,
        var photoUrl: String
    ) : UiEvent()

    data class NewImageCreated(val uri: Uri) : UiEvent()

    data class PostAnyMessage(
        var creatorId: String,
        var name: String,
        var text: String,
        var photoUrl: String
    ) : UiEvent()

    interface LoadingIndication {
        val contextId: Int
        val showProgress: Boolean
        var autoConsumeResult: Boolean
    }
}