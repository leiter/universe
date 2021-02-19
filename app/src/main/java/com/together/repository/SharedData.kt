package com.together.repository

import android.content.Context
import android.content.SharedPreferences
import com.together.BuildConfig
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

data class SharedData(
    val outPutFileName: String,
    ) {
    companion object {
        // keys for Strings
        const val DEVICE_ID = "device_id"
        const val ANDROID_VERSION = "android_version"

        // keys for Ints
        const val BUILD_VERSION = "build_version"

        val stringKeys = listOf(
            DEVICE_ID,
            ANDROID_VERSION
        )
        val stringDefault = listOf(
            "${UUID.randomUUID()}_${System.currentTimeMillis()}",
            android.os.Build.VERSION.RELEASE
        )

        val intKeys = listOf(BUILD_VERSION)
        val intDefault = listOf(BuildConfig.VERSION_CODE)

    }

    lateinit var strings: Map<String, String>
    lateinit var ints: Map<String, Int>


    private fun prepare(context: Context): SharedPreferences {
        return context.getSharedPreferences(outPutFileName, Context.MODE_PRIVATE)
    }

    fun loadValues(context: Context) : Single<SharedData>{
       return Single.fromCallable {
           val s = mutableMapOf<String,String>()
           val i = mutableMapOf<String,Int>()
           val pref = prepare(context)
           stringKeys.forEachIndexed { index, key ->
               s[key] = pref.getString(key , stringDefault[index]) ?: ""}
           strings = s.toMap()

           intKeys.forEachIndexed { index, key ->
               i[key] = pref.getInt(key, intDefault[index] )
           }
           ints = i.toMap()
           this
       }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun save(context: Context): Single<Boolean> {
        return Single.fromCallable {
            prepare(context).edit().also { editor ->
                strings.keys.forEach { editor.putString(it, strings[it]) }
                ints.keys.forEach { key -> ints[key]?.let { values -> editor.putInt(key, values) } }
            }.commit()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }


}

