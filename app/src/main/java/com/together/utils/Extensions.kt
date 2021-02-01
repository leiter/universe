package com.together.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
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

fun View.showIme() {
    this.requestFocus()
    val inputMethod = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethod.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
}



fun Activity.setDrawable(resId: Int, imageView: AppCompatImageButton){
    val d = ResourcesCompat.getDrawable(resources, resId, this.theme)
    imageView.setImageDrawable(d)
}

fun Context.loadImage(imageView: ImageView, url: String){
    Picasso.with(this)
        .load(url)
        .networkPolicy(NetworkPolicy.OFFLINE)
        .into(imageView, object : Callback {
            override fun onSuccess() {
                MainMessagePipe.mainThreadMessage.onNext(
                    Result.ImageLoaded(R.id.pr_load_image_progress, false))
            }

            override fun onError() {
                Picasso.with(applicationContext)
                    .load(url)
                    .error(R.drawable.obst_1)
                    .into(imageView, object : Callback {
                        override fun onSuccess() {
                            MainMessagePipe.mainThreadMessage.onNext(
                                Result.ImageLoaded(R.id.pr_load_image_progress,false))
                        }

                        override fun onError() {
                            MainMessagePipe.mainThreadMessage.onNext(
                                Result.ImageLoaded(R.id.pr_load_image_progress,false))
                        }
                    })
            }
        })
}