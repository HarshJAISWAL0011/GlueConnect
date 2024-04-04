package com.example.chatapplication.db.channeldb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.util.ChannelsWithMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface  ChannelDao {
    @Insert
    suspend fun addNewChannel(channel: Channels)

//    @Update
//    suspend fun updateChannel(channel: Channels)

    @Query("UPDATE channels SET newMessageCount = :newValue WHERE channelId = :id")
    suspend fun updateChannel(id: String, newValue: Int)

    @Delete
    suspend  fun deleteChannel(channel: Channels)

    @Query("Select * from channels WHERE channelId =:channelId")
    fun getChannel(channelId: String): Channels?

//    @Query("Select * from sender WHERE EMAIL =:email")
//    fun getSenderListener(email: String): Flow<Channels>

    @Query("Select * from channels")
    fun getAllChannel(): Flow<List<Channels>>

    @Query("SELECT channels.id, channels.name, channels.channelId, channels.newMessageCount,latest_message.message AS last_message, latest_message.receiveTime, latest_message.messageType " +
            "FROM channels " +
            "LEFT JOIN (" +
            "    SELECT channelId, message, receiveTime, messageType " +
            "    FROM channel_message AS m1 " +
            "    WHERE receiveTime = (" +
            "        SELECT MAX(receiveTime) " +
            "        FROM channel_message AS m2 " +
            "        WHERE m1.channelId = m2.channelId" +
            "    )" +
            ") AS latest_message ON channels.channelId = latest_message.channelId")

    fun getChannelList(): Flow<List<ChannelsWithMessage>>

}