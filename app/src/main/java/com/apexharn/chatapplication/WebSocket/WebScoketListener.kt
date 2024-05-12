package com.apexharn.chatapplication.WebSocket

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.apexharn.Constants
import com.apexharn.Constants.BASE_URL
import com.apexharn.Constants.GroupId
import com.apexharn.Constants.GroupName
import com.apexharn.Constants.MESSAGE_TYPE_AUDIO
import com.apexharn.Constants.MESSAGE_TYPE_IMAGE
import com.apexharn.Constants.MESSAGE_TYPE_TEXT
import com.apexharn.Constants.MY_ID
import com.apexharn.Constants.new_group_message
import com.apexharn.chatapplication.Repository.ConversationRepository
import com.apexharn.chatapplication.db.ChatDatabase
import com.apexharn.chatapplication.db.Message
import com.apexharn.chatapplication.db.SQLFuntions
import com.apexharn.chatapplication.db.Sender
import com.apexharn.chatapplication.db.groupdb.Group
import com.apexharn.chatapplication.db.groupdb.GroupDatabase
import com.apexharn.chatapplication.db.groupdb.GroupMessage
 import com.apexharn.util.NewConnection
import com.apexharn.util.util.NotifyNewMessage
import com.apexharn.util.util.URLdownloadFile
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.sql.SQLException

@SuppressLint("StaticFieldLeak")
object webSocketListener : WebSocketListener() {

    private val TAG = "Websocket"
    private lateinit var context: Context;
    private lateinit var conversationRepository: ConversationRepository;

    fun setContext(context: Context, conversationRepository: ConversationRepository){
        this.conversationRepository = conversationRepository
        this.context=context
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        val id = MY_ID
        val report = NewConnection(id)
        webSocket.send(report.jsonObject.toString())
        Log.d(TAG, "onOpen:")
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        Log.d(TAG, "onMessage: $text")
        if(text.contains("Server Received")) return
        try {
            val jsonObject = JSONObject(text)

            val type = jsonObject.getString(Constants.type)


            val receivedFrom = jsonObject.getString(Constants.senderId)
            val message = jsonObject.getString(Constants.message)
            val timestamp = jsonObject.getString(Constants.timestamp)
            val messageId = jsonObject.getString(Constants.messageId)
            val messageType = jsonObject.getString(Constants.messageType)
            val sentTime = jsonObject.getLong(Constants.timestamp);


            if(type == new_group_message)
            {
                var groupId = jsonObject.getString(GroupId)
                var groupName = jsonObject.getString(GroupName)
                val groupDatabase = GroupDatabase.getDatabase(context)
                CoroutineScope(Dispatchers.IO).launch {

                    val groupObj = SQLFuntions.getGroupFromId(groupId, context)
                    val messageObj = SQLFuntions.getGroupMessageWithID(messageId, context)

                    if(groupObj == null ){
                        // new group
                        val profile_url = Firebase.firestore.collection(Constants.path_groups).document(groupId).get().await()?.get("profile_url").toString()
                        groupDatabase.groupDao().insertNewGroup(Group(0,profile_url,groupName,groupId,0))
                    }else
                        groupDatabase.groupDao().updateGroup(groupObj.copy(newMessageCount = groupObj.newMessageCount + 1))

                    if(messageObj == null){
                        NotifyNewMessage(context,"New group message received",message, groupId)
                    if(messageType == MESSAGE_TYPE_TEXT)
                    groupDatabase.groupMessageDao()
                        .insertMessage(GroupMessage(messageId,receivedFrom,messageType, message ,1, System.currentTimeMillis(), sentTime ,groupId))

                    else if(messageType == MESSAGE_TYPE_IMAGE || messageType == MESSAGE_TYPE_AUDIO){
                        val file = URLdownloadFile(message, messageType, receivedFrom, context)
                        println("location of saving file = ${file.toString()}")
                        var location = ""
                        if (file != null)
                            location = file.toString();
                        groupDatabase.groupMessageDao().insertMessage(
                            GroupMessage(
                                messageId,receivedFrom,messageType, location ,1, System.currentTimeMillis(), sentTime ,groupId
                        ))
                    }
                }
                   else {
                    groupDatabase.groupMessageDao().editMessage(messageObj.copy(message = message))
                    }

            }
            }
            else{
                val receivedMessage = Message(
                    messageId,
                    receivedFrom,
                    messageType,
                    message,
                    1,
                    System.currentTimeMillis(),
                    timestamp.toLong()
                )
                Log.d(TAG, "onMessage: senderOBj ${receivedMessage}")



            CoroutineScope(Dispatchers.IO).launch {

                val senderObj = SQLFuntions.getSenderDetails(receivedFrom, context)
                val messageObj = SQLFuntions.getMessageWithID(messageId, context)

                if (senderObj == null) { // new message first time
                    val profile_url = Firebase.firestore.collection(Constants.path_users).document(receivedFrom).get().await()?.get("profile_url").toString()
                    conversationRepository.insert(
                        Sender(
                            name = receivedFrom,
                            email = receivedFrom,
                            newMessageCount = 1,
                            profile_url =profile_url
                        )
                    )
                }
                else if (messageObj == null) { // new message
                    ChatDatabase.getDatabase(context).senderDao()
                        .updateSender(senderObj.copy(newMessageCount = senderObj.newMessageCount + 1))

                    NotifyNewMessage(context,"New message received",message, receivedFrom)

                }

                println("test messageOBj= $messageObj")

                if (messageObj == null) {
                    if (messageType == MESSAGE_TYPE_IMAGE || messageType == MESSAGE_TYPE_AUDIO) {
                        val file = URLdownloadFile(
                            receivedMessage.message,
                            receivedMessage.messageType?:"Image",
                            receivedMessage.senderId,
                            context
                        )
                        println("location of saving file = ${file.toString()}")
                        var location = ""
                        if (file != null)
                            location = file.toString();

                        ChatDatabase.getDatabase(context).messageDao()
                            .insertMessage(receivedMessage.copy(message = location))

                    } else
                        ChatDatabase.getDatabase(context).messageDao()
                            .insertMessage(receivedMessage)
                } else if (messageObj != null) {
                    // update in message
                    ChatDatabase.getDatabase(context).messageDao().editMessage(receivedMessage)
                }
            }
                }
        } catch (e: Throwable) {
            Log.d(TAG, "error: ${e.message}")
        }
        catch (e: SQLException){
        Log.d(TAG, "error: ${e.message}")
        }
        catch (e: SQLiteConstraintException){
            e.printStackTrace()
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Log.d(TAG, "onClosing: $code $reason")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Log.d(TAG, "onClosed: $code $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d(TAG, "onFailure: ${t.message} $response")
        super.onFailure(webSocket, t, response)
    }
}
@SuppressLint("StaticFieldLeak")
object WebSocketClient{
     var webSocket: WebSocket? = null
     private lateinit var context:Context

    fun create(context: Context,convRepo: ConversationRepository) {
        if (webSocket == null) {
            val webSocketListener = webSocketListener
            webSocketListener.setContext(context,convRepo)
            val okHttpClient = OkHttpClient()
            webSocket = okHttpClient.newWebSocket(createRequest(), webSocketListener)
        }
    }

    private fun createRequest(): Request {
        val websocketURL =BASE_URL


        return Request.Builder()
            .url(websocketURL)
            .build()
    }
}

