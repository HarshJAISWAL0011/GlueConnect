package com.example

import com.example.Constants.message
import com.example.Constants.new_connection
import com.example.Constants.new_message
import com.example.Constants.senderId

import com.example.Constants.sendtoId
import com.example.Constants.timestamp
import com.example.Constants.type
import org.json.JSONObject
import java.sql.Timestamp

object Constants {
    val new_connection ="New_Connection"
    val new_message ="New_Message"
    val message ="message"
    val type = "type"
    val sendtoId ="sendto_id"
    val senderId ="sender_id"
    val timestamp = "timestamp"
    val messageId = "messageId"
    val messageType = "messageType"
    val SQLoffsetValue =30
    val MESSAGE_NOTIFICATION ="Message"
    val MESSAGE_NOTIFICATION_DESC ="Receive message notification"
    val MESSAGE_NOTIFICATION_CHANNEL_ID ="10"
    val SHARED_PREFS ="shared_prefs"
    val FCM_title = "title"
    val MESSAGE_TYPE_IMAGE = "image"
    val MESSAGE_TYPE_TEXT = "text"
    val MESSAGE_TYPE_AUDIO = "audio"
    val FOLDER_IMAGES ="Images"
    val FOLDER_AUDIOS ="Audios"
    val notif_users_pending ="users_pending"
    val total_message_pending ="message_pending0"
    val FIRESTORE_USERS ="users"
    val FIRESTORE_MESSAGES ="messages"
    val MY_ID ="968226"
    val FIRESTORE_REGISTRATION_TOKEN ="token"
}

class NewConnection(id:String){
    val jsonObject = JSONObject()
    init {
        jsonObject.put(type, new_connection)
        jsonObject.put(senderId, id)
    }
}

 data class SendersWithLastMessage( var id: Int=0,
                                   var name: String,
                                   var email: String,
                                   val messageType: String,
                                   var newMessageCount: Int,
                                    var last_message: String? = "",
     var receiveTime: Long?=0)

class SendMessage(sendToId:String, senderId: String, message: String,id:String, messageType:String){
    val jsonObject = JSONObject()
    init {
        jsonObject.put(type, new_message)
        jsonObject.put(sendtoId, sendToId)
        jsonObject.put(Constants.senderId, senderId)
        jsonObject.put(timestamp, System.currentTimeMillis())
        jsonObject.put(Constants.message, message)
        jsonObject.put(Constants.messageId,id )
        jsonObject.put(Constants.messageType,messageType )
    }
}

class Message(var receivedFrom:String,var message: String,var timestamp: Long,var isSender: Boolean){}

data class DeleteMessageData(
    val userId: String,
)
