package com.example.chatapplication.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.GroupPage.GroupListViewModel
import com.example.chatapplication.Repository.ChannelRepo
import com.example.chatapplication.Repository.ConversationRepository
import com.example.chatapplication.Repository.GroupRepo
import com.example.chatapplication.db.channeldb.ChannelDatabase
import com.example.chatapplication.db.channeldb.Channels
import com.example.chatapplication.db.groupdb.Group
import com.example.chatapplication.db.groupdb.GroupDatabase
import com.example.util.ChannelsWithMessage
import com.example.util.GroupSendersWithMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class ChannelViewModel  (val database: ChannelDatabase, private var repository: ChannelRepo): ViewModel() {
    private var _channelListState = MutableStateFlow<List<ChannelsWithMessage>>(emptyList())
    var channelListState: StateFlow<List<ChannelsWithMessage>> = _channelListState
    init {
        viewModelScope.launch {

            repository.channelList.conflate().collect{list->
                _channelListState.value = list.toList().reversed()
                println("groupList in viewmodel $list")
            }
        }
    }

    fun refreshData(){
        viewModelScope.launch {
            repository.resetList().conflate().collect {
                _channelListState.value = it.reversed();
            }
        }
    }

    fun resetMessageCount(channel: ChannelsWithMessage){
        viewModelScope.launch { repository.resetNewMessageCount(channel.channelId) }
    }


}


class ChannelVMFactory(private var database: ChannelDatabase, private var repository: ChannelRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChannelViewModel::class.java)) {
            return ChannelViewModel(database, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
