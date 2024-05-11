package com.apexharn.chatapplication.GroupPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apexharn.chatapplication.Repository.GroupRepo
import com.apexharn.chatapplication.db.groupdb.Group
import com.apexharn.chatapplication.db.groupdb.GroupDatabase
import com.apexharn.util.GroupSendersWithMessage
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
