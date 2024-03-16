package com.example.chatapplication.Repository

import android.util.Log
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.Sender
import com.example.chatapplication.db.groupdb.Group
import com.example.chatapplication.db.groupdb.GroupDatabase
import com.example.util.GroupSendersWithMessage
import com.example.util.SendersWithLastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GroupRepo ( var database: GroupDatabase) {
    var groupList : Flow<List<GroupSendersWithMessage>> = database.groupDao().getAllGroupsWithMessage()
    init {
        Log.d("Test", "db instance from peopleRepo = $database")

        CoroutineScope(Dispatchers.IO).launch {
            groupList.collect {
                println("storing people list size in repo${it.size}")

            }

        }
    }

    suspend fun addGroup(group: Group) {
        database.groupDao().insertNewGroup((group))
    }

    suspend fun resetMessageCount(group: Group) {
        database.groupDao().updateGroup(group)
    }

    fun resetList(): Flow<List<GroupSendersWithMessage>> {
        return  database.groupDao().getAllGroupsWithMessage()
    }

}