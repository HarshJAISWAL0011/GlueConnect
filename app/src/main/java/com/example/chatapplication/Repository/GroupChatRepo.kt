package com.example.chatapplication.Repository

import com.example.Constants
import com.example.Constants.FOLDER_AUDIOS
import com.example.Constants.FOLDER_IMAGES
import com.example.Constants.FOLDER_OTHERS
import com.example.Constants.MESSAGE_TYPE_AUDIO
import com.example.Constants.MESSAGE_TYPE_IMAGE
import com.example.Constants.MY_ID
import com.example.chatapplication.WebSocket.WebSocketClient
import com.example.chatapplication.db.groupdb.GroupDatabase
import com.example.chatapplication.db.groupdb.GroupMessage
import com.example.chatapplication.firebase.FirestoreDb
import com.example.util.GroupMessageData
import com.example.util.SendGroupMessage
import com.example.util.SendMessage
import com.example.util.util
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GroupChatRepo (var groupId: String ,var database: GroupDatabase, val groupName: String){
    var offset =0
    val messages: Flow<List<GroupMessageData>> =database.groupMessageDao().getMessage(groupId, offset,
                                                                                      Constants.SQLoffsetValue.toString())


    suspend fun insert(message: GroupMessage) {
        database.groupMessageDao().insertMessage(message)


        if (message.messageType == MESSAGE_TYPE_IMAGE || message.messageType == MESSAGE_TYPE_AUDIO) {

            val folder = if(message.messageType == MESSAGE_TYPE_IMAGE) FOLDER_IMAGES
            else if(message.messageType == MESSAGE_TYPE_AUDIO) FOLDER_AUDIOS
            else FOLDER_OTHERS

            util.uploadFile( folder,message.message).addOnCompleteListener { task ->
                val downloadUri = task.result
                val messageFormat = SendGroupMessage(
                    MY_ID,
                    downloadUri.toString(),
                    message.messageId,
                    message.messageType,
                    message.groupId,
                    groupName
                )
                WebSocketClient.webSocket?.send(messageFormat.jsonObject.toString())
                FirestoreDb.sendMessageToGroup(messageFormat.jsonObject)
            }
        }else{
            val messageFormat = SendGroupMessage(
                MY_ID,
                message.message,
                message.messageId,
                message.messageType,
                message.groupId,
                groupName
            )
            WebSocketClient.webSocket?.send(messageFormat.jsonObject.toString())
            FirestoreDb.sendMessageToGroup(messageFormat.jsonObject)

        }
    }

    suspend fun update(message: GroupMessage) {
        database.groupMessageDao().editMessage(message)
        val messageFormat = SendGroupMessage(
            MY_ID,
            message.message,
            message.messageId,
            message.messageType,
            message.groupId,
            groupName
        ) // should send message id not pK
        WebSocketClient.webSocket?.send(messageFormat.jsonObject.toString())
        FirestoreDb.sendMessageToGroup(messageFormat.jsonObject)
    }

    suspend fun getOlderMessages(): List<GroupMessageData>? {
        offset += Constants.SQLoffsetValue;
        return database.groupMessageDao().getMessage(groupId, offset, Constants.SQLoffsetValue.toString()).firstOrNull()
    }
}