//package com.karson.network.api
//
//import com.google.gson.*
//import retrofit2.converter.gson.GsonConverterFactory
//import java.lang.reflect.Type
//
//object GalleryDeserializer : JsonDeserializer<List<GalleryItem>> {
//    private lateinit var gson: Gson
//
//    override fun deserialize(
//        json: JsonElement?,
//        typeOfT: Type?,
//        context: JsonDeserializationContext?
//    ): List<GalleryItem> {
//        gson = GsonBuilder()
//            .registerTypeAdapter(typeOfT, this)
//            .create()
//        val galleryItems = mutableListOf<GalleryItem>()
//        if (json is JsonObject) {
//            val jsonObject = json.getAsJsonObject()
//            if (jsonObject.has("photos")) {
//                val outerJson = jsonObject.get("photos").asJsonObject
//                val listJson = outerJson.get("photo").asJsonArray
////                gson.fromJson(listJson, )
//            }
//        }
//        return galleryItems
//    }
//
//    val factory: GsonConverterFactory
//        get() {
//
//        }
//}