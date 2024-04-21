package com.example.chatapplication.db.channeldb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chatapplication.db.Message
import com.example.util.TimeWithId
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelMsgDao {


        @Insert
        suspend fun insertMessage(msg: ChannelMessage)

        @Update
        suspend fun insertOrUpdate(msg: ChannelMessage)

        @Query("SELECT channels.channelId AS id, COALESCE(MAX(channel_message.receiveTime), 0) AS receiveTime " +
                "FROM channels " +
                "LEFT JOIN channel_message ON channels.channelId = channel_message.channelId " +
                "WHERE channels.isAdmin = 0 "+
                "GROUP BY channels.channelId")
        fun getChannelsIds( ): List<TimeWithId>?

        @Query("Select * from channel_message WHERE messageId = :id")
        suspend fun getMessageFromID(id: String): ChannelMessage?

        @Update
        suspend fun editMessage(msg: ChannelMessage)

        @Delete
        suspend  fun deleteMessage(msg: ChannelMessage)

        @Query("Select * from channel_message WHERE channelId = :channelId ORDER BY receiveTime DESC LIMIT :pageSize OFFSET :offset")
        fun getMessage(channelId: String,offset: Int, pageSize:String): Flow<List<ChannelMessage>>

    }
