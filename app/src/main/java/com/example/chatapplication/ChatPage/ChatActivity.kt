package com.example.chatapplication.ChatPage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModelProvider
import androidx.room.withTransaction
import com.example.Constants.MY_ID
import com.example.chatapplication.GroupPage.GroupChatVMFactory
import com.example.chatapplication.GroupPage.GroupChatViewModel
import com.example.chatapplication.InfoPage.InfoActivity
import com.example.chatapplication.ui.theme.ChatApplicationTheme
import com.example.chatapplication.R
import com.example.chatapplication.Repository.ChannelChatRepo
import com.example.chatapplication.Repository.ChatRepository
import com.example.chatapplication.Repository.GroupChatRepo
import com.example.chatapplication.channel.ChannelChatVMFactory
import com.example.chatapplication.channel.ChannelChatViewModel
import com.example.chatapplication.channel.ChannelVMFactory
import com.example.chatapplication.channel.ChannelViewModel
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.channeldb.ChannelDatabase
import com.example.chatapplication.db.groupdb.GroupDatabase
import com.example.chatapplication.db.groupdb.GroupMessage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChatActivity : ComponentActivity() {

    lateinit var chatViewModel: ChatViewModel
    lateinit var groupViewModel: GroupChatViewModel
    lateinit var channelViewModel: ChannelChatViewModel
    lateinit var database: ChatDatabase
    lateinit var groupDatabase: GroupDatabase
    lateinit var channelDatabase: ChannelDatabase
    lateinit var selectedMessageList: MutableList<Message>
    lateinit var selectedMessageListSize: MutableState<Int>
    lateinit var showActions: MutableState<Boolean>
    lateinit var defaultText: MutableState<String>
     var id: String? = null
    var type: String? = "individual";
    var displayName: String? = "";


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

         id = intent.getStringExtra("id")
        type = intent.getStringExtra("type")
        displayName = intent.getStringExtra("displayName")

//        println("all ids are $senderId $groupId $displayName $isGroup" )

        database = ChatDatabase.getDatabase(this)
        groupDatabase = GroupDatabase.getDatabase(this)
        channelDatabase = ChannelDatabase.getDatabase(this)

        var  chatViewModelFactory =
            ChatViewModelFactory(id, ChatRepository(senderId = id?:"", database))
        chatViewModel = ViewModelProvider(this, chatViewModelFactory).get(ChatViewModel::class.java)

        var  groupChatVMFactory =
            GroupChatVMFactory(id, GroupChatRepo(id?:"" , groupDatabase, displayName?:""))
        groupViewModel = ViewModelProvider(this, groupChatVMFactory).get(GroupChatViewModel::class.java)

        var  channelVMFactory =
            ChannelChatVMFactory(id, ChannelChatRepo(id?:"" , channelDatabase, displayName?:""))
        channelViewModel = ViewModelProvider(this, channelVMFactory).get(ChannelChatViewModel::class.java)

        setContent {
            ChatApplicationTheme {

                val systemUiController = rememberSystemUiController()
                val statusBarColor = colorResource(id = R.color.primary)
                showActions = remember { mutableStateOf(false) }
                defaultText = remember { mutableStateOf("") }
                selectedMessageList = remember { mutableStateListOf<Message>() }
                selectedMessageListSize = remember { mutableStateOf(selectedMessageList.size) }

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = statusBarColor,
                        darkIcons = false
                    )
                }
                BackHandler(enabled = selectedMessageList.size > 0) {
                    selectedMessageList.clear()
                    selectedMessageListSize.value = 0;
                    showActions.value = false
                }

                BackHandler(!defaultText.value.equals("")) {
                    defaultText.value = ""
                    selectedMessageList.clear()
                    selectedMessageListSize.value = 0;
                    showActions.value = false
                }



                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(id = R.color.background)
                ) {
                    Scaffold(
                        topBar = {

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = colorResource(id = R.color.background))
                                    .shadow(
                                        45.dp,
                                        RoundedCornerShape(bottomEnd = 20.dp),
                                        spotColor = (Color.Blue)
                                    ),
                                color = colorResource(id = R.color.primary),

                                shape = RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp)
                            ) {
                                if (!showActions.value)
                                    TopBarDesign()
                                else
                                    LongClickTopBarDesign(
                                        selectedMessageListSize.value == 1 && selectedMessageList.get(0).isReceived == 1,
                                        onActionClicked = { /*TODO*/ },
                                        {
                                            selectedMessageList.clear()
                                            println("size of list = " + selectedMessageList.size)
                                            selectedMessageListSize.value = 0;
                                            showActions.value = false

                                        },

                                    )
                            }
                        }
                    ) {
                        Surface(modifier = Modifier.padding(it)) {
                            if(type == "group" && id != null){
                                GroupChatContentList(this, groupViewModel, selectedMessageListSize.value,
                                                 {
                                                     // add in list on click
                                                     selectedMessageList.add(it)
                                                     selectedMessageListSize.value++
                                                 },
                                                 {
                                                     // remove from list on click
                                                     selectedMessageList.remove(it)
                                                     if (selectedMessageList.size == 0)
                                                         showActions.value = false
                                                     selectedMessageListSize.value--

                                                 },
                                                 {
                                                     // Long click
                                                     selectedMessageList.add(it)
                                                     showActions.value = true
                                                     selectedMessageListSize.value++
                                                 }, defaultText.value,
                                                 {
                                                     // update message
                                                     GlobalScope.launch {
                                                         if (selectedMessageListSize.value > 0) {

                                                             var grpMsg = GroupMessage(it.messageId,it.senderId,it.messageType?:""
                                                                                       ,it.message,it.isReceived,it.receiveTime,it.sentTime,id!!)
                                                             groupViewModel.updateMessage(grpMsg)
                                                         } else {
                                                                // insert new message
                                                             println("message id = ${it.toString()}")
                                                             groupViewModel.addMessage( GroupMessage(it.messageId,"You",it.messageType?:""
                                                                                                     ,it.message,it.isReceived,it.receiveTime,it.sentTime,id!!))
                                                         }


                                                         selectedMessageList.clear()
                                                         selectedMessageListSize.value = 0;
                                                         showActions.value = false
                                                         defaultText.value = ""
                                                     }
                                                 })
                            }
                            else if(type == "individual") {
                                ChatsContentList(this, chatViewModel, selectedMessageListSize.value,
                                                 {
                                                     // add in list on click
                                                     selectedMessageList.add(it)
                                                     selectedMessageListSize.value++
                                                 },
                                                 {
                                                     // remove from list on click
                                                     selectedMessageList.remove(it)
                                                     if (selectedMessageList.size == 0)
                                                         showActions.value = false
                                                     selectedMessageListSize.value--

                                                 },
                                                 {
                                                     // Long click
                                                     selectedMessageList.add(it)
                                                     showActions.value = true
                                                     selectedMessageListSize.value++
                                                 }, defaultText.value,
                                                 {
                                                     // update message
                                                     GlobalScope.launch {
                                                         if (selectedMessageListSize.value > 0) {
                                                             var msg = selectedMessageList.get(0)
                                                                 .copy(message = it.message)
                                                             chatViewModel.updateMessage(msg)
                                                         } else {

                                                             println("message id = ${it.messageId} \n ${it.toString()}")
                                                             chatViewModel.addMessage(it)
                                                         }


                                                         selectedMessageList.clear()
                                                         selectedMessageListSize.value = 0;
                                                         showActions.value = false
                                                         defaultText.value = ""
                                                     }
                                                 })

                            }
                            else if(type == "channel"){
                                ChannelContentList(this, channelViewModel, selectedMessageListSize.value,)

                            }
                        }
                    }

                }
            }
        }
    }


    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun DeleteOptions(onDismiss: () -> Unit) {

        AlertDialog(
            onDismissRequest = onDismiss,

            ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(color = colorResource(id = R.color.primary_light))
                    .padding(15.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Image(
                    painter = painterResource(id = R.drawable.delete_illus),
                    contentDescription = "delete",
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Are you sure to: ", color = colorResource(id = R.color.black_variant))
                TextButton(onClick = {
                    deleteMessage(true)
                    onDismiss()
                }, modifier = Modifier.align(Alignment.End), shape = RoundedCornerShape(10.dp)) {
                    Text(text = "Delete for me", color = colorResource(id = R.color.primary))
                }
                TextButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Delete for everyone", color = colorResource(id = R.color.primary))
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Cancel", color = colorResource(id = R.color.primary))
                }

            }

        }
    }

    fun deleteMessage(isForMe: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            if (type == "group" && id != null) {

                if (isForMe) {
                    selectedMessageList.forEach {
                        database.withTransaction {
                            var msg = GroupMessage(it.messageId,it.senderId,it.messageType?:"",it.message,
                                                   it.isReceived,it.receiveTime,it.sentTime,id!!)
                            groupDatabase.groupMessageDao().deleteMessage(msg)
                        }
                    }
                    selectedMessageList.clear()
                    println("size of list = " + selectedMessageList.size)
                    selectedMessageListSize.value = 0;
                    showActions.value = false

                } else {
                    val lastMsg = selectedMessageList.get(0);
                    var msg = GroupMessage(lastMsg.messageId,lastMsg.senderId,lastMsg.messageType?:"","",
                                           lastMsg.isReceived,lastMsg.receiveTime,lastMsg.sentTime,id!!)
                    groupViewModel.updateMessage(msg)
                    selectedMessageList.clear()
                    selectedMessageListSize.value = 0;
                    showActions.value = false
                }

            } else  if (type == "individual" && id != null) {
                if (isForMe) {
                    selectedMessageList.forEach {
                        database.withTransaction {
                            database.messageDao().deleteMessage(it)
                        }
                    }
                    selectedMessageList.clear()
                    println("size of list = " + selectedMessageList.size)
                    selectedMessageListSize.value = 0;
                    showActions.value = false

                } else {
                    chatViewModel.updateMessage(selectedMessageList.get(0).copy(message = ""))
                    selectedMessageList.clear()
                    selectedMessageListSize.value = 0;
                    showActions.value = false
                }
            }
        }
    }



    override fun onDestroy() {
        GlobalScope.launch {
            if (type == "group") {
                    val groupObj = groupDatabase.groupDao().getGroupFromId(id!!)
                    if (groupObj != null) {
                        groupDatabase.groupDao().updateGroup(groupObj.copy(newMessageCount = 0))
                    }
            } else if(type == "individual"){
                val senderObj = database.senderDao().getSender(id!!)
                if (senderObj != null) {
                    database.senderDao().updateSender(senderObj.copy(newMessageCount = 0))
                }
            }
        }
        super.onDestroy()
    }

    @Composable
    private fun TopBarDesign() {
        val intent = Intent(this,InfoActivity::class.java)

        Column() {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 10.dp, top = 15.dp, bottom = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.KeyboardArrowLeft,
                    contentDescription = "",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(end = 15.dp)
                        .size(28.dp)
                        .clickable { super.onBackPressed() }
                )
                Image(
                    painter = painterResource(id = R.drawable.profile_placeholder),
                    contentDescription = "",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Gray)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.clickable {
                    intent.putExtra("type", type)
                    intent.putExtra("id", id)


                    startActivity(intent)
                }) {
                    displayName?.let {
                        Text(
                            text = it, fontSize = 17.sp,
                            color = colorResource(id = R.color.white_variant)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Online", fontSize = 12.sp,
                        color = colorResource(id = R.color.white_variant)
                    )

                }

            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun LongClickTopBarDesign(
        showEditCopy: Boolean,
        onActionClicked: (String) -> Unit,
        onBackPressed: () -> Unit,

    ) {
        var expanded by remember { mutableStateOf(false) }
        var showDeleteOptions by remember { mutableStateOf(false) }

        if (showDeleteOptions)
            DeleteOptions {
                showDeleteOptions = false
            }

            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(id = R.color.primary)),
                title = { Text(text = "") },
                actions = {

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        if (showEditCopy) {
                            IconButton(onClick = { onActionClicked("copy") }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.copy),
                                    contentDescription = "Copy"
                                )
                            }
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options"
                                )
                            }
                            DropdownMenu(
                                modifier = Modifier
                                    .background(color = colorResource(id = R.color.background)),
                                expanded = expanded,
                                offset = DpOffset(x=0.dp,y = 20.dp),
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        defaultText.value = selectedMessageList.get(0).message
                                        expanded = false
                                    },
                                    text = {
                                        Text("Edit", color = Color.Black)
                                    }
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        showDeleteOptions = true
                                        expanded = false
                                    },
                                    text = {
                                        Text("Delete", color = Color.Black)
                                    }
                                )


                            }
                        } else {
                            IconButton(onClick = { showDeleteOptions = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )


        }


}