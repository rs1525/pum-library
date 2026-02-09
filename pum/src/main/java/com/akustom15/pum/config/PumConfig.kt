package com.akustom15.pum.config

import androidx.annotation.DrawableRes

/**
 * Configuration class for PUM library
 *
 * @param appName Name of the application (e.g., "Lunex for kwgt")
 * @param appSubtitle Subtitle or tagline (e.g., "Fantastic Widgets")
 * @param appIcon Resource ID of the app icon (circular recommended)
 * @param packageName Package name of the application
 * @param showWidgets Whether to show the Widgets (KWGT) tab
 * @param showWallpapers Whether to show the Wallpapers (KLWP) tab
 * @param showWallpaperCloud Whether to show the Cloud Wallpaper tab
 * @param cloudWallpapersUrl URL to the JSON file containing cloud wallpapers data
 * @param developerLogoUrl URL to the developer logo image (for About screen)
 * @param changelog Changelog configuration for the changelog dialog
 * @param xIcon Resource ID for Twitter/X social media icon
 * @param instagramIcon Resource ID for Instagram social media icon
 * @param youtubeIcon Resource ID for YouTube social media icon
 * @param facebookIcon Resource ID for Facebook social media icon
 * @param telegramIcon Resource ID for Telegram social media icon
 */
data class PumConfig(
        val appName: String,
        val appSubtitle: String = "",
        @DrawableRes val appIcon: Int? = null,
        val packageName: String,
        val showWidgets: Boolean = true,
        val showWallpapers: Boolean = true,
        val showWallpaperCloud: Boolean = false,
        val cloudWallpapersUrl: String = "",
        val developerLogoUrl: String = "",
        val developerName: String = "AKustom15",
        val moreAppsUrl: String = "",
        val moreApps: List<MoreApp> = emptyList(),
        val moreAppsJsonUrl: String = "",
        val updateJsonUrl: String = "",
        val privacyPolicyUrl: String = "",
        val changelog: ChangelogConfig = ChangelogConfig(),
        @DrawableRes val xIcon: Int = android.R.drawable.ic_menu_send,
        @DrawableRes val instagramIcon: Int = android.R.drawable.ic_menu_camera,
        @DrawableRes val youtubeIcon: Int = android.R.drawable.ic_media_play,
        @DrawableRes val facebookIcon: Int = android.R.drawable.ic_menu_share,
        @DrawableRes val telegramIcon: Int = android.R.drawable.ic_menu_send
) {
    /** Get list of visible tabs based on configuration */
    fun getVisibleTabs(): List<PumTab> {
        return buildList {
            if (showWidgets) add(PumTab.Widgets)
            if (showWallpapers) add(PumTab.Wallpapers)
            if (showWallpaperCloud) add(PumTab.WallpaperCloud)
        }
    }
}

enum class PumTab {
    Widgets,
    Wallpapers,
    WallpaperCloud
}
