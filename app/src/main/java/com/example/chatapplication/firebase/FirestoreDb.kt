package com.example.chatapplication.firebase

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.example.Constants
import com.example.Constants.FIRESTORE_MESSAGES
import com.example.Constants.FIRESTORE_USERS
import com.example.Constants.MESSAGE_TYPE_IMAGE
import com.example.Constants.message
import com.example.Constants.messageId
import com.example.Constants.messageType
import com.example.Constants.senderId
import com.example.Constants.timestamp
import com.example.DeleteMessageData
import com.example.chatapplication.ChatPage.context
import com.example.chatapplication.WebSocket.webSocketListener
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.SQLFuntions
import com.example.chatapplication.db.Sender
import com.example.retrofit.RetrofitBuilder
import com.example.util.util
import com.example.util.util.URLdownloadFile
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

    @SuppressLint("SuspiciousIndentation")
    suspend fun getNewMessageFirestore(id:String, database: ChatDatabase){
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
                    val messageId = document.get(messageId).toString()
                    val messageType = document.get(messageType).toString()


                    val data = Message(messageId,senderId,messageType,message,1,time + i,timestamp ?: 0,)
                    messageList.add(data)
                    i++
                }
                CoroutineScope(Dispatchers.IO).launch {
                    database.withTransaction {
                        messageList.forEach { message ->
                            try {

                                val senderObj = SQLFuntions.getSenderDetails(message.senderId,
                                    context)
                                val messageObj = SQLFuntions.getMessageWithID(message.messageId,
                                   context)

                                if (senderObj == null)
                                    database.senderDao().insertNewSender(
                                        Sender(
                                            name = message.messageId,
                                            email = message.messageId,
                                            newMessageCount = 1
                                        )
                                    )
                                else if (messageObj == null) {
                                    database.senderDao().
                                    updateSender(senderObj.copy(newMessageCount = senderObj.newMessageCount + 1))
                                }



                                if(message.messageType == MESSAGE_TYPE_IMAGE){
                                    val location = URLdownloadFile(
                                       context,
                                        message
                                    )
                                    println("location of saving file = $location")
                                    database.messageDao().insertMessage(message.copy(message = location.toString()))
                                }else
                                database.messageDao().insertMessage(message)


                            } catch (e: SQLiteConstraintException) {
                                println("SQLiteConstraintException: ${e.message}")
                            } catch (e: Exception) {
                                println("Exception: ${e.message}")
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