package com.together.repository

import retrofit2.Retrofit


object NetworkApiFactory {

    private val BASE_URL = "http://"

    fun <T> createDefaultRetrofit(clazz: Class<T>): T {
        val retrofit = Retrofit.Builder()
//            .addCallAdapterFactory()
//            .addConverterFactory()
            .baseUrl(BASE_URL).build()


        return retrofit.create(clazz)
    }
}

