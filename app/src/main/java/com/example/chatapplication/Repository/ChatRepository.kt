package com.example.chatapplication.Repository

import com.example.Constants.FOLDER_AUDIOS
import com.example.Constants.FOLDER_IMAGES
import com.example.Constants.MESSAGE_TYPE_AUDIO
import com.example.Constants.MESSAGE_TYPE_IMAGE
import com.example.Constants.SQLoffsetValue
import com.example.chatapplication.WebSocket.WebSocketClient
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Message
import com.example.util.SendMessage
import com.example.util.util.uploadFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ChatRepository (var senderId: String ,var database: ChatDatabase){
    var offset =0
    val messages: Flow<List<Message>> =database.messageDao().getMessage(senderId,offset,SQLoffsetValue.toString())


    suspend fun insert(message: Message) {
        database.messageDao().insertMessage(message)
        val messageFormat = SendMessage(
            message.senderId,
            "968",
            message.message,
            message.messageId,
            message.messageType?:""
        ) // should send message id not pK
        if (message.messageType == MESSAGE_TYPE_IMAGE) {
            uploadFile(messageFormat, FOLDER_IMAGES)
        } else if (message.messageType == MESSAGE_TYPE_AUDIO) {
            uploadFile(messageFormat, FOLDER_AUDIOS)
        }else{
            WebSocketClient.webSocket?.send(messageFormat.jsonObject.toString())
        }
    }

    suspend fun update(message: Message) {
        database.messageDao().editMessage(message)
        val messageFormat = SendMessage(message.senderId,"968",message.message,message.messageId,message.messageType?:"") // should send message id not pK
        WebSocketClient.webSocket?.send(messageFormat.jsonObject.toString())
    }


    suspend fun getOlderMessages(): List<Message>? {
        offset += SQLoffsetValue;
        return database.messageDao().getMessage(senderId,offset, SQLoffsetValue.toString()).firstOrNull()
    }
}