package com.together.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.together.R
import com.together.addpicture.AddPicture
import com.together.addpicture.AddPictureImpl

class UtilsActivity : AppCompatActivity() {

    private lateinit var addPicture: AddPicture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPicture = AddPictureImpl(this)
        addPicture.startAddPhoto()
    }

    companion object {
        private const val TYPE_HINT = "type_hint"
        private const val IMAGE_ADD = 1213

        fun startAddImage(context: Context) {
            context.startActivity(Intent(context, UtilsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(TYPE_HINT, IMAGE_ADD)
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        addPicture.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        addPicture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
