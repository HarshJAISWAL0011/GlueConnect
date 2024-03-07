package com.example.chatapplication.Repository

import com.example.Constants.SQLoffsetValue
import com.example.SendMessage
import com.example.chatapplication.WebSocket.WebSocketClient
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ChatRepository (var senderId: String ,var database: ChatDatabase){
    var offset =0
    val messages: Flow<List<Message>> =database.messageDao().getMessage(senderId,offset,SQLoffsetValue.toString())


    suspend fun insert(message: Message) {
        database.messageDao().insertMessage(message)
        val messageFormat = SendMessage(message.senderId,"968",message.message,message.id.toString()) // should send message id not pK
       WebSocketClient.webSocket?.send(messageFormat.jsonObject.toString())
    }

    suspend fun getOlderMessages(): List<Message>? {
        offset += SQLoffsetValue;
        return database.messageDao().getMessage(senderId,offset, SQLoffsetValue.toString()).firstOrNull()
    }
}