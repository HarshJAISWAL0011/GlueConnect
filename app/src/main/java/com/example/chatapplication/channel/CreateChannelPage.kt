package com.example.chatapplication.channel

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.example.Constants
import com.example.Constants.EXT_DIR_PROFILE_LOCATION
import com.example.Constants.FOLDER_PROFILE
import com.example.Constants.MY_ID
import com.example.chatapplication.ChatPage.userId
import com.example.chatapplication.R
import com.example.chatapplication.WebSocket.WebSocketClient
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.channeldb.ChannelDatabase
import com.example.chatapplication.db.channeldb.Channels
import com.example.chatapplication.firebase.FirestoreDb.createGroup
import com.example.util.CreateChannelData
import com.example.util.util
import com.example.util.util.uploadFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private var uri: Uri? = null
val customFontFamily = FontFamily(
    Font(R.font.slab, weight = FontWeight.Normal)
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChannelPage(database: ChannelDatabase, onDone: ()-> Unit) {

    val desc = remember { mutableStateOf(TextFieldValue("")) }
    val name = remember { mutableStateOf(TextFieldValue("")) }
    val options = listOf("Select channel type", "News", "Education","Entertainment","Others")
    var selectedIndex by remember { mutableStateOf(0) }
    var nameError by remember { mutableStateOf(false) }
    var descError by remember { mutableStateOf(false) }
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var showDialogState by remember { mutableStateOf(false) }


    val context = LocalContext.current
    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { resultUri: Uri? ->
            resultUri?.let {
                // Load the Bitmap from the URI
                val inputStream = context.contentResolver.openInputStream(resultUri)
                val selectedBitmap = inputStream?.use { stream -> BitmapDrawable.createFromStream(stream, resultUri.toString()) }?.toBitmap()

                // Set the loaded Bitmap to the mutable state variable
                bitmap = selectedBitmap?.asImageBitmap()
            }
         uri= resultUri
        }
    )


    Surface(
        color = colorResource(id = R.color.background)
    ) {
        Column {



        if (showDialogState) {
            AlertDialog(
                onDismissRequest = { },
                modifier = Modifier.background(Color.Transparent),
            )
            {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                ) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.primary),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

            }
        }



        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(top = 35.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Card(
                    backgroundColor = colorResource(id = R.color.background),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Yellow)
                        .size(80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Cyan)
                            .fillMaxSize()
                    ) {
                        bitmap?.let { loadedBitmap ->
                            Image(
                                bitmap = loadedBitmap,
                                contentDescription = "",
                                modifier = Modifier
                                    .fillMaxSize()

                                    .align(Alignment.CenterStart)
                                    .background(Color.Gray)
                            )
                        }
                        Card(
                            backgroundColor = colorResource(id = R.color.primary).copy(0.7f),
                            modifier = Modifier
                                .width(80.dp)
                                .height(25.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            IconButton(
                                onClick = { selectImageLauncher.launch("image/*") },
                                modifier = Modifier
                                    .padding(bottom = 0.dp)
                                    .align(Alignment.BottomCenter)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "",
                                    tint = Color.Black
                                )
                            }
                        }


                    }
                }


                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    maxLines = 2,
                    isError = nameError,
                    placeholder = { Text(text = "name") },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.Black.copy(0.8f),
                        backgroundColor = Color.Transparent,
                        cursorColor = colorResource(id = R.color.primary),
                        focusedIndicatorColor = colorResource(id = R.color.primary),
                        unfocusedIndicatorColor = colorResource(id = R.color.primary).copy(0.7f),
                    ),
                    textStyle = TextStyle(fontSize = 17.sp),
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .defaultMinSize(minHeight = 1.dp)

                )

            }
            Text(
                text = "Description", modifier = Modifier.padding(top = 25.dp, bottom = 15.dp),
                fontSize = 17.sp
            )

            val desc = remember { mutableStateOf(TextFieldValue()) }
            TextField(
                value = desc.value,
                onValueChange = { desc.value = it },
                maxLines = 5,
                isError = descError,
                placeholder = { Text(text = "Description") },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black.copy(0.8f),
                    backgroundColor = Color.Transparent,
                    cursorColor = colorResource(id = R.color.primary),
                    focusedIndicatorColor = colorResource(id = R.color.primary),
                    unfocusedIndicatorColor = colorResource(id = R.color.primary).copy(0.7f),
                ),
                textStyle = TextStyle(fontSize = 17.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp)
                    .defaultMinSize(minHeight = 1.dp)

            )



            Column {
                var expanded by remember { mutableStateOf(false) }


                TextField(
                    value = options[selectedIndex],
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "",
                            tint = colorResource(
                                id = R.color.primary
                            )
                        )
                    },
                    maxLines = 1,
                    enabled = false,
                    placeholder = { Text(text = "Select channel type") },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.Black.copy(0.8f),
                        backgroundColor = Color.Transparent,
                        disabledTrailingIconColor = colorResource(id = R.color.primary),
                        cursorColor = colorResource(id = R.color.primary),
                        disabledTextColor = Color.Black.copy(0.8f),
                        focusedIndicatorColor = colorResource(id = R.color.primary),
                        disabledIndicatorColor = colorResource(id = R.color.primary).copy(0.7f),
                    ),
                    textStyle = TextStyle(fontSize = 17.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp, top = 15.dp)
                        .defaultMinSize(minHeight = 1.dp)
                        .clickable { expanded = !expanded }


                )

                // Dropdown menu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    options.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            onClick = {
                                selectedIndex = index
                                expanded = false
                            }
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.body1,
                                color = if (index == selectedIndex) MaterialTheme.colors.primary else Color.Unspecified
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(top = 50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        onDone()
                    },
                    modifier = Modifier.padding(horizontal = 10.dp),
                    elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent
                    )
                ) {
                    Text(text = "Cancel", color = colorResource(id = R.color.primary))
                }

                Button(
                    onClick = {
                        if (uri == null) {
                            Toast.makeText(context, "Select image for channel", Toast.LENGTH_LONG)
                                .show()
                        } else if (name.value.text.isEmpty()) {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_LONG)
                                .show()
                            nameError = true
                        } else if (desc.value.text.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Description cannot be empty",
                                Toast.LENGTH_LONG
                            ).show()
                            descError = true
                        } else if (selectedIndex == 0) {
                            Toast.makeText(context, "Select channel type", Toast.LENGTH_LONG).show()
                        } else {
                            showDialogState = true
                            CoroutineScope(Dispatchers.IO).launch {
                                if (uri != null) {
                                    val timeStamp: String =
                                        SimpleDateFormat(
                                            "yyyyMMdd_HHmmss",
                                            Locale.getDefault()
                                        ).format(Date())
                                    val fileName = "JPEG_${timeStamp}.jpeg"
                                    val dir = File(
                                        Environment.getExternalStorageDirectory(),
                                        EXT_DIR_PROFILE_LOCATION
                                    )

                                    val file = File(dir, fileName)

                                    var job = CoroutineScope(Dispatchers.IO).launch {
                                        util.saveImageToExternalStorage(
                                            EXT_DIR_PROFILE_LOCATION, context,
                                            uri!!, fileName
                                        )
                                    }
                                    job.join()
                                    println("uri = $uri")
                                    uploadFile(
                                        FOLDER_PROFILE,
                                        file.toString()
                                    ).addOnCompleteListener { task ->
                                        val downloadUri = task.result

                                        val data = CreateChannelData(
                                            name.value.text,
                                            downloadUri.toString(),
                                            desc.value.text,
                                            MY_ID,
                                            0,
                                            options[selectedIndex],
                                            System.currentTimeMillis()
                                        )
                                        createGroup(data).addOnCompleteListener {
                                            println(task)
                                            if (task.isSuccessful) {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    database.channelsDao().addNewChannel(
                                                        Channels(
                                                            0,
                                                            name.value.text,
                                                            it.result,
                                                            0,
                                                            desc.value.text,
                                                            0,
                                                            1,
                                                            System.currentTimeMillis(),
                                                            options[selectedIndex]
                                                        )
                                                    )
                                                    withContext(Dispatchers.Main) {
                                                        showDialogState = false
                                                    }
                                                    onDone()
                                                }
                                            }
                                        }

                                    }

                                    println("test uri = $uri")
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 10.dp),
                    elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = colorResource(id = R.color.primary)
                    )
                ) {
                    Text(text = "Create", color = Color.White)
                }
            }


        }

        Disclaimer()
    }

    }

}


@Composable
private fun Disclaimer() {
Card(
    backgroundColor = colorResource(id = R.color.primary_variant),
    shape = RoundedCornerShape(20.dp),
    elevation = 5.dp,
    modifier = Modifier.padding(15.dp)
) {
    val fontf = FontFamily( Font(resId = R.font.slab, weight = FontWeight.Bold))
    Text(text = "Note:\nThis is public channel. This channel is visible to all the users. All the message should follow Terms & condition",
        modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 18.dp, bottom = 18.dp), color = Color.White,
     fontFamily = fontf
    )

}
}