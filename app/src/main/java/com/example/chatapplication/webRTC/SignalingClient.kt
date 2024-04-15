package com.example.chatapplication.webRTC

import android.annotation.SuppressLint
import android.util.Log
import com.example.Constants.END_CALL
import com.example.Constants.MY_ID
import com.example.chatapplication.webRTC.RTCActivity.Companion.TAGGER
import com.example.chatapplication.webRTC.RTCActivity.Companion.calleeId
import com.example.chatapplication.webRTC.RTCActivity.Companion.callerId
import com.example.chatapplication.webRTC.RTCActivity.Companion.isJoin
 import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

@ExperimentalCoroutinesApi
class SignalingClient(
    private val meetingID : String,
    private val listener: SignalingClientListener
) : CoroutineScope {

    companion object {
        private const val HOST_ADDRESS = "192.168.0.12"
    }



    private val job = Job()

    val TAG = "Check:"

    val db = Firebase.firestore

    private val gson = Gson()

    var SDPtype : String? = null
    override val coroutineContext = Dispatchers.IO + job

//    private val client = HttpClient(CIO) {
//        install(WebSockets)
//        install(JsonFeature) {
//            serializer = GsonSerializer()
//        }
//    }

    private val sendChannel = ConflatedBroadcastChannel<String>()

    init {
        connect()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun connect() = launch {
        db.enableNetwork().addOnSuccessListener {
            listener.onConnectionEstablished()
        }
        val sendData = sendChannel.trySend("").isSuccess

        try {
            db.collection("users")
                .document(MY_ID)
                .addSnapshotListener { snapshot, e ->

                    if (e != null) {
                        Log.w(TAG, "listen:error", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data ?: return@addSnapshotListener

                        if (!data.containsKey("timestamp")) return@addSnapshotListener

                        var timestamp = data.get("timestamp") as Long
                        val currentTimeMillis = System.currentTimeMillis()
                        val differenceMillis = currentTimeMillis - timestamp

                        if (data.containsKey("status") &&
                            data.getValue("status").toString() == END_CALL &&
                            RTCClient.isCallStarted
                        ) {
                            listener.onCallEnded()
                            Log.d(TAG, "call was ended")
                            SDPtype = "End Call"

                            Log.d(TAGGER, "Current data: ${snapshot.data}")
                            return@addSnapshotListener
                        }

                        if (differenceMillis > 10 * 1000) return@addSnapshotListener

                        Log.d(TAGGER, " Listening on answer/offer ")

                        if (data.containsKey("type") && data["type"] == "OFFER"
                            && isJoin
                        ) {
                            listener.onOfferReceived(
                                SessionDescription(
                                    SessionDescription.Type.OFFER, data["sdp"].toString()
                                )
                            )
                            SDPtype = "Offer"
                        }
                        if (data.containsKey("type") && data["type"] == "ANSWER" &&
                            !isJoin
                        ) {
                            Log.d(TAGGER, "${
                                data["sdp"]}")
                            listener.onAnswerReceived(
                                SessionDescription(
                                    SessionDescription.Type.ANSWER, data["sdp"].toString()
                                )
                            )
                            SDPtype = "Answer"
                        }
                    }
                            else {
                            Log.d(TAGGER, "Current data: null")
                        }

                }

                    db.collection("users").document(MY_ID)
                        .collection("calls")
                        .document("IceCandidate")
                        .addSnapshotListener{ dataSnapShot,e->
                    if (e != null) {
                        Log.w(TAGGER, "listen:error", e)
                        return@addSnapshotListener
                    }

                    if (dataSnapShot != null && dataSnapShot.exists()) {

                        val data = dataSnapShot.data ?: return@addSnapshotListener

                        if(!data.containsKey("timestamp")) return@addSnapshotListener

                        var timestamp = data?.get("timestamp") as Long
                        timestamp = timestamp?:0
                        val currentTimeMillis = System.currentTimeMillis()
                        val differenceMillis = currentTimeMillis - timestamp

                        if(differenceMillis > 10 *1000) return@addSnapshotListener
                                Log.e(TAGGER, "IceCandidate received")

                                listener.onIceCandidateReceived(
                                    IceCandidate(data["sdpMid"].toString(),
                                        Math.toIntExact(data["sdpMLineIndex"] as Long),
                                        data["sdpCandidate"].toString()))
                            }
//                            if (SDPtype == "Answer" && data.containsKey("type") && data.get("type")=="answerCandidate") {
//                                Log.e(Constants.TAGGER, "IceCandidate received")
//                                listener.onIceCandidateReceived(
//                                    IceCandidate(data["sdpMid"].toString(),
//                                        Math.toIntExact(data["sdpMLineIndex"] as Long),
//                                        data["sdpCandidate"].toString()))
//
//                        }

                }
//            db.collection("calls").document(meetingID)
//                    .get()
//                    .addOnSuccessListener { result ->
//                        val data = result.data
//                        if (data?.containsKey("type")!! && data.getValue("type").toString() == "OFFER") {
//                            Log.e(TAG, "connect: OFFER - $data")
//                            listener.onOfferReceived(SessionDescription(SessionDescription.Type.OFFER,data["sdp"].toString()))
//                        } else if (data?.containsKey("type") && data.getValue("type").toString() == "ANSWER") {
//                            Log.e(TAG, "connect: ANSWER - $data")
//                            listener.onAnswerReceived(SessionDescription(SessionDescription.Type.ANSWER,data["sdp"].toString()))
//                        }
//                    }
//                    .addOnFailureListener {
//                        Log.e(TAG, "connect: $it")
//                    }

        } catch (exception: Exception) {
            Log.e(TAGGER, "connectException: $exception")

        }
    }

    fun sendIceCandidate(candidate: IceCandidate?,isJoin : Boolean) = runBlocking {
        Log.e(TAGGER, "Sending Ice Candidate:")
        val toId = when {
            isJoin -> callerId
            else -> calleeId
        }
        val candidateConstant = hashMapOf(
            "serverUrl" to candidate?.serverUrl,
            "sdpMid" to candidate?.sdpMid,
            "sdpMLineIndex" to candidate?.sdpMLineIndex,
            "sdpCandidate" to candidate?.sdp,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users")
            .document(toId).collection("calls").document("IceCandidate")
            .set(candidateConstant as Map<String, Any>)
            .addOnSuccessListener {
                Log.e(TAG, "sendIceCandidate: Success" )
            }
            .addOnFailureListener {
                Log.e(TAG, "sendIceCandidate: Error $it" )
            }
    }



    fun destroy() {
//        client.close()
        job.complete()
    }
}