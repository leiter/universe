package com.together.app

import android.content.Intent
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
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
                is UiEvent.ShowLicense -> {
                    it.context.startActivity(Intent(it.context,OssLicensesMenuActivity::class.java))
                }
            }
        }
    }

    val mainThreadMessage: PublishSubject<in Result> = PublishSubject.create()



}