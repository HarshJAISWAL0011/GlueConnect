package com.apexharn.chatapplication.db

import android.content.Context
import com.apexharn.chatapplication.db.ChatDatabase.Companion.getDatabase
import com.apexharn.chatapplication.db.groupdb.Group
import com.apexharn.chatapplication.db.groupdb.GroupDatabase
import com.apexharn.chatapplication.db.groupdb.GroupMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

object SQLFuntions {


    suspend fun getSenderDetails(senderId: String , context: Context):Sender?{
        val database = getDatabase(context)
        var sender = database.senderDao().getSender(senderId);
        return sender
    }

    suspend fun getMessageWithID(messageID: String , context: Context):Message?{
        var message = CoroutineScope(Dispatchers.IO).async {
            val database = getDatabase(context)
            return@async database.messageDao().getMessageFromID(messageID)
        }.await()
        return message
    }

    suspend fun getGroupMessageWithID(messageID: String , context: Context): GroupMessage?{
        var message = CoroutineScope(Dispatchers.IO).async {
            val database = GroupDatabase.getDatabase(context)
            return@async database.groupMessageDao().getMessageFromID(messageID)
        }.await()
        return message
    }

    suspend fun getGroupFromId(id: String , context: Context):Group?{
        val database = GroupDatabase.getDatabase(context)
        var sender = database.groupDao().getGroupFromId(id);
        return sender
    }

}