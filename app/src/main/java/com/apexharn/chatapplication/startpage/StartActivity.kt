package com.apexharn.chatapplication.startpage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexharn.Constants
import com.apexharn.chatapplication.MainActivity
import com.apexharn.chatapplication.R
import com.apexharn.chatapplication.startpage.ui.theme.ChatApplicationTheme

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the status bar.

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        val sharedPref = getSharedPreferences(Constants.PREF, MODE_PRIVATE)
        val userId = sharedPref.getString("userId","");
        if(userId?.isEmpty()?:true){
            setContent {
                ChatApplicationTheme {

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = colorResource(id = R.color.background)
                    ) {
                        GetStarted(this)
                    }
                }
            }
        }

    }

    override fun onResume() {

        val sharedPref = getSharedPreferences(Constants.PREF, MODE_PRIVATE)
        val userId = sharedPref.getString("userId","");
        if(userId?.isNotEmpty()?:false){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        super.onResume()
    }
}


@Composable
private fun GetStarted(context: Context) {
    Column (modifier = Modifier.fillMaxSize()){
        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)){
            Icon(
                painter = painterResource(id = R.drawable.bubble), contentDescription = "",
                tint = colorResource(id = R.color.primary).copy(0.6f),
                modifier = Modifier
                    .scale(scaleX = -1f, scaleY = -1f)
                    .offset(y = 70.dp)
                    .align(Alignment.TopEnd)
                    .size(520.dp)

                   ,
            )
            Image(painter = painterResource(id =  R.drawable.women2), contentDescription ="",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(end = 60.dp, top = 60.dp)
                    .clip(CircleShape)
                    .size(80.dp)

                    .align(Alignment.TopEnd)
                  )
            Image(painter = painterResource(id =  R.drawable.woment1), contentDescription ="",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(end = 120.dp, top = 155.dp)
                    .size(135.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .align(Alignment.TopEnd)
                  )
        }

        Text(text = "Let's Get\n Started", fontSize = 55.sp, color = Color.Black,
            textAlign = TextAlign.Center, lineHeight = 60.sp, letterSpacing = 1.sp,
            fontFamily = FontFamily(Font(R.font.merriweather, FontWeight.W900)),
            fontWeight = FontWeight.W900,
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(bottom = 50.dp)
        )

        TextButton(onClick = {
                             val intent = Intent(context,LoginActivity::class.java)
            context.startActivity(intent)
        }, modifier = Modifier
            .padding(bottom = 20.dp, start = 10.dp, end = 10.dp)
            .align(Alignment.CenterHorizontally)
            .clip(RoundedCornerShape(20.dp))
            .background(colorResource(id = R.color.primary))) {
            Text(text = "Start", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 80.dp),
                fontFamily = FontFamily(Font(R.font.robot_slab, FontWeight.W700)) , fontWeight = FontWeight.W900,)
        }
    }
}


