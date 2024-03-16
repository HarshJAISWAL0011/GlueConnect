package com.example.chatapplication.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
 import com.example.util.SendersWithLastMessage
import kotlinx.coroutines.flow.Flow


@Dao
    interface SenderDao {
        @Insert
        suspend fun insertNewSender(sender: Sender)

        @Update
        suspend fun updateSender(sender: Sender)

        @Delete
        suspend  fun deleteSender(sender: Sender)

        @Query("Select * from sender WHERE EMAIL =:email")
        fun getSender(email: String): Sender?

        @Query("Select * from sender")
        fun getAllSenders(): Flow<List<Sender>>

    @Query("SELECT sender.email AS senderId, sender.*, latest_message.message AS last_message, latest_message.receiveTime, latest_message.messageType " +
            "FROM sender " +
            "LEFT JOIN (" +
            "    SELECT senderId, message, receiveTime, messageType " +
            "    FROM messages AS m1 " +
            "    WHERE receiveTime = (" +
            "        SELECT MAX(receiveTime) " +
            "        FROM messages AS m2 " +
            "        WHERE m1.senderId = m2.senderId" +
            "    )" +
            ") AS latest_message ON sender.email = latest_message.senderId")

    fun getSendersWithLastMessage(): Flow<List<SendersWithLastMessage>>

}