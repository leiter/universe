package com.together.base

import android.app.Application
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.OkHttpDownloader
import com.squareup.picasso.Picasso

class App : Application(){

    override fun onCreate() {
        super.onCreate()
        val picassoBuild = Picasso.Builder(this)
            .downloader(OkHttp3Downloader(this, Long.MAX_VALUE))
            .build()
        picassoBuild.setIndicatorsEnabled(true)
        picassoBuild.isLoggingEnabled = true
        Picasso.setSingletonInstance(picassoBuild)
    }
}


