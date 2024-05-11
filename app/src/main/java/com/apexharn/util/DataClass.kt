package com.apexharn.util

import com.apexharn.Constants
import org.json.JSONObject


class NewConnection(id:String){
    val jsonObject = JSONObject()
    init {
        jsonObject.put(Constants.type, Constants.new_connection)
        jsonObject.put(Constants.senderId, id)
    }
}

data class SendersWithLastMessage( var id: Int=0,
                                   var profile_url: String,
                                   var name: String,
                                   var email: String,
                                   val messageType: String? = "text",
                                   var newMessageCount: Int,
                                   var last_message: String? = "",
                                   var sentTime: Long?=0)

data class GroupSendersWithMessage(
    var id: Int = 0,
    var profile_url: String,
    var groupId: String,
    var groupName: String = "",
    var senderName: String?,
    var messageType: String? = "text",
    var newMessageCount: Int,
    var last_message: String? = "",
    var sentTime: Long? = 0
)

data class CreateChannelData(

    var name: String,
    var profileUrl: String,
    var description: String = "",
    var creatorId: String,
    var followers: Int = 0,
    var channelType: String,
    var time: Long
)

data class ChannelData(
    var name: String,
    var profileUrl: String,
    var creatorId: String,
    var followers: Int = 0,
    var channelType: String,
    var channelId: String,
    var time: Long
){
    constructor():this("","","",0,"","",0)
}

data class SearchUserData(
    var name: String,
    var desc: String,
    var id: String,
    var profileUrl: String,
    var connection_status: String = "",

)
data class ChannelsWithMessage(
    var id: Int = 0,
    var profile_url: String,
    var channelId: String,
    var name: String = "",
    var messageType: String? = "text",
    var newMessageCount: Int,
    var last_message: String? = "",
    var sentTime: Long? = 0,
    var isAdmin: Int
)

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

class ChannelMessageData(var channelId: String,var message: String,var messageId:String,var messageType:String){
//    val jsonObject = JSONObject()
     var  timestamp: Long = System.currentTimeMillis()
    init {
//        jsonObject.put(Constants.type, Constants.new_channel_message)
//         jsonObject.put(Constants.timestamp, System.currentTimeMillis())
//        jsonObject.put(Constants.message, message)
//        jsonObject.put(Constants.messageId, messageId )
//        jsonObject.put(Constants.messageType, messageType )
//        jsonObject.put(Constants.ChannelId, channelId )
    }
}


class SendGroupMessage(var senderId: String,var message: String,var messageId:String,var messageType:String,var groupId: String,var groupName: String){
    val jsonObject = JSONObject()
    init {
        jsonObject.put(Constants.type, Constants.new_group_message)
        jsonObject.put(Constants.senderId, senderId)
        jsonObject.put(Constants.timestamp, System.currentTimeMillis())
        jsonObject.put(Constants.message, message)
        jsonObject.put(Constants.messageId, messageId )
        jsonObject.put(Constants.messageType, messageType )
        jsonObject.put(Constants.GroupId, groupId )
        jsonObject.put(Constants.GroupName, groupName )
    }
}

data class CreateGroupData(val groupName: String, val groupMembers: List<String>, val createdBy: String)

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

data class TimeWithId(
    val id: String,
    val receiveTime: Long
)

data class DeleteMessageData(
    val userId: String,
)
data class GroupId(val id: String)