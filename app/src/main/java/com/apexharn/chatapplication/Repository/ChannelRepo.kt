package com.apexharn.chatapplication.Repository

import com.apexharn.chatapplication.db.channeldb.ChannelDatabase
import com.apexharn.chatapplication.db.channeldb.Channels
import com.apexharn.util.ChannelsWithMessage
import kotlinx.coroutines.flow.Flow

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