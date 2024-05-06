package com.example.chatapplication.ChatPage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.graphics.drawable.shapes.Shape
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.Constants.EXT_DIR_IMAGE_LOCATION
import com.example.Constants.MESSAGE_TYPE_AUDIO
import com.example.Constants.MESSAGE_TYPE_IMAGE
import com.example.Constants.MY_ID
import com.example.chatapplication.GroupPage.GroupChatViewModel
import com.example.chatapplication.R
import com.example.chatapplication.channel.ChannelChatViewModel
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.channeldb.ChannelDatabase
import com.example.chatapplication.db.channeldb.ChannelMessage
import com.example.chatapplication.firebase.FirestoreDb
import com.example.chatapplication.firebase.FirestoreDb.getChannelChats
import com.example.chatapplication.firebase.FirestoreDb.getOlderChannelChats
import com.example.util.util
import com.example.util.util.getMinSecond
import com.example.util.util.saveImageToExternalStorage
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TAG = "ChatScreen"
lateinit var bottomSheetVisible:MutableState<Boolean>
var userId: String = ""
var audioLocation: File? = null
private lateinit var context: Context
private var recorder: MediaRecorder? = null
private var sender_group_id: String? = null


@Composable
fun PublicChannel(context2: Context, id: String, onJoinClicked: ()->Unit ){

    context = context2
    var chatList  = remember { mutableStateListOf<ChannelMessage>() }
    var latestTime by remember{ mutableStateOf( System.currentTimeMillis())}
    var isDataProcessing by remember{ mutableStateOf( false)}

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            isDataProcessing = true
           val snapShotList = getOlderChannelChats(id, latestTime)
           var result = snapShotList.mapNotNull { document ->
               ChannelMessage(document["messageId"].toString(), document["channelId"].toString(), document["messageType"].toString(), document["message"].toString(),
                   document["timestamp"].toString().toLong())
             }
            if(snapShotList.isNotEmpty())
                latestTime = result[result.size-1].sentTime

            chatList.addAll(result)
            isDataProcessing =false
        }
    }
    val listState = rememberLazyListState()


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(id = R.color.background),

        ) {
        Column(modifier = Modifier
            .fillMaxHeight()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 5.dp)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                state = listState,
                reverseLayout = true
            ) {
                itemsIndexed(chatList) {idx,it->

                    ChatBubble(Message(it.messageId,it.channelId,it.messageType,it.message,1,it.sentTime),"", false, context,
                        {
                            // onclick

                        }){
                        // onLongClick
                    }

                    LaunchedEffect(idx == chatList.size -1  ) {
                        // pagination
                        if (!isDataProcessing) {
                            isDataProcessing = true
                            CoroutineScope(Dispatchers.IO).launch {
                                val snapShotList = getOlderChannelChats(id, latestTime)
                                var result = snapShotList.mapNotNull { document ->
                                    ChannelMessage(
                                        document["messageId"].toString(),
                                        document["channelId"].toString(),
                                        document["messageType"].toString(),
                                        document["message"].toString(),
                                        document["timestamp"].toString().toLong()
                                    )
                                }
                                if (result.isNotEmpty()) {
                                    latestTime = result[result.size - 1].sentTime
                                    println("getChannelChats data updated $latestTime")
                                }
                                chatList.addAll(result)
                                isDataProcessing = false
                            }
                        }
                    }
                }
            }

            Button(onClick = {
                onJoinClicked()
            },
                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.primary)),
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    ) {
                Text(text = "Join", color = Color.White)
            }
        }

    }
}


@Composable
fun JoinedChannelChats(
    context2: Context, viewModel: ChannelChatViewModel,id:String ){
    context = context2
    sender_group_id = viewModel.id
    var chatList = viewModel.chatListState.collectAsState()
    var list = remember { mutableStateListOf<ChannelMessage>() }
    var latestTime = remember{System.currentTimeMillis()}
    var dbHaveData by remember {
        mutableStateOf(true)
    }
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        list.addAll(chatList.value)
        println("joined channel ${chatList.value}")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(id = R.color.background),

        ) {
        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp,top=8.dp, bottom = 5.dp)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                state = listState,
                reverseLayout = true
            ) {
                itemsIndexed(chatList.value) {idx,it->

                    var bubbleSelected by remember {
                        mutableStateOf(false)
                    }
                    val msg = Message(it.messageId,it.channelId,it.messageType,it.message,1,it.sentTime)
                    ChatBubble(msg,"", bubbleSelected, context,
                        {// onclick
                        }){
                        // onLongClick
                    }
                    LaunchedEffect(idx == chatList.value.size -1 ) { // pagination

                        CoroutineScope(Dispatchers.IO).launch {
                            if (list.size > 0) {
                                latestTime = list.get(list.size - 1).sentTime
                            }

                            list.clear()
                            if(!dbHaveData) return@launch
                            val oldMessage = viewModel.loadOldMessage()

                            list.addAll(oldMessage)
                            Log.d(TAG,"message list size from local DB = ${oldMessage.size}" )
//                            println("joined channel 2nd last ${idx} &&& ${oldMessage.size}")
                        }
                    }

                    }
                }
            LaunchedEffect( list.size == 0 && dbHaveData) {
                // get from db
               val oldMessage = viewModel.loadOldMessageDB(latestTime)
                if(oldMessage.isEmpty()) dbHaveData = false
                list.addAll(oldMessage)

                CoroutineScope(Dispatchers.IO).launch {
                    oldMessage.forEach {
                        try {
                            ChannelDatabase.getDatabase(context).channelMsgDao().insertMessage(it)
                        } catch (e: SQLiteConstraintException) {
                            println("SQLiteConstraintException ${e.printStackTrace()}")
                        }
                    }
                }
                Log.d(TAG,"message list size from firestore = ${oldMessage.size}" )
//                println("ChatScre ${oldMessage.size} && $dbHaveData")
            }

        }
    }

}

@Composable
fun MyChannels(
    context2: Context, viewModel: ChannelChatViewModel, messageListSize:Int,addSelected:(data: Message)->Unit,
    removeSelected:(data:Message)->Unit, onLongClick:(data:Message)->Unit, defaultText: String,
    onMessageSend:(message:Message)->Unit){

    context = context2

    context = context2
    sender_group_id = viewModel.id
    val chatList = viewModel.chatListState.collectAsState()
    val listState = rememberLazyListState()
    var islongClickEnable by remember {
        mutableStateOf(false)
    }
    if(messageListSize == 0) {
        islongClickEnable = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(id = R.color.background),

        ) {
        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp,top=8.dp, bottom = 5.dp)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                state = listState,
                reverseLayout = true
            ) {
                itemsIndexed(chatList.value) {idx,it->

                    var bubbleSelected by remember {
                        mutableStateOf(false)
                    }
                    val msg = Message(it.messageId,it.channelId,it.messageType,it.message,0,it.sentTime)
                    ChatBubble(msg,"", bubbleSelected, context,
                        {
                            // onclick
                            if(islongClickEnable) {
                                if (bubbleSelected) {
                                    removeSelected(msg)
                                } else {
                                    addSelected(msg)
                                }
                                bubbleSelected = !bubbleSelected
                            }else{
                                bubbleSelected = false
                            }
                        }){
                        // onLongClick
                        islongClickEnable = true
                        onLongClick(msg)
                        bubbleSelected = true
                    }

                    LaunchedEffect(idx == chatList.value.size -5) { // pagination
                        viewModel.loadOldMessage()
                    }
                }
            }
            sendMessageBox(defaultText, onSend = onMessageSend)
        }
    }

}

@Composable
fun GroupChatContentList(  context2: Context, viewModel: GroupChatViewModel, messageListSize:Int, addSelected:(data: Message)->Unit,
                           removeSelected:(data:Message)->Unit, onLongClick:(data:Message)->Unit, defaultText: String,
                           onMessageSend:(message:Message)->Unit){

    context = context2
    sender_group_id = viewModel.groupId

    val chatList = viewModel.chatListState.collectAsState()
    val listState = rememberLazyListState()
    var islongClickEnable by remember {
        mutableStateOf(false)
    }
    if(messageListSize == 0) {
        islongClickEnable = false
        println("size of list islongclick enabled = $islongClickEnable")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(id = R.color.background),

        ) {
        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp,top=8.dp, bottom = 5.dp)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                state = listState,
                reverseLayout = true
            ) {
                itemsIndexed(chatList.value) {idx,it->
                    var bubbleSelected by remember {
                        mutableStateOf(false)
                    }

                    if(!islongClickEnable) bubbleSelected =false
                    var msg = Message(it.messageId,sender_group_id?:"",it.messageType,it.message,it.isReceived,it.receiveTime,it.sentTime)
                    ChatBubble(msg,it.senderName?:"", bubbleSelected, context,{
                        // onclick
                        if(islongClickEnable) {
                            if (bubbleSelected) {
                                removeSelected(msg)
                            } else {
                                addSelected(msg)
                            }
                            bubbleSelected = !bubbleSelected
                        }else{
                            bubbleSelected = false
                        }
                    }){
                        // onLongClick
                        islongClickEnable = true
                        onLongClick(msg)
                        bubbleSelected = true
                    }

                    if(idx == chatList.value.size -5) { // pagination
                        viewModel.loadOldMessage()
                    }
                }
            }

            sendMessageBox(defaultText, onSend = onMessageSend)
        }
    }
}

@Composable
fun ChatsContentList(
    context2: Context, chatViewModel: ChatViewModel,receiverId: String, messageListSize:Int, addSelected:(data: Message)->Unit,
    removeSelected:(data:Message)->Unit, onLongClick:(data:Message)->Unit, defaultText: String,
    onMessageSend:(message:Message)->Unit ){

    context = context2
    userId =receiverId;
    sender_group_id = chatViewModel.senderId
    val chatList = chatViewModel.chatListState.collectAsState()
    val listState = rememberLazyListState()
    var islongClickEnable by remember {
        mutableStateOf(false)
    }
    if(messageListSize == 0) {
        islongClickEnable = false
        println("size of list islongclick enabled = $islongClickEnable")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(id = R.color.background),

    ) {
//        Box(modifier = Modifier.fillMaxSize()){
//            Image(painter = painterResource(id = R.drawable.confetti), contentDescription ="",
//                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
//        }
        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp,top=8.dp, bottom = 5.dp)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                state = listState,
                reverseLayout = true
            ) {
                itemsIndexed(chatList.value) {idx,it->
                    var bubbleSelected by remember {
                        mutableStateOf(false)
                    }

                    if(islongClickEnable == false) bubbleSelected =false
                    ChatBubble(it,"", bubbleSelected, context,{
                        // onclick
                        if(islongClickEnable) {
                            if (bubbleSelected) {
                                removeSelected(it)
                            } else {
                                addSelected(it)
                            }
                            bubbleSelected = !bubbleSelected
                        }else{
                            bubbleSelected = false
                        }
                    }){
                        // onLongClick
                        islongClickEnable = true
                        onLongClick(chatList.value.get(idx))
                        bubbleSelected = true}

                    if(idx == chatList.value.size -5) { // pagination
                        chatViewModel.loadOldMessage()
                    }
                }
            }
            var dt by remember {
                mutableStateOf("")
            }
            dt = defaultText
            sendMessageBox(defaultText, onSend = onMessageSend)
        }
    }

}

@Composable
fun sendMessageBox(defaultText:String,onSend: (msg: Message)->Unit){
    val containerColor = colorResource(id = R.color.textfeild_bg_color)
    var messageText by remember { mutableStateOf(defaultText) }
     bottomSheetVisible = remember { mutableStateOf(false) }
     var showAudio by remember { mutableStateOf(true) }
    var isRecording by remember { mutableStateOf(false) }



    showBottomSheet(onSend, context )

    LaunchedEffect(defaultText){
        messageText = defaultText
    }

    Box(modifier = Modifier
        .clip(RoundedCornerShape(18.dp))
        .background(color = containerColor)

    ) {
        Row (){

            val containerColor1 = containerColor

            TextField(
                value = messageText,
                onValueChange = {
                    messageText = it
                    if (it.length > 0)
                        showAudio =false
                    else
                        showAudio = true
                },
                maxLines = 5,
                placeholder = { Text("Enter your message here")},
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(10.dp)
                    .fillMaxWidth()
                ,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = containerColor1,
                    unfocusedContainerColor = containerColor1,
                    disabledContainerColor = containerColor1,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
                    cursorColor = colorResource(id = R.color.primary)
                ),

                trailingIcon = {
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.attach2),
                            contentDescription = "",
                            modifier = Modifier
                                .size(32.dp)
                                .scale(scaleX = -1f, scaleY = 1f)
                                .rotate(0f)
                                .padding(5.dp)
                                .align(Alignment.CenterVertically)
                                .clickable {
                                    bottomSheetVisible.value = true
                                },
                            tint = colorResource(id = R.color.primary_variant)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colorResource(id = R.color.primary))
                                .align(Alignment.CenterVertically)
                                .clickable {
                                    if (messageText.isNotEmpty()) {
                                        val time = System.currentTimeMillis()
                                        var msg = Message(
                                            MY_ID + time,
                                            sender_group_id ?: "",
                                            "text",
                                            messageText,
                                            0,
                                            time,
                                            time
                                        )
                                        onSend(msg)
                                    }
                                    messageText = ""
                                }

                        ) {
                            Icon(
                                Icons.Outlined.Send,
                                contentDescription = "",
                                modifier = Modifier
                                    .rotate(-30f)
                                    .size(45.dp)
                                    .padding(10.dp),
                                tint = Color.White

                            )
                        }
                    }
                },

                    leadingIcon = {
                        if (showAudio) {
                        IconButton(onClick = {
                            isRecording = !isRecording

                            val rootDir = File(Environment.getExternalStorageDirectory(), "/Chat/Audios")
                            rootDir.mkdirs()


                            if (isRecording) {
                                val audioFileName = "${MY_ID}_${ System.currentTimeMillis()}.wav"
                                 audioLocation = File(rootDir,audioFileName)
                                startRecording(audioLocation!!)
                            } else {
                                stopRecording()
                                val time = System.currentTimeMillis()

                                val msg = Message("$MY_ID$time",
                                    sender_group_id?:"",MESSAGE_TYPE_AUDIO,audioLocation.toString()
                                ,0,time,time)
                                onSend(msg)
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.audio),
                                contentDescription = "",
                                tint = colorResource(
                                    id = R.color.primary
                                ),
                                modifier =  Modifier.size(26.dp)
                            )
                        }
                    }else{
                            IconButton(onClick = {}) {
                                Icon(
                                    painter = painterResource(id = R.drawable.keyboard),
                                    contentDescription = "",
                                    tint = colorResource(
                                        id = R.color.primary
                                    ),
                                    modifier =  Modifier.size(26.dp)
                                )
                            }
                        }
                }
            )

        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(messageObj: Message,senderName: String,isBubbleSelected: Boolean,context: Context,onClick:()->Unit,
               onLongClick: () -> Unit, ) {
    var time = util.getHourAndMinuteFromTimestamp(messageObj.sentTime)
    var isReceived = messageObj.isReceived == 1
    var isExceedingOneLine by remember { mutableStateOf(false) }
    var showTimestampOnSameColumn by remember { mutableStateOf(true) }
    var colorBg =
        if (isReceived) colorResource(id = R.color.textfeild_bg_color) else  colorResource(id = R.color.primary)
    var colorText = if (isReceived) Color.Black.copy(0.6f)  else  Color.White.copy(0.9f)
    var colorTimestamp =
        if (isReceived) colorResource(id = R.color.black_variant)  else colorResource(id = R.color.white_variant)
    val startPadding = if (isReceived) 8.dp else  65.dp
    val endPadding = if (isReceived)  65.dp else 8.dp
    val selectedColor =
        if (isBubbleSelected) colorResource(id = R.color.primary_variant).copy(0.7f) else Color.Transparent
    var bubblePadding = PaddingValues(
        start = startPadding, end = endPadding
    )
    var alpha = if (isBubbleSelected) 0.6f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(selectedColor)
            .padding(vertical = 3.dp)
            .alpha(alpha)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bubblePadding),
            horizontalArrangement = if (isReceived) Arrangement.Start else Arrangement.End ,
        ) {

            if (messageObj.messageType == "text") {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 10.dp,
                                topEnd = 10.dp,
                                bottomStart = if (isReceived) 0.dp else 10.dp,
                                bottomEnd = if (isReceived) 10.dp else 0.dp
                            )
                        )
                        .background(colorBg)
                        .padding(start = 8.dp, top = 8.dp, end = 3.dp)
                ) {
                    Column {

                        Row() {
                            if (messageObj.message.isEmpty()) {
                                Row(modifier = Modifier.background(color = colorText)) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = "",
                                        modifier = Modifier
                                            .background(colorBg),
                                        tint = Color.DarkGray
                                    )
                                    Text(
                                        text = "This message is deleted",
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier
                                            .background(colorBg)
                                            .padding(bottom = 8.dp)
                                            .align(Alignment.CenterVertically)
                                            .weight(1f, false),
                                        color = colorText,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                            else {
                                Text(
                                    text = messageObj.message,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .align(Alignment.CenterVertically)
                                        .weight(1f, false),
                                    color = colorText,
                                    onTextLayout = { textLayoutResult ->
                                        var lineCount = textLayoutResult.lineCount
                                        val firstLineEnd = textLayoutResult.getLineEnd(0)
                                        val lastLineEnd =
                                            textLayoutResult.getLineEnd(textLayoutResult.lineCount - 1)
                                        val firstLineLength =
                                            messageObj.message.substring(0, firstLineEnd).length
                                        val lastLineLength = messageObj.message.substring(
                                            firstLineEnd * (lineCount - 1),
                                            lastLineEnd
                                        ).length

                                        isExceedingOneLine = lineCount > 1
                                        if (isExceedingOneLine)
                                            showTimestampOnSameColumn =
                                                firstLineLength - lastLineLength > 8
                                    }
                                )
                            }
                            if (!isExceedingOneLine)
                                Text(
                                    text = time,
                                    modifier = Modifier
                                        .padding(end = 5.dp, start = 5.dp, bottom = 2.dp)
                                        .align(Alignment.Bottom),
                                    fontSize = 10.sp,
                                    color = colorTimestamp
                                )

                        }
                        if (isExceedingOneLine && !showTimestampOnSameColumn) {
                            Text(
                                text = time,
                                modifier = Modifier
                                    .padding(end = 5.dp, start = 5.dp, bottom = 2.dp)
                                    .align(Alignment.End),
                                fontSize = 10.sp,
                                color = colorTimestamp
                            )
                        }
                    }
                    if (showTimestampOnSameColumn && isExceedingOneLine)
                        Text(
                            text = time,
                            modifier = Modifier
                                .padding(end = 5.dp, start = 5.dp, bottom = 5.dp)
                                .align(Alignment.BottomEnd),
                            fontSize = 10.sp,
                            color = colorTimestamp
                        )
                }
            }
            else if (messageObj.messageType == MESSAGE_TYPE_IMAGE ) {

                Box() {
                    Card(
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(context, ShowImage::class.java)
                                intent.putExtra("filepath", messageObj.message)
                                context.startActivity(intent)
                            }
                            .clip(
                                RoundedCornerShape(
                                    topStart = 10.dp,
                                    topEnd = 10.dp,
                                    bottomStart = 10.dp,
                                    bottomEnd = 10.dp
                                )
                            )
                            .background(colorBg)
                            .padding(start = 4.dp, top = 5.dp, end = 4.dp, bottom = 5.dp)
                    ) {

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(messageObj.message))
                                .listener(object : ImageRequest.Listener {
                                    override fun onStart(request: ImageRequest) {
                                        // Image loading started
                                    }

                                    override fun onError(
                                        request: ImageRequest,
                                        result: ErrorResult
                                    ) {
                                        super.onError(request, result)
                                        println("test error while loading image = ${result.throwable.message}")
                                    }

                                    override fun onCancel(request: ImageRequest) {
                                        // Image loading cancelled
                                    }
                                }).build(),
                            contentDescription = "",
                            contentScale = ContentScale.Fit,
                            placeholder = painterResource(id = R.drawable.profile_placeholder),
                            error = painterResource(id = R.drawable.delete_illus),

                            modifier = Modifier.size(width = 200.dp, height = 280.dp)
                        )
                    }
                    
                    Text(text = time, modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 14.dp, bottom = 7.dp),
                        fontSize = 12.sp)
                }
                }
            else if(messageObj.messageType == MESSAGE_TYPE_AUDIO){

                var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
                var audioDuration by remember { mutableStateOf(1) }
                var audioPosition by remember { mutableStateOf(0) }
                var isPlaying by remember { mutableStateOf(false) }
                var currentPosition by remember { mutableStateOf(0) }
                var lottieIteration by remember { mutableStateOf(1) }
                val scope = rememberCoroutineScope()
                var audioBoxColor = if(isReceived)colorResource(id = R.color.textfeild_bg_color) else colorResource(id = R.color.primary_variant)
                var sliderTrackcolor = if(isReceived) colorResource(id = R.color.primary).copy(0.2f) else Color.White.copy(0.15f)
                var iconTint = if(isReceived) colorResource(id = R.color.primary) else Color.White
                var audioTextColor = if(isReceived) colorResource(id = R.color.black_variant) else Color.White
                var lottieFile = if (isReceived) LottieCompositionSpec.RawRes(R.raw.wave_lottie2) else LottieCompositionSpec.RawRes(R.raw.wave_lottie)


                LaunchedEffect(Unit ){
                    try {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(File(messageObj.message).toString())
                            prepare()
                        }
                        audioDuration = mediaPlayer?.duration ?: 0
                        mediaPlayer?.setOnCompletionListener {mp ->
                                isPlaying = false
                                audioPosition = 0
                                mp.seekTo(0)
                            lottieIteration = 0

                        }

                    }catch (e: FileNotFoundException){println("Exception while playing ${e.message}")}
                    catch (e: ClassCastException){println("Exception while playing ${e.message}")}
                    catch (e: IOException){println("Exception while playing ${e.message}")}
                }

                Box(modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .height(62.dp)
                    .background(audioBoxColor)
                    .padding(3.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Row {

                        Card(modifier = Modifier

                            .padding(5.dp)
                            .background(color = audioBoxColor)) {
                            val composition by rememberLottieComposition(lottieFile)


                            LottieAnimation(
                                speed = 1.5f,
                                isPlaying = isPlaying,
                                modifier = Modifier
                                    .background(
                                        color = audioBoxColor
                                    )
                                    .size(44.dp),
                                composition = composition,
                                reverseOnRepeat = true,
                                iterations = 500,


                            )

                        }
                        IconButton(onClick = {

                            if(mediaPlayer == null){
                                Toast.makeText(context,"Something went wrong",Toast.LENGTH_LONG).show()
                            }
                             else if (!isPlaying ) {
                                mediaPlayer?.start()
                                isPlaying = true
                                lottieIteration = Integer.MAX_VALUE
                                audioDuration = mediaPlayer?.duration ?: 0
                                audioPosition = mediaPlayer?.currentPosition ?: 0
                                currentPosition = mediaPlayer?.currentPosition ?: 0

                                scope.launch {
                                    while (isPlaying) {
                                        audioPosition = mediaPlayer?.currentPosition ?: 0
                                        println("Media current pos = ${mediaPlayer?.currentPosition}")
                                        delay(40)
                                    }
                                }
                            } else {
                                mediaPlayer?.pause()
                                isPlaying = false
                                lottieIteration = 0
                            }
                        }
                        ) {
                            if(isPlaying)
                            Icon(painter = painterResource(id = R.drawable.pause), contentDescription ="",
                                tint = iconTint, modifier = Modifier.size(20.dp))
                            else
                                Icon(painter = painterResource(id = R.drawable.play), contentDescription ="",
                                    tint = iconTint, modifier = Modifier.size(20.dp))
                        }
                        Box(modifier = Modifier
                            .height(55.dp)
                            .padding(end = 5.dp)){

                            val pos = if (audioDuration > 0 && audioPosition > 0) {
                                audioPosition.toFloat() / audioDuration
                            } else {
                                0f // Set pos to 0 if audioDuration is 0
                            }

                             Indicator(pos, iconTint,sliderTrackcolor)

                             Text(text =  getMinSecond( audioDuration ) , fontSize = 12.sp, color =  audioTextColor ,fontWeight = FontWeight.W500, modifier = Modifier
                                 .align(
                                     Alignment.BottomStart
                                 )
                                 .padding(start = 5.dp))

                            Text(text = time, fontSize = 12.sp, color =  audioTextColor,fontWeight = FontWeight.W500, modifier = Modifier
                                .align(
                                    Alignment.BottomEnd
                                )
                                .padding(end = 5.dp))
                        }
                    }
                }
            }
            }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
@OptIn( ExperimentalMaterial3Api::class)
fun showBottomSheet(onSend: (msg: Message) -> Unit, context: Context) {
    val sheetState = rememberModalBottomSheetState()

    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            CoroutineScope(Dispatchers.IO).launch {
                if (uri != null) {
                    val imgDir = File(Environment.getExternalStorageDirectory(), "/Chat/Images")
                    imgDir.mkdirs()
                    val timeStamp: String =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "JPEG_${timeStamp}.jpeg"
                    val imageFile = File(imgDir, fileName)


                    val time = System.currentTimeMillis()
                    var job = CoroutineScope(Dispatchers.IO).launch {
                        saveImageToExternalStorage(EXT_DIR_IMAGE_LOCATION,context, uri, fileName)
                    }
                    job.join()

                    onSend(
                        Message(
                            MY_ID + time,userId, "image",
                            "$imageFile", 0, time, time
                        )
                    )

                    println("test uri = $uri")
                }
            }
        }
    )



        if (bottomSheetVisible.value) {
            ModalBottomSheet(
                onDismissRequest = {
                    bottomSheetVisible.value = false
                },
                dragHandle = {},
                shape = RoundedCornerShape(20.dp),
                sheetState = sheetState,
                modifier = Modifier
                    .offset(y = (-100).dp)
                    .padding(20.dp)
                   ,
                containerColor = colorResource(id = R.color.primary_light),
                scrimColor = Color.Transparent

            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                    IconButton(onClick = {
                        selectImageLauncher.launch("image/*")
                        bottomSheetVisible.value =false}) {
                        Icon(
                            painter = painterResource(id = R.drawable.camera),
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.primary))
                                .padding(8.dp)
                        )
                    }

                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.primary))
                                .padding(10.dp)
                        )
                    }

                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.primary))
                                .padding(10.dp)
                        )
                    }
                }
            }
        }

}


fun startRecording(audioLocation: File) {
    try {
    recorder = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        setOutputFile(audioLocation.absolutePath)
        prepare()
        start()
    }
    }catch (e: FileNotFoundException){println("Exception ${e.message}")}
    catch (e: RuntimeException){println("Exception ${e.message}")}
}

fun stopRecording() {
    recorder?.apply {
        stop()
        release()
    }
    recorder = null

}

@Preview
@Composable
private fun check() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.wave_lottie))
    Box(modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .height(62.dp)
        .background(colorResource(id = R.color.textfeild_bg_color))
        .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row {
            Card(modifier = Modifier

                .padding(5.dp)
                .background(color = colorResource(id = R.color.textfeild_bg_color))) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.wave_lottie2))


                LottieAnimation(
                    speed = 1.5f,
                    isPlaying = false,
                    modifier = Modifier
                        .background(
                            color = colorResource(
                                id = R.color.textfeild_bg_color
                            )
                        )
                        .size(44.dp),
                    composition = composition,
                    reverseOnRepeat = true,
                    iterations = 500,


                    )

            }
            IconButton(onClick = {
            }
            ) {

                    Icon(painter = painterResource(id = R.drawable.play), contentDescription ="",
                        tint = colorResource(id = R.color.primary), modifier = Modifier.size(20.dp))
            }
            Box(modifier = Modifier
                .height(50.dp)
                .padding(end = 5.dp)){

//                Indicator(0.5f)

                Text(text =  getMinSecond( 6000 ) , fontSize = 12.sp, color =  colorResource(id = R.color.black_variant), modifier = Modifier
                    .align(
                        Alignment.BottomStart
                    )
                    .padding(start = 5.dp))

                Text(text = "time", fontSize = 12.sp, color =  colorResource(id = R.color.black_variant), modifier = Modifier
                    .align(
                        Alignment.BottomEnd
                    )
                    .padding(end = 5.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Indicator(position:Float, thumbColor: Color, trackColor: Color){

    var sliderPosition by remember { mutableStateOf(0f) }
    sliderPosition = position

    val interactionSource = remember {

        MutableInteractionSource()
    }


    Column {

        Slider(
            modifier = Modifier.semantics { contentDescription = "Localized Description" },
            value = sliderPosition,
            colors = SliderDefaults.colors(inactiveTrackColor = trackColor , activeTrackColor = colorResource(id = R.color.primary)),
            onValueChange = {
                sliderPosition = it
                            },
            valueRange = 0f..1f,
            steps = 0,
            interactionSource = interactionSource,
            onValueChangeFinished = {
                // launch some business logic update with the state you hold
            },
            thumb = {

                Box(
                    modifier = Modifier
                        .offset(y = 4.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(thumbColor),
                    contentAlignment = Alignment.Center
                ){


                }
            },
        )
    }
}