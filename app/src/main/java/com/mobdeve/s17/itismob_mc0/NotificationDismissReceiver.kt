package com.mobdeve.s17.itismob_mc0

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", -1)
        Log.d("NotificationDismiss", "User dismissed notification: $notificationId")
    }
}