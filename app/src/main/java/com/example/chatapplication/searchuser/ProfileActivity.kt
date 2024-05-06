package com.example.chatapplication.searchuser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.chatapplication.R
import com.example.chatapplication.searchuser.ui.theme.ChatApplicationTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class ProfileActivity : ComponentActivity() {
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
                    color = colorResource(id = R.color.primary).copy(0.1f)
                ) {
                    Header()
                 }
            }
        }
    }
}

