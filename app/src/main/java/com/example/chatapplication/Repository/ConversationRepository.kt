package com.example.chatapplication.Repository

import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.SendersWithLastMessage
import com.example.chatapplication.WebSocket.webSocketListener
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.Sender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ConversationRepository( var database: ChatDatabase) {
    var peopleList : Flow<List<SendersWithLastMessage>> = database.senderDao().getSendersWithLastMessage()
init {
    Log.d("Test","db instance from peopleRepo = $database")

    CoroutineScope(Dispatchers.IO).launch {
        peopleList.collect {
            println("storing people list size in repo${it.size}")

        }

    }
}

     fun insertORupdate(message:Message){
         CoroutineScope(Dispatchers.IO).launch {
             database.messageDao().insertORupdate((message))
         }
    }
    suspend fun insert(sender: Sender) {
        database.senderDao().insertNewSender(sender)
    }

    suspend fun resetNewMessageCount(sender: Sender) {
        database.senderDao().updateSender(sender)
    }

     fun resetList(): Flow<List<SendersWithLastMessage>> {
     return  database.senderDao().getSendersWithLastMessage()
    }

}