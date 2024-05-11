package com.apexharn.chatapplication.HomePage

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.apexharn.Constants.MESSAGE_TYPE_AUDIO
import com.apexharn.Constants.MESSAGE_TYPE_IMAGE
import com.apexharn.chatapplication.R
import com.apexharn.chatapplication.R.drawable.profile_placeholder
import com.apexharn.chatapplication.R.drawable.search
import com.apexharn.chatapplication.ChatPage.ChatActivity
import com.apexharn.chatapplication.GroupPage.GroupListViewModel
import com.apexharn.chatapplication.MainActivity
import com.apexharn.chatapplication.PeopleBook.PeopleViewModel
import com.apexharn.chatapplication.R.drawable.no_profile
import com.apexharn.chatapplication.channel.ChannelViewModel
import com.apexharn.chatapplication.db.Sender
import com.apexharn.chatapplication.db.channeldb.ChannelDatabase
import com.apexharn.chatapplication.firebase.FirestoreDb
import com.apexharn.chatapplication.searchuser.SearchActivity
import com.apexharn.util.ChannelData
import com.apexharn.util.ChannelsWithMessage
import com.apexharn.util.SendersWithLastMessage
import com.apexharn.util.util.formatTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val allChannels = viewModel.channelListState.collectAsState()
    var joinedPublicChannels = allChannels.value.filter {
        it.isAdmin == 0
    }
    var myChannels = allChannels.value.filter { it->
        it.isAdmin == 1
    }
    var channelList by remember { mutableStateOf(mutableStateListOf<ChannelsWithMessage>()) }

    var channelType by remember {
        mutableStateOf("Open Clubs")
    }
    LaunchedEffect(allChannels) {
        withContext(Dispatchers.Main) {
            channelList.clear()
            channelList.addAll(joinedPublicChannels)
            channelType = "Open Clubs"
        }
    }

    var showSearchBox by remember { mutableStateOf(false) }
    var showMyChannel by remember { mutableStateOf(false) }



    val intent = Intent(context, ChatActivity::class.java)

    Surface(modifier = Modifier.background(colorResource(id = R.color.background))) {
        CompositionLocalProvider(LocalRippleTheme provides MainActivity.ButtonRippleTheme) {
            if (showSearchBox)
                SearchBox({ showSearchBox = !showSearchBox }, context)
            else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colorResource(id = R.color.background))
                ) {


                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        IconButton(
                            onClick = {
                            }, modifier = Modifier.padding(start = 20.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_change),
                                contentDescription = "",
                                modifier = Modifier
                                    .clickable {
                                        showMyChannel = !showMyChannel
                                        if (showMyChannel) {
                                            channelList.clear()
                                            channelList.addAll(myChannels)
                                            channelType = "Clubs Created"
                                        } else {
                                            channelList.clear()
                                            channelList.addAll(joinedPublicChannels)
                                            channelType = "Open Clubs"
                                        }
                                    }
                                    .padding(5.dp)
                                    .size(25.dp),
                                tint = colorResource(id = R.color.primary).copy(0.7f)
                            )
                        }

                        Text(
                            text = channelType, style = fontStyleHeading, fontSize = 17.sp,
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically),
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                        IconButton(onClick = {
                            showSearchBox = !showSearchBox
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.search_thick),
                                contentDescription = "",
                                modifier = Modifier
                                    .padding(end = 15.dp)
                                    .size(25.dp),
                                tint = colorResource(id = R.color.primary).copy(0.7f)
                            )
                        }
                    }

                    LazyColumn(modifier = Modifier.background(color = colorResource(id = R.color.background))) {
                        items(channelList) { it ->

                            var details = SendersWithLastMessage(
                                it.id,
                                it.profile_url,
                                it.name,
                                it.channelId,
                                it.messageType,
                                it.newMessageCount,
                                it.last_message,
                                it.sentTime
                            )
                            ChatItem(details, "") {

                                intent.putExtra("id", it.channelId)
                                if(showMyChannel)
                                    intent.putExtra("type", "my_channel")
                                else
                                    intent.putExtra("type", "joined_channel")
                                intent.putExtra("displayName", it.name)


                                if (it.newMessageCount > 0)
                                    viewModel.resetMessageCount(it)

                                context.startActivity(intent)
                            }
                        }
                    }
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

                var details = SendersWithLastMessage(it.id,it.profile_url,it.groupName,it.groupId,it.messageType,it.newMessageCount,it.last_message,it.sentTime)
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

                if(it.last_message != null) {
                    ChatItem(it, "") {
                        if (it.newMessageCount > 0)
                            peopleViewModel.resetNewMessageCount(it)

                        intent.putExtra("id", it.email)
                        intent.putExtra("type", "individual")
                        intent.putExtra("displayName", it.name)
                        context.startActivity(intent)
                    }
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
    val time = formatTime(sender.sentTime)

    Row (
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ){
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(sender.profile_url)
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
            placeholder = painterResource(id = no_profile),
            contentDescription ="",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
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
                maxLines = 1,
                style = if (sender.newMessageCount == 0) fontStyleHeading else fontStyleHeadingNewMsg,
            )
            Spacer(modifier = Modifier.height(7.dp))


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

                    if(it.isNotEmpty()) {

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
                        } else if (sender.messageType == MESSAGE_TYPE_AUDIO) {

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
                        } else
                            Text(
                                text = it,
                                style = fontStyleContent,
                                modifier = Modifier.padding(bottom = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                    }else{
                        Text(
                            text = "deleted message",
                            style = fontStyleContent,
                            modifier = Modifier.padding(bottom = 4.dp),
                            maxLines = 1,
                            fontStyle = FontStyle.Italic,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
fun StatusChatList(context: Context){
    Row (modifier = Modifier
        .fillMaxWidth(1f)
        .padding(horizontal = 40.dp)){
        Box(modifier = Modifier
            .height(42.dp).
            fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .clickable {
                val intent = Intent(context, SearchActivity::class.java)
                context.startActivity(intent)
            }
            .background(colorResource(id = R.color.primary_variant))
            , contentAlignment = Alignment.Center) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                TextField(
//                    value ="",
//                    onValueChange = {  },
//                    maxLines = 2,
//                     colors = TextFieldDefaults.textFieldColors(
//                        textColor = Color.Black.copy(0.8f),
//                        backgroundColor = Color.Transparent,
//                        cursorColor = colorResource(id = R.color.primary),
//                        focusedIndicatorColor = colorResource(id = R.color.primary),
//                        unfocusedIndicatorColor = colorResource(id = R.color.primary).copy(0.7f),
//                    ),
//                    textStyle = TextStyle(fontSize = 17.sp),
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(horizontal = 10.dp)
//                        .defaultMinSize(minHeight = 1.dp)
//
//                )

                Image(
                    painter = painterResource(id = search), contentDescription = "", modifier = Modifier
                        .size(40.dp)
                        .padding(top = 4.dp)
                        .align(Alignment.CenterEnd),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(color = Color.White)
                )
//            }

        }
//        LazyRow(modifier = Modifier
//            .padding(start = 20.dp)
//            .fillMaxWidth(1f)) {
//            items(10){
//
//                StatusItem()
//            }
//        }
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
                     GroupSelectionItem(it.name, it.profile_url) { onClick(it) }
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
private fun GroupSelectionItem(name: String,profile_url: String, onClick: () -> Unit){ // indiv. user items shown while creating group
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
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(profile_url)
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
            placeholder = painterResource(id = no_profile),
            contentDescription ="", contentScale = ContentScale.FillBounds, modifier = Modifier
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

 @Composable
fun SearchBox( backPressed: ()->Unit,context:Context ){
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var searchChannelList = remember { mutableStateListOf<ChannelData>() }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {

            var list = FirestoreDb.getChannelList( "", ChannelDatabase.getDatabase(context))
            searchChannelList.clear()
            searchChannelList.addAll(list)

        }
    }


    Column(modifier = Modifier
        .background(colorResource(id = R.color.background))
        .fillMaxSize()) {


    Row (verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.background(colorResource(id = R.color.background)) ){

   IconButton(onClick = { backPressed() }) {
       Icon(Icons.Outlined.KeyboardArrowLeft, contentDescription ="", tint = colorResource(id = R.color.primary) )
   }

        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .height(40.dp)
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.primary),
                    shape = RoundedCornerShape(20.dp) // Rounded corners
                ),
            contentAlignment = Alignment.CenterStart // Center align content horizontally
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it
                    CoroutineScope(Dispatchers.IO).launch {

                        var list = FirestoreDb.getChannelList( it.text, ChannelDatabase.getDatabase(context))
                                 searchChannelList.clear()
                        println("inside ${list.size}")
                                 searchChannelList.addAll(list)
                        println("size="+searchChannelList.size)

                    }
                                },
                textStyle = TextStyle(fontSize = 16.sp),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.fillMaxHeight()) {
                        // Placeholder text
                        if (text.text.isEmpty()) {
                            Text(
                                text = "  Search...",
                                style = TextStyle(color = Color.Gray)
                            )
                        }
                            innerTextField()

                    }
                    },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Add padding inside the text field
            )
        }
    }
        LazyColumn(modifier = Modifier.background(color = colorResource(id = R.color.background))) {

                items(searchChannelList) {

                    ChannelItem(name = it.name , followers =it.followers.toString(), it.profileUrl ) {
                        val intent = Intent(context, ChatActivity::class.java)
                        intent.putExtra("id",it.channelId)
                        intent.putExtra("type","public_channel")
                        intent.putExtra("displayName",it.name)
                        context.startActivity(intent)
                }
            }
        }

    }

}

@Composable
fun ChannelItem(name: String, followers: String, profile_url: String, onClick: () -> Unit){ // indiv. user items shown while creating group
    var isCheck by remember {
        mutableStateOf(false)
    }
    Row    (
        modifier = Modifier
            .clickable {
                isCheck = !isCheck
                onClick()
            }
            .padding(horizontal = 20.dp, vertical = 15.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ){
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(profile_url)
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
            placeholder = painterResource(id = no_profile),
            clipToBounds = true,
            contentDescription ="",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
            .size(45.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .background(Color.Gray),

        )
        Spacer(modifier = Modifier.width(20.dp))

        Column ( modifier =  Modifier.weight(0.8f)){
            Text(
                text = name,
                style =   fontStyleHeading,
                fontSize = 17.sp,
            )
            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = " $followers followers",
                style =   fontStyleHeading,
                fontSize = 11.sp,
                color = Color.Gray,

            )
        }

        Spacer(modifier = Modifier.width(11.dp))
            IconButton(onClick = {
                onClick()},
            ) {
                Icon(Icons.Outlined.KeyboardArrowLeft, contentDescription = "", tint = colorResource(id = R.color.primary_variant),
                    modifier = Modifier.rotate(180.0f))
            }

    }
}