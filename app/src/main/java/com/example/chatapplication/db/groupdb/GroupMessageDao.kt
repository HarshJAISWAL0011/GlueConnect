package com.example.chatapplication.db.groupdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.Sender
import com.example.util.GroupMessageData
import com.example.util.SendersWithLastMessage
import kotlinx.coroutines.flow.Flow



@Dao
interface GroupMessageDao {
    @Insert
    suspend fun insertMessage(msg: GroupMessage)

    @Query("Select * from group_message WHERE messageId = :id")
    suspend fun getMessageFromID(id: String): GroupMessage?

    @Update
    suspend fun editMessage(msg: GroupMessage)

    @Delete
    suspend  fun deleteMessage(msg: GroupMessage)

    @Query("SELECT DISTINCT group_message.*, group_member.name AS senderName " +
                   "        FROM group_message " +
                   "           INNER JOIN group_member ON group_message.senderId = group_member.senderId " +
                   "           WHERE group_message.groupId = :groupId" +
                   "          ORDER BY group_message.receiveTime DESC LIMIT :pageSize OFFSET :offset")
    fun getMessage(groupId: String,offset: Int, pageSize:String): Flow<List<GroupMessageData>>


    }