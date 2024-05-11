package com.apexharn.chatapplication.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
    interface MessageDao {
        @Insert
        suspend fun insertMessage(msg: Message)

        @Update
        suspend fun insertORupdate(msg: Message)

        @Query("Select * from messages WHERE messageId = :id")
        suspend fun getMessageFromID(id: String): Message?

        @Update
        suspend fun editMessage(msg: Message)

        @Delete
        suspend  fun deleteMessage(msg: Message)

        @Query("Select * from messages WHERE senderId = :senderId ORDER BY receiveTime DESC LIMIT :pageSize OFFSET :offset")
         fun getMessage(senderId: String,offset: Int, pageSize:String): Flow<List<Message>>

    }