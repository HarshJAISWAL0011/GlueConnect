package com.example.chatapplication.webRTC

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import com.example.Constants
import com.example.chatapplication.MainActivity
import com.example.chatapplication.R
import com.example.chatapplication.webRTC.RTCClient.Companion.isCallStarted
import com.example.chatapplication.webRTC.RTCClient.Companion.isVideoCall
import com.example.chatapplication.webRTCon.PeerConnectionObserver
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer




class RTCActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
        const val READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
        const val WRITE_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        const val READ_CONTACT_PERMISSION = Manifest.permission.READ_CONTACTS
         const val TAGGER = "Check:"
         var isJoin = false
        var callerId = ""
        var calleeId =""

    }

    private lateinit var rtcClient: RTCClient
    private lateinit var signallingClient: SignalingClient

    private val audioManager by lazy { RTCAudioManager.create(this) }

    val TAG = "MainActivity"

    private var target : String = "test-call"


    private var isMute = false

    private var isVideoPaused = false

    private var inSpeakerMode = true

    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
        }
    }

    private lateinit var audio_output_button: ImageView
    private lateinit var video_button: ImageView
    private lateinit var switch_camera_button: ImageView
    private lateinit var mic_button: ImageView
    private lateinit var end_call_button: ImageView
    private lateinit var remote_view: SurfaceViewRenderer
    private lateinit var local_view: SurfaceViewRenderer
    private lateinit var remote_view_loading: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_call)

            audio_output_button = findViewById(R.id.audio_output_button)
            video_button = findViewById(R.id.video_button)
            switch_camera_button = findViewById(R.id.switch_camera_button)
            mic_button = findViewById(R.id.mic_button)
            end_call_button = findViewById(R.id.end_call_button)
            remote_view_loading = findViewById(R.id.remote_view_loading)
            remote_view = findViewById(R.id.remote_view)
            local_view = findViewById(R.id.local_view)

            if (intent.hasExtra("calleeId"))
                calleeId = intent.getStringExtra("calleeId")!!
            if (intent.hasExtra("callerId"))
               callerId = intent.getStringExtra("callerId")!!
            if (intent.hasExtra("isJoin"))
                isJoin = intent.getBooleanExtra("isJoin", false)
        if (intent.hasExtra("isVideoCall"))
            isVideoCall = intent.getBooleanExtra("isVideoCall", true)

        Log.d(TAGGER, "callerId = $callerId callee = $calleeId join = $isJoin")

        Constants.CURRENT_ACTIVITY = "RTCActivity"
        Constants.CURRENT_ACTIVITY_ID = ""

            checkCameraAndAudioPermission()
            audioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
            switch_camera_button.setOnClickListener {
                rtcClient.switchCamera()
            }

            audio_output_button.setOnClickListener {
                if (inSpeakerMode) {
                    inSpeakerMode = false
                    audio_output_button.setImageResource(R.drawable.ic_baseline_hearing_24)
                    audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
                } else {
                    inSpeakerMode = true
                    audio_output_button.setImageResource(R.drawable.ic_baseline_speaker_up_24)
                    audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
                }
            }
            video_button.setOnClickListener {
                if (isVideoPaused) {
                    isVideoPaused = false
                    video_button.setImageResource(R.drawable.ic_baseline_videocam_off_24)
                } else {
                    isVideoPaused = true
                    video_button.setImageResource(R.drawable.ic_baseline_videocam_24)
                }
                rtcClient.enableVideo(isVideoPaused)
            }
            mic_button.setOnClickListener {
                if (isMute) {
                    isMute = false
                    mic_button.setImageResource(R.drawable.ic_baseline_mic_off_24)
                } else {
                    isMute = true
                    mic_button.setImageResource(R.drawable.ic_baseline_mic_24)
                }
                rtcClient.enableAudio(isMute)
            }
            
            end_call_button.setOnClickListener {
                GlobalScope.launch {

                    Firebase.firestore.collection("users").document(callerId).update(
                        hashMapOf("type" to Constants.END_CALL,
                            "sdp" to "") as Map<String, Any>
                    )
                    Firebase.firestore.collection("users").document(calleeId).update(
                        hashMapOf("type" to Constants.END_CALL,
                            "sdp" to "") as Map<String, Any>
                    )
                }
                rtcClient.endCall()
                remote_view.isGone = false
                remote_view.release()
                local_view.release()

                 finish()
                startActivity(Intent(this@RTCActivity, MainActivity::class.java))
            }
        }
    

        private fun checkCameraAndAudioPermission() {
            if ((ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION)
                        != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this,AUDIO_PERMISSION)
                        != PackageManager.PERMISSION_GRANTED)) {
                requestCameraAndAudioPermission()
            } else {
                onCameraAndAudioPermissionGranted()
            }
        }

        private fun onCameraAndAudioPermissionGranted() {
            rtcClient = RTCClient(
                application,
                object : PeerConnectionObserver() {
                    override fun onIceCandidate(p0: IceCandidate?) {
                        super.onIceCandidate(p0)
                        signallingClient.sendIceCandidate(p0, isJoin)
                        rtcClient.addIceCandidate(p0)
                    }

                    override fun onAddStream(p0: MediaStream?) {
                        super.onAddStream(p0)
                        try{
                        Log.e(TAG, "onAddStream: $p0")
                        p0?.videoTracks?.get(0)?.addSink(remote_view)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                    }

                    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                        Log.e(TAG, "onIceConnectionChange: $p0")
                    }

                    override fun onIceConnectionReceivingChange(p0: Boolean) {
                        Log.e(TAG, "onIceConnectionReceivingChange: $p0")
                    }

                    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                        Log.e(TAG, "onConnectionChange: $newState")
                    }

                    override fun onDataChannel(p0: DataChannel?) {
                        Log.e(TAG, "onDataChannel: $p0")
                    }

                    override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                        Log.e(TAG, "onStandardizedIceConnectionChange: $newState")
                    }

                    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
                        Log.e(TAG, "onAddTrack: $p0 \n $p1")
                    }

                    override fun onTrack(transceiver: RtpTransceiver?) {
                        Log.e(TAG, "onTrack: $transceiver" )
                    }
                }
            )

            rtcClient.initSurfaceView(remote_view)
            rtcClient.initSurfaceView(local_view)
            rtcClient.startLocalVideoCapture(local_view)
            signallingClient =  SignalingClient(target,createSignallingClientListener())
            if (!isJoin)
                rtcClient.call(sdpObserver,target)
        }

        private fun createSignallingClientListener() = object : SignalingClientListener {
            override fun onConnectionEstablished() {
                end_call_button.isClickable = true
            }

            override fun onOfferReceived(description: SessionDescription) {
                Log.d(TAGGER," offer received")
                rtcClient.onRemoteSessionReceived(description)
                 rtcClient.answer(sdpObserver,target)
                remote_view_loading.isGone = true
            }

            override fun onAnswerReceived(description: SessionDescription) {
                rtcClient.onRemoteSessionReceived(description)
                remote_view_loading.isGone = true
                isCallStarted = true
                Log.d(TAGGER," answer received")
            }

            override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
                rtcClient.addIceCandidate(iceCandidate)
            }

            override fun onCallEnded() {
                 rtcClient.endCall()
                isCallStarted =false
                finish()
                startActivity(Intent(this@RTCActivity, MainActivity::class.java))

            }
        }

        private fun requestCameraAndAudioPermission(dialogShown: Boolean = false) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, AUDIO_PERMISSION) &&
                !dialogShown) {
                showPermissionRationaleDialog()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION, AUDIO_PERMISSION), CAMERA_AUDIO_PERMISSION_REQUEST_CODE)
            }
        }

        private fun showPermissionRationaleDialog() {
            AlertDialog.Builder(this)
                .setTitle("Camera And Audio Permission Required")
                .setMessage("This app need the camera and audio to function")
                .setPositiveButton("Grant") { dialog, _ ->
                    dialog.dismiss()
                    requestCameraAndAudioPermission(true)
                }
                .setNegativeButton("Deny") { dialog, _ ->
                    dialog.dismiss()
                    onCameraPermissionDenied()
                }
                .show()
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == CAMERA_AUDIO_PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onCameraAndAudioPermissionGranted()
            } else {
                onCameraPermissionDenied()
            }
        }

        private fun onCameraPermissionDenied() {
            Toast.makeText(this, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
        }

    override fun onBackPressed() {
        rtcClient.endCall()
        super.onBackPressed()
    }

    override fun onDestroy() {
            signallingClient.destroy()
            super.onDestroy()
        }
    }