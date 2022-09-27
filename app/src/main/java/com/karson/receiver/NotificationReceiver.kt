package com.karson.receiver

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.karson.work.PollWorker

private const val TAG = "NotificationReceiver"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "received broadcast: ${intent.action}")
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val requestCode = intent.getIntExtra(PollWorker.REQUEST_CODE, -1)
        val notification = intent.getParcelableExtra<Notification>(PollWorker.NOTIFICATION)
        val notificationManager = NotificationManagerCompat.from(context)
        if (notification != null) {
            notificationManager.notify(requestCode, notification)
        }
    }
}