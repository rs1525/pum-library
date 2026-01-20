package com.akustom15.pum.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

/**
 * Utility class for integrating with Kustom apps (KWGT and KLWP) Following EXACTLY the official
 * Kustom documentation
 */
object KustomIntegration {

    private const val TAG = "KustomIntegration"
    private const val KWGT_PACKAGE = "org.kustom.widget"
    private const val KWGT_PRO_PACKAGE = "org.kustom.widget.pro"
    private const val KLWP_PACKAGE = "org.kustom.wallpaper"
    private const val KLWP_PRO_PACKAGE = "org.kustom.wallpaper.pro"

    /** Apply a widget to KWGT following EXACTLY the official documentation */
    fun applyWidget(context: Context, widgetFileName: String, packageName: String) {
        android.util.Log.d(TAG, "=== applyWidget called ===")
        android.util.Log.d(TAG, "File: $widgetFileName")
        android.util.Log.d(TAG, "Package: $packageName")

        // Check if KWGT is installed
        val kwgtInstalled =
                isPackageInstalled(context, KWGT_PACKAGE) ||
                        isPackageInstalled(context, KWGT_PRO_PACKAGE)

        if (!kwgtInstalled) {
            android.util.Log.e(TAG, "KWGT is not installed!")
            Toast.makeText(
                            context,
                            "KWGT is not installed. Please install it first.",
                            Toast.LENGTH_LONG
                    )
                    .show()
            openPlayStore(context, KWGT_PACKAGE)
            return
        }

        try {
            // Build kfile:// URI
            val kfileUri = "kfile://$packageName/widgets/$widgetFileName"
            android.util.Log.d(TAG, "URI: $kfileUri")

            // EXACTLY AS DOCUMENTATION - component always uses base package
            val intent = Intent()
            intent.component =
                    ComponentName("org.kustom.widget", "org.kustom.widget.picker.WidgetPicker")
            intent.data = Uri.parse(kfileUri)

            android.util.Log.d(TAG, "Starting KWGT")
            context.startActivity(intent)
            android.util.Log.d(TAG, "KWGT started successfully")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "ERROR in applyWidget", e)
            e.printStackTrace()
            Toast.makeText(context, "Error opening KWGT: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /** Apply a wallpaper to KLWP following EXACTLY the official documentation */
    fun applyWallpaper(context: Context, wallpaperFileName: String, packageName: String) {
        android.util.Log.d(TAG, "=== applyWallpaper called ===")
        android.util.Log.d(TAG, "File: $wallpaperFileName")
        android.util.Log.d(TAG, "Package: $packageName")

        // Check if KLWP is installed
        val klwpInstalled =
                isPackageInstalled(context, KLWP_PACKAGE) ||
                        isPackageInstalled(context, KLWP_PRO_PACKAGE)

        if (!klwpInstalled) {
            android.util.Log.e(TAG, "KLWP is not installed!")
            Toast.makeText(
                            context,
                            "KLWP is not installed. Please install it first.",
                            Toast.LENGTH_LONG
                    )
                    .show()
            openPlayStore(context, KLWP_PACKAGE)
            return
        }

        try {
            // Build kfile:// URI - EXACTLY as per Kustom documentation
            val kfileUri = "kfile://$packageName/wallpapers/$wallpaperFileName"
            android.util.Log.d(TAG, "URI: $kfileUri")

            // EXACTLY AS DOCUMENTATION FOR KLWP - always use base package
            val intent = Intent()
            intent.component =
                    ComponentName(
                            "org.kustom.wallpaper",
                            "org.kustom.lib.editor.WpAdvancedEditorActivity"
                    )
            intent.data = Uri.parse(kfileUri)

            android.util.Log.d(TAG, "Starting KLWP with intent: $intent")
            android.util.Log.d(TAG, "Component: ${intent.component}")
            android.util.Log.d(TAG, "Data: ${intent.data}")

            context.startActivity(intent)
            android.util.Log.d(TAG, "KLWP started successfully")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "ERROR in applyWallpaper", e)
            android.util.Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            android.util.Log.e(TAG, "Exception message: ${e.message}")
            e.printStackTrace()
            Toast.makeText(context, "Error opening KLWP: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /** Check if a package is installed */
    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            android.util.Log.d(TAG, "Package $packageName is installed")
            true
        } catch (e: PackageManager.NameNotFoundException) {
            android.util.Log.d(TAG, "Package $packageName is NOT installed")
            false
        }
    }

    /** Open Play Store for a package */
    private fun openPlayStore(context: Context, packageName: String) {
        try {
            val intent =
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=$packageName")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser
            val intent =
                    Intent(Intent.ACTION_VIEW).apply {
                        data =
                                Uri.parse(
                                        "https://play.google.com/store/apps/details?id=$packageName"
                                )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
            context.startActivity(intent)
        }
    }
}
