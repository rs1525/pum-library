package com.akustom15.pum.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.akustom15.pum.data.PumPreferences
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Helper class for managing push notifications via Firebase Cloud Messaging.
 * Handles notification channels, FCM topic subscriptions, and permission checks.
 */
object PumNotificationHelper {

    private const val TAG = "PumNotificationHelper"
    const val CHANNEL_ID = "pum_updates"
    const val TOPIC_UPDATES = "app_updates"

    /**
     * Initialize notifications: create channel and subscribe to topic if enabled.
     * Call this from MainActivity.onCreate().
     */
    fun initialize(context: Context) {
        createNotificationChannel(context)
        syncSubscription(context)
    }

    /**
     * Create the notification channel (required for Android 8+)
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Updates"
            val descriptionText = "Notifications for new versions and content"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Sync FCM topic subscription with the user preference.
     * Subscribes if notifications are enabled, unsubscribes otherwise.
     */
    fun syncSubscription(context: Context) {
        val preferences = PumPreferences.getInstance(context)
        val enabled = preferences.getNotificationsEnabled()

        if (enabled) {
            subscribeToUpdates()
        } else {
            unsubscribeFromUpdates()
        }
    }

    /**
     * Subscribe to the updates FCM topic
     */
    private fun subscribeToUpdates() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_UPDATES)
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to subscribe to topic: $TOPIC_UPDATES", e)
            }
    }

    /**
     * Unsubscribe from the updates FCM topic
     */
    private fun unsubscribeFromUpdates() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_UPDATES)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Unsubscribed from topic: $TOPIC_UPDATES")
                } else {
                    Log.e(TAG, "Failed to unsubscribe from topic: $TOPIC_UPDATES", task.exception)
                }
            }
    }

    /**
     * Check if POST_NOTIFICATIONS permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
