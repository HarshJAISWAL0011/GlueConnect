package com.apexharn.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.apexharn.Constants
import com.apexharn.chatapplication.MainActivity
import com.apexharn.chatapplication.R
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

object  Notification {


    fun createChannelId(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val name = Constants.MESSAGE_NOTIFICATION
            val descriptionText = Constants.MESSAGE_NOTIFICATION_DESC
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(
                Constants.MESSAGE_NOTIFICATION_CHANNEL_ID,
                name,
                importance
            )
            mChannel.description = descriptionText

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }


    @SuppressLint("MissingPermission")
    fun sendNotification(context: Context, title: String?, message: String?, senderId: String?) {
        println(" Message data payload from sendNotif")
        var msgBody = message

        var intent = Intent(context, MainActivity::class.java)
        intent.putExtra("id",senderId)
        var pendingTotalMessage = getIntDataSharedPref(context,Constants.total_message_pending) + 1;

        var pendingTotalUser = getListSharedPref(context,Constants.notif_users_pending);

        println(" Message data payload from sendNotif "+pendingTotalMessage+" "+pendingTotalUser.toString()+"msgBody="+msgBody)
        if(pendingTotalMessage > 1) {

            if(!pendingTotalUser.contains(senderId))
                senderId?.let { pendingTotalUser.add(it) }

            msgBody = "$pendingTotalMessage messages from ${pendingTotalUser.size} chats"
            intent = Intent(context, MainActivity::class.java)
        }
        saveListSharedPref(context,Constants.notif_users_pending,pendingTotalUser.toList())
        saveIntSharedPref(context, Constants.total_message_pending,pendingTotalMessage)

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        var builder = NotificationCompat.Builder(context, context.getString(R.string.message_notification_channel_id))
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(msgBody)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(context)

        notificationManager.notify(1, builder.build())
    }

    private fun isNotificationVisible(context: Context): Boolean {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val test = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        return test != null
    }

     fun getStringDataSharedPref(context: Context, key: String): String{
        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key,"")!!
    }

     fun getIntDataSharedPref(context: Context, key: String): Int{
        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(key,0)
    }

    fun getListSharedPref(context: Context, key: String): MutableList<String> {
        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(key, null)
        val type = object : TypeToken<List<String>>() {}.type
        return (gson.fromJson(json, type) ?: emptyList<String>()).toMutableList()
    }

    fun saveListSharedPref(context: Context, key: String, list: List<String>) {
        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }

    fun saveIntSharedPref(context: Context, key: String, value: Int){
        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE).edit()
        sharedPreferences.putInt(key,value)
        sharedPreferences.apply()

    }

    fun saveStringSharedPref(context: Context, key: String,value: String) {
        val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE).edit()
        sharedPreferences.putString(key,value)
        sharedPreferences.apply()
    }
}