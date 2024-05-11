package com.apexharn.chatapplication.Repository

import com.apexharn.Constants
import com.apexharn.Constants.FOLDER_AUDIOS
import com.apexharn.Constants.FOLDER_IMAGES
import com.apexharn.Constants.MESSAGE_TYPE_AUDIO
import com.apexharn.Constants.MESSAGE_TYPE_IMAGE
import com.apexharn.Constants.MY_ID
import com.apexharn.Constants.SQLoffsetValue
import com.apexharn.chatapplication.WebSocket.WebSocketClient
import com.apexharn.chatapplication.db.ChatDatabase
import com.apexharn.chatapplication.db.Message
import com.apexharn.util.SendMessage
import com.apexharn.util.util.uploadFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ChatRepository (var senderId: String ,var database: ChatDatabase){
    var offset =0
    val messages: Flow<List<Message>> =database.messageDao().getMessage(senderId,offset,SQLoffsetValue.toString())


    suspend fun insert(message: Message) {
        database.messageDao().insertMessage(message)
        val messageFormat = SendMessage(
            message.senderId,
            MY_ID,
            message.message,
            message.messageId,
            message.messageType?:""
        ) // should send message id not pK
        if (message.messageType == MESSAGE_TYPE_IMAGE || message.messageType == MESSAGE_TYPE_AUDIO) {

            val folder = if(message.messageType == MESSAGE_TYPE_IMAGE) FOLDER_IMAGES
            else if(message.messageType == MESSAGE_TYPE_AUDIO) FOLDER_AUDIOS
            else Constants.FOLDER_OTHERS

            uploadFile(folder, message.message).addOnCompleteListener{task->
                val downloadUri = task.result
                messageFormat.jsonObject.put(Constants.message,downloadUri) // update download uri
                WebSocketClient.webSocket?.send(messageFormat.jsonObject.toString())
            }
        }else{
            WebSocketClient.webSocket?.send(messageFormat.jsonObject.toString())
        }
    }

    suspend fun update(message: Message) {
        database.messageDao().editMessage(message)
        val messageFormat = SendMessage(message.senderId,
            MY_ID,message.message,message.messageId,message.messageType?:"") // should send message id not pK
        WebSocketClient.webSocket?.send(messageFormat.jsonObject.toString())
    }


    suspend fun getOlderMessages(): List<Message>? {
        offset += SQLoffsetValue;
        return database.messageDao().getMessage(senderId,offset, SQLoffsetValue.toString()).firstOrNull()
    }
}