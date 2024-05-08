package com.example.chatapplication.GroupPage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.PeopleBook.PeopleViewModel
import com.example.chatapplication.Repository.ConversationRepository
import com.example.chatapplication.Repository.GroupRepo
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.Sender
import com.example.chatapplication.db.groupdb.Group
import com.example.chatapplication.db.groupdb.GroupDatabase
import com.example.util.GroupSendersWithMessage
import com.example.util.SendersWithLastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class GroupListViewModel  (val database: GroupDatabase, private var groupRepo: GroupRepo): ViewModel() {
    private var _groupListState = MutableStateFlow<List<GroupSendersWithMessage>>(emptyList())
    var groupListState: StateFlow<List<GroupSendersWithMessage>> = _groupListState
    init {
        viewModelScope.launch {

            groupRepo.groupList.conflate().collect{list->
                _groupListState.value = list.toList().reversed()
                println("groupList in viewmodel $list")
            }
        }
    }

    fun refreshData(){
        viewModelScope.launch {
            groupRepo.resetList().conflate().collect {
                _groupListState.value = it.reversed();
            }
        }
    }

    fun resetMessageCount(group: GroupSendersWithMessage){
        val resetData = Group(group.id,group.profile_url, group.groupName, group.groupId, 0)
        viewModelScope.launch { groupRepo.resetMessageCount(resetData) }
    }


}


class GroupVMFactory(private var database: GroupDatabase, private var groupRepo: GroupRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupListViewModel::class.java)) {
            return GroupListViewModel(database, groupRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
