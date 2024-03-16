package com.example.chatapplication.WebSocket

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.Constants
import com.example.Constants.MESSAGE_TYPE_AUDIO
import com.example.Constants.MESSAGE_TYPE_IMAGE
 import com.example.chatapplication.Repository.ConversationRepository
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.SQLFuntions
import com.example.chatapplication.db.Sender
import com.example.util.NewConnection
import com.example.util.util.URLdownloadFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.sql.SQLException

@SuppressLint("StaticFieldLeak")
object webSocketListener : WebSocketListener() {

    private val TAG = "Test"
    private lateinit var context: Context;
    private lateinit var conversationRepository: ConversationRepository;

    fun setContext(context: Context, conversationRepository: ConversationRepository){
        this.conversationRepository = conversationRepository
        this.context=context
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        val report = NewConnection("968")
        webSocket.send(report.jsonObject.toString())
        Log.d(TAG, "onOpen:")
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        Log.d(TAG, "onMessage: $text")
        if(text.contains("Server Recived")) return
        try {
            val jsonObject = JSONObject(text)


            val receivedFrom = jsonObject.getString(Constants.senderId)
            val message = jsonObject.getString(Constants.message)
            val timestamp = jsonObject.getString(Constants.timestamp)
            val messageId = jsonObject.getString(Constants.messageId)
            val messageType = jsonObject.getString(Constants.messageType)
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

                if (senderObj == null) // new message first time
                    conversationRepository.insert(
                        Sender(
                            name = receivedFrom,
                            email = receivedFrom,
                            newMessageCount = 1
                        )
                    )
                else if (messageObj == null) { // new message
                    ChatDatabase.getDatabase(context).senderDao()
                        .updateSender(senderObj.copy(newMessageCount = senderObj.newMessageCount + 1))
                }

                println("test messageOBj= $messageObj")

                if (messageObj == null) {
                    if(messageType == MESSAGE_TYPE_IMAGE || messageType == MESSAGE_TYPE_AUDIO){
                        val file = URLdownloadFile(context,receivedMessage)
                        println("location of saving file = ${file.toString()}")
                        var location = ""
                        if(file != null)
                            location = file.toString();

                        ChatDatabase.getDatabase(context).messageDao().insertMessage(receivedMessage.copy(message = location))

                    }else
                    ChatDatabase.getDatabase(context).messageDao().insertMessage(receivedMessage)
                } else if (messageObj != null) {
                    // update in message
                    ChatDatabase.getDatabase(context).messageDao().editMessage(receivedMessage)
                }

            }

        } catch (e: Throwable) {
            Log.d(TAG, "error: ${e.message}")
        }
        catch (e: SQLException){
        Log.d(TAG, "error: ${e.message}")
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
        val websocketURL ="ws://chat-websocker.onrender.com"
//            "wss://socketsbay.com/wss/v2/1/demo/"

        return Request.Builder()
            .url(websocketURL)
            .build()
    }
}

