package com.example.chatapplication.db.groupdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chatapplication.db.Sender
import com.example.util.SendersWithLastMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMemberDao {

    @Insert
    suspend fun insertNewMember(sender: GroupMember)

    @Update
    suspend fun updateMember(sender: GroupMember)

    @Delete
    suspend  fun removeMember(sender: GroupMember)

    @Query("Select * from group_member WHERE groupId =:groupId")
    fun getMembers(groupId: String): GroupMember?



}