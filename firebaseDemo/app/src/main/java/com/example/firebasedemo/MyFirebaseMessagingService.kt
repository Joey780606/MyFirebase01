package com.example.firebasedemo

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val tag = "FCMService"
    override fun onNewToken(value: String) {
        super.onNewToken(value)
        Log.v("Test", "Token = $value")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {  //收到推播後要做什麼事情
        super.onMessageReceived(remoteMessage)
        Log.d(tag , "From: " + remoteMessage.from);

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(tag, "Message Notification Body: " + remoteMessage.notification!!.body);
        }
    }
}