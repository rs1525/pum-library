package com.akustom15.pum.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

/** Utility to extract preview images from Kustom widget (.kwgt) and wallpaper (.klwp) files */
object PreviewExtractor {

    private const val TAG = "PreviewExtractor"

    // Preview file names inside the .kwgt/.klwp archives
    private const val PORTRAIT_THUMB = "preset_thumb_portrait.jpg"
    private const val LANDSCAPE_THUMB = "preset_thumb_landscape.jpg"

    /**
     * Extract preview image from a .kwgt widget file Returns the path to the extracted preview
     * image file, or null if extraction fails
     *
     * @param context Android context
     * @param widgetFileName Name of the .kwgt file in assets/widgets
     * @param usePortrait Whether to extract portrait (true) or landscape (false) thumbnail
     * @return Absolute path to extracted preview image, or null
     */
    fun extractWidgetPreview(
            context: Context,
            widgetFileName: String,
            usePortrait: Boolean = true
    ): String? {
        return extractPreview(
                context = context,
                assetPath = "widgets/$widgetFileName",
                fileName = widgetFileName,
                usePortrait = usePortrait
        )
    }

    /**
     * Extract preview image from a .klwp wallpaper file Returns the path to the extracted preview
     * image file, or null if extraction fails
     *
     * @param context Android context
     * @param wallpaperFileName Name of the .klwp file in assets/wallpapers
     * @param usePortrait Whether to extract portrait (true) or landscape (false) thumbnail
     * @return Absolute path to extracted preview image, or null
     */
    fun extractWallpaperPreview(
            context: Context,
            wallpaperFileName: String,
            usePortrait: Boolean = true
    ): String? {
        return extractPreview(
                context = context,
                assetPath = "wallpapers/$wallpaperFileName",
                fileName = wallpaperFileName,
                usePortrait = usePortrait
        )
    }

    /** Generic function to extract preview from a Kustom archive */
    private fun extractPreview(
            context: Context,
            assetPath: String,
            fileName: String,
            usePortrait: Boolean
    ): String? {
        try {
            // Create cache directory for previews
            val previewCacheDir = File(context.cacheDir, "previews")
            if (!previewCacheDir.exists()) {
                previewCacheDir.mkdirs()
            }

            // Generate output file name
            val orientation = if (usePortrait) "portrait" else "landscape"
            val outputFileName = "${fileName}_${orientation}.jpg"
            val outputFile = File(previewCacheDir, outputFileName)

            // If already extracted, return cached path
            if (outputFile.exists()) {
                Log.d(TAG, "Using cached preview: ${outputFile.absolutePath}")
                return outputFile.absolutePath
            }

            // Open the .kwgt/.klwp file as ZIP from assets
            val inputStream = context.assets.open(assetPath)
            val zipInputStream = ZipInputStream(inputStream)

            val thumbFileName = if (usePortrait) PORTRAIT_THUMB else LANDSCAPE_THUMB

            // Search for the thumbnail file inside the ZIP
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                if (entry.name == thumbFileName) {
                    // Found the thumbnail, decode it
                    val bitmap = BitmapFactory.decodeStream(zipInputStream)

                    if (bitmap != null) {
                        // Save to cache
                        FileOutputStream(outputFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        }

                        Log.d(TAG, "Extracted preview for $fileName: ${outputFile.absolutePath}")
                        zipInputStream.close()
                        return outputFile.absolutePath
                    }
                }
                entry = zipInputStream.nextEntry
            }

            zipInputStream.close()
            Log.w(TAG, "Preview not found in $fileName")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting preview from $fileName", e)
            return null
        }
    }

    /** Clear all cached preview images */
    fun clearPreviewCache(context: Context) {
        try {
            val previewCacheDir = File(context.cacheDir, "previews")
            if (previewCacheDir.exists()) {
                previewCacheDir.deleteRecursively()
                Log.d(TAG, "Preview cache cleared")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing preview cache", e)
        }
    }
}
