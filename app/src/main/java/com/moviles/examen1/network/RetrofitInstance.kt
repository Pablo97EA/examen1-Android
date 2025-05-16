package com.moviles.examen1.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.moviles.examen1.common.Constants.API_BASE_URL

object RetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
