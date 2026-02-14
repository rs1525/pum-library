package com.akustom15.pum.security

/**
 * Configuration for PUM security checks.
 * Each app provides its own config based on AppConfig values.
 */
data class PumSecurityConfig(
    /** Enable APK signature verification */
    val enableSignatureCheck: Boolean = true,
    /** Enable installer verification (Play Store only) */
    val enableInstallerCheck: Boolean = true,
    /** Enable debug/emulator/piracy detection */
    val enableDebugCheck: Boolean = true,
    /** Enable Google Play license verification (LVL) */
    val enableLicenseCheck: Boolean = true,
    /**
     * Expected SHA-256 hash of the release signing certificate.
     * Leave empty on first run — the current hash will be logged to Logcat
     * so you can copy it into AppConfig.
     * Run: ./gradlew signingReport  OR  check Logcat for "PumSecurity"
     */
    val expectedSignatureHash: String = "",
    /**
     * RSA public key from Google Play Console for license verification.
     * Go to: Play Console > Your App > Monetization > Licensing
     */
    val licenseKey: String = "",
    /** Whether this is a debug build (BuildConfig.DEBUG from the app) */
    val isDebugBuild: Boolean = false
)

/**
 * Result of security checks performed by [PumSecurityManager].
 */
data class PumSecurityResult(
    val isSecure: Boolean,
    val failedChecks: List<String>
)
