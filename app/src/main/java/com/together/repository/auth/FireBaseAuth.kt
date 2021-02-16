package com.together.repository.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.together.base.MainMessagePipe
import com.together.base.UiState
import com.together.repository.Result
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CancellationException


object FireBaseAuth : FirebaseAuth.AuthStateListener {

    init {
        FirebaseAuth.getInstance().addAuthStateListener(this)
    }

    private lateinit var createUserDisposable: Disposable

    fun getAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    fun isLoggedIn(): UiState {
        return when (getAuth().currentUser != null) {
            true -> UiState.BaseAuth()
            else -> UiState.LoggedOut
        }
    }

    fun loginAnonymously(){
        createUserDisposable = getAuth().signInAnonymously().getSingle().subscribe(
            {
                if(it.user!= null) {
                    MainMessagePipe.mainThreadMessage.onNext(Result.LoggedIn(it.user!!))
                } else MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
            },{
                // FIXME
                MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
            }
        )
    }

    fun deleteAccount() : Task<Void> {
        return getAuth().currentUser!!.delete()
    }

    fun loginWithEmailAndPassWord(email: String, passWord: String) {
        createUserDisposable.dispose()
        createUserDisposable = getAuth().signInWithEmailAndPassword(email,passWord)
            .getSingle().subscribe(
            {
                if(it.user!= null) {
                    MainMessagePipe.mainThreadMessage.onNext(Result.LoggedIn(it.user!!))
                } else MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
            },{
                    // FIXME
                    MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
                }
        )
    }

    fun loginWithGoogleCredentials(credentials: AuthCredential){
        createUserDisposable = getAuth().signInWithCredential(credentials)
            .getSingle().subscribe( {
                if(it.user!=null) {
                    MainMessagePipe.mainThreadMessage.onNext(Result.LoggedIn(it.user!!))
                } else MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)

            },{
                // FIXME
                MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
            })
    }

    fun createAccountWithEmailAndPassword(email: String, passWord: String,
                                          migrateAnonymous: Boolean = false){
        createUserDisposable =
            getAuth().createUserWithEmailAndPassword(email,passWord).getSingle()
                .map {
                if (migrateAnonymous) getAuth().currentUser!!.linkWithCredential(it.credential!!)
                it
            }.subscribe(
                {
                    if(it != null && it.user != null) {
                        MainMessagePipe.mainThreadMessage.onNext(Result.LoggedIn(it.user!!))
                    } else MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)

                },{
                    // FIXME
                    MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
                }
            )
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        val user = p0.currentUser
        if (user == null) {
            MainMessagePipe.mainThreadMessage.onNext(Result.LoggedOut)
        } else {
            MainMessagePipe.mainThreadMessage.onNext(Result.LoggedIn(user))
        }
    }

    fun logOut() { getAuth().signOut() }

    private fun Task<AuthResult>.getSingle(): Single<AuthResult> {
        return Single.create { emitter ->
            addOnCompleteListener { it.result?.let { result -> emitter.onSuccess(result)}
                ?: emitter.onError(it.exception?: NullPointerException("AuthResult is null")) }
            addOnCanceledListener { emitter.onError(exception ?: CancellationException("Task was cancelled")) }
            addOnFailureListener { emitter.onError(it) }
        }

    }

}