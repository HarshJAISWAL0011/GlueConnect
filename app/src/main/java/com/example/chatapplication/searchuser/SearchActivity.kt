package com.example.chatapplication.searchuser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.example.Constants
import com.example.chatapplication.ChatPage.ChatActivity
import com.example.chatapplication.R
import com.example.chatapplication.firebase.FirestoreDb.getSearchUserList
import com.example.chatapplication.searchuser.ui.theme.ChatApplicationTheme
import com.example.util.SearchUserData
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class SearchActivity : ComponentActivity() {
    private val fontStyleHeading= TextStyle(
        fontFamily = FontFamily( Font(R.font.robot_slab)),
        fontWeight = FontWeight(800),
        fontSize = 18.sp,
        color = Color.Black.copy(0.8f)
    )

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatApplicationTheme {

                val systemUiController = rememberSystemUiController()
                val statusBarColor = colorResource(id = R.color.primary)

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = statusBarColor,
                        darkIcons = false
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(id = R.color.background)
                ) {
                    var searchList = remember { mutableStateListOf<SearchUserData>() }
                    var job = remember { mutableStateOf<Job?>(null) }
                    LaunchedEffect(Unit) {
                        GlobalScope.launch {
                            searchList.addAll(getSearchUserList(""))
                        }
                    }
                    Scaffold(
                        topBar = {
                          SearchUser(){
                              job.value?.cancel()
                              job.value =CoroutineScope(Dispatchers.IO).launch {
                                 searchList.clear()
                                  searchList.addAll(getSearchUserList(it))
                              }
                           }
                        },
                        containerColor = colorResource(id = R.color.background)
                    ) {
                         LazyColumn(
                            modifier = Modifier.padding(it)
                        ) {
                            items(searchList) {
                                SearchItem(LocalContext.current,it ) {

                                }
                            }
                        }

                    }
                }
            }
        }

        Constants.CURRENT_ACTIVITY = "SearchActivity"
        Constants.CURRENT_ACTIVITY_ID = ""
    }

     @Composable
    private fun SearchUser(seachText:(text: String)->Unit) {
        var searchText by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.background)
            ),
            shape = RectangleShape,
            elevation = CardDefaults.cardElevation(11.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.background))
                .requiredHeight(48.dp)
                .focusRequester(focusRequester)
        ) {
//            Box(modifier = Modifier
//                 .fillMaxWidth()
//                .padding(horizontal = 10.dp),
//                 contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value =searchText,
                    onValueChange = { searchText = it
                        seachText(it)},
                    maxLines = 1,
                    placeholder = { Text(text = "Search...", color = Color.Gray.copy(0.6f))},
                     colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.Black.copy(0.8f),
                        backgroundColor = Color.Transparent,
                        cursorColor = colorResource(id = R.color.primary),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    textStyle = TextStyle(fontSize = 17.sp, fontFamily = FontFamily(
                        Font(R.font.sedan)
                    )),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                        .defaultMinSize(minHeight = 1.dp)

                )

                    Image(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = "",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(top = 4.dp),
                         contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(color = colorResource(id = R.color.primary))
                    )
                }

        }
    }

     @Composable
    private fun SearchItem(context: Context,data: SearchUserData, onClick: () -> Unit){

            Row(
                modifier = Modifier
                    .clickable {
                        val intent = Intent(context, ProfileActivity::class.java)
                        intent.putExtra("id", data.id)
                        context.startActivity(intent)
                        onClick()
                    }
                    .padding(horizontal = 20.dp, vertical = 15.dp)

                ,
                verticalAlignment = Alignment.CenterVertically
            ){
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(data.profileUrl)
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
                    placeholder = painterResource(id = R.drawable.profile_placeholder),
                    contentDescription ="", contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(20.dp))

                Column ( modifier =  Modifier.weight(0.8f)){
                    Text(
                        text = data.name,
                        style =   fontStyleHeading,
                        fontSize = 19.sp,
                    )
                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = data.desc,
                        style =   fontStyleHeading,
                        fontSize = 12.sp,
                        color = Color.Gray,

                        )
                }

                Spacer(modifier = Modifier.width(11.dp))
                Box(modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, colorResource(id = R.color.primary), CircleShape)
                    .clickable { }

                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "",
                        tint = colorResource(id = R.color.primary_variant),
                        modifier = Modifier
                            .padding(5.dp)
                            .size(25.dp)
                    )
                }
            }
    }
}
