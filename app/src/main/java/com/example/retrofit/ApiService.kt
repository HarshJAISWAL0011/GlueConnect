package com.example.retrofit

import com.example.util.CreateGroupData
import com.example.util.DeleteMessageData
import com.example.util.GroupId
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/delete")
    fun postData(@Body data: DeleteMessageData): Call<String>

    @POST("/create_group")
    fun create_group(@Body data: CreateGroupData): Call<GroupId>
}