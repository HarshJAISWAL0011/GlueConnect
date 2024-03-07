package com.example.chatapplication.Repository

import com.example.SendersWithLastMessage
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
    CoroutineScope(Dispatchers.IO).launch {
        peopleList.collect {
            println("storing people list size in repo${it.size}")
        }

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