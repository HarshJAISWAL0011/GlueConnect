package com.example.chatapplication.db

import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

class FirestoreDB {
    fun getMessage(context: Context){
        GlobalScope.launch {
            sleep(5000)
//            val db = ChatDatabase.getDatabase(context)
//            db.messageDao().insertMessage(Message(0,"968226","after 5 sec", 0,System.currentTimeMillis(),System.currentTimeMillis()))
//            db.messageDao().insertMessage(Message(0,"968226","after 6 sec", 0,System.currentTimeMillis(),System.currentTimeMillis()))
//            db.messageDao().insertMessage(Message(0,"968226","after 7 sec", 0,System.currentTimeMillis(),System.currentTimeMillis()))
        }
    }
}