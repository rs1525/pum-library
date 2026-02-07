package com.akustom15.pum.config

/**
 * Data class representing an app to showcase in the "More Apps" section.
 *
 * @param name App name displayed on the card
 * @param description Short description of the app
 * @param iconUrl URL to the app icon image
 * @param screenshotUrls List of URLs to app screenshots (displayed in horizontal scroll)
 * @param playStoreUrl URL to the app's Play Store page (used by INSTALL button)
 */
data class MoreApp(
    val name: String,
    val description: String = "",
    val iconUrl: String = "",
    val screenshotUrls: List<String> = emptyList(),
    val playStoreUrl: String = ""
)
