package com.together

import android.app.Application
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import dagger.hilt.android.HiltAndroidApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        setupPicasso()
    }

    private fun setupPicasso(){
        val picassoBuild = Picasso.Builder(this)
            .downloader(OkHttp3Downloader(this, Long.MAX_VALUE))
            .build()
//        if(BuildConfig.DEBUG) {
//            picassoBuild.isLoggingEnabled = true
////            picassoBuild.setIndicatorsEnabled(true)
//        }
        Picasso.setSingletonInstance(picassoBuild)
    }
}


