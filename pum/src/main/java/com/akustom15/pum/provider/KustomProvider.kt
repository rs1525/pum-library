package com.akustom15.pum.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File
import java.io.FileNotFoundException

/**
 * Content Provider that exposes Kustom widget and wallpaper files from assets to KWGT and KLWP apps.
 * This makes the pack name appear in KWGT/KLWP app list.
 * 
 * To use this provider, add the following to your app's AndroidManifest.xml:
 * 
 * <provider
 *     android:name="com.akustom15.pum.provider.KustomProvider"
 *     android:authorities="${applicationId}.kustom.provider"
 *     android:exported="true"
 *     android:grantUriPermissions="true">
 *     <intent-filter>
 *         <action android:name="org.kustom.provider.WIDGETS" />
 *         <action android:name="org.kustom.provider.WALLPAPERS" />
 *     </intent-filter>
 * </provider>
 */
class KustomProvider : ContentProvider() {
    
    companion object {
        private const val TAG = "KustomProvider"
        private const val WIDGETS = 1
        private const val WALLPAPERS = 2
        private const val WIDGET_FILE = 3
        private const val WALLPAPER_FILE = 4
        
        private const val KWGT_MIME_TYPE = "application/vnd.kustom.widget"
        private const val KLWP_MIME_TYPE = "application/vnd.kustom.wallpaper"
        
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI("*.kustom.provider", "widgets", WIDGETS)
            addURI("*.kustom.provider", "wallpapers", WALLPAPERS)
            addURI("*.kustom.provider", "widgets/*", WIDGET_FILE)
            addURI("*.kustom.provider", "wallpapers/*", WALLPAPER_FILE)
        }
    }

    override fun onCreate(): Boolean = true

    override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?
    ): Cursor? {
        val context = context ?: return null
        
        return when (uriMatcher.match(uri)) {
            WIDGETS -> {
                val cursor = MatrixCursor(arrayOf("_id", "title", "thumb"))
                val widgets = listAssetItems(context, "widgets", setOf("kwgt"))
                widgets.forEachIndexed { index, item ->
                    cursor.addRow(arrayOf(index, item.first, item.second))
                }
                cursor
            }
            WALLPAPERS -> {
                val cursor = MatrixCursor(arrayOf("_id", "title", "thumb"))
                val wallpapers = listAssetItems(context, "wallpapers", setOf("klwp"))
                wallpapers.forEachIndexed { index, item ->
                    cursor.addRow(arrayOf(index, item.first, item.second))
                }
                cursor
            }
            else -> null
        }
    }
    
    private fun listAssetItems(context: android.content.Context, directory: String, extensions: Set<String>): List<Pair<String, String>> {
        return try {
            val names = context.assets.list(directory) ?: emptyArray()
            names.asSequence()
                .filter { name -> !name.startsWith('.') }
                .filter { name ->
                    val ext = name.substringAfterLast('.', "").lowercase()
                    extensions.isEmpty() || extensions.contains(ext)
                }
                .sorted()
                .map { name ->
                    val displayName = name.substringBeforeLast('.')
                        .replace('_', ' ')
                        .replace('-', ' ')
                    Pair(displayName, name)
                }
                .toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error listing assets from $directory", e)
            emptyList()
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val currentContext = context ?: throw FileNotFoundException("Context is null")
        
        return when (uriMatcher.match(uri)) {
            WIDGET_FILE -> {
                val fileName = uri.lastPathSegment ?: throw FileNotFoundException("No filename")
                openAssetFile(currentContext, "widgets", fileName)
            }
            WALLPAPER_FILE -> {
                val fileName = uri.lastPathSegment ?: throw FileNotFoundException("No filename")
                openAssetFile(currentContext, "wallpapers", fileName)
            }
            else -> throw FileNotFoundException("Invalid URI: $uri")
        }
    }
    
    private fun openAssetFile(context: android.content.Context, folder: String, fileName: String): ParcelFileDescriptor {
        val assetPath = "$folder/$fileName"
        try {
            val inputStream = context.assets.open(assetPath)
            val cacheDir = File(context.cacheDir, "kustom_$folder")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            
            val tempFile = File(cacheDir, fileName)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            return ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening asset: $assetPath", e)
            throw FileNotFoundException("Asset not found: $assetPath")
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            WIDGET_FILE -> KWGT_MIME_TYPE
            WALLPAPER_FILE -> KLWP_MIME_TYPE
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
