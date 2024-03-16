package com.example.chatapplication.GroupPage

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Constants
import com.example.chatapplication.ChatPage.ChatActivity
import com.example.chatapplication.PeopleBook.PeopleViewModel
import com.example.chatapplication.R
import com.example.util.GroupSendersWithMessage
import com.example.util.SendersWithLastMessage
import com.example.util.util


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
fun GroupChatList(viewModel: GroupListViewModel, context: Context){
    val groupList = viewModel.groupListState.collectAsState()

    val intent = Intent(context, ChatActivity::class.java)
    println("storing groupList size = ${groupList.value.size}")

    Surface(modifier = Modifier.background(colorResource(id = R.color.background))) {
        LazyColumn (modifier = Modifier.background(color = colorResource(id = R.color.background))){
            items(groupList.value){it->

                GroupChatItem(it) {

                    intent.putExtra("groupId",it.groupId)
                    intent.putExtra("isGroup",true)
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
private fun GroupChatItem(group: GroupSendersWithMessage, onClick: ()->Unit){

    val fontStyleHeadingNewMsg= TextStyle(
        fontFamily = FontFamily( Font(R.font.josefinsans)),
        fontWeight = FontWeight.W900,
        fontSize = 20.sp,
        color = colorResource(id = R.color.primary)
    )
    val time = util.formatTime(group.sentTime)

    Row (
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ){
        Image(painter = painterResource(id = R.drawable.profile_placeholder), contentDescription ="", contentScale = ContentScale.Fit, modifier = Modifier
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
                text = group.groupName ?: "",
                style = if (group.newMessageCount == 0) fontStyleHeading else fontStyleHeadingNewMsg,
            )
            Spacer(modifier = Modifier.height(11.dp))


            group.last_message?.let {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {

                    if (group.messageType == Constants.MESSAGE_TYPE_IMAGE) {
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

                    else if (group.messageType == Constants.MESSAGE_TYPE_AUDIO) {

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
            if (group.newMessageCount > 0 ) {
                val size = if (group.newMessageCount >99) 21.dp else if(group.newMessageCount > 9) 19.dp else 17.dp
                val textSize = if (group.newMessageCount >99) 11.sp else if(group.newMessageCount > 9) 12.sp else 13.sp
                Card(
                    shape = RoundedCornerShape(50.dp),
                    colors = CardDefaults.cardColors(containerColor = colorResource(
                        id = R.color.primary
                    )
                    ),
                    modifier = Modifier.size(size),
                ) {

                    Row(
                        modifier = Modifier.fillMaxSize(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Text(text = group.newMessageCount.toString(), fontSize = textSize)
                    }
                }
            }
        }
    }
}
