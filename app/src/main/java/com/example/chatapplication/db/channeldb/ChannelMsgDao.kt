package com.example.chatapplication.db.channeldb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chatapplication.db.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelMsgDao {


        @Insert
        suspend fun insertMessage(msg: ChannelMessage)

        @Update
        suspend fun insertOrUpdate(msg: ChannelMessage)

        @Query("Select * from channel_message WHERE messageId = :id")
        suspend fun getMessageFromID(id: String): ChannelMessage?

        @Update
        suspend fun editMessage(msg: ChannelMessage)

        @Delete
        suspend  fun deleteMessage(msg: ChannelMessage)

        @Query("Select * from channel_message WHERE channelId = :channelId ORDER BY receiveTime DESC LIMIT :pageSize OFFSET :offset")
        fun getMessage(channelId: String,offset: Int, pageSize:String): Flow<List<ChannelMessage>>

    }
