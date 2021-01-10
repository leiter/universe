package com.together.utils

import android.content.Context
import android.net.Uri
import android.telecom.Call
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.together.R
import com.together.base.MainMessagePipe
import com.together.repository.Result


fun View.hideIme() {
    val inputMethod =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethod.hideSoftInputFromWindow(windowToken, 0)
    clearFocus()
}

fun Context.loadImage(imageView: ImageView, url: String){
    Picasso.with(this)
        .load(url)
        .networkPolicy(NetworkPolicy.OFFLINE)
        .into(imageView, object : Callback {
            override fun onSuccess() {
                MainMessagePipe.mainThreadMessage.onNext(
                    Result.ImageLoaded(R.id.load_image_progress, false))
            }

            override fun onError() {
                Picasso.with(applicationContext)
                    .load(url)
                    .error(R.drawable.obst_1)
                    .into(imageView, object : Callback {
                        override fun onSuccess() {
                            MainMessagePipe.mainThreadMessage.onNext(
                                Result.ImageLoaded(R.id.load_image_progress,false))
                        }

                        override fun onError() {
                            MainMessagePipe.mainThreadMessage.onNext(
                                Result.ImageLoaded(R.id.load_image_progress,false))
                        }

                    }) // Maybe add callback again.
            }

        })
}