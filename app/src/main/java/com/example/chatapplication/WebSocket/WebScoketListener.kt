package com.example.chatapplication.WebSocket

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.Constants
import com.example.Message
import com.example.NewConnection
import com.example.chatapplication.Repository.ConversationRepository
import com.example.chatapplication.db.SQLFuntions
import com.example.chatapplication.db.Sender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

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
        try{
        val jsonObject = JSONObject(text)
//        if(!jsonObject.(Constants.senderId)) return

        val receivedFrom = jsonObject.getString(Constants.senderId)
        val message = jsonObject.getString(Constants.message)
        val timestamp= jsonObject.getString(Constants.timestamp)
        val receivedMessage = Message(receivedFrom,message, timestamp.toLong(),false)
            Log.d(TAG, "onMessage: senderOBj ${receivedMessage}")
        CoroutineScope(Dispatchers.IO).launch {
            val senderObj = SQLFuntions.getSenderDetails(receivedFrom, context)

            if (senderObj == null)
            conversationRepository.insert(Sender(name=receivedFrom,email=receivedFrom, newMessageCount = 1))
            SQLFuntions.insertMessageSQL(receivedMessage, context)
        }

        }
        catch (e : Throwable) {  Log.d(TAG, "error: ${e.message}")}
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