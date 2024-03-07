package com.example.chatapplication.ChatPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.Constants
import com.example.chatapplication.Repository.ChatRepository
import com.example.chatapplication.db.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChatViewModel( var  senderId: String, private val repository: ChatRepository) : ViewModel() {
    private var _chatListState = MutableStateFlow<List<Message>>(emptyList())
    val chatListState: StateFlow<List<Message>> = _chatListState

    init {
        viewModelScope.launch {
        repository.messages.collect {list->


            _chatListState.value =  list
        }
    }
    }


    fun addMessage(message: Message) {
        viewModelScope.launch {
            repository.insert(message)
        }
    }

    fun loadOldMessage() {
        println("loading more data")
        viewModelScope.launch {
            var temp = mutableListOf<Message>()
           repository.getOlderMessages()?.forEach(){
               temp.add(it)
           }
            _chatListState.value = _chatListState.value + temp
        }
    }
}




class ChatViewModelFactory(private val initialValue: String, private val repository: ChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(initialValue, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }


}

