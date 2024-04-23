package com.example.chatapplication.profile

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.TextField
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import coil.compose.AsyncImage
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.example.Constants
import com.example.Constants.FOLDER_IMAGES
import com.example.Constants.MY_ID
import com.example.Constants.path_users
import com.example.chatapplication.ChatPage.userId
import com.example.chatapplication.R
import com.example.chatapplication.db.Message
import com.example.chatapplication.db.channeldb.Channels
import com.example.chatapplication.firebase.FirestoreDb
import com.example.chatapplication.profile.ui.theme.ChatApplicationTheme
import com.example.util.CreateChannelData
import com.example.util.SendGroupMessage
import com.example.util.SharedPrefConstant.PREF_PROFILE
import com.example.util.util
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : ComponentActivity() {
    var showBottomSheet = mutableStateOf(false)
    var displayName = mutableStateOf("")
    var displayPhone = mutableStateOf("")
    var displayStatus = mutableStateOf("")
    var imageUri = mutableStateOf("")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            ChatApplicationTheme {
                // A surface container using the 'background' color from the theme
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
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen()
                }
            }
        }

        val sharedPref = getSharedPreferences(Constants.PREF, MODE_PRIVATE)
        displayName.value = sharedPref.getString("name","")?:""
        displayStatus.value = sharedPref.getString("status","")?:""
        displayPhone.value = sharedPref.getString("phone","")?:""
        imageUri.value = sharedPref.getString(PREF_PROFILE,"")?:""
        print("data of imageUri = ${imageUri.value}")


    }

     @Composable
    private fun ProfileScreen() {

         val context = LocalContext.current

         val selectImageLauncher = rememberLauncherForActivityResult(
             contract = ActivityResultContracts.GetContent(),
         onResult = { uri: Uri? ->
             CoroutineScope(Dispatchers.IO).launch {
                 if (uri != null) {
                     val imgDir = File(
                         Environment.getExternalStorageDirectory(),
                         Constants.EXT_DIR_PROFILE_LOCATION
                     )
                     imgDir.mkdirs()
                     val timeStamp: String =
                         SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                     val fileName = "JPEG_${timeStamp}.jpeg"
                     val imageFile = File(imgDir, fileName)


                     val job = CoroutineScope(Dispatchers.IO).launch {
                         util.saveImageToExternalStorage(
                             Constants.EXT_DIR_PROFILE_LOCATION,
                             context,
                             uri,
                             fileName
                         )
                     }
                     job.join()

                     util.uploadFile(FOLDER_IMAGES, imageFile.toString())
                         .addOnCompleteListener { task ->
                             val downloadUri = task.result
                             Firebase.firestore.collection(path_users).document(MY_ID).set(
                                 hashMapOf<String, String>(
                                     "profile_url" to downloadUri.toString()
                                 ), SetOptions.merge()
                             )

                             val sharedPref =
                                 getSharedPreferences(Constants.PREF, MODE_PRIVATE).edit()
                             sharedPref.putString(PREF_PROFILE, imageFile.toString()).apply()
                             imageUri.value = imageFile.toString();

                         }
                 }
             }
         }
         )


         Box(modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    colorResource(id = R.color.primary),
                                    colorResource(id = R.color.primary_related),
                                )
                            )
                        )
                ) {
                    IconButton(onClick = { super.onBackPressed() },
                        modifier = Modifier.padding(start = 10.dp, top = 14.dp)) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "", tint = Color.White,)
                    }
                    // Profile Image
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(imageUri.value))
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
                        contentDescription = "",
                        contentScale= ContentScale.FillBounds,
                        modifier = Modifier
                            .size(130.dp)
                            .clickable { }
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .clickable {
                                selectImageLauncher.launch("image/*")
                            }
                    )


                }

                Column(modifier = Modifier.padding(top = 24.dp, start = 21.dp ,end = 15.dp),
                    ) {

                Text(text = "Account Info", fontSize = 23.sp, color = Color.Black,
                    textAlign = TextAlign.Start, lineHeight = 60.sp, letterSpacing = 1.sp,
                    fontFamily = FontFamily(Font(R.font.merriweather, FontWeight.W900)),
                    fontWeight = FontWeight.W900,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp, top = 13.dp)

                )

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 29.dp)) {
                        Icon(painter = painterResource(id = R.drawable.person_border), contentDescription ="",
                            tint = colorResource(id = R.color.primary),
                            modifier = Modifier
                                .size(30.dp)
                                .align(Alignment.CenterVertically))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column (modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f)){
                            Text(text = "Name", color = Color.Black, fontWeight = FontWeight.W600
                            , fontFamily = FontFamily(Font(R.font.merriweather, FontWeight.W800))
                                , fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(text ="${displayName.value}", color = Color.Gray, fontSize = 16.sp)
                        }
                        Icon(Icons.Outlined.Edit, contentDescription = "", tint =colorResource(id = R.color.primary),
                            modifier = Modifier
                                .padding(end = 15.dp)
                                .size(28.dp)
                                .align(Alignment.CenterVertically)
                                .clickable {
                                    showBottomSheet.value = true
                                    BottomSheet("name")
                                })
                    }

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 29.dp)) {
                        Icon(painter = painterResource(id = R.drawable.call), contentDescription ="",
                            tint = colorResource(id = R.color.primary),
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.CenterVertically))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column (modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f)){
                            Text(text = "Mobile", color = Color.Black, fontWeight = FontWeight.W600
                                , fontFamily = FontFamily(Font(R.font.merriweather, FontWeight.W800))
                                , fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(5.dp))

                            Text(text = "${displayPhone.value}", color = Color.Gray, fontSize = 16.sp)
                        }
                        Icon(Icons.Outlined.Edit, contentDescription = "", tint =colorResource(id = R.color.primary),
                            modifier = Modifier
                                .padding(end = 15.dp)
                                .size(28.dp)
                                .align(Alignment.CenterVertically)
                                .clickable {
                                    showBottomSheet.value = true
                                    BottomSheet("phone")
                                })
                    }

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 20.dp)) {
                        Icon(painter = painterResource(id = R.drawable.circle_broken), contentDescription ="",
                            tint = colorResource(id = R.color.primary),
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.CenterVertically))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column (modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f)){
                            Text(text = "Status", color = Color.Black, fontWeight = FontWeight.W600
                                , fontFamily = FontFamily(Font(R.font.merriweather, FontWeight.W800))
                                , fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(text = "${displayStatus.value}", color = Color.Gray, fontSize = 16.sp)
                        }
                        Icon(Icons.Outlined.Edit, contentDescription = "", tint =colorResource(id = R.color.primary),
                            modifier = Modifier
                                .padding(end = 15.dp)
                                .size(28.dp)
                                .align(Alignment.CenterVertically)
                                .clickable {
                                    showBottomSheet.value = true
                                    BottomSheet("status")
                                })
                    }

                }

            }
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                modifier = Modifier
                    .padding(top = 200.dp)
                    .width(150.dp)
                    .height(60.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { super.onBackPressed() }
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    colorResource(id = R.color.gradient3),
                                    colorResource(id = R.color.gradient1),
                                    colorResource(id = R.color.gradient2),
                                    colorResource(id = R.color.gradient3),
                                    colorResource(id = R.color.gradient3),
                                    colorResource(id = R.color.gradient1),
//                                    colorResource(id = R.color.gradient2),
                                    colorResource(id = R.color.gradient3),

                                    )
                            )
                        ),
                    verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Back to Home",  fontFamily = FontFamily(Font(R.font.merriweather, FontWeight.W800)), color = Color.White)

                }

            }
        }
    }

    private fun BottomSheet(heading: String){

        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)

        val btnClose = view.findViewById<TextView>(R.id.cancelButton)
        val okButton = view.findViewById<Button>(R.id.okButton)
        val headingText = view.findViewById<TextView>(R.id.headingText)
        val editText = view.findViewById<TextInputEditText>(R.id.outlinedTextField)
        headingText.setText("Edit your $heading")
        editText.setText(
            if(heading == "name") displayName.value
            else if (heading == "status") displayStatus.value
            else displayPhone.value
        )


        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        okButton.setOnClickListener{
            if(editText.text.toString().isEmpty()) return@setOnClickListener

            val sharedPref = getSharedPreferences(Constants.PREF, MODE_PRIVATE).edit()
            if(heading == "name"){
                sharedPref.putString("name", editText.text.toString())
                displayName.value =  editText.text.toString()
            }
            else if(heading == "status"){
                sharedPref.putString("status", editText.text.toString())
                displayStatus.value = editText.text.toString()
            }
            else if(heading == "phone"){
                if(editText.text.toString().startsWith("+91") && editText.text.toString().length == 10) {
                    sharedPref.putString("phone", editText.text.toString().substring(3))
                    displayPhone.value =  editText.text.toString()
                }
                else if (editText.text.toString().length == 10) {
                    sharedPref.putString("phone", "+91 ${editText.text.toString()}")
                    displayPhone.value =   "+91 ${editText.text.toString()}"
                }
                else
                    Toast.makeText(this,"Enter correct phone number",Toast.LENGTH_LONG).show()
            }
            sharedPref.apply()
            dialog.dismiss()

        }

        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    fun BottomSheetToEdit(
        heading: String,
        defaultText: String,
        onOkClick: () -> Unit,
        onCancelClick: () -> Unit
    ) {

        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()

        Scaffold(

        ) {
            val pad= it
            ModalBottomSheet(
                sheetState = sheetState,
                contentColor =colorResource(id = R.color.background),
                dragHandle = {},
                containerColor = colorResource(id = R.color.background),
                shape = RoundedCornerShape(topEnd = 13.dp, topStart = 13.dp),
                onDismissRequest = {
                   onCancelClick()
                },
             ) {
                     // Bottom sheet content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var messageText by remember { mutableStateOf("") }
                        LaunchedEffect(heading){
                            messageText = defaultText
                        }
                        Text(
                            text = heading,
                            color = Color.Black,
                            fontWeight = FontWeight.W600,
                            fontFamily = FontFamily(Font(R.font.merriweather, FontWeight.W800)),
                            fontSize = 18.sp,
                            modifier = Modifier
                                .padding(top = 0.dp, bottom = 10.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        TextField(value = "", onValueChange ={} )
                        // Outlined text box
                        OutlinedTextField(
                            value = "",
                            onValueChange = { messageText = it },
//                            textStyle = TextStyle(fontSize = 18.sp, color = Color.Black,),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = colorResource(id = R.color.primary),
//                                cursorColor = colorResource(id = R.color.primary),
                                unfocusedBorderColor = colorResource(id = R.color.primary).copy(
                                    0.7f
                                )
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { /* Handle keyboard done action */ })
                        )

                        // Spacer
                        Spacer(modifier = Modifier.height(16.dp))

                        // Row with OK and Cancel buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            // Cancel button
                            TextButton(onClick = {
                                onCancelClick()
                                scope.launch { sheetState.hide() }
                            }) {
                                Text("Cancel", color = Color.Black)
                            }
                            // Spacer between buttons
                            Spacer(modifier = Modifier.width(8.dp))
                            // OK button
                            Button(
                                onClick = {
                                    scope.launch { sheetState.hide() }
                                    onOkClick()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = colorResource(
                                        id = R.color.primary
                                    )
                                )
                            ) {
                                Text(" OK ", color = Color.White, fontWeight = FontWeight.W600)
                            }
                        }
                    }
                }

         }
    }


}

