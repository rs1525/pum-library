package com.akustom15.pum.model

/**
 * Represents a Kustom Live Wallpaper (KLWP) item
 *
 * @param id Unique identifier
 * @param name Display name
 * @param description Brief description
 * @param fileName Name of the .klwp file in assets
 * @param previewUrl URL or path to preview image
 */
data class WallpaperItem(
        val id: String,
        val name: String,
        val description: String,
        val fileName: String,
        val previewUrl: String? = null
)
