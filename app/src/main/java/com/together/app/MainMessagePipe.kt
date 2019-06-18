package com.together.app

import android.view.Gravity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.together.repository.Result
import com.together.repository.auth.FirebaseAuth
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

object MainMessagePipe {

    private val uiDisposable: Disposable
    val uiEvent: PublishSubject<in UiEvent> = PublishSubject.create()

    init {
        uiDisposable = uiEvent.subscribe {
            when (it) {
                is UiEvent.LogIn -> {
                    MainActivity.startLogin(it.context)
                }
                is UiEvent.LogOut -> {
                    FirebaseAuth.logOut()
                }

                is UiEvent.ShowToast -> {
                    ToastProvider(it).show()
                }

                is UiEvent.DrawerState -> {
                    when (it.gravity) {
                        Gravity.START -> it.drawerLayout.closeDrawers()
                        Gravity.END -> it.drawerLayout.openDrawer(it.gravity)
                    }
                }


            }
        }
    }

    val mainThreadMessage: PublishSubject<in Result> = PublishSubject.create()

    var listenerMap: MutableMap<ChildEventListener, Pair<ChildEventListener,
            DatabaseReference>> = hashMapOf()


}