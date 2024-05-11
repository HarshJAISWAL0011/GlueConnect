package com.apexharn.chatapplication.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "sender",
        indices = [Index(value = ["email"], unique = true)])
data class Sender(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val name: String,
    val profile_url: String,
    val email: String,
    var newMessageCount: Int
)

@Entity(tableName = "messages",foreignKeys = [
    ForeignKey(entity = Sender::class,
        parentColumns = ["email"],
        childColumns = ["senderId"],
        onDelete = ForeignKey.CASCADE)])
data class Message(
    @PrimaryKey
    val messageId: String,
    val senderId: String,
    val messageType: String?="text",
    var message: String,
    val isReceived: Int = 1,
    val receiveTime: Long = 0,
    val sentTime: Long = 0
)