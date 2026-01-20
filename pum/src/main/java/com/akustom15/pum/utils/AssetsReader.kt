package com.akustom15.pum.utils

import android.content.Context
import com.akustom15.pum.model.WallpaperItem
import com.akustom15.pum.model.WidgetItem

/** Utility class for reading widget and wallpaper files from assets */
object AssetsReader {

    private const val WIDGETS_FOLDER = "widgets"
    private const val WALLPAPERS_FOLDER = "wallpapers"
    private const val KWGT_EXTENSION = ".kwgt"
    private const val KLWP_EXTENSION = ".klwp"

    /** Read all KWGT widget files from assets/widgets folder */
    fun getWidgetsFromAssets(context: Context): List<WidgetItem> {
        return try {
            val assetManager = context.assets
            val widgetFiles = assetManager.list(WIDGETS_FOLDER) ?: emptyArray()

            widgetFiles.filter { it.endsWith(KWGT_EXTENSION, ignoreCase = true) }.mapIndexed {
                    index,
                    fileName ->
                val nameWithoutExtension = fileName.removeSuffix(KWGT_EXTENSION)

                // Extract preview image from .kwgt file
                val previewPath =
                        PreviewExtractor.extractWidgetPreview(
                                context = context,
                                widgetFileName = fileName,
                                usePortrait = true
                        )

                WidgetItem(
                        id = "widget_$index",
                        name = formatName(nameWithoutExtension),
                        description = "Widget from $fileName",
                        fileName = fileName,
                        previewUrl = previewPath // Now contains actual preview path
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Read all KLWP wallpaper files from assets/wallpapers folder */
    fun getWallpapersFromAssets(context: Context): List<WallpaperItem> {
        return try {
            val assetManager = context.assets
            val wallpaperFiles = assetManager.list(WALLPAPERS_FOLDER) ?: emptyArray()

            wallpaperFiles.filter { it.endsWith(KLWP_EXTENSION, ignoreCase = true) }.mapIndexed {
                    index,
                    fileName ->
                val nameWithoutExtension = fileName.removeSuffix(KLWP_EXTENSION)

                // Extract preview image from .klwp file
                val previewPath =
                        PreviewExtractor.extractWallpaperPreview(
                                context = context,
                                wallpaperFileName = fileName,
                                usePortrait = true
                        )

                WallpaperItem(
                        id = "wallpaper_$index",
                        name = formatName(nameWithoutExtension),
                        description = "Wallpaper from $fileName",
                        fileName = fileName,
                        previewUrl = previewPath
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Format file name to display name Example: "widget_001" -> "Widget 001" */
    private fun formatName(fileName: String): String {
        return fileName.replace("_", " ").split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    /** Get the full path to a widget file in assets */
    fun getWidgetAssetPath(fileName: String): String {
        return "$WIDGETS_FOLDER/$fileName"
    }

    /** Get the full path to a wallpaper file in assets */
    fun getWallpaperAssetPath(fileName: String): String {
        return "$WALLPAPERS_FOLDER/$fileName"
    }
}
