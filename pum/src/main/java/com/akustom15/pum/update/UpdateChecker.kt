package com.akustom15.pum.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * UpdateChecker - Sistema de verificación de actualizaciones
 * 
 * Uso:
 * 1. Crea un archivo JSON en tu servidor con el formato:
 *    {
 *      "version_code": 2,
 *      "version_name": "1.1.0",
 *      "update_url": "https://play.google.com/store/apps/details?id=com.tuapp",
 *      "changelog": "Nuevas funciones y correcciones"
 *    }
 * 
 * 2. Llama a checkForUpdate() en tu MainActivity
 */
object UpdateChecker {
    
    private const val CHANNEL_ID = "app_updates"
    private const val NOTIFICATION_ID = 1001
    private const val PREFS_NAME = "update_prefs"
    private const val KEY_DISMISSED_VERSION = "dismissed_version"
    
    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val updateUrl: String,
        val changelog: String,
        val isUpdateAvailable: Boolean
    )
    
    /**
     * Verifica si hay una actualización disponible
     * @param context Context de la aplicación
     * @param updateJsonUrl URL del archivo JSON con la información de versión
     * @return UpdateInfo con los detalles de la actualización, o null si hay error
     */
    suspend fun checkForUpdate(
        context: Context,
        updateJsonUrl: String
    ): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(updateJsonUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.requestMethod = "GET"
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                val remoteVersionCode = json.getInt("version_code")
                val remoteVersionName = json.getString("version_name")
                val updateUrl = json.getString("update_url")
                val changelog = json.optString("changelog", "")
                
                val currentVersionCode = getCurrentVersionCode(context)
                val isUpdateAvailable = remoteVersionCode > currentVersionCode
                
                UpdateInfo(
                    versionCode = remoteVersionCode,
                    versionName = remoteVersionName,
                    updateUrl = updateUrl,
                    changelog = changelog,
                    isUpdateAvailable = isUpdateAvailable
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Muestra una notificación si hay actualización disponible
     */
    suspend fun checkAndNotify(
        context: Context,
        updateJsonUrl: String,
        notificationTitle: String = "Actualización disponible",
        notificationText: String = "Hay una nueva versión disponible"
    ) {
        val updateInfo = checkForUpdate(context, updateJsonUrl)
        
        if (updateInfo != null && updateInfo.isUpdateAvailable) {
            if (!isVersionDismissed(context, updateInfo.versionCode)) {
                showUpdateNotification(
                    context = context,
                    title = notificationTitle,
                    text = "$notificationText: v${updateInfo.versionName}",
                    updateUrl = updateInfo.updateUrl
                )
            }
        }
    }
    
    /**
     * Muestra una notificación de actualización
     */
    fun showUpdateNotification(
        context: Context,
        title: String,
        text: String,
        updateUrl: String
    ) {
        createNotificationChannel(context)
        
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    /**
     * Crea el canal de notificaciones (requerido para Android 8+)
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Actualizaciones"
            val descriptionText = "Notificaciones de nuevas versiones"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Obtiene el código de versión actual de la app
     */
    private fun getCurrentVersionCode(context: Context): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Marca una versión como descartada (el usuario no quiere actualizar)
     */
    fun dismissVersion(context: Context, versionCode: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_DISMISSED_VERSION, versionCode)
            .apply()
    }
    
    /**
     * Verifica si una versión fue descartada
     */
    private fun isVersionDismissed(context: Context, versionCode: Int): Boolean {
        val dismissedVersion = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_DISMISSED_VERSION, 0)
        return dismissedVersion >= versionCode
    }
    
    /**
     * Limpia las versiones descartadas (útil para testing)
     */
    fun clearDismissedVersions(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_DISMISSED_VERSION)
            .apply()
    }
}
