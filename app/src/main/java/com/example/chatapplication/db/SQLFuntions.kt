package com.example.chatapplication.db

import android.content.Context
import android.util.Log
import com.example.Message
import com.example.chatapplication.db.ChatDatabase.Companion.getDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

object SQLFuntions {



    suspend fun insertMessageSQL(message: Message, context: Context){
        val msgDb = com.example.chatapplication.db.Message(0,message.receivedFrom,message.message,if (message.isSender) 1 else 0,
            System.currentTimeMillis(),message.timestamp.toLong())
        val database = getDatabase(context)
        database.messageDao().insertMessage(msgDb)
    }

    suspend fun getSenderDetails(senderId: String , context: Context):Sender{
        val database = getDatabase(context)
        var sender = database.senderDao().getSender(senderId);
        return sender
    }

    suspend fun insertSender(senderId: String, senderName: String, context: Context, newMsgCnt: Int){
        val database = getDatabase(context)
        database.senderDao().insertNewSender(Sender(name=senderName,email=senderId, newMessageCount = newMsgCnt))
    }
}