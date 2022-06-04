package com.okaythis.jasvir.fcm

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.okaythis.jasvir.ui.MainActivity
import com.okaythis.jasvir.data.repository.PreferenceRepo

class OkayDemoFirebaseMessagingService : FirebaseMessagingService() {


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        token?.run {
            PreferenceRepo(this@OkayDemoFirebaseMessagingService).putAppPNS(token)
        }
    }

    override fun onMessageReceived(remoteData: RemoteMessage) {

        if (remoteData.data.isNotEmpty()) {
            // handle notification
            val notificationData = NotificationHandler.extractRemoteData(remoteData)
            Log.d("Firebase", "${notificationData!!.sessionId!!} ")

            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(ACTIVITY_WAKE_UP_KEY, notificationData.sessionId!!.toLong())
            })
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    companion object {
        val ACTIVITY_WAKE_UP_KEY = "wake_up_key"
    }
}