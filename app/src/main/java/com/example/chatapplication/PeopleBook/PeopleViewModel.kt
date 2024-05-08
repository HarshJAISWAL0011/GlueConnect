package com.example.chatapplication.PeopleBook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
 import com.example.chatapplication.Repository.ConversationRepository
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Sender
import com.example.util.SendersWithLastMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch


class PeopleViewModel (val database: ChatDatabase, private var conversationRepo: ConversationRepository): ViewModel() {
    private var _peopleListState = MutableStateFlow<List<SendersWithLastMessage>>(emptyList())
    var peopleListState: StateFlow<List<SendersWithLastMessage>> = _peopleListState
    init {
        viewModelScope.launch {

            conversationRepo.peopleList.conflate().collect{list->
                _peopleListState.value = list.toList().reversed()
                println("PeopleList in viewmodel "+ list)
            }
        }
    }

    fun refreshData(){
        viewModelScope.launch {
            conversationRepo.resetList().conflate().collect {
                _peopleListState.value = it.reversed();
            }
        }
    }

    fun resetNewMessageCount(sender: SendersWithLastMessage){
        val resetData =Sender(sender.id,sender.profile_url,sender.name,sender.email,0)
        viewModelScope.launch { conversationRepo.resetNewMessageCount(resetData) }
    }


}


class PeopleViewModelFactory(private var database: ChatDatabase, private var conversationRepo: ConversationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeopleViewModel::class.java)) {
            return PeopleViewModel(database, conversationRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
