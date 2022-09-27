package com.karson.network.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.karson.network.RetrofitHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object FlickrRepository {

    private val apiService = RetrofitHelper.apiService

    fun fetchPhotosRequest(): Call<FlickrResponse> {
        return apiService.fetchPhotos()
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(fetchPhotosRequest())
    }

    fun searchPhotosRequest(text: String) : Call<FlickrResponse> {
        return apiService.searchPhotos(text)
    }

    fun searchPhotos(text: String): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(searchPhotosRequest(text))
    }

    @WorkerThread
    fun fetchUrl(url: String): Bitmap {
        val response = RetrofitHelper.apiService.fetchUrlBytes(url).execute()
        return response.body()?.byteStream().use(BitmapFactory::decodeStream)
    }

    private fun fetchPhotoMetadata(flickrRequest: Call<FlickrResponse>) : LiveData<List<GalleryItem>> {
        val responseLiveData = MutableLiveData<List<GalleryItem>>()
        flickrRequest.enqueue(object : Callback<FlickrResponse> {
            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {
                val flickResponse = response.body()
                val photoResponse = flickResponse?.photos
                var galleryItems = photoResponse?.galleryItem ?: emptyList()
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
//                galleryItems.subList(0, 9)
                responseLiveData.value = galleryItems
                Log.e("FlickrRepository", "onSuccess")
            }

            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                t.printStackTrace()
                Log.e("FlickrRepository", "onFailure ${t.message}" )
            }
        })
        return responseLiveData
    }
}