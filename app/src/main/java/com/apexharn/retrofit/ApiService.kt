package com.apexharn.retrofit

import com.apexharn.util.ChannelMessageData
import com.apexharn.util.CreateGroupData
import com.apexharn.util.DeleteMessageData
import com.apexharn.util.GroupId
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/delete")
    fun postData(@Body data: DeleteMessageData): Call<String>

    @POST("/create_group")
    fun create_group(@Body data: CreateGroupData): Call<GroupId>

    @POST("/channel_message")
    fun channel_message(@Body data: ChannelMessageData): Call<Void>
}