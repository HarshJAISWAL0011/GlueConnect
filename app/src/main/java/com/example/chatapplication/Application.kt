package com.example.chatapplication

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.Constants
import com.example.Constants.INCOMING_AUDIO_CALL
import com.example.Constants.INCOMING_VIDEO_CALL
import com.example.Constants.MY_ID
import com.example.chatapplication.webRTC.IncomingCallListener
import com.example.chatapplication.webRTC.RTCActivity
import com.example.chatapplication.webRTC.RTCActivity.Companion.TAGGER
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestore

class Application: Application() {
    var listener: IncomingCallListener? = null
    override fun onCreate() {
        super.onCreate()

        val sharedPref = getSharedPreferences(Constants.PREF, MODE_PRIVATE)
        val userId = sharedPref.getString("userId","error");
        if(userId?.isEmpty()?:true){
          return
        }
        MY_ID = userId?:""
        listener = initListener()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()

        val db =Firebase.firestore
            db.firestoreSettings= settings

        db.collection("users").document(MY_ID).addSnapshotListener{ documentSnapshot, e->
            if(e != null){
                Log.d(TAGGER, "error = $e");
            }

            if(documentSnapshot != null && documentSnapshot.exists()){
                if(documentSnapshot.contains("type") && (documentSnapshot.get("type") == INCOMING_VIDEO_CALL || documentSnapshot.get("type") == INCOMING_AUDIO_CALL)
                    && documentSnapshot.contains("timestamp")){

                    var timestamp = documentSnapshot.get("timestamp") as Long
                    timestamp = timestamp
                    val currentTimeMillis = System.currentTimeMillis()
                    val differenceMillis = currentTimeMillis - timestamp

                    if (differenceMillis < 50 * 1000) {
                       if( documentSnapshot.get("type") == INCOMING_VIDEO_CALL)
                           (listener as IncomingCallListener).onVideoCallReceived(documentSnapshot.get("callerId").toString() ?: "", this)
                        else
                          (listener as IncomingCallListener).onAudioCallReceived(documentSnapshot.get("callerId").toString() ?: "", this)
                    }
                }

            }


        }
    }

    private fun initListener()=object : IncomingCallListener {

        @SuppressLint("MissingPermission")
        override fun onVideoCallReceived(callerId: String, context: Context) {

            val answerIntent = Intent(context, RTCActivity::class.java).apply {
                action = "ACTION_ANSWER_CALL"
                putExtra("isVideoCall",true);
                putExtra("isJoin",true);
                putExtra("calleeId",MY_ID);
                putExtra("callerId",callerId);
            }

            val answerPendingIntent = PendingIntent.getActivity(
                context,
                0,
                answerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or FLAG_MUTABLE
            )

            val declineIntent = Intent(context, MainActivity::class.java).apply {
                action = "ACTION_DECLINE_CALL"
            }

            val declinePendingIntent = PendingIntent.getActivity(
                context,
                0,
                declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or FLAG_MUTABLE
            )

            val notification = NotificationCompat.Builder(context, "incoming_call_channel_id")
                .setContentTitle("Incoming Video Call")
                .setContentText("$callerId is calling")
                .setSmallIcon(R.drawable.ic_call)
                .addAction(R.drawable.ic_call, "Answer", answerPendingIntent)
                .addAction(R.drawable.ic_baseline_call_end_24, "Decline", declinePendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false) // Close notification when clicked
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1, notification)

            // Schedule automatic dismissal after 30 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                notificationManager.cancel(1)
            }, 50000)
        }

        override fun onAudioCallReceived(callerId: String, context: Context) {
            val answerIntent = Intent(context, RTCActivity::class.java).apply {
                action = "ACTION_ANSWER_CALL"
                putExtra("isVideoCall",false);
                putExtra("isJoin",true);
                putExtra("calleeId",MY_ID);
                putExtra("callerId",callerId);
            }

            val answerPendingIntent = PendingIntent.getActivity(
                context,
                0,
                answerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or FLAG_MUTABLE
            )

            val declineIntent = Intent(context, MainActivity::class.java).apply {
                action = "ACTION_DECLINE_CALL"
            }

            val declinePendingIntent = PendingIntent.getActivity(
                context,
                0,
                declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or FLAG_MUTABLE
            )

            val notification = NotificationCompat.Builder(context, "incoming_call_channel_id")
                .setContentTitle("Incoming Audio Call")
                .setContentText("$callerId is calling")
                .setSmallIcon(R.drawable.ic_call)
                .addAction(R.drawable.ic_call, "Answer", answerPendingIntent)
                .addAction(R.drawable.ic_baseline_call_end_24, "Decline", declinePendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false) // Close notification when clicked
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
             notificationManager.notify(1, notification)

            // Schedule automatic dismissal after 30 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                notificationManager.cancel(1)
            }, 50000)
        }


    }
}