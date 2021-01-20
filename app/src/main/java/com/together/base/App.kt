package com.together.base

import android.app.Application
import android.os.Build
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.together.BuildConfig

class App : Application(){

    override fun onCreate() {
        super.onCreate()
        val picassoBuild = Picasso.Builder(this)
            .downloader(OkHttp3Downloader(this, Long.MAX_VALUE))
            .build()
        picassoBuild.setIndicatorsEnabled(true)
        if(BuildConfig.DEBUG) picassoBuild.isLoggingEnabled = true
        Picasso.setSingletonInstance(picassoBuild)
    }
//    override fun attachBaseContext(base: Context) {
//        super.attachBaseContext(base)
//        MultiDex.install(this)
//    }
}


