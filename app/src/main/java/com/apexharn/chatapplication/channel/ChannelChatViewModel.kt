package com.apexharn.chatapplication.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apexharn.chatapplication.Repository.ChannelChatRepo
import com.apexharn.chatapplication.db.channeldb.ChannelMessage
import com.apexharn.chatapplication.firebase.FirestoreDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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


    suspend fun loadOldMessageDB(latestTime: Long): List<ChannelMessage>{
        val messageList =  FirestoreDb.getOlderChannelChats(id,latestTime)
        var result = messageList.mapNotNull { document ->
            ChannelMessage(document["messageId"].toString(), document["channelId"].toString(), document["messageType"].toString(), document["message"].toString(),
                document["timestamp"].toString().toLong())
        }
//        _chatListState.value = _chatListState.value + result
        println("joined channel result size = ${messageList.size} $latestTime ")
        return result
    }
    suspend fun loadOldMessage(): List<ChannelMessage> {
        println("loading more data")
        var temp = mutableListOf<ChannelMessage>()

        var list = withContext(Dispatchers.IO) {
            repository.getOlderMessages() ?: emptyList()
        }
        _chatListState.value += list
        return list
//        viewModelScope.launch {
//            repository.getOlderMessages()?.forEach(){
//                temp.add(it)
//            }

//        }
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




