package com.akustom15.pum.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a cloud-based wallpaper
 *
 * @param name Display name
 * @param author Author/creator of the wallpaper
 * @param url URL to the wallpaper image (full resolution)
 * @param collections Category/collection name
 * @param downloadable Whether the wallpaper can be downloaded
 * @param size File size in bytes (optional)
 * @param dimensions Image dimensions e.g. "1080x1920" (optional)
 * @param copyright Copyright information
 */
data class CloudWallpaperItem(
        @SerializedName("name")
        val name: String,
        
        @SerializedName("author")
        val author: String,
        
        @SerializedName("url")
        val url: String,
        
        @SerializedName("collections")
        val collections: String = "",
        
        @SerializedName("downloadable")
        val downloadable: Boolean = true,
        
        @SerializedName("size")
        val size: Long? = null,
        
        @SerializedName("dimensions")
        val dimensions: String? = null,
        
        @SerializedName("copyright")
        val copyright: String = ""
) {
    /** Unique identifier based on URL */
    val id: String get() = url.hashCode().toString()
}
