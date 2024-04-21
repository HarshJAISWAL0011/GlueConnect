package com.example.chatapplication.firebase

import android.content.Context
import android.util.Log
import com.example.Constants
import com.example.Constants.MY_ID
import com.example.chatapplication.db.channeldb.ChannelDatabase
import com.example.chatapplication.db.channeldb.ChannelMessage
import com.example.chatapplication.db.groupdb.GroupDatabase
import com.example.chatapplication.db.groupdb.GroupMessage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Listeners {
    private val TAG ="Listener"

    suspend fun messageListener(context: Context) {
        val groupdb = GroupDatabase.getDatabase(context)
        val channeldb = ChannelDatabase.getDatabase(context)
        Log.e(TAG, "groupdb data: ${channeldb.channelMsgDao().getChannelsIds()}")
        groupdb.groupDao().getGroupIds()?.forEach {
            groupListener(it.id, it.receiveTime, groupdb)
        }

        channeldb.channelMsgDao().getChannelsIds()?.forEach {
            channelListener(it.id, it.receiveTime, channeldb)
        }
    }

    fun groupListener(id: String, timeThreshold: Long, groupdb: GroupDatabase) {
        val db = Firebase.firestore
        val collectionRef = db.collection(Constants.path_groups).document(id)
            .collection(Constants.message)
            .whereGreaterThan("timestamp", timeThreshold)
            .whereNotEqualTo("sender_id",MY_ID)

        collectionRef.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Error fetching messages: $exception")
                return@addSnapshotListener
            }

            if (querySnapshot != null) {
                Log.d(TAG, "Snapshot received with ${querySnapshot.documents.size} documents")
                querySnapshot.documentChanges.forEach { change ->
                    try {

                    Log.d(TAG, "Document change: ${change.type}, ${change.document.data}")
                    val changedData = change.document.data
                    val updatedMessage = GroupMessage(
                        changedData["messageId"].toString(),
                        changedData["sender_id"].toString(),
                        changedData["messageType"].toString(),
                        changedData["message"].toString(),
                        1,System.currentTimeMillis(),
                        changedData["timestamp"].toString()?.toLong()?: System.currentTimeMillis(),
                        changedData["group_id"].toString())

                    Log.d(TAG, "Modified message: $updatedMessage")
                        CoroutineScope(Dispatchers.IO).launch {
                            val msg = groupdb.groupMessageDao().getMessageFromID(updatedMessage.messageId)
                            Log.d(TAG, "Message got from sqllite $msg")
                            if(msg != null){

                                groupdb.groupMessageDao().editMessage(msg.copy(message = updatedMessage.message))

                            }else{
                                groupdb.groupMessageDao().insertMessage(updatedMessage)

                            }
                        }
                }catch (e: NullPointerException ){
                        Log.d(TAG, "Error while reading data ${e.printStackTrace()}")
                }catch (e: RuntimeException){
                        Log.d(TAG, "Error while reading data ${e.printStackTrace()}")

                }
                }

            } else {
                Log.d(TAG, "Snapshot is null")
            }
        }
    }

    fun channelListener(id: String, timeThreshold: Long, channeldb: ChannelDatabase) {
        val db = Firebase.firestore
        val collectionRef = db.collection(Constants.path_channels).document(id)
            .collection("messages")
            .whereGreaterThan("time", timeThreshold)


        collectionRef.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Error fetching messages: $exception")
                return@addSnapshotListener
            }

            if (querySnapshot != null) {
                Log.d(TAG, "Snapshot received with ${querySnapshot.documents.size} documents")
                querySnapshot.documentChanges.forEach { change ->
                    try {

                        Log.d(TAG, "Document change: ${change.type}, ${change.document.data}")
                        val changedData = change.document.data
                        val updatedMessage = ChannelMessage(
                            changedData["messageId"].toString(),
                            changedData["channelId"].toString(),
                            changedData["messageType"].toString(),
                            changedData["message"].toString(),
                            System.currentTimeMillis(),
                            changedData["timestamp"].toString()?.toLong()?: System.currentTimeMillis(),
                           )

                        Log.d(TAG, "Modified message: $updatedMessage")
                        CoroutineScope(Dispatchers.IO).launch {
                            val msg = channeldb.channelMsgDao().getMessageFromID(updatedMessage.messageId)
                            Log.d(TAG, "Message got from sqllite $msg")
                            if(msg != null){

                                channeldb.channelMsgDao().editMessage(msg.copy(message = updatedMessage.message))

                            }else{
                                channeldb.channelMsgDao().insertMessage(updatedMessage)

                            }
                        }
                    }catch (e: NullPointerException ){
                        Log.d(TAG, "Error while reading data ${e.printStackTrace()}")
                    }catch (e: RuntimeException){
                        Log.d(TAG, "Error while reading data ${e.printStackTrace()}")

                    }
                }

            } else {
                Log.d(TAG, "Snapshot is null")
            }
        }
    }

}