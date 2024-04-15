package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.room.withTransaction
import com.example.Constants
import com.example.Constants.MESSAGE_TYPE_AUDIO
import com.example.Constants.MESSAGE_TYPE_IMAGE
import com.example.Constants.type
import com.example.chatapplication.db.channeldb.ChannelMessage
import com.example.chatapplication.db.groupdb.Group
import com.example.chatapplication.db.groupdb.GroupMember
import com.example.retrofit.RetrofitBuilder
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object util {

    fun getHourAndMinuteFromTimestamp(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val min = String.format("%02d", calendar.get(Calendar.MINUTE))
        val period = if (hour < 12) "AM" else "PM"
        val formattedHour = if (hour % 12 == 0) 12 else hour % 12
        return "$formattedHour:$min $period"
    }

    fun formatTime(timestamp: Long?): String {
        if(timestamp == null) return ""
        val calendar = Calendar.getInstance().apply {
            if (timestamp != null) {
                timeInMillis = timestamp
            }
        }
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }


    fun getMinSecond(milliseconds: Int): String {
//        val hours = (milliseconds / (1000 * 60 * 60)) % 24
        val minutes = (milliseconds / (1000 * 60)) % 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d",  minutes, seconds)
    }


    suspend fun saveImageToExternalStorage(path:String, context: Context, uri: Uri,fileName: String) {

            try {
                val imgDir = File(Environment.getExternalStorageDirectory(),path)
                imgDir.mkdirs()
                val imageFile = File(imgDir, fileName)

                val bitmap =
                    BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))


                FileOutputStream(imageFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 5, outputStream)
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH,  Environment.DIRECTORY_PICTURES)
                }

                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

            } catch (e: IOException) {
                e.printStackTrace()
                println("error = ${e.message}")
            }

     }

    suspend fun loadImageFromExternalStorage(filepath: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(filepath)

                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    null
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

     fun uploadFile(location: String, uri: String): Task<Uri> {
        println("Uploading file")
        val storage = Firebase.storage
        val storageRef = storage.reference
        var file = Uri.fromFile(File(uri))
        val riversRef = storageRef.child("${location}/${file.lastPathSegment}")
        val uploadTask = riversRef.putFile(file)

         return uploadTask.continueWithTask { task ->
             if (!task.isSuccessful) {
                 task.exception?.let {
                     println("failure uploading file ${task.exception?.message}")
                     throw it
                 }
             }
             riversRef.downloadUrl
         }
    }

    suspend fun URLdownloadFile(link: String, messageType: String, senderId: String): File?{
        try {
            val url = URL(link)
            val contentType = when(messageType){
                 MESSAGE_TYPE_IMAGE ->"/Images"
                "audio" ->"/Audios"
                else -> "/Images"
            }

            val extension = when(messageType){
                MESSAGE_TYPE_IMAGE ->"jpeg"
                "audio" ->"wav"
                else -> "jpeg"
            }
            val time = System.currentTimeMillis()
            val fileName = "${senderId}_$time.$extension"
            val directory = Environment.getExternalStorageDirectory()
            val file = File("$directory/Chat/Received",contentType )
            file.mkdirs()

            val filepath = File(file,fileName)

            val connection = url.openConnection()
            connection.connect()

            val inputStream = connection.getInputStream()

            if(messageType == MESSAGE_TYPE_AUDIO) {
                FileOutputStream(filepath).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }else if(messageType == MESSAGE_TYPE_IMAGE) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                FileOutputStream(filepath).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }

            withContext(Dispatchers.IO) {// Just in case you call it from main thread
                inputStream.close()
            }
            return filepath
        } catch (e: Exception) {
            e.printStackTrace()
            println("Exception in downloading file ${e.message}")

        }
        return null
    }

    fun sendChannelMessage(data: ChannelMessageData){
        val apiService = RetrofitBuilder.create()
        apiService.channel_message(data).enqueue((object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {

                } else {
                    // Handle error response
                    println("Failed Group create message result " + response.message())
                }
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                println("Failed Group create message result " + t.message + " "+ call)
            }
        }))
    }





}
