package com.example.chatapplication.firebase

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.example.Constants
import com.example.Constants.FIRESTORE_MESSAGES
import com.example.Constants.FIRESTORE_USERS
import com.example.Constants.MESSAGE_TYPE_IMAGE
import com.example.Constants.message
import com.example.Constants.messageId
import com.example.Constants.messageType
import com.example.Constants.path_channels
import com.example.Constants.path_groups
import com.example.Constants.path_users
import com.example.Constants.senderId
import com.example.Constants.timestamp
import com.example.chatapplication.WebSocket.webSocketListener
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.SQLFuntions
import com.example.chatapplication.db.Sender
import com.example.retrofit.RetrofitBuilder
import com.example.util.CreateChannelData
import com.example.util.DeleteMessageData
import com.example.util.util.URLdownloadFile
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.CompletableFuture

object  FirestoreDb {

    @SuppressLint("SuspiciousIndentation")
    suspend fun getNewMessageFirestore(id:String, database: ChatDatabase, context: Context){
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
                    println("message got from firestore $data")
                    messageList.add(data)
                    i++
                }
                CoroutineScope(Dispatchers.IO).launch {
                    database.withTransaction {
                        messageList.forEach { message ->
                            println("message in fs $message")
                            try {
                                val senderObj = SQLFuntions.getSenderDetails(message.senderId,
                                   context
                                )
                                val messageObj = SQLFuntions.getMessageWithID(message.messageId,
                                   context
                                )

                                println("message got from fs senderObj =$senderObj $messageObj")
                                if (senderObj == null) // new message first time
                                   database.senderDao().insertNewSender(
                                        Sender(
                                            name = message.messageId,
                                            email =  message.messageId,
                                            newMessageCount = 1
                                        )
                                    )
                                else if (messageObj == null) { // new message
                                    database.senderDao()
                                        .updateSender(senderObj.copy(newMessageCount = senderObj.newMessageCount + 1))
                                }

                                println("test messageOBj= $messageObj")

                                if (messageObj == null) {
                                    if (messageType == MESSAGE_TYPE_IMAGE || messageType == Constants.MESSAGE_TYPE_AUDIO) {
                                        val file = URLdownloadFile(
                                            message.message,
                                            message.messageType?:"Image",
                                            message.senderId
                                        )
                                        println("location of saving file = ${file.toString()}")
                                        var location = ""
                                        if (file != null)
                                            location = file.toString();

                                        database.messageDao()
                                            .insertMessage(message.copy(message = location))

                                    } else
                                        database.messageDao()
                                            .insertMessage(message)
                                } else if (messageObj != null) {
                                    // update in message
                                   database.messageDao().editMessage(message)
                                }

                            } catch (e: SQLiteConstraintException) {
                                println("message SQLiteConstraintException: ${e.message}")
                            } catch (e: Exception) {
                                println("message Exception: ${e.message}")
                            }
                        }
                    }
                    if(messageList.size > 0)
                        deleteMessages(id) // app userId
                }


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

    fun createGroup( data: CreateChannelData): Task<String> {
        val result =  TaskCompletionSource<String>()
        CoroutineScope(Dispatchers.IO).launch{
        val db = Firebase.firestore
        val id = db.collection(path_channels).document().id


        db.collection(path_channels).document(id).set(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val grpId= db.collection(path_users).document(data.creatorId)
                        .collection(path_groups).document().id
                    println(grpId)

                    val groupData = mapOf(grpId to id)

                    db.collection(path_users).document(data.creatorId)
                        .collection(path_groups)
                        .document("created")
                        .update(groupData)
                        .addOnCompleteListener { innerTask ->
                            if (innerTask.isSuccessful) {
                                result.setResult(id) // Complete the CompletableFuture when both tasks are successful
                            } else {
                                result.setException(innerTask.exception!!)
                                println("Exceiption ${innerTask.exception}")
                            }
                        }
                } else {
                    result.setException(task.exception!!)
                }
            }
    }
        return result.task
    }


}