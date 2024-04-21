package com.example.chatapplication.webRTC
import android.app.Application
import android.content.Context
import android.util.Log
import com.example.chatapplication.webRTC.RTCActivity.Companion.TAGGER
import com.example.chatapplication.webRTC.RTCActivity.Companion.calleeId
import com.example.chatapplication.webRTC.RTCActivity.Companion.callerId
import com.google.firebase.firestore.SetOptions

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.webrtc.*

class RTCClient(
    context: Application,
    observer: PeerConnection.Observer
) {

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
        var isCallStarted = false
        var isVideoCall = true;
    }

    private val rootEglBase: EglBase = EglBase.create()

    private var localAudioTrack : AudioTrack? = null
    private var localVideoTrack : VideoTrack? = null
    private var localStream : MediaStream? = null
    val TAG = "RTCClient"

    var remoteSessionDescription : SessionDescription? = null

    val db = Firebase.firestore

    init {
        initPeerConnectionFactory(context)
    }

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val videoCapturer by lazy { getVideoCapturer(context) }

    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints())}
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val peerConnection by lazy { buildPeerConnection(observer) }

    private fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory
            .builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true))
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true
                disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()
    }

    private fun buildPeerConnection(observer: PeerConnection.Observer) = peerConnectionFactory.createPeerConnection(
        iceServer,
        observer
    )

    private fun getVideoCapturer(context: Context)=

        Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
    }

    fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    fun startLocalVideoCapture(localVideoOutput: SurfaceViewRenderer) {
        val surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localAudioTrack = peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + "_audio", audioSource);
        localStream?.addTrack(localAudioTrack)

        Log.e(TAGGER, "isVideoCall $isVideoCall")

            if(isVideoCall) {
                (videoCapturer as VideoCapturer).initialize(
                    surfaceTextureHelper,
                    localVideoOutput.context,
                    localVideoSource.capturerObserver
                )
                videoCapturer!!.startCapture(320, 240, 60)
                localVideoTrack =
                    peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
                localVideoTrack?.addSink(localVideoOutput)
                localStream?.addTrack(localVideoTrack)
            }
        peerConnection?.addStream(localStream)
    }

    private fun PeerConnection.call(sdpObserver: SdpObserver, meetingID: String) {
        Log.e(TAGGER, "Sending Offer")

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        Log.e(TAG, "onSetFailure: $p0")
                    }

                    override fun onSetSuccess() {
                        val offer = hashMapOf(
                            "sdp" to desc?.description,
                            "type" to desc?.type,
                            "timestamp" to System.currentTimeMillis()
                        )
                        db.collection("users").document(calleeId)
                            .set(offer as Map<String, Any>, SetOptions.merge())
                            .addOnSuccessListener {
                                Log.e(TAG, "DocumentSnapshot added")
//                                isCallStarted = true
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error adding document", e)
                            }
                        Log.e(TAG, "onSetSuccess")
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.e(TAG, "onCreateSuccess: Description $p0")
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(TAG, "onCreateFailure: $p0")
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "onSetFailure: $p0")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailure: $p0")
            }
        }, constraints)
    }

    private fun PeerConnection.answer(sdpObserver: SdpObserver, meetingID: String) {
        Log.e(TAGGER, "Sending Answer")

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                val answer = hashMapOf(
                    "sdp" to desc?.description,
                    "type" to desc?.type,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("users").document(callerId)
                    .set(answer as Map<String, Any>, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.e(TAG, "DocumentSnapshot added")
                        isCallStarted = true
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error adding document", e)
                    }
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        Log.e(TAG, "onSetFailure: $p0")
                    }

                    override fun onSetSuccess() {
                        Log.e(TAG, "onSetSuccess")
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.e(TAG, "onCreateSuccess: Description $p0")
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(TAG, "onCreateFailureLocal: $p0")
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailureRemote: $p0")
            }
        }, constraints)
    }

    fun call(sdpObserver: SdpObserver, meetingID: String) = peerConnection?.call(sdpObserver, meetingID)

    fun answer(sdpObserver: SdpObserver, meetingID: String) = peerConnection?.answer(sdpObserver, meetingID)

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        remoteSessionDescription = sessionDescription
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "onSetFailure: $p0")
            }

            override fun onSetSuccess() {
                Log.e(TAG, "onSetSuccessRemoteSession")
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
                Log.e(TAG, "onCreateSuccessRemoteSession: Description $p0")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailure")
            }
        }, sessionDescription)

    }

    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun endCall() {
        try {

        localStream?.removeTrack(localAudioTrack)
        localStream?.removeTrack(localVideoTrack)

        localAudioTrack?.let { audioTrack ->
            audioTrack.dispose()
        }
        localAudioTrack = null

        localVideoTrack?.let { videoTrack ->
            videoTrack.dispose()
        }
        localVideoTrack = null

        peerConnection?.close()
//        rootEglBase?.release()
        rootEglBase.releaseSurface()
        localStream?.dispose()
        isCallStarted = false

        }catch (e: NullPointerException){
            Log.d(TAG, "error in end call = $e")
        }
        catch (e: IllegalStateException){
            Log.d(TAG, "error in end call = $e")
        }


     }

    fun enableVideo(videoEnabled: Boolean) {
        if (localVideoTrack !=null)
            localVideoTrack?.setEnabled(videoEnabled)
    }

    fun enableAudio(audioEnabled: Boolean) {
        if (localAudioTrack != null)
            localAudioTrack?.setEnabled(audioEnabled)
    }
    fun switchCamera() {
        videoCapturer?.switchCamera(null)
    }
}