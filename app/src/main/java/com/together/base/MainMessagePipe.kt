package com.together.base

import android.content.Intent
import android.view.Gravity
import androidx.transition.Fade
import androidx.transition.Slide
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.together.R
import com.together.app.MainActivity
import com.together.repository.Result
import com.together.repository.auth.FireBaseAuth
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
                    //fixme
                    FireBaseAuth.logOut()
                }

                is UiEvent.ShowToast -> {
                    ToastProvider(it).show()
                }
                is UiEvent.ShowLicense -> {
                    it.context.startActivity(Intent(it.context, OssLicensesMenuActivity::class.java))
                }
                is UiEvent.ReplaceFragment -> {
                    val f = it.fragMange.findFragmentByTag(it.tag)
                    if (f == null) {
                        it.fragment.exitTransition = Fade(Fade.MODE_OUT)
                        it.fragment.enterTransition = Fade(Fade.MODE_IN)
                        it.fragment.returnTransition = Fade(Fade.MODE_OUT)
                        it.fragMange.beginTransaction()
                            .replace(R.id.container, it.fragment, it.tag)
                            .commit()
                    }
                }
                is UiEvent.AddFragment -> {
                    val f = it.fragMange.findFragmentByTag(it.tag)
                    if (f == null) {
                        it.fragment.exitTransition = Slide(Gravity.START)
                        it.fragment.enterTransition = Slide(Gravity.END)
                        it.fragment.returnTransition = Slide(Gravity.START)
                        it.fragMange.beginTransaction()
                            .add(R.id.container, it.fragment, it.tag)
                            .addToBackStack(null)
                            .commit()
                    }

                }
                is UiEvent.DialogFragment -> {
                    val f = it.fragMange.findFragmentByTag(it.tag)
                    if (f == null) {
                        it.fragment.exitTransition = Fade(Fade.MODE_OUT)
                        it.fragment.enterTransition = Fade(Fade.MODE_IN)
                        it.fragment.returnTransition = Fade(Fade.MODE_OUT)
                        it.fragMange.beginTransaction()
                            .replace(R.id.container, it.fragment, it.tag)
                            .commit()
                    }
                }
            }
        }
    }

    val mainThreadMessage: PublishSubject<in Result> = PublishSubject.create()

}