package com.example.chatapplication.Repository

import com.example.Constants
import com.example.chatapplication.WebSocket.WebSocketClient
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.channeldb.ChannelDatabase
import com.example.chatapplication.db.channeldb.ChannelMessage
import com.example.util.ChannelsWithMessage
import com.example.util.SendMessage
import com.example.util.util
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