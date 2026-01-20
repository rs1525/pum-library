package com.akustom15.pum.config

import androidx.annotation.StringRes

/**
 * Configuration for the changelog dialog
 * 
 * @param entries List of changelog entries describing what's new
 */
data class ChangelogConfig(
    val entries: List<ChangelogEntry> = emptyList()
)

/**
 * A single changelog entry
 * Supports both direct strings and string resource IDs for localization
 * 
 * @param description Direct description text (used if descriptionResId is 0)
 * @param descriptionResId String resource ID for localized description
 */
data class ChangelogEntry(
    val description: String = "",
    @StringRes val descriptionResId: Int = 0
) {
    companion object {
        /** Create entry with direct string (not localized) */
        operator fun invoke(description: String) = ChangelogEntry(description = description, descriptionResId = 0)
        
        /** Create entry with string resource ID (localized) */
        fun fromResource(@StringRes resId: Int) = ChangelogEntry(description = "", descriptionResId = resId)
    }
}

