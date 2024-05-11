package com.apexharn.chatapplication.db.channeldb

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.apexharn.chatapplication.db.Converter

@Database(entities = [Channels::class,  ChannelMessage::class], version = 1)
@TypeConverters(Converter::class)
abstract class  ChannelDatabase: RoomDatabase() {

        abstract fun channelsDao(): ChannelDao
        abstract fun channelMsgDao(): ChannelMsgDao

        companion object {

            private var INSTANCE: ChannelDatabase? = null;

            @SuppressLint("SuspiciousIndentation")
            fun getDatabase(context: Context): ChannelDatabase{
                synchronized(this){
                    if(INSTANCE == null)
                        INSTANCE = Room.databaseBuilder(context,ChannelDatabase::class.java,"channelDB").build()
                }
                return INSTANCE!!
            }
        }
    }
