package com.example.chatapplication.Repository

import com.example.Constants
import com.example.chatapplication.WebSocket.WebSocketClient
import com.example.chatapplication.db.groupdb.GroupDatabase
import com.example.chatapplication.db.groupdb.GroupMessage
import com.example.util.GroupMessageData
import com.example.util.SendMessage
import com.example.util.util
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GroupChatRepo (var groupId: String ,var database: GroupDatabase){
    var offset =0
    val messages: Flow<List<GroupMessageData>> =database.groupMessageDao().getMessage(groupId, offset,
                                                                                      Constants.SQLoffsetValue.toString())


    suspend fun insert(message: GroupMessage) {
        database.groupMessageDao().insertMessage(message)
        val messageFormat = SendMessage(
            message.senderId,
            "968",
            message.message,
            message.messageId,
            message.messageType
        ) // should send message id not pK
        if (message.messageType == Constants.MESSAGE_TYPE_IMAGE) {
            util.uploadFile(messageFormat, Constants.FOLDER_IMAGES)
        } else if (message.messageType == Constants.MESSAGE_TYPE_AUDIO) {
            util.uploadFile(messageFormat, Constants.FOLDER_AUDIOS)
        }else{
            WebSocketClient.webSocket?.send(messageFormat.jsonObject.toString())
        }
    }

    suspend fun update(message: GroupMessage) {
        database.groupMessageDao().editMessage(message)
        val messageFormat = SendMessage(message.senderId,"968",message.message,message.messageId,message.messageType) // should send message id not pK
        WebSocketClient.webSocket?.send(messageFormat.jsonObject.toString())
    }

    suspend fun getOlderMessages(): List<GroupMessageData>? {
        offset += Constants.SQLoffsetValue;
        return database.groupMessageDao().getMessage(groupId, offset, Constants.SQLoffsetValue.toString()).firstOrNull()
    }
}