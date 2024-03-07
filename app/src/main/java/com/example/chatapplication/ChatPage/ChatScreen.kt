package com.example.chatapplication.ChatPage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatapplication.R
import com.example.chatapplication.db.Message
import com.example.util.util


@Composable
fun ChatsContentList(chatViewModel: ChatViewModel, messageListSize:Int, addSelected:(data: Message)->Unit, removeSelected:(data:Message)->Unit, onLongClick:(data:Message)->Unit){

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
        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp,top=8.dp, bottom = 5.dp)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = listState,
                reverseLayout = true
            ) {
                itemsIndexed(chatList.value) {idx,it->
                    var bubbleSelected by remember {
                        mutableStateOf(false)
                    }

                    if(islongClickEnable == false) bubbleSelected =false
                    ChatBubble(it.message, it.isSender == 1,it.sentTime,bubbleSelected,{
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
            sendMessageBox {
               chatViewModel.addMessage(Message(0,chatViewModel.senderId,it,1,System.currentTimeMillis(),System.currentTimeMillis()))
            }
        }
    }

}

@Composable
fun sendMessageBox(onSend: (text:String)->Unit){
    val containerColor = colorResource(id = R.color.textfeild_bg_color)
    var messageText by remember { mutableStateOf("") }


    Box(modifier = Modifier
        .clip(RoundedCornerShape(18.dp))
        .background(color = containerColor)
    ) {
        Row (){

            val containerColor1 = containerColor

            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                maxLines = 5,
                placeholder = { Text("Enter your message here")},
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(10.dp)
                    .fillMaxWidth()
                ,
                colors = TextFieldDefaults.colors(
//                            focusedTextColor = back,
                    focusedContainerColor = containerColor1,
                    unfocusedContainerColor = containerColor1,
                    disabledContainerColor = containerColor1,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.Black,
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
                                .align(Alignment.CenterVertically),
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
                                    onSend(messageText)
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
                }
            )

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(message: String, isSender: Boolean, time: Long,isBubbleSelected: Boolean,onClick:()->Unit, onLongClick: () -> Unit) {
    var time = util.getHourAndMinuteFromTimestamp(time)
    var isExceedingOneLine by remember { mutableStateOf(false) }
    var showTimestampOnSameColumn by remember { mutableStateOf(true) }
    var colorBg =  if(isBubbleSelected) colorResource(id = R.color.primary_variant) else  if(isSender) colorResource(id = R.color.primary) else colorResource(id = R.color.textfeild_bg_color)
    var colorText = if(isSender) Color.White else Color.Black
    var colorTimestamp = if(isSender) colorResource(id = R.color.white_variant) else colorResource(id = R.color.black_variant)
    val startPadding = if(isSender) 65.dp else 8.dp
    val endPadding = if(isSender) 8.dp else 65.dp
    val selectedColor= if (isBubbleSelected) colorResource(id = R.color.primary_variant) else Color.Transparent
    var bubblePadding = PaddingValues(
      start = startPadding, end = endPadding
    )
    Row(
        modifier = Modifier

            .fillMaxWidth()
            .background(selectedColor)

            .padding(bubblePadding)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick) ,
        horizontalArrangement =if(isSender) Arrangement.End else Arrangement.Start,
    ) {

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = if (isSender) 10.dp else 0.dp,
                        bottomEnd = if (isSender) 0.dp else 10.dp
                    )
                )
                .background(colorBg)
                .padding(start = 8.dp, top = 8.dp, end = 3.dp)
        ) {
            Column {

                Row() {

                    Text(
                        text = message,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1f, false),
                        color = colorText,
                        onTextLayout = { textLayoutResult ->
                            var lineCount = textLayoutResult.lineCount
                            val firstLineEnd = textLayoutResult.getLineEnd(0)
                            val lastLineEnd = textLayoutResult.getLineEnd(textLayoutResult.lineCount - 1)
                            val firstLineLength = message.substring(0, firstLineEnd).length
                            val lastLineLength = message.substring(firstLineEnd *(lineCount-1), lastLineEnd).length

                            isExceedingOneLine = lineCount > 1
                            if(isExceedingOneLine)
                                showTimestampOnSameColumn = firstLineLength - lastLineLength > 8
                        }
                    )
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
                if(isExceedingOneLine && !showTimestampOnSameColumn){
                    Text(
                        text = "12:00 AM",
                        modifier = Modifier
                            .padding(end = 5.dp, start = 5.dp, bottom = 2.dp)
                            .align(Alignment.End),
                        fontSize = 10.sp,
                        color = colorTimestamp
                    )
                }
            }
            if(showTimestampOnSameColumn && isExceedingOneLine)
                Text(
                    text = "12:00 AM",
                    modifier = Modifier
                        .padding(end = 5.dp, start = 5.dp, bottom = 5.dp)
                        .align(Alignment.BottomEnd),
                    fontSize = 10.sp,
                    color = colorTimestamp
                )
        }
    }
}
