package com.karson.network

import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.karson.network.api.FlickrRepository
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T>(private val responseHandler: Handler, private val onThumbnailDownloaded: (T, Bitmap) -> Unit) : HandlerThread(TAG) {

    val fragmentLifecycleObserver : LifecycleObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun setup() {
            Log.i(TAG, "Starting background thread")
            start()
            //If this thread has been started, this method will block until the looper has been initialized.
            looper
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun tearDown() {
            Log.i(TAG, "Destroying background thread")
            quit()
        }
    }

    val viewLifecycleObserver :LifecycleObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun clearQueue() {
            Log.i(TAG, "clearing all requests from queue")
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }
    }

    private var hasQuit = false

    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetch = FlickrRepository

    fun queueThumbnail(holder: T, url: String) {
        Log.i(TAG, "Got a URL: $url")
        requestMap[holder] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, holder).sendToTarget()
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("HandlerLeak")
    override fun onLooperPrepared() {
        super.onLooperPrepared()
        requestHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val holder = msg.obj as T
                    Log.i(TAG, "Got a request for URL: ${requestMap[holder]}")
                    handleRequest(holder)
                }
            }
        }
    }

    private fun handleRequest(holder: T) {
        val url = requestMap[holder] ?: return
        val bitmap = flickrFetch.fetchUrl(url)
        responseHandler.post {
            if (requestMap[holder] != url || hasQuit) {
                return@post
            }
            requestMap.remove(holder)
            onThumbnailDownloaded(holder, bitmap)
        }
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }
}