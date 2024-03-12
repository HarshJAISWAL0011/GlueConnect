package com.example.chatapplication.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.checkerframework.common.aliasing.qual.Unique
import java.util.Date


@Entity(tableName = "sender",
        indices = [Index(value = ["email"], unique = true)])
data class Sender(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val name: String,
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
    val messageType: String,
    var message: String,
    val isReceived: Int,
    val receiveTime: Long,
    val sentTime: Long
)