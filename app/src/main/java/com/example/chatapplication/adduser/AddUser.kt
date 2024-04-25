package com.example.chatapplication.adduser

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chatapplication.R
import com.example.chatapplication.firebase.FirestoreDb.checkPhoneNumbersInFirestore
import com.example.chatapplication.ui.theme.ChatApplicationTheme
import com.example.chatapplication.webRTC.RTCActivity
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddUser : ComponentActivity() {

    private val CONTACT_PERMISSION_CODE =121
    private var contacts = mutableStateListOf<Contact>()
    private var showDialogState = mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
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

                    LaunchedEffect(Unit) {
//                        val deviceContacts = retrieveContacts(this@AddUser.contentResolver)
//                        val sortedDeviceContacts = deviceContacts.sortedBy { it.name }
//                        val ans = sortedDeviceContacts.distinctBy { it.phoneNumber }
//                        val finalSortedContacts = ans.sortedBy { !it.isConnected }
//                        contacts.addAll(finalSortedContacts)


                    }

                    if (showDialogState.value) {
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
                    ContactList(contacts){
                        super.onBackPressed()
                    }
                }
            }
        }
        checkContactPermission()
    }

    private fun checkContactPermission() {
        if ((ContextCompat.checkSelfPermission(this, RTCActivity.READ_CONTACT_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) ) {
            requestContactPermission()
        } else {
            // permission already granted
         }
    }

    private fun showPermissionRationaleDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Contact Permission Required")
            .setMessage("This app need to access contacts")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestContactPermission(true)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this,"Permission Denied", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun requestContactPermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, RTCActivity.READ_CONTACT_PERMISSION) &&
            !dialogShown) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(
                RTCActivity.READ_CONTACT_PERMISSION
            ), CONTACT_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CONTACT_PERMISSION_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permission granted
                CoroutineScope(Dispatchers.IO).launch {
                    showDialogState.value = true
                    val deviceContacts = retrieveContacts(this@AddUser.contentResolver)
                    // Sort device contacts by name
                    val sortedDeviceContacts = deviceContacts.sortedBy { it.name }.distinctBy { it.phoneNumber }

                    // Extract phone numbers from device contacts
                    val devicePhoneNumbers = sortedDeviceContacts.mapNotNull { it.phoneNumber }

                    val chunkSize = 30
                    val devicePhoneChunks = devicePhoneNumbers.chunked(chunkSize)

                    // List to hold the combined results from Firestore
                    val connectedUserPhoneNumbers = mutableListOf<String>()

                    devicePhoneChunks.forEach { phoneChunk ->
                        val chunkResult = checkPhoneNumbersInFirestore(phoneChunk)
                        connectedUserPhoneNumbers.addAll(chunkResult)
                    }

                    sortedDeviceContacts.forEach { contact ->
                        contact.isConnected = connectedUserPhoneNumbers.contains(contact.phoneNumber)
                    }

                    val finalSortedContacts0 = sortedDeviceContacts.sortedBy { it.name }
                    val finalSortedContacts = finalSortedContacts0.sortedBy { !it.isConnected }
                    contacts.addAll(finalSortedContacts)
                    showDialogState.value = false

                }
            } else {
                // Permission denied
                checkContactPermission()

            }
        }
    }

}

