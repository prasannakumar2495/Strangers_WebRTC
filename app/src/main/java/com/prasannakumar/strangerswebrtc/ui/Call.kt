package com.prasannakumar.strangerswebrtc.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.prasannakumar.strangerswebrtc.databinding.ActivityCallBinding
import com.prasannakumar.strangerswebrtc.model.InterfaceKotlin
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class Call : AppCompatActivity() {
    private lateinit var binding: ActivityCallBinding
    private lateinit var webView: WebView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var backgroundExecutor: ScheduledExecutorService

    private var uniqueID: String = ""
    private var userName: String = ""
    private var friendsUserName: String = ""
    private var isPeerConnected: Boolean = false
    private var isAudio: Boolean = true
    private var isVideo: Boolean = true
    private var pageExit: Boolean = false
    private var createdBy: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        webView = binding.webView
        mAuth = FirebaseAuth.getInstance()
        firebaseRef = FirebaseDatabase.getInstance().reference.child("users")
        backgroundExecutor = Executors.newSingleThreadScheduledExecutor()

        uniqueID = generateUniqueID()

        userName = intent.getStringExtra("userName")!!
        createdBy = intent.getStringExtra("createdBy")!!
        val incoming = intent.getStringExtra("incoming")

        //friendsUserName = ""
        friendsUserName = incoming.toString()

        setUpWebView()
        binding.mic.setOnClickListener {
            isAudio = !isAudio
            callJavaScriptFunction("javascript:toggleAudio(\"$isAudio\")")
            if (isAudio) {
                Toast.makeText(this, "Audio Enabled!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Audio Disabled!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.video.setOnClickListener {
            isVideo = !isVideo
            callJavaScriptFunction("javascript:toggleVideo(\"$isVideo\")")
            if (isVideo) {
                Toast.makeText(this, "Video Enabled!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Video Disabled!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setUpWebView() {

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    request?.grant(request.resources)
                }
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.addJavascriptInterface(InterfaceKotlin(this), "Android")

        loadVideoCall()
    }

    private fun loadVideoCall() {
        val filePath = "file:android_asset/call.html"
        webView.loadUrl(filePath)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                initializePeer()
            }
        }
    }

    private fun initializePeer() {
        callJavaScriptFunction("javascript:init(\"$uniqueID\")")

        if (createdBy.equals(userName, true)) {
            firebaseRef.child(userName).child("connId").setValue(uniqueID)
            firebaseRef.child(userName).child("isAvailable").setValue(true)

            binding.controls.visibility = View.VISIBLE
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                friendsUserName = createdBy
                FirebaseDatabase.getInstance().reference.child("users").child(friendsUserName)
                    .child("connId")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value != null) {
                                //sendCallRequest
                                sendCallRequest()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }, 2000)
            backgroundExecutor.schedule(
                {
//                friendsUserName = createdBy
//                FirebaseDatabase.getInstance().reference.child("users").child(friendsUserName)
//                    .child("connId")
//                    .addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            if (snapshot.value != null) {
//                                //sendCallRequest
//                                sendCallRequest()
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//
//                        }
//                    })
                }, 2, TimeUnit.SECONDS)
        }
    }

    private fun sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, "You are not connected to Internet!", Toast.LENGTH_SHORT).show()
            return
        }

        //listenConnId
        listenConnId()
    }

    fun onPeerConnected() {
        isPeerConnected = true
    }

    private fun listenConnId() {
        firebaseRef.child(friendsUserName).child("connId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        return
                    }
                    binding.controls.visibility = View.VISIBLE
                    val connId = snapshot.value.toString()
                    callJavaScriptFunction("javascript:startCall(\"$connId\")")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun callJavaScriptFunction(function: String) {
        webView.post {
            webView.evaluateJavascript(function, null)
        }
    }

    private fun generateUniqueID(): String = UUID.randomUUID().toString()
}