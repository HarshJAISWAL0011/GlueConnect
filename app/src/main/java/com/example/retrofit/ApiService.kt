package com.example.retrofit

import com.example.DeleteMessageData
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/delete")
    fun postData(@Body data: DeleteMessageData): Call<String>
}