package com.akustom15.pum.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Utility class for network connectivity checks
 */
object NetworkUtils {

    /**
     * Check if the device is currently connected to WiFi
     */
    fun isOnWifi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Check if the device has any active network connection
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Check if download is allowed based on WiFi-only preference.
     * Returns true if download is allowed, false if blocked by WiFi-only setting.
     */
    fun isDownloadAllowed(context: Context): Boolean {
        val preferences = com.akustom15.pum.data.PumPreferences.getInstance(context)
        val wifiOnly = preferences.getDownloadOnWifiOnly()
        return if (wifiOnly) {
            isOnWifi(context)
        } else {
            true
        }
    }
}
