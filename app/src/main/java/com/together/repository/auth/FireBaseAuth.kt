package com.together.repository.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.together.base.MainMessagePipe
import com.together.base.UiState
import com.together.repository.Result
import com.together.repository.storage.getCompletable
import io.reactivex.Single
import io.reactivex.disposables.Disposable


object FireBaseAuth : FirebaseAuth.AuthStateListener {

    init {
        FirebaseAuth.getInstance().addAuthStateListener(this)
    }

    lateinit var createUserDisposable: Disposable

    var userProfile: Result.BuyerProfile = Result.BuyerProfile()

    fun getAuth(): FirebaseAuth? {
        return FirebaseAuth.getInstance()
    }
    fun isLoggedIn(): UiState {
        return when (getAuth()?.currentUser != null) {
            true -> UiState.BASE_AUTH
            else -> UiState.LOGGEDOUT
        }
    }

    fun loginWithCredentials(email: String, passWord: String) {
        createUserDisposable.dispose()
        createUserDisposable = getAuth()?.signInWithEmailAndPassword(email,passWord)!!
            .getCompletable().subscribe(
            {
                if(it) {
                    MainMessagePipe.mainThreadMessage.onNext(Result.LoggedIn)
                } else MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
            },{
                    // FIXME
                    MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
                }
        )
    }

    fun deleteAccount(){
        createUserDisposable = getAuth()?.currentUser?.delete()!!.getCompletable().subscribe({

        },{
            // FIXME
            MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
        })
    }

    fun createAccount(email: String, passWord: String){
        createUserDisposable =
            getAuth()?.createUserWithEmailAndPassword(email,passWord)?.getCompletable()?.subscribe(
                {
                    if(it) {
                        MainMessagePipe.mainThreadMessage.onNext(Result.LoggedIn)
                    } else MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)

                },{
                    // FIXME
                    MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
                }
            )!!
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        val user = p0.currentUser
        if (user == null) {
            MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
        } else {
            MainMessagePipe.mainThreadMessage.onNext(Result.LoggedIn)
        }
    }

    fun logOut(){
        getAuth()?.signOut()
    }


    fun Task<AuthResult>.getCompletable(): Single<Boolean> {
        return Single.create { emitter ->
            addOnCompleteListener { emitter.onSuccess(true) }
            addOnCanceledListener { emitter.onSuccess(false) }
            addOnFailureListener { emitter.onError(it) }
        }

    }

}