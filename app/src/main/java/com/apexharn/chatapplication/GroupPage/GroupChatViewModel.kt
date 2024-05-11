package com.apexharn.chatapplication.GroupPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apexharn.chatapplication.Repository.GroupChatRepo
import com.apexharn.chatapplication.db.groupdb.GroupMessage
import com.apexharn.util.GroupMessageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class GroupChatViewModel ( var  groupId: String, private val repository: GroupChatRepo) : ViewModel() {
    private var _chatListState = MutableStateFlow<List<GroupMessageData>>(emptyList())
    val chatListState: StateFlow<List<GroupMessageData>> = _chatListState

    init {
        viewModelScope.launch {
            repository.messages.collect {list->


                _chatListState.value =  list
            }
        }
    }


    fun addMessage(message: GroupMessage) {
        viewModelScope.launch {
            repository.insert(message)
        }
    }

    fun updateMessage(message: GroupMessage) {
        viewModelScope.launch {
            repository.update(message)
        }
    }

    fun loadOldMessage() {
        println("loading more data")
        viewModelScope.launch {
            var temp = mutableListOf<GroupMessageData>()
            repository.getOlderMessages()?.forEach(){
                temp.add(it)
            }
            _chatListState.value = _chatListState.value + temp
        }
    }
}




class GroupChatVMFactory(private val initialValue: String?, private val repository: GroupChatRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupChatViewModel::class.java)) {
            return GroupChatViewModel(initialValue?:"", repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }


}

