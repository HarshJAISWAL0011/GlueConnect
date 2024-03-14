package com.example.chatapplication

import android.util.Log
import com.example.Constants.FCM_title
import com.example.Constants.message
import com.example.Constants.senderId
import com.example.util.Notification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseServiceMessaging  : FirebaseMessagingService() {
 val TAG = "FirebaseServiceMessaging";

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            Notification.sendNotification(this,remoteMessage.data.get(FCM_title),
                remoteMessage.data.get(message),
                remoteMessage.data.get(senderId))
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