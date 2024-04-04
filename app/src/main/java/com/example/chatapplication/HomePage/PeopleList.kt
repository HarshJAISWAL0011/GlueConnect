package com.example.chatapplication.HomePage

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Constants.MESSAGE_TYPE_AUDIO
import com.example.Constants.MESSAGE_TYPE_IMAGE
import com.example.chatapplication.R
import com.example.chatapplication.R.drawable.profile_placeholder
import com.example.chatapplication.R.drawable.search
import com.example.chatapplication.ChatPage.ChatActivity
import com.example.chatapplication.GroupPage.GroupListViewModel
import com.example.chatapplication.MainActivity
import com.example.chatapplication.PeopleBook.PeopleViewModel
import com.example.chatapplication.channel.ChannelViewModel
import com.example.chatapplication.db.Sender
import com.example.retrofit.RetrofitBuilder
import com.example.util.SendersWithLastMessage
import com.example.util.util.formatTime
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val fontStyleHeading= TextStyle(
    fontFamily = FontFamily( Font(R.font.josefinsans)),
    fontWeight = FontWeight(800),
            fontSize = 20.sp,
    color = Color.Black
)



private val fontStyleContent= TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    color = Color.Gray
)

@Composable
fun ChannelList(viewModel: ChannelViewModel, context: Context){
    val channelList = viewModel.channelListState.collectAsState()

    val intent = Intent(context, ChatActivity::class.java)
    println("storing channelList size = ${channelList.value.size}")

    Surface(modifier = Modifier.background(colorResource(id = R.color.background))) {
        LazyColumn (modifier = Modifier.background(color = colorResource(id = R.color.background))){
            items(channelList.value){it->

                var details = SendersWithLastMessage(it.id,it.name,it.channelId,it.messageType,it.newMessageCount,it.last_message,it.receiveTime)
                ChatItem(details, "") {

                    intent.putExtra("id",it.channelId)
                    intent.putExtra("type","channel")
                    intent.putExtra("displayName",it.name)


                    if(it.newMessageCount!! > 0)
                        viewModel.resetMessageCount(it)

                    context.startActivity(intent)
                }
            }
        }
    }

}


@Composable
fun GroupChatList(viewModel: GroupListViewModel, context: Context){
    val groupList = viewModel.groupListState.collectAsState()

    val intent = Intent(context, ChatActivity::class.java)
    println("storing groupList size = ${groupList.value.size}")

    Surface(modifier = Modifier.background(colorResource(id = R.color.background))) {
        LazyColumn (modifier = Modifier.background(color = colorResource(id = R.color.background))){
            items(groupList.value){it->

                var details = SendersWithLastMessage(it.id,it.groupName,it.groupId,it.messageType,it.newMessageCount,it.last_message,it.sentTime)
                ChatItem(details, it.senderName?:"") {

                    intent.putExtra("id",it.groupId)
                    intent.putExtra("type","group")
                    intent.putExtra("displayName",it.groupName)


                    if(it.newMessageCount!! > 0)
                        viewModel.resetMessageCount(it)

                    context.startActivity(intent)
                }
            }
        }
    }

}


@Composable
fun ChatList(peopleViewModel: PeopleViewModel, context:Context){
    val peopleList = peopleViewModel.peopleListState.collectAsState()

    val intent =Intent(context, ChatActivity::class.java)
    println("storing peopleList size = ${peopleList.value.size}")

    Surface(modifier = Modifier.background(colorResource(id = R.color.background))) {
        LazyColumn (modifier = Modifier.background(color = colorResource(id = R.color.background))){
            items(peopleList.value){it->

                ChatItem(it,"") {

                    intent.putExtra("id",it.email)
                    intent.putExtra("type","individual")
                    intent.putExtra("displayName",it.name)

                    if(it.newMessageCount > 0)
                        peopleViewModel.resetNewMessageCount(it)

                    context.startActivity(intent)
                }
            }
        }
    }

}


@Composable
fun ChatItem(sender: SendersWithLastMessage, lastMsgSender: String, onClick: ()->Unit){

     val fontStyleHeadingNewMsg= TextStyle(
        fontFamily = FontFamily( Font(R.font.josefinsans)),
        fontWeight = FontWeight.W900,
        fontSize = 20.sp,
        color = colorResource(id = R.color.primary)
    )
    val time = formatTime(sender.receiveTime)

    Row (
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ){
        Image(painter = painterResource(id = profile_placeholder), contentDescription ="", contentScale = ContentScale.Fit, modifier = Modifier
            .size(45.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .background(Color.Gray)
             )
        Spacer(modifier = Modifier.width(15.dp))
        Column(verticalArrangement = Arrangement.Center
            , modifier = Modifier
                .weight(0.9f)
                .background(color = colorResource(id = R.color.background)) )
        {
            Text(
                text = sender.name,
                style = if (sender.newMessageCount == 0) fontStyleHeading else fontStyleHeadingNewMsg,
            )
            Spacer(modifier = Modifier.height(11.dp))


            sender.last_message?.let {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {

                    if(lastMsgSender.isNotBlank()){
                        Text(
                            text = "$lastMsgSender:",
                            style = fontStyleContent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (sender.messageType == MESSAGE_TYPE_IMAGE) {
                        Icon(
                            painter = painterResource(id = R.drawable.picture_file),
                            contentDescription = "",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Image",
                            style = fontStyleContent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    else if (sender.messageType == MESSAGE_TYPE_AUDIO) {

                        Icon(
                            painter = painterResource(id = R.drawable.audio_file),
                            contentDescription = "",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Audio",
                            style = fontStyleContent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    else
                        Text(
                            text = it,
                            style = fontStyleContent,
                            modifier = Modifier.padding(bottom = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                }
            }
        }
        Column( modifier = Modifier
            .fillMaxHeight(1f)
            .background(color = colorResource(id = R.color.background))) {
            Text(text = time, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.ExtraBold, modifier = Modifier.alpha(0.7f) )
            Spacer(modifier = Modifier.height(8.dp))
            if (sender.newMessageCount > 0 ) {
                val size = if (sender.newMessageCount >99) 21.dp else if(sender.newMessageCount > 9) 19.dp else 17.dp
                val textSize = if (sender.newMessageCount >99) 11.sp else if(sender.newMessageCount > 9) 12.sp else 13.sp
            Card(
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors( containerColor = colorResource(
                    id = R.color.primary
                )),
                modifier = Modifier.size(size),
            ) {

                    Row(
                        modifier = Modifier.fillMaxSize(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Text(text = sender.newMessageCount.toString(), fontSize = textSize)
                    }
                }
            }
        }
    }
}


@Composable
fun StatusChatList(){
    Row (modifier = Modifier
        .fillMaxWidth(1f)){
        Box(modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(colorResource(id = R.color.primary_variant))
            , contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = search), contentDescription = "", modifier = Modifier
                    .size(40.dp)
                    .padding(top = 4.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(color = Color.White)
            )
        }
        LazyRow(modifier = Modifier
            .padding(start = 20.dp)
            .fillMaxWidth(1f)) {
            items(10){

                StatusItem()
            }
        }
    }

}

@Composable
fun StatusItem(){
    Box(modifier = Modifier
        .padding(horizontal = 10.dp)
        .clip(RoundedCornerShape(11.dp))
        .background(Color.Gray)
        , contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = profile_placeholder),
            contentDescription = "",
            modifier = Modifier
                .size(42.dp),
            contentScale = ContentScale.FillBounds
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CreateGroupBottomSheet(senderList: List<Sender>,showSheet:(show: Boolean )->Unit,onClick: (sender: Sender) -> Unit, createGroup:(String)->Unit) {
    val sheetState   = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var text by remember { mutableStateOf(TextFieldValue("")) }
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

            ModalBottomSheet(
                onDismissRequest = {
                        showBottomSheet = false
                    showSheet(false)
                },

                containerColor = colorResource(id = R.color.background),
                sheetState = sheetState,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = 60.dp)
            ) {

                Text(text = "Create Group", fontStyle = FontStyle(R.font.robot_slab), fontWeight = FontWeight.W600, fontSize = 22.sp,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Black.copy(0.7f))

                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        isError = false
                    },
                    isError = isError,
                    textStyle = TextStyle(fontSize = 18.sp),
                    placeholder = { Text(text = "Enter group name") },
                    maxLines = 1,
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = colorResource(id = R.color.primary),
                        backgroundColor = Color.Transparent,
                        focusedBorderColor = colorResource(id = R.color.primary),
                        unfocusedBorderColor = colorResource(id = R.color.primary).copy(0.8f)

                    ),
                    modifier = Modifier
                        .padding(horizontal = 40.dp, vertical = 20.dp)
                        .defaultMinSize(minHeight = 2.dp)
                )

                Text(text = "Select group member", fontSize = 14.sp, fontStyle = FontStyle(R.font.robot_slab)
                    , fontWeight = FontWeight.W600, color = Color.Black.copy(0.4f),
                    modifier = Modifier.padding(start = 40.dp))

                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.background)),
                    verticalArrangement = Arrangement.spacedBy(2.dp),) {
                 items(senderList){
                     GroupSelectionItem(it.name) { onClick(it) }
                 }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .padding(bottom = 100.dp)
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Button(onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showSheet(false)
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    },
                        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
                        modifier = Modifier.padding(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent)) {
                        Text("Cancel", color = colorResource(id = R.color.primary))
                    }

                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {

                                if(text.text.isEmpty()){
                                    isError =true
                                }else {
                                    createGroup(text.text)
                                    showBottomSheet =false
                                    showSheet(false)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.primary)),
                        modifier = Modifier.padding(horizontal = 10.dp),
                    ) {
                        Text("Create", color = Color.White)
                    }
                }

            }

}


@Composable
private fun GroupSelectionItem(name: String, onClick: () -> Unit){
    var isCheck by remember {
        mutableStateOf(false)
    }
    Row    (
        modifier = Modifier
            .clickable {
                isCheck = !isCheck
                onClick()
            }
            .padding(horizontal = 40.dp, vertical = 10.dp)
           ,
        verticalAlignment = Alignment.CenterVertically
    ){
        Image(painter = painterResource(id = profile_placeholder), contentDescription ="", contentScale = ContentScale.Fit, modifier = Modifier
            .size(35.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = name,
                style =   fontStyleHeading,
                fontSize = 17.sp,
                modifier =  Modifier.weight(0.8f)
            )
            Spacer(modifier = Modifier.height(11.dp))

        if(isCheck){
            IconButton(onClick = { isCheck =!isCheck
                onClick()},
               ) {
                Icon(painter = painterResource(id = R.drawable.checkbox), contentDescription = "", tint = colorResource(
                    id = R.color.primary_variant
                ) )
            }
        }else{
            IconButton(onClick = {  isCheck =!isCheck
                onClick()}) {
                Icon(painter = painterResource(id = R.drawable.unchecked), contentDescription = "", tint = colorResource(
                    id = R.color.primary_variant
                ) )
            }
        }



    }
}