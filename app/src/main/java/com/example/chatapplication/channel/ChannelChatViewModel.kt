package com.example.chatapplication.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.Repository.ChannelChatRepo
import com.example.chatapplication.Repository.ChannelRepo
import com.example.chatapplication.Repository.GroupChatRepo
import com.example.chatapplication.db.channeldb.ChannelMessage
import com.example.chatapplication.db.groupdb.GroupMessage
import com.example.util.GroupMessageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChannelChatViewModel( var  id: String, private val repository: ChannelChatRepo) : ViewModel() {
    private var _chatListState = MutableStateFlow<List<ChannelMessage>>(emptyList())
    val chatListState: StateFlow<List<ChannelMessage>> = _chatListState

    init {
        viewModelScope.launch {
            repository.messages.collect {list->
                _chatListState.value =  list
            }
        }
    }


    fun loadOldMessage() {
        println("loading more data")
        viewModelScope.launch {
            var temp = mutableListOf<ChannelMessage>()
            repository.getOlderMessages()?.forEach(){
                temp.add(it)
            }
            _chatListState.value = _chatListState.value + temp
        }
    }

}





class ChannelChatVMFactory(private val initialValue: String?, private val repository: ChannelChatRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChannelChatViewModel::class.java)) {
            return ChannelChatViewModel(initialValue?:"", repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }


}




