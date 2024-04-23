package com.example.retrofit

import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {
    lateinit var apiService: ApiService
    val baseurl ="https://chat-websocket-64k2.onrender.com"
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