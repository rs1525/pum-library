package com.akustom15.pum.security

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.android.vending.licensing.ILicenseResultListener
import com.android.vending.licensing.ILicensingService
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

/**
 * Google Play License Verification Library (LVL) implementation.
 *
 * Verifies that the user legitimately purchased the app by communicating
 * with the Google Play Store app on the device via AIDL.
 *
 * Flow:
 * 1. Bind to Play Store's ILicensingService
 * 2. Send license check request with a random nonce
 * 3. Play Store responds with: responseCode + signedData + signature
 * 4. Verify the RSA signature using the app's public key from Play Console
 * 5. Return LICENSED / NOT_LICENSED / ERROR via callback
 *
 * Requires: com.android.vending.CHECK_LICENSE permission in AndroidManifest.xml
 */
class PumLicenseChecker(
    private val context: Context,
    private val base64PublicKey: String
) {

    companion object {
        private const val TAG = "PumLicense"

        // License response codes from Google Play
        const val LICENSED = 0
        const val NOT_LICENSED = 1
        const val LICENSED_OLD_KEY = 2
        const val ERROR_NOT_MARKET_MANAGED = 3
        const val ERROR_SERVER_FAILURE = 0x101
        const val ERROR_OVER_QUOTA = 0x102
        const val ERROR_CONTACTING_SERVER = 0x103
        const val ERROR_INVALID_PACKAGE_NAME = 0x104
        const val ERROR_NON_MATCHING_UID = 0x105
    }

    interface LicenseCallback {
        fun onLicenseResult(isLicensed: Boolean, reason: String)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var service: ILicensingService? = null
    private var serviceConnection: ServiceConnection? = null
    private var isBound = false

    /**
     * Start the license check. Result delivered on main thread via [callback].
     * In debug builds this should NOT be called (PumSecurityManager skips it).
     */
    fun checkLicense(callback: LicenseCallback) {
        if (base64PublicKey.isEmpty()) {
            Log.w(TAG, "LICENSE_KEY is empty — skipping license check")
            mainHandler.post { callback.onLicenseResult(true, "No license key configured") }
            return
        }

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service = ILicensingService.Stub.asInterface(binder)
                try {
                    val nonce = SecureRandom().nextLong()
                    val listener = object : ILicenseResultListener.Stub() {
                        override fun verifyLicense(
                            responseCode: Int,
                            signedData: String?,
                            signature: String?
                        ) {
                            handleResponse(responseCode, signedData, signature, nonce, callback)
                            unbind()
                        }
                    }
                    service?.checkLicense(nonce, context.packageName, listener)
                } catch (e: Exception) {
                    Log.e(TAG, "Error calling checkLicense", e)
                    mainHandler.post { callback.onLicenseResult(true, "License check error: ${e.message}") }
                    unbind()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
            }
        }

        serviceConnection = connection

        val intent = Intent("com.android.vending.licensing.ILicensingService").apply {
            setPackage("com.android.vending")
        }

        try {
            isBound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            if (!isBound) {
                Log.w(TAG, "Could not bind to Play Store licensing service")
                mainHandler.post {
                    callback.onLicenseResult(true, "Play Store not available for license check")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind to licensing service", e)
            mainHandler.post {
                callback.onLicenseResult(true, "Bind failed: ${e.message}")
            }
        }
    }

    /**
     * Handle the license response from Play Store.
     * Validates the RSA signature to ensure the response is authentic.
     */
    private fun handleResponse(
        responseCode: Int,
        signedData: String?,
        signature: String?,
        nonce: Long,
        callback: LicenseCallback
    ) {
        // Verify the RSA signature of the response
        if (signedData != null && signature != null) {
            if (!verifySignature(signedData, signature)) {
                Log.w(TAG, "License response signature verification FAILED")
                mainHandler.post {
                    callback.onLicenseResult(false, "Invalid license response signature")
                }
                return
            }

            // Verify nonce matches to prevent replay attacks
            if (!verifyNonce(signedData, nonce)) {
                Log.w(TAG, "License response nonce mismatch — possible replay attack")
                mainHandler.post {
                    callback.onLicenseResult(false, "License nonce mismatch")
                }
                return
            }
        }

        when (responseCode) {
            LICENSED, LICENSED_OLD_KEY -> {
                Log.i(TAG, "License check PASSED (code=$responseCode)")
                mainHandler.post { callback.onLicenseResult(true, "Licensed") }
            }
            NOT_LICENSED -> {
                Log.w(TAG, "License check FAILED — NOT LICENSED")
                mainHandler.post { callback.onLicenseResult(false, "Not licensed") }
            }
            ERROR_NOT_MARKET_MANAGED -> {
                Log.w(TAG, "License check: app not market managed (code=$responseCode)")
                // Don't block — could be testing or free app
                mainHandler.post { callback.onLicenseResult(true, "Not market managed") }
            }
            else -> {
                Log.w(TAG, "License check error (code=$responseCode)")
                // On server errors, don't block the user — graceful degradation
                mainHandler.post {
                    callback.onLicenseResult(true, "Server error (code=$responseCode)")
                }
            }
        }
    }

    /**
     * Verify the RSA-SHA1 signature of the license response
     * using the app's public key from Google Play Console.
     */
    private fun verifySignature(signedData: String, signatureBase64: String): Boolean {
        return try {
            val decodedKey = android.util.Base64.decode(base64PublicKey, android.util.Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(decodedKey)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey = keyFactory.generatePublic(keySpec)

            val sig = Signature.getInstance("SHA1withRSA")
            sig.initVerify(publicKey)
            sig.update(signedData.toByteArray(Charsets.UTF_8))

            val decodedSig = android.util.Base64.decode(signatureBase64, android.util.Base64.DEFAULT)
            sig.verify(decodedSig)
        } catch (e: Exception) {
            Log.e(TAG, "Signature verification error", e)
            false
        }
    }

    /**
     * Verify that the nonce in the signed response matches the one we sent.
     * Response format: "responseCode|nonce|packageName|versionCode|userId|timestamp:extras"
     */
    private fun verifyNonce(signedData: String, expectedNonce: Long): Boolean {
        return try {
            val fields = signedData.split("|")
            if (fields.size >= 2) {
                val responseNonce = fields[1].toLong()
                responseNonce == expectedNonce
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not parse nonce from response", e)
            false
        }
    }

    /**
     * Unbind from the licensing service.
     */
    private fun unbind() {
        try {
            if (isBound && serviceConnection != null) {
                context.unbindService(serviceConnection!!)
                isBound = false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error unbinding from licensing service", e)
        }
    }
}
