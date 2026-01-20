package com.akustom15.pum.config

import androidx.annotation.DrawableRes

data class SocialMediaLink(
    val name: String,
    val url: String,
    @DrawableRes val iconRes: Int
)

object SocialMediaConfig {
    fun getSocialMediaLinks(
        @DrawableRes xIcon: Int,
        @DrawableRes instagramIcon: Int,
        @DrawableRes youtubeIcon: Int,
        @DrawableRes facebookIcon: Int,
        @DrawableRes telegramIcon: Int
    ): List<SocialMediaLink> {
        return listOf(
            SocialMediaLink(
                name = "Twitter",
                url = "https://twitter.com/Android15Kustom",
                iconRes = xIcon
            ),
            SocialMediaLink(
                name = "Instagram",
                url = "https://www.instagram.com/akustom15",
                iconRes = instagramIcon
            ),
            SocialMediaLink(
                name = "YouTube",
                url = "https://www.youtube.com/@androidkustom15",
                iconRes = youtubeIcon
            ),
            SocialMediaLink(
                name = "Facebook",
                url = "https://www.facebook.com/AndroidKustom15",
                iconRes = facebookIcon
            ),
            SocialMediaLink(
                name = "Telegram",
                url = "https://t.me/KlwpAndroidKustom15",
                iconRes = telegramIcon
            )
        )
    }
}
