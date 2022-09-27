package com.karson.network

import com.karson.network.api.FlickrApi
import com.karson.network.api.PhotoInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitHelper {
    private const val BASE_URL = "https://api.flickr.com/"

    private val instance: Retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient().newBuilder()
            .connectTimeout(10000, TimeUnit.MILLISECONDS)
            .readTimeout(10000, TimeUnit.MILLISECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(PhotoInterceptor())
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
//            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: FlickrApi by lazy {
        instance.create(FlickrApi::class.java)
    }
}