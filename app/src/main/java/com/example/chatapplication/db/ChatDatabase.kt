package com.example.chatapplication.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.xml.validation.Schema


@Database(entities = [Sender::class,  Message::class], version = 1)
@TypeConverters(Converter::class)
abstract class ChatDatabase: RoomDatabase() {
    abstract fun senderDao(): SenderDao
    abstract fun messageDao(): MessageDao

    companion object {

        private var INSTANCE: ChatDatabase? = null;

        fun getDatabase(context: Context): ChatDatabase{
            synchronized(this){
                if(INSTANCE == null)
                INSTANCE = Room.databaseBuilder(context,ChatDatabase::class.java,"chatDB").build()
            }
            return INSTANCE!!
        }
    }
}