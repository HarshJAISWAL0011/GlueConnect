package com.example.chatapplication.webRTC

import android.content.Context

interface IncomingCallListener {
    fun onVideoCallReceived(callerId : String, context: Context)
    fun onAudioCallReceived(callerId : String, context: Context)
}