package com.apexharn.chatapplication.db.groupdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.apexharn.util.GroupSendersWithMessage
import com.apexharn.util.TimeWithId
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Insert
    suspend fun insertNewGroup(group: Group)

    @Update
    suspend fun updateGroup(group: Group)

    @Delete
    suspend  fun deleteGroup(group: Group)

    @Query("SELECT groups.groupId AS id, COALESCE(MAX(group_message.receiveTime), 0) AS receiveTime " +
            "FROM groups " +
            "LEFT JOIN group_message ON groups.groupId = group_message.groupId " +
            "GROUP BY groups.groupId")
    fun getGroupIds( ): List<TimeWithId>?

    @Query("Select * from groups WHERE groupId =:groupId")
    fun getGroupFromId(groupId: String): Group?

    @Query("SELECT groups.*, latest_message.message AS last_message, latest_message.sentTime, latest_message.messageType " +
                   "FROM groups " +
                   "LEFT JOIN (" +
                   "    SELECT  message, sentTime, messageType, groupId " +
                   "    FROM group_message AS g1 " +
                   "    WHERE receiveTime = (" +
                   "        SELECT MAX(receiveTime) " +
                   "        FROM group_message AS g2 " +
                   "        WHERE g1.groupId = g2.groupId" +
                   "    )" +
                   ") AS latest_message ON groups.groupId = latest_message.groupId")
    fun getAllGroupsWithMessage(): Flow<List<GroupSendersWithMessage>>

}