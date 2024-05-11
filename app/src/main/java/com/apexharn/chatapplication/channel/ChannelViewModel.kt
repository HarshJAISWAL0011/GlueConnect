package com.apexharn.chatapplication.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apexharn.chatapplication.Repository.ChannelRepo
import com.apexharn.chatapplication.db.channeldb.ChannelDatabase
import com.apexharn.util.ChannelsWithMessage
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
