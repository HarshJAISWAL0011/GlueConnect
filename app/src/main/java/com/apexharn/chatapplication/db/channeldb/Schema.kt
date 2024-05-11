package com.apexharn.chatapplication.db.channeldb

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "channels",
    indices = [Index(value = ["channelId"], unique = true)])
data class Channels(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val name: String,
    val channelId: String,
    val profile_url: String,
    var newMessageCount: Int,
    var description:String,
    var followers: Int,
    var isAdmin: Int,
    var creationDate: Long,
    var channelType: String
)

@Entity(tableName = "channel_message",foreignKeys = [
    ForeignKey(entity = Channels::class,
        parentColumns = ["channelId"],
        childColumns = ["channelId"],
        onDelete = ForeignKey.CASCADE)])
data class ChannelMessage(
    @PrimaryKey
    val messageId: String,
    val channelId: String,
    val messageType: String?="text",
    var message: String,
    val sentTime: Long = 0,
)