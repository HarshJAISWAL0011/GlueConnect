package com.example.chatapplication.db.groupdb

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "groups",
        indices = [Index(value = ["groupId"], unique = true)])
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val groupName: String="",
    val groupId: String,
    var newMessageCount: Int = 0
)

@Entity(tableName = "group_member",
        foreignKeys = [
            ForeignKey(entity = Group::class,
                       parentColumns = ["groupId"],
                       childColumns = ["groupId"],
                       onDelete = ForeignKey.CASCADE)])
data class GroupMember(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val senderId: String,
    val name: String = "",
    val groupId: String = "",
    var isAdmin: Int = 0
)

@Entity(tableName = "group_message", foreignKeys = [
    ForeignKey(entity = Group::class,
               parentColumns = ["groupId"],
               childColumns = ["groupId"],
               onDelete = ForeignKey.CASCADE)])
data class GroupMessage(
    @PrimaryKey
    val messageId: String ,
    val senderId: String = "",
//    var senderName: String? = "",
    val messageType: String = "text",
    var message: String = "",
    val isReceived: Int = 1,
    val receiveTime: Long = 0,
    val sentTime: Long = 0,
    val groupId: String
)