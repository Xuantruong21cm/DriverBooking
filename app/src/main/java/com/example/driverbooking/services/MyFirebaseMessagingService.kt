package com.example.driverbooking.services

import android.app.Application
import android.util.Log
import com.example.driverbooking.Common
import com.example.driverbooking.utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (FirebaseAuth.getInstance().currentUser != null){
            UserUtils.updateToken(this,token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        if (data != null){
            Common.showNotification(this, Random.nextInt(),data[Common.NOTI_TITLE],data[Common.NOTI_BODY],
            null)
            Log.d("done", "onMessageReceived: "+data.toString())
        }
    }
}