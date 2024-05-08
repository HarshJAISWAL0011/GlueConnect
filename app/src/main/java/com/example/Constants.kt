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

   const val BASE_URL ="https://chat-websocker.onrender.com"
   //        "https://chat-websocket-64k2.onrender.com"
//        "https://chat-websocker.onrender.com"
   const val new_connection ="New_Connection"
   const val new_message ="New_Message"
   const val new_group_message ="New_Group_Message"
      const val new_channel_message ="new_channel_message"
   const val message ="message"
   const val type = "type"
   const val ACTION_DELETE = "action_delete"
   const val GroupId = "group_id"
   const val GroupName = "group_name"
   const val sendtoId ="sendto_id"
   const val senderId ="sender_id"
   const val timestamp = "timestamp"
   const val messageId = "messageId"
   const val messageType = "messageType"
   const val SQLoffsetValue =30
   const val MESSAGE_NOTIFICATION ="Message"
   const val MESSAGE_NOTIFICATION_DESC ="Receive message notification"
   const val MESSAGE_NOTIFICATION_CHANNEL_ID ="10"
   const val SHARED_PREFS ="shared_prefs"
   const val FCM_title = "title"
    const val MESSAGE_TYPE_IMAGE = "image"
   const val MESSAGE_TYPE_TEXT = "text"
   const val MESSAGE_TYPE_AUDIO = "audio"
   const val FOLDER_IMAGES ="Images"
   const val FOLDER_AUDIOS ="Audios"
   const val FOLDER_PROFILE ="Profile"
   const val FOLDER_OTHERS ="Others"
   const val notif_users_pending ="users_pending"
   const val total_message_pending ="message_pending0"
   const val FIRESTORE_USERS ="users"
   const val FIRESTORE_MESSAGES ="messages"
   const val path_channels ="channels"
   const val path_users ="users"
   const val path_groups ="groups"
    var MY_ID =""
   const val INCOMING_VIDEO_CALL = "INCOMING_VIDEO_CALL"
   const val INCOMING_AUDIO_CALL = "INCOMING_AUDIO_CALL"
   const val END_CALL = "END_CALL"
   const val IN_CALL = "IN_CALL"
   const val FIRESTORE_REGISTRATION_TOKEN ="token"
   const val EXT_DIR_IMAGE_LOCATION ="/Chat/Images"
   const val EXT_DIR_IMAGE_CHANNEL_LOCATION ="/Chat/Images/Channel"
    const val EXT_DIR_PROFILE_LOCATION ="/Chat/Profile"
   const val ChannelId ="channel_id"
   const val channels_joined ="channels_joined"
   const val channels_created ="channels_created"
   const val groups_created ="groups_created"
   const val groups_joined ="groups_joined"
   const val group_joined ="group_joined"
   const val group_created ="group_created"
   const val ChannelName ="channel_name"
   const val feild_phone ="phone"
   const val PREF ="preference"
    var CURRENT_ACTIVITY =""
   var CURRENT_ACTIVITY_ID =""

}

