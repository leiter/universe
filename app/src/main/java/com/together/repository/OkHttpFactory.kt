package com.together.repository

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object OkHttpFactory {

    fun createClient(timeoutBase: Long = 50): OkHttpClient {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(1000 * timeoutBase, TimeUnit.MILLISECONDS)
            .readTimeout(1000 * timeoutBase, TimeUnit.MILLISECONDS)
            .writeTimeout(1000 * timeoutBase, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                val request= chain.request()
//                val r = Request.Builder()
//                r.
                chain.proceed(request)
            }

        return client.build()
    }
}