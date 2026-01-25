package com.akustom15.pum.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

/**
 * Content Provider that exposes Kustom widget and wallpaper files from assets to KWGT and KLWP apps
 * using the kfile:// URI scheme
 */
class KustomProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val context = context ?: return null

        android.util.Log.d("KustomProvider", "=== openAssetFile called ===")
        android.util.Log.d("KustomProvider", "Full URI: $uri")
        android.util.Log.d("KustomProvider", "URI authority: ${uri.authority}")
        android.util.Log.d("KustomProvider", "URI path: ${uri.path}")
        android.util.Log.d("KustomProvider", "URI pathSegments: ${uri.pathSegments}")
        android.util.Log.d("KustomProvider", "Mode: $mode")

        try {
            // Parse URI: content://com.akustom15.pum/widgets/filename.kwgt
            val pathSegments = uri.pathSegments
            android.util.Log.d("KustomProvider", "Path segments count: ${pathSegments.size}")
            for (i in pathSegments.indices) {
                android.util.Log.d("KustomProvider", "  Segment[$i]: ${pathSegments[i]}")
            }

            if (pathSegments.size < 2) {
                android.util.Log.e("KustomProvider", "Invalid URI - need at least 2 segments")
                throw FileNotFoundException("Invalid URI: $uri")
            }

            // Extract folder and filename
            val folder = pathSegments[0] // "widgets" or "wallpapers"
            val fileName = pathSegments[1] // "filename.kwgt" or "filename.klwp"

            android.util.Log.d("KustomProvider", "Extracted folder: $folder")
            android.util.Log.d("KustomProvider", "Extracted fileName: $fileName")

            // Build asset path
            val assetPath = "$folder/$fileName"
            android.util.Log.d("KustomProvider", "Asset path: $assetPath")

            // Check if asset exists
            val assetList = context.assets.list(folder) ?: emptyArray()
            android.util.Log.d(
                    "KustomProvider",
                    "Assets in '$folder': ${assetList.joinToString(", ")}"
            )

            if (!assetList.contains(fileName)) {
                android.util.Log.e("KustomProvider", "Asset not found: $assetPath")
                throw FileNotFoundException("Asset not found: $assetPath")
            }

            android.util.Log.d("KustomProvider", "Asset found! Opening...")

            // For read mode, return asset file descriptor
            if (mode == "r") {
                val afd = context.assets.openFd(assetPath)
                android.util.Log.d("KustomProvider", "Opened asset FD successfully")
                return afd
            }

            // For write mode or other modes, copy to cache first
            android.util.Log.d("KustomProvider", "Non-read mode, copying to cache...")
            val cacheDir = File(context.cacheDir, "kustom")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val cachedFile = File(cacheDir, fileName)

            // Copy asset to cache
            context.assets.open(assetPath).use { input ->
                FileOutputStream(cachedFile).use { output -> input.copyTo(output) }
            }

            // Return file descriptor for cached file
            val pfd = ParcelFileDescriptor.open(cachedFile, ParcelFileDescriptor.MODE_READ_ONLY)

            android.util.Log.d("KustomProvider", "Cached file opened successfully")
            return AssetFileDescriptor(pfd, 0, AssetFileDescriptor.UNKNOWN_LENGTH)
        } catch (e: Exception) {
            android.util.Log.e("KustomProvider", "ERROR opening asset", e)
            e.printStackTrace()
            throw FileNotFoundException("Error opening asset: ${e.message}")
        }
    }

    override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?
    ): Cursor? {
        // Not needed for Kustom integration
        return null
    }

    override fun getType(uri: Uri): String {
        return when {
            uri.path?.endsWith(".kwgt") == true -> "application/x-kustom-widget"
            uri.path?.endsWith(".klwp") == true -> "application/x-kustom-wallpaper"
            else -> "application/octet-stream"
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Insert not supported")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Delete not supported")
    }

    override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("Update not supported")
    }
}
