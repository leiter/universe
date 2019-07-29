package com.together.base

import android.app.Application

class App : Application() {


//    , HasActivityInjector
//    @Inject
//    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
//
//    override fun activityInjector(): AndroidInjector<Activity> {
//        return activityDispatchingAndroidInjector
//    }

    override fun onCreate() {
        super.onCreate()
//        DaggerAppComponent.builder()
//            .create(this).inject(this)
    }
}


