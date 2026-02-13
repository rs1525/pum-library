package com.akustom15.pum.notifications

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.akustom15.pum.data.PumPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging service for PUM library.
 * Handles incoming push notifications and displays them to the user.
 * 
 * Notifications are only shown if the user has enabled them in Settings.
 */
class PumFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "PumFCMService"
        private const val NOTIFICATION_ID = 2001
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Check if user has notifications enabled
        val preferences = PumPreferences.getInstance(applicationContext)
        if (!preferences.getNotificationsEnabled()) {
            return
        }

        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        // Open the app's launcher activity when notification is tapped
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                this,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            null
        }

        // Custom sound from app's res/raw (for pre-O devices where channel sound isn't used)
        val soundResId = resources.getIdentifier(
            "new_notification_011", "raw", packageName
        )
        val builder = NotificationCompat.Builder(this, PumNotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        if (soundResId != 0) {
            builder.setSound(
                Uri.parse("android.resource://$packageName/$soundResId")
            )
        }
        val notification = builder.build()

        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to show notification: permission denied", e)
        }
    }
}
