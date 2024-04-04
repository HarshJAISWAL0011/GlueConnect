package com.example.chatapplication.InfoPage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
 import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.chatapplication.InfoPage.ui.theme.ChatApplicationTheme
import com.example.chatapplication.R
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Sender
import com.example.chatapplication.db.channeldb.ChannelDatabase
import com.example.chatapplication.db.groupdb.GroupDatabase
import com.example.chatapplication.db.groupdb.GroupMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfoActivity : ComponentActivity() {
    var id: String? = null
    var type: String? = null
    lateinit var groupDatabase: GroupDatabase
    lateinit var chatDatabase: ChatDatabase
    lateinit var channelDatabase: ChannelDatabase
    lateinit var sender: MutableState<Sender?>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        type = intent.getStringExtra("type")
        id = intent.getStringExtra("id")?:""

        setContent {
            ChatApplicationTheme {
                // A surface container using the 'background' color from the theme
                val gradientColors = listOf(
                     colorResource(id = R.color.primary).copy(alpha = 0.4f),
                    colorResource(id = R.color.primary_light).copy(alpha = 0.3f)
                )
                val navController = rememberNavController()

                // Create a vertical gradient brush
                val brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = 1000f
                )


                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush),
                    color = Color.Transparent,

                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (type == "group") {
                            var memberList = remember {
                                mutableListOf<GroupMember>()
                            }
                            var name by remember { mutableStateOf("") }
                            var totalMember by remember { mutableStateOf(0) }
                            var context = LocalContext.current
                            val coroutineScope = rememberCoroutineScope()
                            LaunchedEffect(Unit) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    groupDatabase = GroupDatabase.getDatabase(context)
                                    memberList.clear()
                                    memberList.addAll(
                                        groupDatabase.groupMemberDao().getMembers(id ?: "")
                                    )
                                    val group = groupDatabase.groupDao().getGroupFromId(id ?: "")
                                    name = group?.groupName ?: ""
                                    totalMember =
                                        groupDatabase.groupMemberDao().getMembers(id ?: "").size
                                            ?: 0

                                }
                            }


                            DetailScreen(
                                memberList ?: emptyList(),
                                name ?: "null",
                                totalMember.toString(),
                                type!!,
                                {},
                                { super.onBackPressed() }, "")

                        } else if (type == "individual") {

                            var name by remember { mutableStateOf("") }
                            var phone by remember { mutableStateOf("") }
                            var context = LocalContext.current
                            sender = remember { mutableStateOf(null) }
                            val senderState = remember { mutableStateOf<Sender?>(null) }
                            val coroutineScope = rememberCoroutineScope()

                            LaunchedEffect(Unit) {
                                coroutineScope.launch(Dispatchers.IO) {

                                    chatDatabase = ChatDatabase.getDatabase(context)
                                    var senderFlow =
                                        chatDatabase.senderDao().getSenderListener(id ?: "")
                                    senderFlow.collect { it ->
                                        senderState.value = it
                                        sender.value = it
                                        name = it?.name ?: ""
                                        phone = it?.email ?: ""
                                    }
                                    name = sender.value?.name ?: ""
                                    phone = sender.value?.email ?: ""
                                }
                            }

                            DetailScreen(emptyList(), name, phone, type!!, {
                                changeName(it)
                            }, { super.onBackPressed() },"")


                            Row (verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.BottomCenter)) {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = "",
                                    tint = Color.Gray.copy(0.6f),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Messages are secured",
                                    modifier = Modifier.padding(10.dp),
                                    fontWeight = FontWeight.W500,
                                    fontFamily = FontFamily(
                                        Font(R.font.robot_slab)
                                    ),
                                    color = Color.Black.copy(0.6f)
                                )
                            }

                            } else if (type == "channel") {
                            var name by remember { mutableStateOf("") }
                            var totalMember by remember { mutableStateOf(0) }
                            var description by remember { mutableStateOf("") }
                            var context = LocalContext.current
                            val coroutineScope = rememberCoroutineScope()
                            LaunchedEffect(Unit) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    channelDatabase =ChannelDatabase.getDatabase(context)

                                    val channel = channelDatabase.channelsDao().getChannel(id ?: "")
                                    description = channel?.description ?:""
                                    name = channel?.name ?: ""
                                    totalMember =
                                        channel?.followers ?: 0


                                }
                            }
                            DetailScreen(
                                 emptyList(),
                                name,
                               "$totalMember following",
                                type!!,
                                {},
                                { super.onBackPressed() },
                                description)
                        }


                    }
                }
            }
        }



    }

     fun changeName(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if(sender != null)
                sender.value?.copy(name = name)?.let { chatDatabase.senderDao().updateSender(it) }
        }
    }
}

