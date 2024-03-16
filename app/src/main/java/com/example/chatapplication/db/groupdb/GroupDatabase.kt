package com.example.chatapplication.db.groupdb

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

    @Database(entities = [Group::class,  GroupMember::class,  GroupMessage::class], version = 1)
    abstract class GroupDatabase: RoomDatabase() {
        abstract fun groupDao(): GroupDao
        abstract fun groupMemberDao(): GroupMemberDao
        abstract fun groupMessageDao(): GroupMessageDao

        companion object {

            private var INSTANCE: GroupDatabase? = null;

            @SuppressLint("SuspiciousIndentation")
            fun getDatabase(context: Context): GroupDatabase{
                synchronized(this){
                    if(INSTANCE == null)
                        INSTANCE = Room.databaseBuilder(context, GroupDatabase::class.java, "groupDB").build()
                }
                return INSTANCE!!
            }
        }
    }

