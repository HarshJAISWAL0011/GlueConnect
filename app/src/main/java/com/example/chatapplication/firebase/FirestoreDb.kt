package com.example.chatapplication.firebase

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.withTransaction
import com.example.Constants
import com.example.Constants.FIRESTORE_MESSAGES
import com.example.Constants.FIRESTORE_USERS
import com.example.Constants.MESSAGE_TYPE_IMAGE
import com.example.Constants.MY_ID
import com.example.Constants.channels_created
import com.example.Constants.channels_joined
import com.example.Constants.feild_phone
import com.example.Constants.groups_created
import com.example.Constants.groups_joined
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
import com.example.chatapplication.db.channeldb.ChannelDatabase
import com.example.chatapplication.db.channeldb.ChannelMessage
import com.example.chatapplication.db.channeldb.Channels
import com.example.chatapplication.db.groupdb.Group
import com.example.chatapplication.db.groupdb.GroupDatabase
import com.example.chatapplication.db.groupdb.GroupMember
import com.example.retrofit.RetrofitBuilder
import com.example.util.ChannelData
import com.example.util.CreateChannelData
import com.example.util.DeleteMessageData
import com.example.util.SearchUserData
import com.example.util.SendGroupMessage
import com.example.util.util.URLdownloadFile
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
 import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.CompletableFuture

object  FirestoreDb {

    @SuppressLint("SuspiciousIndentation")
    suspend fun getNewMessageFirestore(id:String, database: ChatDatabase, context: Context){
        val messageList = mutableListOf<Message>()


        val db = Firebase.firestore

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
                                if (senderObj == null) { // new message first time
                                    val profile_url = db.collection(path_users).document(id).get().await()?.get("profile_url")
                                    database.senderDao().insertNewSender(
                                        Sender(
                                            name = message.messageId,
                                            email = message.messageId,
                                            newMessageCount = 1,
                                            profile_url = profile_url.toString()
                                        )
                                    )
                                }
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
            }
        }))
    }

     fun createChannel( data: CreateChannelData): Task<String> {
        val result =  TaskCompletionSource<String>()
        CoroutineScope(Dispatchers.IO).launch{
        val db = Firebase.firestore
        val id = db.collection(path_channels).document().id


        db.collection(path_channels).document(id).set(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val ref = com.google.firebase.Firebase.firestore.collection(path_users).document(data.creatorId)
                    CoroutineScope(Dispatchers.IO).launch {
                        val alldocs = ref.get().await()

                        if (alldocs.contains(channels_created)) {
                            val data = listOf(id)
                            ref.update(channels_created, FieldValue.arrayUnion(*data.toTypedArray()))
                                .addOnCompleteListener { innerTask ->
                                    if (innerTask.isSuccessful) {
                                        result.setResult(id) // Complete the CompletableFuture when both tasks are successful
                                    } else {
                                        result.setException(innerTask.exception!!)
                                        println("Exception ${innerTask.exception}")
                                    }
                                }
                        } else {
                            val hashData = hashMapOf(channels_created to listOf(id))
                            ref.set(hashData, SetOptions.merge())
                                .addOnCompleteListener { innerTask ->
                                    if (innerTask.isSuccessful) {
                                        result.setResult(id) // Complete the CompletableFuture when both tasks are successful
                                    } else {
                                        result.setException(innerTask.exception!!)
                                        println("Exception ${innerTask.exception}")
                                    }
                                }
                        }
                    }
                } else {
                    result.setException(task.exception!!)
                }
            }
    }
        return result.task
    }

    fun sendMessageToGroup(message: JSONObject, ){
        val db = Firebase.firestore
        println(message)
        val map = mutableMapOf<String, Any>()

        // Iterate over the keys in the JSONObject
        message.keys().forEach { key ->
            val value = message.opt(key) ?: JSONObject.NULL // Get the value or null
            map[key] = value // Store the key-value pair in the map
        }
        db.collection(path_groups).document(message.getString("group_id")).collection(Constants.message).add(map)
    }
    suspend fun getChannelList( searchText: String, database: ChannelDatabase): List<ChannelData> {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection(path_channels)
            .whereGreaterThanOrEqualTo("name", searchText)
            .whereLessThan("name", searchText + "\uf8ff")

        val dataList = mutableListOf<ChannelData>()

        try {
            val querySnapshot = collectionRef.get().await()
            val storedChannelList = database.channelsDao().getAllChannelIds()
            println(" stored list $storedChannelList")

            for (document in querySnapshot.documents) {
                val channelData = document.toObject(ChannelData::class.java)
                channelData?.channelId = document.id
                if (channelData != null && !storedChannelList.contains(channelData.channelId)) {
                    dataList.add(channelData)
                }
            }
            println(querySnapshot.size())
        } catch (e: Exception) {
            println("Error fetching channel list: ${e.message}")
        }

        return dataList
    }

    suspend fun getSearchUserList( searchText: String,): List<SearchUserData> {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection(path_users)
            .whereGreaterThanOrEqualTo("name", searchText)
            .whereLessThan("name", searchText + "\uf8ff")

        val dataList = mutableListOf<SearchUserData>()

        try {
            val querySnapshot = collectionRef.get().await()
            println("search query data =${querySnapshot.documents}")

            for (document in querySnapshot.documents) {
                val data = SearchUserData(document["name"].toString(),document["description"].toString(),document.id,document["profile_url"].toString(),"")
                data?.connection_status = document.id
                if(data != null && data.id != MY_ID)
                    dataList.add(data)
            }
            println(querySnapshot.size())
        } catch (e: Exception) {
            println("Error fetching channel list: ${e.message}")
        }

        return dataList
    }

   suspend fun getChannelFromId(id: String): Channels?{
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection(path_channels).document(id)
        try {
            val querySnapshot = collectionRef.get().await()
            val channelData = Channels(0,querySnapshot.get("name").toString(),querySnapshot.id,querySnapshot.get("profileUrl").toString(),0,querySnapshot.get("description").toString(),
                querySnapshot.get("followers")?.toString()?.toInt() ?:  0,0,querySnapshot.get("creationDate")?.toString()?.toLong()?:0,
                querySnapshot.get("channelType")?.toString()?:"")

            return channelData
        }catch (e: Exception) {
            println("Error fetching channel list: ${e.message}")
            return null
        }
    }

    suspend fun getGroupFromId(id: String): Group?{
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection(path_groups).document(id)
        try {
            val querySnapshot = collectionRef.get().await()
            val groupData = Group(0,querySnapshot.get("profile_url").toString(),
                querySnapshot.get("name").toString(),querySnapshot.id,0)

            return groupData
        }catch (e: Exception) {
            println("Error fetching channel list: ${e.message}")
            return null
        }
    }

    suspend fun getGroupMemebersFromId(id: String): List<String>?{
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection(path_groups).document(id)
        try {
            val querySnapshot = collectionRef.get().await()
            val groupData = querySnapshot.get( "groupMembers") as? List<String>

            return groupData
        }catch (e: Exception) {
            println("Error fetching channel list: ${e.message}")
            return emptyList()
        }
    }

    suspend fun getOlderChannelChats(id:String,latestTime: Long):List< DocumentSnapshot>{
        println(" getChannelChats data =$id ")
        val db = FirebaseFirestore.getInstance()
        var ref =
            db.collection(path_channels).document(id)
                .collection("messages")
                .whereLessThan("timestamp", latestTime)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)


        val snapshot = ref.get().await()



        val results = snapshot.documents
        println(" getChannelChats data result size =${results.size}  time $latestTime")
        return results
    }

    suspend fun getNewChannelChats(id:String,lastMsgTime: Long):List< DocumentSnapshot>{
        println(" getChannelChats data =$id ")
        val db = FirebaseFirestore.getInstance()
        var ref =
            db.collection(path_channels).document(id)
                .collection("messages")
                .whereGreaterThan("timestamp", lastMsgTime)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(15)


        val snapshot = ref.get().await()



        val results = snapshot.documents
        println(" getChannelChats data result size =${results.size}  time $lastMsgTime")
        return results
    }


    suspend fun addFCMtoken(token: String){
        Firebase.firestore.collection(path_users).document(MY_ID)
            .set(mapOf(
                "registrationToken" to  token
            ), SetOptions.merge())
    }

    suspend fun getChannelChats(id:String,lastItem: DocumentSnapshot?):List< DocumentSnapshot>{
        println(" getChannelChats data =$id ")
        val db = FirebaseFirestore.getInstance()
        var ref =if(lastItem == null)
            db.collection(path_channels).document(id)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(25)
        else
            db.collection(path_channels).document(id)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastItem)
                .limit(25)

        val snapshot = ref.get().await()

        val results = snapshot.documents

        return results
    }

    suspend fun getInitialData(id: String, context: Context){
        val db = Firebase.firestore
       var ref = db.collection(path_users).document(id)

        // add channels joioned
        try {
             val documentSnapshot = ref.get().await()
            var channelList = if (documentSnapshot.exists()) {
                val dataArray = documentSnapshot.get(channels_joined) as? List<String>
                dataArray
            } else {
                null
            }
            channelList?.forEach {
                try {
               val channel = getChannelFromId(it)
                if(channel != null)
                 ChannelDatabase.getDatabase(context).channelsDao().addNewChannel(channel)
                }catch (e: SQLiteConstraintException){
                    Log.e("FirestoreDb", "error occured ${e.printStackTrace()}")
                }
                println("channel data $it")
            }
            println("channel data $channelList")

        } catch (e: Exception) {
            e.printStackTrace()
        }

        // add channels created
        try {
            val documentSnapshot = ref.get().await()
            var channelList = if (documentSnapshot.exists()) {
                val dataArray = documentSnapshot.get(channels_created) as? List<String>
                dataArray
            } else {
                null
            }
            channelList?.forEach {
                try {
                    val channel = getChannelFromId(it)?.copy(isAdmin = 1)
                    if(channel != null)
                        ChannelDatabase.getDatabase(context).channelsDao().addNewChannel(channel)
                }catch (e: SQLiteConstraintException){
                    Log.e("FirestoreDb", "error occured ${e.printStackTrace()}")
                }
                println("channel data $it")
            }
            println("channel data $channelList")

        } catch (e: Exception) {
            e.printStackTrace()
        }

        // add groups created
        try {
            val documentSnapshot = ref.get().await()
            var groupList = if (documentSnapshot.exists()) {
                val dataArray = documentSnapshot.get(groups_created) as? List<String>
                dataArray
            } else {
                null
            }
            groupList?.forEach {
                try {
                    val group = getGroupFromId(it)
                    if(group != null) {
                        GroupDatabase.getDatabase(context).groupDao().insertNewGroup(group)
                        val memberList = getGroupMemebersFromId(it)
                        memberList?.forEach { memberId ->
                            try {
                            val member = GroupMember(0,it,memberId,it,1)
                            GroupDatabase.getDatabase(context).groupMemberDao().insertNewMember(member)
                            }catch (e: SQLiteConstraintException){
                                Log.e("FirestoreDb", "error occured ${e.printStackTrace()}")
                            }
                        }
                    }
                }catch (e: SQLiteConstraintException){
                    Log.e("FirestoreDb", "error occured ${e.printStackTrace()}")
                }
                println("channel data $it")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        // add groups joined
        try {
            val documentSnapshot = ref.get().await()
            var groupList = if (documentSnapshot.exists()) {
                val dataArray = documentSnapshot.get(groups_joined) as? List<String>
                dataArray
            } else {
                null
            }
            groupList?.forEach {
                try {
                    val group = getGroupFromId(it)
                    if(group != null) {
                        GroupDatabase.getDatabase(context).groupDao().insertNewGroup(group)
                        val memberList = getGroupMemebersFromId(it)
                        memberList?.forEach { memberId ->
                            try {
                                val member = GroupMember(0,it,memberId,it,0)
                                GroupDatabase.getDatabase(context).groupMemberDao().insertNewMember(member)
                            }catch (e: SQLiteConstraintException){
                                Log.e("FirestoreDb", "error occured ${e.printStackTrace()}")
                            }
                        }
                    }
                }catch (e: SQLiteConstraintException){
                    Log.e("FirestoreDb", "error occured ${e.printStackTrace()}")
                }
                println("channel data $it")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

   suspend fun getProfileData(id:String): Task<Map<String, Any>> {
       val result =  TaskCompletionSource<Map<String, Any>>()
       CoroutineScope(Dispatchers.IO).launch {
           val db = Firebase.firestore

           val datamap = mutableMapOf<String, Any>()

           val docSnapshot = db.collection(path_users).document(id).get().await()

           if(!docSnapshot.exists()) {
               result.setResult(emptyMap<String, Any>())
               return@launch
           }
          if(docSnapshot.contains("description")) datamap.put("description",docSnapshot["description"].toString())
          if(docSnapshot.contains("about")) datamap.put("about",docSnapshot["about"].toString())
          if(docSnapshot.contains("location")) datamap.put("location",docSnapshot["location"].toString())
          if(docSnapshot.contains("job_type")) datamap.put("job_type",docSnapshot["job_type"].toString())
          if(docSnapshot.contains("profile_url")) datamap.put("profile_url",docSnapshot["profile_url"].toString())
          if(docSnapshot.contains("name")) datamap.put("name",docSnapshot["name"].toString())
          if(docSnapshot.contains("skills")) (docSnapshot["skills"] as? List<String>)?.let {
              datamap.put("skills",
                  it
              )
          }
           result.setResult(datamap)
       }
       return result.task
   }
    suspend fun checkPhoneNumbersInFirestore(phoneNumbersList: List<String>): List<Pair<String,String>> {
         val db = Firebase.firestore
        val collectionRef = db.collection(path_users)
        println("device phone ")
        try {
            // Use whereIn query to check if phone numbers exist in Firestore
            val querySnapshot = collectionRef
                .whereIn(feild_phone, phoneNumbersList)
                .get()
                .await()

            println("device phone ${querySnapshot.documents}")

            // Retrieve existing phone numbers from Firestore
            val existingPhoneNumbers = mutableListOf<Pair<String,String>>()
            for (document in querySnapshot.documents) {
                println("device phone ${document.get("phone")}")
                val phoneNumber = document.getString(feild_phone) ?: ""
                val profile_url = document.getString("profile_url") ?: ""
                existingPhoneNumbers.add(Pair(phoneNumber,profile_url))
            }

            return existingPhoneNumbers
        } catch (e: Exception) {
            // Handle any exceptions here
            e.printStackTrace()
            println("device phone error ${ e}")
            return emptyList()
        }
    }


}