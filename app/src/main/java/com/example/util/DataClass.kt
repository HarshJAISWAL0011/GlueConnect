package com.example.util

import androidx.room.PrimaryKey
import com.example.Constants
import org.json.JSONObject


class NewConnection(id:String){
    val jsonObject = JSONObject()
    init {
        jsonObject.put(Constants.type, Constants.new_connection)
        jsonObject.put(Constants.senderId, id)
    }
}

data class SendersWithLastMessage( var id: Int=0,
                                   var name: String,
                                   var email: String,
                                   val messageType: String? = "text",
                                   var newMessageCount: Int,
                                   var last_message: String? = "",
                                   var receiveTime: Long?=0){

    constructor(
        id: Int,
        name: String,
        email: String,
        messageType: String?,
        last_message: Int,
        receiveTime: Long?,

    ) : this(id, "", email, "text", 0, "", )

}

data class GroupSendersWithMessage(
    var id: Int = 0,
    var groupId: String,
    var groupName: String = "",
    var senderName: String?,
    var messageType: String? = "text",
    var newMessageCount: Int,
    var last_message: String? = "",
    var sentTime: Long? = 0
) {
    constructor(
        id: Int,
        groupId: String,
        groupName: String?,
        senderName: String?,
        newMessageCount: Int,
        last_message: String?,
        sentTime: Long?
    ) : this(id, groupId, "no-name", "", "text", 0, "", 0)
}

class SendMessage(sendToId:String, senderId: String, message: String,id:String, messageType:String){
    val jsonObject = JSONObject()
    init {
        jsonObject.put(Constants.type, Constants.new_message)
        jsonObject.put(Constants.sendtoId, sendToId)
        jsonObject.put(Constants.senderId, senderId)
        jsonObject.put(Constants.timestamp, System.currentTimeMillis())
        jsonObject.put(Constants.message, message)
        jsonObject.put(Constants.messageId, id )
        jsonObject.put(Constants.messageType, messageType )
    }
}

data class GroupMessageData(
    val messageId: String = "0",
    val senderId: String = "",
    var senderName: String? = "",
    val messageType: String = "text",
    var message: String = "",
    val isReceived: Int = 1,
    val receiveTime: Long = 0,
    val sentTime: Long = 0,
    val groupId: String = ""
)
class Message(var receivedFrom:String,var message: String,var timestamp: Long,var isSender: Boolean){}

data class DeleteMessageData(
    val userId: String,
)