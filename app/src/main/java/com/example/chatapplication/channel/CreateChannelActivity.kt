package com.example.chatapplication.channel

import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Constants
import com.example.chatapplication.R
import com.example.chatapplication.channel.ui.theme.ChatApplicationTheme
import com.example.chatapplication.db.channeldb.ChannelDatabase
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class CreateChannelActivity : ComponentActivity() {
    lateinit var database: ChannelDatabase
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

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    
                    Scaffold (
                        topBar = {

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .background(color = colorResource(id = R.color.background))
                                    .shadow(
                                        45.dp,
                                        RoundedCornerShape(bottomEnd = 20.dp),
                                        spotColor = (Color.Blue)
                                    ),
                                color = colorResource(id = R.color.primary),

                                shape = RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp)
                            ) {
                                Row (modifier = Modifier.fillMaxWidth()){
                                    IconButton(onClick = { super.onBackPressed()},
                                        modifier = Modifier.padding(start = 20.dp).align(Alignment.CenterVertically)
                                            .clip(CircleShape)) {
                                        Icon(
                                            Icons.Outlined.KeyboardArrowLeft,
                                            contentDescription = "",
                                            tint = Color.White,

                                        )
                                    }
                                    Text(text = "Create Channel", color = Color.White, fontSize = 21.sp,
                                        modifier = Modifier
                                            .padding(start = 10.dp)
                                            .align(Alignment.CenterVertically))
                                }

                            }
                        }
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it),
                            color = MaterialTheme.colorScheme.background

                        ) {
                            CreateChannelPage(database) {
                                finish()
                            }
                        }
                    }
                    

                   
                }
            }
        }

        Constants.CURRENT_ACTIVITY = "CreateChannelActivity"
        Constants.CURRENT_ACTIVITY_ID = ""

        database = ChannelDatabase.getDatabase(this)
    }
}
