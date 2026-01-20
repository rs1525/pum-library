package com.akustom15.pum.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.akustom15.pum.model.CloudWallpaperItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Repository for fetching cloud wallpapers from a remote JSON endpoint
 */
class CloudWallpaperRepository(
    private val context: Context,
    private val jsonUrl: String
) {
    private val gson = Gson()
    private val wallpaperListType = object : TypeToken<List<CloudWallpaperItem>>() {}.type
    
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    /**
     * Fetch wallpapers from the configured JSON URL
     */
    suspend fun getWallpapers(): List<CloudWallpaperItem> = withContext(Dispatchers.IO) {
        try {
            if (jsonUrl.isBlank()) {
                Log.w(TAG, "Cloud wallpapers URL is empty")
                return@withContext emptyList()
            }
            
            val jsonString = fetchJson(jsonUrl)
            gson.fromJson(jsonString, wallpaperListType)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching wallpapers", e)
            emptyList()
        }
    }
    
    /**
     * Fetch JSON from URL
     */
    private suspend fun fetchJson(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PUM-Library/1.0 OkHttp/4.12.0")
            .build()
        
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP error: ${response.code}")
        }
        
        response.body?.string() ?: throw IOException("Empty response body")
    }
    
    /**
     * Load image bitmap from URL
     */
    suspend fun loadImage(url: String): Bitmap = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP error: ${response.code}")
            
            response.body?.bytes()?.let { bytes ->
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } ?: throw IOException("Empty response body")
        }
    }
    
    /**
     * Download wallpaper to device storage
     */
    suspend fun downloadWallpaper(url: String, name: String): java.io.File = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Download error: ${response.code}")
            
            val downloadDir = context.getExternalFilesDir(null)
            val file = java.io.File(downloadDir, "wallpaper_${name.replace(" ", "_")}.png")
            
            response.body?.bytes()?.let { bytes ->
                file.writeBytes(bytes)
                file
            } ?: throw IOException("Empty download response")
        }
    }
    
    companion object {
        private const val TAG = "CloudWallpaperRepo"
    }
}
