package com.apexharn.chatapplication.ChatPage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.apexharn.chatapplication.R
import com.apexharn.chatapplication.ui.theme.ChatApplicationTheme
import java.io.File

class ShowImage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var filepath = intent.getStringExtra("filepath")
        setContent {
            ChatApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if(filepath != null)
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(File(filepath))
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
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(id = R.drawable.profile_placeholder),
                        error = painterResource(id = R.drawable.delete_illus),

                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

}

