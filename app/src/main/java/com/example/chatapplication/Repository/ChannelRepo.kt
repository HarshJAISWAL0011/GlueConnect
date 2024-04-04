package com.example.chatapplication.Repository

import android.util.Log
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.Sender
import com.example.chatapplication.db.channeldb.ChannelDatabase
import com.example.chatapplication.db.channeldb.Channels
import com.example.util.ChannelsWithMessage
import com.example.util.SendersWithLastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.nio.channels.Channel

class ChannelRepo (var database: ChannelDatabase) {
    var channelList : Flow<List<ChannelsWithMessage>> = database.channelsDao().getChannelList()

//    fun insertORupdate(message: Message){
//        CoroutineScope(Dispatchers.IO).launch {
//            database.messageDao().insertORupdate((message))
//        }
//    }
    suspend fun insert(channel: Channels) {
        database.channelsDao().addNewChannel(channel)
    }

    suspend fun resetNewMessageCount(id: String) {
        database.channelsDao().updateChannel(id, 0)
    }

    fun resetList(): Flow<List<ChannelsWithMessage>> {
        return  database.channelsDao().getChannelList()
    }

}