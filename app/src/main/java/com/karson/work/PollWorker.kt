package com.karson.work

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.karson.NOTIFICATION_CHANNEL_ID
import com.karson.network.api.FlickrApi
import com.karson.network.api.FlickrRepository
import com.karson.network.api.FlickrResponse
import com.karson.photogallery.PhotoGalleryActivity
import com.karson.photogallery.R
import com.karson.utils.PREF_LAST_RESULT_ID
import com.karson.utils.PREF_SEARCH_QUERY
import com.karson.utils.QueryPreferences

private const val TAG = "PollWorker"

const val POLL_WORK = "PollWorker"

class PollWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
//        Log.i(TAG, "Work request triggered")
//        val search = QueryPreferences.getValue(context, PREF_SEARCH_QUERY)
//        val lastResultId = QueryPreferences.getValue(context, PREF_LAST_RESULT_ID)
//        val items = if (search.isEmpty()) {
//            FlickrRepository.fetchPhotosRequest()
//                .execute().body()?.photos?.galleryItem
//        } else {
//            FlickrRepository.searchPhotosRequest(search)
//                .execute().body()?.photos?.galleryItem
//        } ?: emptyList()
//
//        if (items.isEmpty()) {
//            return Result.success()
//        }
//
//        val resultId = items.first().id
//        if (resultId == lastResultId) {
//            Log.i(TAG, "Got an old result: $resultId")
//        } else {
//            Log.i(TAG, "Got an new result: $resultId")
//            QueryPreferences.putValue(context, PREF_SEARCH_QUERY, resultId)

            val intent = PhotoGalleryActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            val resources = context.resources
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            val notificationManager = NotificationManagerCompat.from(context)
//            notificationManager.notify(0, notification)

//            context.sendBroadcast(Intent(ACTION_SHOW_NOTIFICATION), PERMISSION_PRIVATE)
//        }
            showBackgroundNotification(0, notification)
        return Result.success()
    }

    private fun showBackgroundNotification(requestCode: Int, notification: Notification) {
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, requestCode)
            putExtra(NOTIFICATION, notification)
        }
        context.sendOrderedBroadcast(intent, PERMISSION_PRIVATE)
    }

    companion object {
        const val ACTION_SHOW_NOTIFICATION = "com.karson.photogallery.NOTIFICATION"
        const val PERMISSION_PRIVATE = "com.karson.photogallery.PRIVATE"
        const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
    }
}