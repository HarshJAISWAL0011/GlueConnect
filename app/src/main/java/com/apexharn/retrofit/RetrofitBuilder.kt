package com.apexharn.retrofit

import com.apexharn.Constants.BASE_URL
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {
    lateinit var apiService: ApiService
    val baseurl =BASE_URL
//        "https://chat-websocket-64k2.onrender.com"
//        "https://chat-websocker.onrender.com"

    fun create():ApiService {
        runBlocking {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseurl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

             apiService = retrofit.create(ApiService::class.java)

        }
        return apiService
    }
}