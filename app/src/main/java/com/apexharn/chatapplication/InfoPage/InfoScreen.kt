package com.apexharn.chatapplication.InfoPage

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout  .Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.apexharn.chatapplication.R
import com.apexharn.chatapplication.db.groupdb.GroupMember


//@Preview(showSystemUi = true)
@Composable
 fun DetailScreen(
    member: List<GroupMember>,
    profile_url: String,
    nameText: String,
    phone: String,
    type: String,
    changeName: (String) -> Unit,
    backpressed: () ->Unit,
    description: String
) {
     var totalMember = "$phone Members"
    var name by remember { mutableStateOf("") }
    name = nameText

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()

            .padding(top = 15.dp)
    ) {
        items(1) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = {
                    backpressed()

                }) {
                    Icon(
                        Icons.Outlined.ArrowBack,
                        contentDescription = "",
                        tint = Color.Gray.copy(0.9f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(15.dp)
                    )
                }


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
                    placeholder =  painterResource(id = R.drawable.profile_placeholder),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = "",
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(100.dp)
                        .align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = name, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(5.dp))
            if (type == "group") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Group", fontSize = 17.sp, color = Color.Gray.copy(0.7f))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.circle),
                        contentDescription = "",
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = totalMember, fontSize = 15.sp)

                }
            } else if(type == "individual")
                Text(text = phone, fontSize = 15.sp)
            else if(type == "channel")
                Text(text = phone, fontSize = 15.sp)

            Spacer(modifier = Modifier.height(20.dp))


            // Action cards
            if(type == "channel"){

                Row(modifier = Modifier.padding(bottom = 10.dp)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .clickable { }
                        .border(1.dp, Color.Gray.copy(0.4f), RoundedCornerShape(15.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(
                            top = 10.dp,
                            bottom = 10.dp,
                            start = 17.dp,
                            end = 17.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.tick),
                            contentDescription = null,
                            tint = colorResource(id = R.color.primary),
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Following",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier

                        .padding(horizontal = 15.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .clickable { }
                        .border(1.dp, Color.Gray.copy(0.4f), RoundedCornerShape(15.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(
                            top = 10.dp,
                            bottom = 10.dp,
                            start = 17.dp,
                            end = 17.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Share,
                            tint = colorResource(id = R.color.primary),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Share",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            }
            else {
                Row(modifier = Modifier.padding(bottom = 10.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .padding(horizontal = 15.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .clickable { }
                            .border(1.dp, Color.Gray.copy(0.4f), RoundedCornerShape(15.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                top = 10.dp,
                                bottom = 10.dp,
                                start = 17.dp,
                                end = 17.dp
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Call,
                                tint = colorResource(id = R.color.primary),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Audio",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .padding(horizontal = 15.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .clickable { }
                            .border(1.dp, Color.Gray.copy(0.4f), RoundedCornerShape(15.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                top = 10.dp,
                                bottom = 10.dp,
                                start = 17.dp,
                                end = 17.dp
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.video_camera),
                                contentDescription = null,
                                tint = colorResource(id = R.color.primary),
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Video",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier

                            .padding(horizontal = 15.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .clickable { }
                            .border(1.dp, Color.Gray.copy(0.4f), RoundedCornerShape(15.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                top = 10.dp,
                                bottom = 10.dp,
                                start = 17.dp,
                                end = 17.dp
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.Share,
                                tint = colorResource(id = R.color.primary),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Share",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider(
                modifier = Modifier
                    .height(5.dp)
                    .padding(horizontal = 15.dp),
                color = Color.White.copy(0.6f)
            )
            SecondCard(type, member, totalMember, name,description) {
                name = it
                changeName(it)
            }
            if(type =="group")
             Row (verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 15.dp)){
                Icon(Icons.Outlined.Lock, contentDescription ="", tint = Color.Gray.copy(0.6f), modifier = Modifier.size(15.dp) )
                Text(text = "Messages are secured", modifier = Modifier.padding(10.dp), fontWeight = W500, fontFamily = FontFamily(
                    Font( R.font.robot_slab)), color = Color.Black.copy(0.6f))
                
            }

        }
    }
}


    @Composable
    fun SecondCard(type: String, members: List<GroupMember>, totalMember: String,name: String,description:String, changeName: (String)->Unit) {
        var textValue by remember { mutableStateOf(TextFieldValue("")) }
        LaunchedEffect(name) {
            textValue = TextFieldValue(name)
        }
        Column {

    if(type == "group"){
        Text(text =totalMember, fontSize = 13.sp, fontWeight = W600, color = Color.Black.copy(0.5f),
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 7.dp, top = 6.dp) )
            members.forEach(){gm->
                    Item(name = gm.name)
            }
    }
    else if(type == "individual") {
        var isEdittingOn by remember {
            mutableStateOf(false)
        }
        TextField(value = textValue, onValueChange = { textValue = it },
            readOnly = !isEdittingOn,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                textColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle.Default.copy(fontSize = 18.sp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp, start = 20.dp, end = 10.dp),
            trailingIcon = {
                IconButton(onClick = {
                    isEdittingOn = !isEdittingOn
                    if( !isEdittingOn){
                        changeName(textValue.text)

                    }
                }) {
                    if(isEdittingOn)
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = "",
                            tint = Color.Green.copy(0.8f)
                        )
                    else
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "",
                        tint = colorResource(id = R.color.primary)
                    )
                }
            }
        )
    }else if(type == "channel"){
                Text(
                    text = "Description",
                    fontFamily = FontFamily(Font(R.font.josefinsans)),
                    modifier = Modifier.padding(start = 20.dp, top =20.dp, ),
                    color = Color.Black.copy(0.6f),
                    fontSize = 18.sp
                    )
             Text(text = description,  fontWeight = W400, fontSize = 19.sp, color = Color.Black.copy(0.6f),
                 modifier= Modifier.padding(20.dp))
    }

    Divider(modifier = Modifier
        .height(5.dp)
        .padding(horizontal = 15.dp),
            color = Color.White.copy(0.6f))

    if(type == "group"){
        Row(
            modifier = Modifier.padding(start = 20.dp, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.exit),
                contentDescription = "",
                tint = colorResource(id = R.color.dark_red)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = "Exit",
                fontSize = 18.sp,
                color = colorResource(id = R.color.dark_red),
                fontWeight = W600
            )
        }
    }
    else if(type == "individual") {
        Row(
            modifier = Modifier
                .padding(start = 20.dp, top = 20.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cancel_svgrepo_com),
                contentDescription = "",
                tint = colorResource(id = R.color.dark_red)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = "Block",
                fontSize = 18.sp,
                color = colorResource(id = R.color.dark_red),
                fontWeight = W600
            )
        }
    }
     else if(type == "channel"){
        Row(
            modifier = Modifier.padding(start = 20.dp, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.exit),
                contentDescription = "",
                tint = colorResource(id = R.color.dark_red)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = "Leave Channel",
                fontSize = 18.sp,
                color = colorResource(id = R.color.dark_red),
                fontWeight = W600
            )
        }
    }

    Row(modifier = Modifier.padding(start = 20.dp, top = 20.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(id = R.drawable.thumbs_down), contentDescription = "", tint = colorResource(id =R.color.dark_red))
        Spacer(modifier = Modifier.width(15.dp))
        Text(text = "Report", fontSize = 18.sp, color = colorResource(id =R.color.dark_red), fontWeight = W600)
    }

}

}



@Composable
fun Item(name: String){
    Row (verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 10.dp, start = 20.dp, end = 10.dp),){
        Image(painter = painterResource(id = R.drawable.profile_placeholder), contentDescription ="",
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .padding(end = 10.dp))

        Text(text = name , fontSize = 18.sp)
    }
}