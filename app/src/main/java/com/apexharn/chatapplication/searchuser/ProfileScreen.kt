package com.apexharn.chatapplication.searchuser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.font.FontWeight.Companion.W800
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.apexharn.chatapplication.R
import com.apexharn.chatapplication.firebase.FirestoreDb
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


private val fontStyleHeading= TextStyle(
    fontFamily = FontFamily( Font(R.font.robot_slab)),
    fontWeight = FontWeight(800),
    fontSize = 18.sp,
    color = Color.Black.copy(0.8f)
)

private val fontStyleSubHeading= TextStyle(
    fontFamily = FontFamily( Font(R.font.sedan)),
    fontWeight = FontWeight(200),
    fontSize = 15.sp,
    color = Color.Gray.copy(0.8f)
)


@Composable
  fun Header(id:String) {

    var desc by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
     var about by remember { mutableStateOf("") }
     var type by remember { mutableStateOf("") }
     var name by remember { mutableStateOf("") }
     var profile_url by remember { mutableStateOf("") }
      val skills = remember { mutableStateListOf<String>() }

    LaunchedEffect (id){
        GlobalScope.launch {
            if(id.isNotEmpty())
            FirestoreDb.getProfileData(id).addOnCompleteListener{
                if(it.isSuccessful){
                    val map = it.result
                    if(map.containsKey("description")) desc =(map["description"].toString())
                    if(map.contains("location")) location = (map["location"].toString())
                    if(map.contains("about")) about = (map["about"].toString())
                    if(map.contains("skills")) skills.addAll(map["skills"] as List<String>)
                    if(map.contains("job_type")) type = map["job_type"].toString()
                    if(map.contains("name")) name = map["name"].toString()
                    if(map.contains("profile_url")) profile_url = map["profile_url"].toString()
                }
            }
        }
    }
    LazyColumn {
        item {

        Column(
            modifier = Modifier
                .background(colorResource(id = R.color.background))
                .padding(bottom = 5.dp)
        ) {

                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.bg_banner_profile),
                        contentDescription = "",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
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
                        placeholder = painterResource(id = R.drawable.profile_placeholder),
                        contentDescription = "",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .padding(start = 20.dp, top = 72.dp)
                            .size(90.dp)
                            .align(Alignment.TopStart)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .clickable {
                            })
                }
                Text(
                    text = name, style = fontStyleHeading,
                    fontSize = 23.sp,
                    modifier = Modifier.padding(top = 18.dp, start = 12.dp)
                )

                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = desc,
                    style = fontStyleSubHeading,
                    fontWeight = W800,
                    modifier = Modifier.padding(start = 12.dp, end = 20.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Bangalore, Karnataka, India",
                    style = fontStyleSubHeading,
                    fontWeight = W600,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 12.dp, end = 20.dp, bottom = 15.dp)
                )

            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(25.dp),
                border = BorderStroke(1.dp, colorResource (id = R.color.primary)) ,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp)

                ) {
                Text(text = "Send  Request", color = colorResource(id = R.color.primary))
            }
            Spacer(modifier = Modifier.height(22.dp))

            }
            AboutSection(about)
            SkillSection(skills.toList())
        }
    }
}

 @Composable
private fun AboutSection(about: String) {
     Column(modifier = Modifier
         .fillMaxWidth()
         .padding(top = 15.dp, bottom = 15.dp)
         .background(colorResource(id = R.color.background))
        ) {
         Text(text = "About", style = fontStyleHeading,
             fontSize = 19.sp,
             modifier = Modifier.padding(top = 20.dp, start = 12.dp))
         Spacer(modifier = Modifier.height(15.dp))

         Text(text =about ,
             style = fontStyleSubHeading,
             fontFamily = FontFamily(Font(R.font.exo2)),
             fontWeight = W800,
             modifier = Modifier.padding(start = 12.dp, end = 20.dp, top = 5.dp, bottom = 20.dp))
         }
     }

@Composable
private fun SkillSection(skills:List<String>) {
Column(
     modifier = Modifier
         .fillMaxWidth()
         .padding(top = 15.dp, bottom = 15.dp)
         .background(colorResource(id = R.color.background))
) {

        Text(text = "Skills", style = fontStyleHeading,
            fontSize = 19.sp,
            modifier = Modifier.padding(top = 20.dp, start = 12.dp))
        Spacer(modifier = Modifier.height(15.dp))
        skills.forEach {
            SkillItem(it)
        }
}
}

@Composable
private fun SkillItem(skill: String ) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)) {
        Text(text = skill, modifier = Modifier
            .weight(1f)
            .padding(start = 5.dp),
                style = fontStyleSubHeading, color = Color.Black, fontSize = 18.sp)
        IconButton(onClick = { /*TODO*/ }) {

            Image(painter = painterResource(id = R.drawable.like) , contentDescription ="",
                modifier = Modifier.scale(scaleX = -1f, scaleY = 1f)
                )
        }

    }
    Divider(modifier = Modifier.padding(horizontal = 20.dp), color = Color.Gray.copy(0.7f))
}

