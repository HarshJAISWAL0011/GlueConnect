package com.apexharn.chatapplication.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [Sender::class,  Message::class], version = 1)
@TypeConverters(Converter::class)
abstract class ChatDatabase: RoomDatabase() {
    abstract fun senderDao(): SenderDao
    abstract fun messageDao(): MessageDao

    companion object {

        private var INSTANCE: ChatDatabase? = null;

        @SuppressLint("SuspiciousIndentation")
        fun getDatabase(context: Context): ChatDatabase{
            synchronized(this){
                if(INSTANCE == null)
                INSTANCE = Room.databaseBuilder(context,ChatDatabase::class.java,"chatDB").build()
            }
            return INSTANCE!!
        }
    }
}