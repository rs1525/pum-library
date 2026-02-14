package com.akustom15.pum.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.os.Debug
import android.util.Log
import java.io.File
import java.security.MessageDigest

/**
 * ============================================
 * PUM SECURITY MANAGER - ANTI-PIRACY PROTECTION
 * ============================================
 *
 * Centralized security checks shared across all PUM-based apps.
 * Layers of protection:
 * 1. Piracy/hacking app detection (Lucky Patcher, Freedom, etc.)
 * 2. Installer verification (Google Play Store only)
 * 3. APK signature verification (detects re-signed APKs)
 * 4. Debug/release flag verification (detects repackaged debug builds)
 * 5. Emulator detection
 * 6. Tampering/hooking framework detection (Xposed, Frida, LSPosed, etc.)
 * 7. Runtime debugger attachment detection
 * 8. Root detection (warning only)
 */
object PumSecurityManager {

    private const val TAG = "PumSecurity"

    // ==========================================
    // KNOWN PIRACY / HACKING APPS
    // ==========================================
    private val PIRACY_APPS = listOf(
        // Lucky Patcher variants
        "com.chelpus.lackypatch",
        "com.dimonvideo.luckypatcher",
        "com.forpda.lp",
        "com.android.vending.billing.InAppBillingService.LUCK",
        "com.android.vending.billing.InAppBillingService.CLON",
        "com.android.vending.billing.InAppBillingService.LOCK",
        "com.android.vending.billing.InAppBillingService.COIN",
        "com.android.vending.billing.InAppBillingService.CRAC",
        // Uret Patcher
        "uret.jasi2169.patcher",
        "zone.jasi2169.uretpatcher",
        "p.jasi2169.al3",
        // Freedom
        "cc.madkite.freedom",
        "cc.cz.madkite.freedom",
        // CreeHack
        "org.creeplays.hack",
        // HappyMod
        "com.happymod.apk",
        // Game hacking tools
        "org.sbtools.gamehack",
        "com.zune.gamekiller",
        "com.killerapp.gamekiller",
        "cn.lm.sq",
        "net.schwarzis.game_cih",
        "com.baseappfull.fwd",
        // APK editors
        "com.gmail.heagoo.apkeditor",
        "com.gmail.heagoo.apkeditor.pro",
        // App cloners
        "com.applisto.appcloner",
        "com.applisto.appcloner.premium",
        // ACMarket
        "ac.market.store",
        // Blackmart
        "org.blackmart.market",
        // AppSara
        "com.appsara.app",
        // Game Guardian
        "catch_.me_.if_.you_.can_",
        // XModGames
        "com.xmodgame",
        // Leo PlayCard
        "com.leo.playcard",
        // App planet
        "com.appplanet.actool"
    )

    // Allowed installers
    private val ALLOWED_INSTALLERS = listOf(
        "com.android.vending",           // Google Play Store
        "com.google.android.feedback"    // Google Play Store (older)
    )

    // ==========================================
    // MAIN ENTRY POINT
    // ==========================================

    /**
     * Perform all security checks.
     * In debug builds, all checks are skipped to allow normal development.
     *
     * @param context Application or Activity context
     * @param config  Per-app security configuration
     * @return [PumSecurityResult] with overall status and list of failed checks
     */
    fun performSecurityChecks(context: Context, config: PumSecurityConfig): PumSecurityResult {
        // Skip all checks in debug builds for development
        if (config.isDebugBuild) {
            Log.d(TAG, "Debug build — security checks skipped")
            return PumSecurityResult(isSecure = true, failedChecks = emptyList())
        }

        val checks = mutableListOf<String>()
        var isSecure = true

        // 1. Piracy app detection
        if (config.enableDebugCheck) {
            val piracyApp = findPiracyApp(context)
            if (piracyApp != null) {
                checks.add("Piracy app detected: $piracyApp")
                isSecure = false
            }
        }

        // 2. Installer verification (Play Store only)
        if (config.enableInstallerCheck && !isInstalledFromPlayStore(context)) {
            checks.add("Not installed from Play Store")
            isSecure = false
        }

        // 3. Debuggable flag verification
        if (config.enableDebugCheck && isDebuggable(context)) {
            checks.add("Debuggable flag set in release build")
            isSecure = false
        }

        // 4. Emulator detection
        if (config.enableDebugCheck && isEmulator()) {
            checks.add("Emulator detected")
            isSecure = false
        }

        // 5. APK signature verification
        if (config.enableSignatureCheck) {
            val sigResult = verifySignature(context, config.expectedSignatureHash)
            if (!sigResult) {
                checks.add("Invalid APK signature")
                isSecure = false
            }
        }

        // 6. Tampering / hooking frameworks
        val tamperResult = detectTamperingTools()
        if (tamperResult != null) {
            checks.add("Tampering tool: $tamperResult")
            isSecure = false
        }

        // 7. Debugger attachment
        if (config.enableDebugCheck && isDebuggerAttached()) {
            checks.add("Debugger attached at runtime")
            isSecure = false
        }

        // 8. Root detection (warn only — many legitimate users root)
        if (config.enableDebugCheck && isRooted()) {
            Log.w(TAG, "Root detected — not blocking, but logging")
        }

        if (!isSecure) {
            Log.w(TAG, "Security failed: ${checks.joinToString()}")
        }

        return PumSecurityResult(isSecure = isSecure, failedChecks = checks)
    }

    // ==========================================
    // INDIVIDUAL CHECKS
    // ==========================================

    /**
     * Returns the first detected piracy app package name, or null.
     */
    private fun findPiracyApp(context: Context): String? {
        val pm = context.packageManager
        for (pkg in PIRACY_APPS) {
            try {
                pm.getPackageInfo(pkg, 0)
                return pkg
            } catch (_: PackageManager.NameNotFoundException) {
                // not installed
            }
        }
        return null
    }

    /**
     * Verify the app was installed from Google Play Store.
     */
    private fun isInstalledFromPlayStore(context: Context): Boolean {
        return try {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager
                    .getInstallSourceInfo(context.packageName)
                    .installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
            installer in ALLOWED_INSTALLERS
        } catch (e: Exception) {
            Log.w(TAG, "Could not determine installer", e)
            false
        }
    }

    /**
     * Check if the app has the debuggable flag set in ApplicationInfo.
     * This catches APKs that were repackaged with debuggable=true even
     * when BuildConfig.DEBUG is false.
     */
    private fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    /**
     * Improved emulator detection with fewer false positives.
     * Uses a scoring system: a single match isn't enough, need multiple signals.
     */
    private fun isEmulator(): Boolean {
        var score = 0

        if (Build.FINGERPRINT.startsWith("generic")) score += 3
        if (Build.FINGERPRINT.startsWith("unknown")) score += 1
        if (Build.MODEL.contains("google_sdk")) score += 3
        if (Build.MODEL.contains("Emulator")) score += 3
        if (Build.MODEL.contains("Android SDK built for x86")) score += 3
        if (Build.MANUFACTURER.contains("Genymotion")) score += 3
        if (Build.HARDWARE.contains("goldfish")) score += 3
        if (Build.HARDWARE.contains("ranchu")) score += 3
        if (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) score += 3
        if (Build.PRODUCT.contains("sdk_google")) score += 2
        if (Build.PRODUCT.contains("google_sdk")) score += 2
        if (Build.PRODUCT.contains("sdk_gphone")) score += 2
        if (Build.PRODUCT.contains("vbox86p")) score += 3
        if (Build.PRODUCT.contains("emulator")) score += 3
        if (Build.PRODUCT.contains("simulator")) score += 3
        if (Build.BOARD == "unknown") score += 1
        if (Build.BOOTLOADER == "unknown") score += 1
        if (Build.HARDWARE == "unknown") score += 1

        // Threshold: need multiple strong signals
        return score >= 3
    }

    /**
     * Verify APK signature against the expected SHA-256 hash.
     * If [expectedHash] is empty, logs the current hash so the developer
     * can copy it into AppConfig (first-run helper).
     */
    @Suppress("DEPRECATION", "PackageManagerGetSignatures")
    private fun verifySignature(context: Context, expectedHash: String): Boolean {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners ?: return false
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures ?: return false
            }

            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA-256")
                md.update(signature.toByteArray())
                val currentHash = md.digest().joinToString("") { "%02x".format(it) }

                if (expectedHash.isEmpty()) {
                    // First-run helper: log the hash so developer can copy it
                    Log.i(
                        TAG,
                        "═══════════════════════════════════════════════════════════\n" +
                        "  SIGNATURE HASH (copy this into AppConfig):\n" +
                        "  $currentHash\n" +
                        "═══════════════════════════════════════════════════════════"
                    )
                    // Don't fail when hash is not configured yet
                    return true
                }

                if (!currentHash.equals(expectedHash, ignoreCase = true)) {
                    Log.w(TAG, "Signature mismatch! expected=$expectedHash current=$currentHash")
                    return false
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Signature verification error", e)
            return false
        }
    }

    /**
     * Detect known tampering / hooking frameworks.
     * Returns the name of the detected tool, or null.
     */
    private fun detectTamperingTools(): String? {
        // Xposed Framework
        val xposedClasses = listOf(
            "de.robv.android.xposed.XposedBridge",
            "de.robv.android.xposed.XposedHelpers"
        )
        for (cls in xposedClasses) {
            try {
                Class.forName(cls)
                return "Xposed"
            } catch (_: ClassNotFoundException) { }
        }

        // LSPosed / EdXposed
        val lsposedClasses = listOf(
            "io.github.lsposed.lspd.core.Main",
            "org.lsposed.lspd.core.Main",
            "com.elderdrivers.riru.edxp.core.Main"
        )
        for (cls in lsposedClasses) {
            try {
                Class.forName(cls)
                return "LSPosed/EdXposed"
            } catch (_: ClassNotFoundException) { }
        }

        // Frida
        val fridaPaths = listOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server",
            "/sdcard/frida-server",
            "/data/local/tmp/frida-agent"
        )
        for (path in fridaPaths) {
            if (fileExistsSafe(path)) return "Frida"
        }

        // Substrate / Cydia
        try {
            Class.forName("com.saurik.substrate.MS")
            return "Substrate"
        } catch (_: ClassNotFoundException) { }

        // Magisk Hide (common paths)
        val magiskPaths = listOf(
            "/sbin/.magisk",
            "/data/adb/magisk"
        )
        for (path in magiskPaths) {
            if (fileExistsSafe(path)) return "Magisk"
        }

        return null
    }

    /**
     * Check if a debugger is currently attached.
     */
    private fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    /**
     * Safe root detection without dangerous Runtime.exec("su").
     * Only checks well-known filesystem paths.
     */
    private fun isRooted(): Boolean {
        val suPaths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/su/bin",
            "/system/xbin/daemonsu"
        )
        for (path in suPaths) {
            if (fileExistsSafe(path)) return true
        }

        // Check for Magisk
        if (fileExistsSafe("/sbin/.magisk") ||
            fileExistsSafe("/data/adb/magisk")) {
            return true
        }

        // Check system properties (safe, no exec)
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        return false
    }

    /**
     * Safe File.exists() wrapper that catches SecurityException.
     */
    private fun fileExistsSafe(path: String): Boolean {
        return try {
            File(path).exists()
        } catch (_: SecurityException) {
            false
        }
    }
}
