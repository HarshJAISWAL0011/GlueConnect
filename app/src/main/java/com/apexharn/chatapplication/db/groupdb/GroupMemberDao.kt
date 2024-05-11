package com.apexharn.chatapplication.db.groupdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface GroupMemberDao {

    @Insert
    suspend fun insertNewMember(sender: GroupMember)

    @Update
    suspend fun updateMember(sender: GroupMember)

    @Delete
    suspend  fun removeMember(sender: GroupMember)

    @Query("Select * from group_member WHERE groupId =:groupId")
    fun getMembers(groupId: String): List<GroupMember>



}