package com.akustom15.pum.notifications

/**
 * Data model for in-app messages fetched from Firestore.
 * 
 * Firestore collection: "in_app_messages"
 * Document fields:
 *   - title: String
 *   - body: String
 *   - imageUrl: String? (optional)
 *   - actionUrl: String? (optional, e.g. Play Store link)
 *   - actionText: String? (optional, button label)
 *   - active: Boolean
 *   - createdAt: Long (timestamp millis)
 */
data class PumInAppMessage(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val actionText: String? = null,
    val active: Boolean = false,
    val createdAt: Long = 0L
)
