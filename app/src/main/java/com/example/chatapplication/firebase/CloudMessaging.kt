package com.example.chatapplication.firebase

import android.util.Log
import com.example.Constants.FCM_title
import com.example.Constants.message
import com.example.Constants.senderId
import com.example.chatapplication.db.ChatDatabase
import com.example.util.Notification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CloudMessaging  : FirebaseMessagingService() {
 val TAG = "FirebaseServiceMessaging";

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        GlobalScope.launch {
            remoteMessage.data.isNotEmpty().let {
                Log.d(TAG, "Message data payload: ${remoteMessage.data}")
                var receivedFrom = remoteMessage.data.get(senderId) ?: ""
                var messageData = remoteMessage.data.get(message) ?: ""
                var title = remoteMessage.data.get(FCM_title) ?: ""

                if(receivedFrom.isNotEmpty()){
                    val senderName =ChatDatabase.getDatabase(this@CloudMessaging).senderDao().getSender(receivedFrom)?.name?:""
                    if(senderName.isNotEmpty())
                        receivedFrom = senderName
                }

                Notification.sendNotification(
                    this@CloudMessaging,
                    title,
                    messageData,
                    receivedFrom
                )
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
//        sendRegistrationToServer(token)
    }
}