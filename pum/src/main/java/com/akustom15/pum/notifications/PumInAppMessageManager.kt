package com.akustom15.pum.notifications

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Manager for fetching and displaying custom in-app messages from Firestore.
 * Replaces Firebase In-App Messaging (FIAM) without requiring advertising declaration.
 *
 * Usage:
 * 1. Create a Firestore collection called "in_app_messages" in your Firebase project.
 * 2. Add documents with fields: title, body, imageUrl, actionUrl, actionText, active, createdAt.
 * 3. Call [fetchActiveMessage] from your MainActivity to get the latest active message.
 * 4. Display it using PumInAppMessageDialog composable.
 */
object PumInAppMessageManager {

    private const val TAG = "PumInAppMsgManager"
    private const val COLLECTION_NAME = "in_app_messages"
    private const val PREFS_NAME = "pum_in_app_messages"
    private const val KEY_DISMISSED_IDS = "dismissed_message_ids"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Fetch the latest active in-app message from Firestore that the user hasn't dismissed.
     * Returns null if no message is available or if fetching fails.
     */
    suspend fun fetchActiveMessage(context: Context): PumInAppMessage? {
        return try {
            val dismissed = getDismissedIds(context)

            val snapshot = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .whereEqualTo("active", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()

            for (doc in snapshot.documents) {
                if (doc.id in dismissed) continue

                val message = PumInAppMessage(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    body = doc.getString("body") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    actionUrl = doc.getString("actionUrl"),
                    actionText = doc.getString("actionText"),
                    active = doc.getBoolean("active") ?: false,
                    createdAt = doc.getLong("createdAt") ?: 0L
                )

                if (message.title.isNotEmpty() || message.body.isNotEmpty()) {
                    return message
                }
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching in-app messages", e)
            null
        }
    }

    /**
     * Mark a message as dismissed so it won't be shown again.
     */
    fun dismissMessage(context: Context, messageId: String) {
        val dismissed = getDismissedIds(context).toMutableSet()
        dismissed.add(messageId)
        getPrefs(context).edit()
            .putStringSet(KEY_DISMISSED_IDS, dismissed)
            .apply()
    }

    /**
     * Get the set of dismissed message IDs.
     */
    private fun getDismissedIds(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_DISMISSED_IDS, emptySet()) ?: emptySet()
    }

    /**
     * Clear all dismissed messages (useful for testing or reset).
     */
    fun clearDismissedMessages(context: Context) {
        getPrefs(context).edit().remove(KEY_DISMISSED_IDS).apply()
    }
}
