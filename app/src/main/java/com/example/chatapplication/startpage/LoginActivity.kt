
package com.example.chatapplication.startpage

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Constants
import com.example.Constants.MY_ID
import com.example.Constants.PREF
import com.example.Constants.feild_phone
import com.example.Constants.path_users
import com.example.chatapplication.MainActivity
import com.example.chatapplication.R
import com.example.chatapplication.startpage.ui.theme.ChatApplicationTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import com.mukeshsolanki.OTP_VIEW_TYPE_BORDER
import com.mukeshsolanki.OtpView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.NumberFormatException
import java.util.concurrent.TimeUnit


class LoginActivity : ComponentActivity() {
    val TAG = "Login Activity"
    var auth = FirebaseAuth.getInstance()
    lateinit var firebaseAuthSettings: FirebaseAuthSettings
     lateinit var phoneNumber: String
    lateinit var  resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var  storedVerificationId: String
    var isOtpView = mutableStateOf(false)
    var showProgress = mutableStateOf(false)


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
                    systemUiController.isStatusBarVisible = false // Status & Navigation bars

                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(id = R.color.background)
                ) {
                    GetPhoneNumber(this)
                 }
            }
        }

        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

          firebaseAuthSettings = auth.firebaseAuthSettings
        // Force reCAPTCHA flow
        FirebaseAuth.getInstance().getFirebaseAuthSettings().forceRecaptchaFlowForTesting(true);

    }
     private fun optEntered(code: String){
         showProgress.value = true
         val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
         signInWithPhoneAuthCredential(credential)
         firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(storedVerificationId!!, code)
      }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    showProgress.value = false

                    val user = task.result?.user
                    GlobalScope.launch {
                        Firebase.firestore.collection(path_users).document(phoneNumber).set(
                            hashMapOf(feild_phone to phoneNumber) , SetOptions.merge()
                        )
                    }
                    val sharedPref = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                    sharedPref.putString("userId",phoneNumber)
                    sharedPref.putString(feild_phone,phoneNumber)
                        .apply()
                    MY_ID = phoneNumber

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        showProgress.value = false
                        Toast.makeText(this,"Invalid verification code",Toast.LENGTH_LONG).show()
                    }
                    // Update UI
                }
            }
    }

    private  fun onSendClicked(phone: String, context: Activity){
        phoneNumber = phone
        CoroutineScope(Dispatchers.IO).launch {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(context) // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)

                withContext(Dispatchers.Main) {
                    Toast.makeText(baseContext, "OTP sent successfully", Toast.LENGTH_SHORT).show()

            }
        }
    }


        private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                showProgress.value = false
                Log.d(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
            }

    }

    override fun onBackPressed() {
        if (isOtpView.value)
            isOtpView.value =false
        else
          super.onBackPressed()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GetPhoneNumber(context: Activity) {
        var phoneNumber by remember { mutableStateOf("") }
         isOtpView =  remember { mutableStateOf(false) }
        var timeToResend by remember { mutableStateOf(0) }
        val context = LocalContext.current






        // Content for OTP verification screen
        Surface(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(!isOtpView.value)
                item {
                    Image(
                        painter = painterResource(id = R.drawable.illus), contentDescription = "",
                        modifier = Modifier
                            .padding(10.dp)
                            .size(350.dp)

                     )
                    Text(
                        text = "Welcome Back!", fontSize = 45.sp, color = Color.Black,
                        textAlign = TextAlign.Start, lineHeight = 60.sp, letterSpacing = 1.sp,
                        fontFamily = FontFamily(Font(R.font.merriweather, FontWeight.W900)),
                        fontWeight = FontWeight.W900,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, start = 10.dp)
                    )

                    Text(
                        text = "Enter Phone Number", fontSize = 20.sp, color = Color.Black,
                        textAlign = TextAlign.Start, lineHeight = 60.sp, letterSpacing = 1.sp,
                        fontFamily = FontFamily(Font(R.font.robot_slab, FontWeight.W900)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp, start = 10.dp)

                    )

                    TextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        textStyle = TextStyle(
                            fontSize = 21.sp,
                            fontFamily = FontFamily(Font(R.font.robot_slab, FontWeight.W700)),
                        ),
                        maxLines = 1,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_call),
                                contentDescription = "",
                                tint = colorResource(id = R.color.primary)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = colorResource(id = R.color.primary),
                            unfocusedIndicatorColor = colorResource(id = R.color.primary).copy(0.7f),
                            cursorColor = colorResource(id = R.color.primary).copy(0.7f),
                            backgroundColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { onSendClicked("+91$phoneNumber", context as Activity)
                                  isOtpView.value = !isOtpView.value},
                         modifier = Modifier

                             .padding(bottom = 20.dp, start = 10.dp, end = 10.dp)
                             .clip(RoundedCornerShape(20.dp))
                             .background(colorResource(id = R.color.primary))
                    ) {
                        Text(
                            text = "Send OTP",
                            color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(horizontal = 80.dp),
                            fontFamily = FontFamily(Font(R.font.robot_slab, FontWeight.W700)),
                            fontWeight = FontWeight.W900,
                        )
                    }
                }
                else // otp screen
                    item {
                        val focusRequester = remember { FocusRequester() }
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                            CoroutineScope(Dispatchers.IO).launch {
                                timeToResend= 59;
                               while(timeToResend > 0){
                                    delay(1000)
                                   timeToResend--;
                                }
                            }
                        }

                        Image(
                            painter = painterResource(id = R.drawable.otp_illus), contentDescription = "",
                            modifier = Modifier
                                .padding(top = 30.dp, start = 13.dp, end = 13.dp)
                                .size(280.dp)

                        )


                        Text(
                            text = "Enter verification code sent to\n+${phoneNumber}", fontSize = 18.sp, color = Color.Black,
                            textAlign = TextAlign.Start, lineHeight = 28.sp, letterSpacing = 1.sp,
                            fontFamily = FontFamily(Font(R.font.sedan, FontWeight.W900)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp)

                        )

                        Column (modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = {
                                    onSendClicked(phoneNumber, context as Activity)
                                },
                                modifier = Modifier
                                     .align(Alignment.End)

                            ) {
                            Text(
                                text = if (timeToResend > 0) "resend in ${timeToResend} sec"
                                else "resend >",
                                fontSize = 14.sp,
                                color = colorResource(id = R.color.primary),
                                textAlign = TextAlign.Start,
                                fontFamily = FontFamily(Font(R.font.robot_slab, FontWeight.W900)),
                                modifier = Modifier.padding(2.dp)
                                   
                            )
                        }
                        }

                        var otpValue by remember { mutableStateOf("") }
                        OtpView(
                            otpText = otpValue,
                            onOtpTextChange = {
                                otpValue = it
                                Log.d("Actual Value", otpValue)
                            },
                            type = OTP_VIEW_TYPE_BORDER,
                            otpCount = 6,
                             containerSize = 48.dp,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            charColor = Color.Black,
                            modifier = Modifier
                                .padding(bottom = 30.dp, top = 5.dp)
                                .focusRequester(focusRequester)
                        )

                        TextButton(
                            onClick = {
                                if(otpValue.length == 6)
                                   optEntered(otpValue)
                                else
                                    Toast.makeText(context,"Enter 6 digit code",Toast.LENGTH_LONG).show()
                                      },
                            modifier = Modifier

                                .padding(bottom = 20.dp, start = 10.dp, end = 10.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(colorResource(id = R.color.primary))
                        ) {
                            Text(
                                text = "Verify",
                                color = Color.White,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(horizontal = 40.dp),
                                fontFamily = FontFamily(Font(R.font.robot_slab, FontWeight.W700)),
                                fontWeight = FontWeight.W900,
                            )
                        }
                    }
            }

            Box(modifier = Modifier.fillMaxSize()){
                if(showProgress.value)
                    AlertDialog(
                        onDismissRequest = { },
                        modifier = Modifier.background(Color.Transparent),
                    ){
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                        ) {
                    CircularProgressIndicator(
                    color = colorResource(id = R.color.primary),
                    modifier = Modifier.align(Alignment.CenterHorizontally))
                        }

                    }
            }
            }
        }

    }




