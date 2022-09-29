package com.prasannakumar.strangerswebrtc.model

import android.webkit.JavascriptInterface
import com.prasannakumar.strangerswebrtc.ui.Call

class InterfaceKotlin(val callActivity: Call) {


    @JavascriptInterface
    fun onPeerConnected() {
        callActivity.onPeerConnected()
    }
}