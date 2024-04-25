package com.example.chatapplication.adduser

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings.Global.getString
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
 import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContentResolverCompat
import androidx.core.content.ContextCompat.getString
import androidx.core.view.isGone
import com.example.chatapplication.ChatPage.ChatActivity
import com.example.chatapplication.MainActivity
import com.example.chatapplication.R
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Sender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class Contact(
    val name: String,
    val phoneNumber: String,
    var isConnected: Boolean = false
)

@Composable
fun ContactList(contacts:List<Contact>,onBackPressed:()->Unit) {
    val context = LocalContext.current
    var sequenceHeading = remember { mutableStateOf((-1)) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(verticalArrangement = Arrangement.Center) {
                        Text("Select contact", color = Color.White, fontSize = 19.sp)
//                        Spacer(modifier = Modifier.height(1.dp))
                        Text("${contacts.size} contacts", color = Color.White.copy(0.6f), fontSize = 15.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() },
                        modifier = Modifier.padding(start = 5.dp)) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription ="", tint = Color.White )
                    }
                                 },
                backgroundColor = colorResource(id = R.color.primary)
                
            )
        }
    ) {
         LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(contacts) { contact ->

                var currentHeading = when(contact.isConnected){
                    true -> 1
                    else -> 2
                }
                if(currentHeading != sequenceHeading.value){
                    println("currentHeading $currentHeading  && sequeHeading ${sequenceHeading.value}")
                     sequenceHeading.value =currentHeading
                     if(currentHeading == 1){
                        Text(
                            text = "Contacts on Glue Connect",
                            textAlign = TextAlign.Start,
                            color = colorResource(id = R.color.primary),
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(0.9f)
                                .padding(start = 15.dp, top = 10.dp, bottom = 15.dp)
                        )
                    }else if(currentHeading == 2){
                        println("Inviting")
                        Text(
                            text = "Invite to Glue Connect",
                            textAlign = TextAlign.Start,
                            color = colorResource(id = R.color.primary),
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(0.9f)
                                .padding(start = 15.dp, top = 10.dp, bottom = 15.dp)
                        )
                    }
                }
                ContactItem(contact,{
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                        ChatDatabase.getDatabase(context).senderDao()
                            .insertNewSender(Sender(0, contact.name, contact.phoneNumber, 0))
                        }catch (e: SQLiteConstraintException){
                            e.printStackTrace()
                        }
                        val intent =Intent(context, ChatActivity::class.java)
                        intent.putExtra("id",contact.phoneNumber)
                        intent.putExtra("type","individual")
                        intent.putExtra("displayName",contact.name)
                        context.startActivity(intent)
                    }

                },{
                    try {
                        // Create an intent to launch the SMS app
                        val smsIntent = Intent(Intent.ACTION_VIEW)

                        // Set the data URI for sending an SMS
                        smsIntent.data = Uri.parse("smsto:${contact.phoneNumber}")

                        // Add the message body to the intent
                        smsIntent.putExtra("sms_body", context.getString(R.string.sms_body))

                        // Launch the SMS app
                        context.startActivity(smsIntent)
                    } catch (e: Exception) {
                        // Handle any exceptions that may occur
                        e.printStackTrace()
                        Log.e("SendMessage", "Error sending SMS: $e")
                        Toast.makeText(context, "Error sending SMS", Toast.LENGTH_SHORT).show()
                    }
                })
                Spacer(modifier = Modifier.height(9.dp))
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact, onClick:() ->Unit, onInvite:()-> Unit) {
    Row    (
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile_placeholder),
            contentDescription = "",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(40.dp)
                .clip(shape = RoundedCornerShape(10.dp))
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(20.dp))


        Text(
            text = contact.name,
             fontSize = 17.sp,
            color = Color.Black.copy(0.8f),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            , maxLines = 1,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )


        Spacer(modifier = Modifier.width(11.dp))
        if (!contact.isConnected)
            CompositionLocalProvider(LocalRippleTheme provides MainActivity.ButtonRippleTheme) {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(

                    ),
                    onClick = {
                        onInvite()
                    },
                    modifier = Modifier.clip(RoundedCornerShape(80.dp))
                ) {
                    Text(
                        text = "Invite",
                        color = colorResource(id = R.color.primary),
                     )
                }
            }

    }

}

fun retrieveContacts(contentResolver: ContentResolver): MutableList<Contact> {
    val contacts = mutableListOf<Contact>()
    val cursor: Cursor? = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (it.moveToNext()) {
            val name = it.getString(nameIndex)
            var phoneNumber = it.getString(numberIndex)
            phoneNumber = phoneNumber.replace(" ","").trim()
            if(phoneNumber.length ==12 && phoneNumber.startsWith("91")){
                phoneNumber = "+$phoneNumber"
            }
            else if(!phoneNumber.startsWith("+91"))
                phoneNumber = "+91$phoneNumber"

//            val contact = if(phoneNumber == "+919682262005")Contact(name, phoneNumber, true) else Contact(name, phoneNumber)
            val contact=Contact(name, phoneNumber)
            contacts.add(contact)
        }
    }

    return contacts
}

