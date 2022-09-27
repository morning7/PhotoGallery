package com.karson.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.karson.network.api.FlickrRepository
import com.karson.network.api.GalleryItem

class PhotoGalleryViewModel(): ViewModel() {
    var galleryItemLiveData: LiveData<List<GalleryItem>>
    private val mutableSearchTerm = MutableLiveData<String>()

    init {
        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            FlickrRepository.searchPhotos(searchTerm)
        }
    }

    fun searchPhotos(query: String = "") {
        mutableSearchTerm.value = query
    }

    fun fetchPhotos() {
        galleryItemLiveData = FlickrRepository.fetchPhotos()
    }


//    val test: String
//        get() {
//            return ""
//        }
}