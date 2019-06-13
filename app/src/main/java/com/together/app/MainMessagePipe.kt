package com.together.app

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.together.repository.Result
import com.together.repository.auth.FirebaseAuth
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

object MainMessagePipe {

    val uiEvent : PublishSubject<in UiEvent> = PublishSubject.create()
    private val uiDisposable: Disposable

    init {
        uiDisposable = uiEvent.subscribe {
            when(it) {
                is UiEvent.LogIn -> {
                    MainActivity.startLogin(it.context)
                }
                is UiEvent.LogOut -> {
                    FirebaseAuth.logOut()
                }

                is UiEvent.ShowToast -> {
                    ToastProvider(it).show()
                }




            }
        }
    }

    val mainThreadMessage : PublishSubject<in Result> = PublishSubject.create()

    var listenerMap: MutableMap<ChildEventListener, Pair<ChildEventListener,
                    DatabaseReference>> = hashMapOf()




}