package com.apexharn.chatapplication.Repository

import com.apexharn.Constants
import com.apexharn.chatapplication.db.channeldb.ChannelDatabase
import com.apexharn.chatapplication.db.channeldb.ChannelMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ChannelChatRepo (var id: String ,var database: ChannelDatabase,val name: String){
    var offset =0
    val messages: Flow<List<ChannelMessage>> =database.channelMsgDao().getMessage(id,offset,
        Constants.SQLoffsetValue.toString())

    suspend fun getOlderMessages(): List<ChannelMessage>? {
        offset += Constants.SQLoffsetValue;
        return database.channelMsgDao().getMessage(id,offset, Constants.SQLoffsetValue.toString()).firstOrNull()
    }
}