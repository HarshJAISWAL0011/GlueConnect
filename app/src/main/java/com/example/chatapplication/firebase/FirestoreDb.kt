package com.example.chatapplication.firebase

import androidx.room.withTransaction
import com.example.Constants.FIRESTORE_MESSAGES
import com.example.Constants.FIRESTORE_USERS
import com.example.Constants.message
import com.example.Constants.senderId
import com.example.Constants.timestamp
import com.example.DeleteMessageData
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Message
import com.example.retrofit.RetrofitBuilder
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.memoryCacheSettings
import com.google.firebase.firestore.ktx.persistentCacheSettings
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Callback
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

object  FirestoreDb {

    suspend fun getNewMessageFirestore( id:String,database: ChatDatabase){
        val messageList = mutableListOf<Message>()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()

        val db = Firebase.firestore
        db.firestoreSettings = settings
        val msgRef = db.collection(FIRESTORE_USERS)
            .document(id)
            .collection(FIRESTORE_MESSAGES)

            msgRef.get()
            .addOnSuccessListener { documents ->
                val time = System.currentTimeMillis()
                var i =0
                for (document in documents) {
                    val message = document.get(message).toString()
                    val senderId= document.get(senderId).toString()
                    val timestamp = document.getLong(timestamp)

                    val data = Message(0,"97",message,0,time + i,timestamp ?: 0,)
                    messageList.add(data)
                    i++
                }
                CoroutineScope(Dispatchers.IO).launch {
                    database.withTransaction {
                        messageList.forEach { message ->
                            database.messageDao().insertMessage(message)

                            val sender = database.senderDao().getSender(message.senderId)
                            if (sender != null) {
                                val updatedSender = sender.copy(newMessageCount = sender.newMessageCount + 1)
                                database.senderDao().updateSender(updatedSender)
                                println("Stored message and updated newMessageCount for sender: $updatedSender")
                            } else {
                                println("Sender not found for message: $message")
                            }
                        }
                    }
                }

                if(messageList.size > 0)
                deleteMessages(id) // app userId
            }
            .addOnFailureListener { exception ->
                println("Error getting documents. $exception")
            }
    }

    fun deleteMessages(userId:String){
        val apiService = RetrofitBuilder.create()

        val data = DeleteMessageData(userId)
        apiService.postData(data).enqueue((object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    println("delete message result "+response.message())
                } else {
                    // Handle error response
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                TODO("Not yet implemented")
            }
        }))
    }


}