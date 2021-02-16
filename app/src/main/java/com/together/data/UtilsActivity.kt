package com.together.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.together.R
import com.together.addpicture.AddPicture
import com.together.addpicture.AddPictureImpl
import com.together.repository.auth.FireBaseAuth

class UtilsActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    private lateinit var addPicture: AddPicture
    private val actionMode: Int by lazy { extractActionMode() }
    private var calledOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when(actionMode){
            IMAGE_ADD ->{
                addPicture = AddPictureImpl(this)
                addPicture.startAddPhoto()
            }
            GOOGLE_LOGIN -> {
                FirebaseAuth.getInstance().addAuthStateListener(this)
                val googleSignIn = createGoogleApiClient()
                startActivityForResult(googleSignIn.signInIntent,456)
            }
            else -> throw IllegalStateException("There is no action bound to this value. $actionMode ")
        }
    }

    private fun extractActionMode () : Int {
        return intent.getIntExtra(TYPE_HINT,0)
    }

    private fun createGoogleApiClient() : GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this,gso)
    }


    companion object {
        private const val TYPE_HINT = "type_hint"
        private const val IMAGE_ADD = 1213
        private const val GOOGLE_LOGIN = 1222

        fun startAddImage(context: Context) {
            context.startActivity(Intent(context, UtilsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(TYPE_HINT, IMAGE_ADD)
            })
        }

        fun startGoogleSigning(context: Context) {
            context.startActivity(Intent(context, UtilsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(TYPE_HINT, GOOGLE_LOGIN)
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(actionMode){
            IMAGE_ADD -> addPicture.onActivityResult(requestCode, resultCode, data)
            GOOGLE_LOGIN -> {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                val credentials = GoogleAuthProvider.getCredential(
                    result.signInAccount?.idToken,null)
                FireBaseAuth.loginWithGoogleCredentials(credentials)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        addPicture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        if(calledOnce) finish()
        else calledOnce = true
    }
}
