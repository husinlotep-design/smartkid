package com.smartkids.launcher.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILE   = "smartkids_secure_prefs"
        private const val KEY_PIN_HASH = "parent_pin_hash"
        private const val KEY_PIN_SET  = "pin_is_set"
        const val DEFAULT_PIN          = "1234"
    }

    // EncryptedSharedPreferences ensures the data is encrypted at rest
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Checks if a custom PIN has been created by the parent.
     */
    fun isPinSet(): Boolean = prefs.getBoolean(KEY_PIN_SET, false)

    /**
     * Hashes and saves a new PIN.
     */
    fun setPin(pin: String) {
        prefs.edit()
            .putString(KEY_PIN_HASH, pin.sha256())
            .putBoolean(KEY_PIN_SET, true)
            .apply()
    }

    /**
     * Verifies the input against the saved hash.
     * If no PIN is set, it checks against the DEFAULT_PIN (1234).
     */
    fun verifyPin(input: String): Boolean {
        return if (!isPinSet()) {
            // If first time, allow access with default and set it
            if (input == DEFAULT_PIN) {
                setPin(DEFAULT_PIN)
                true
            } else false
        } else {
            val savedHash = prefs.getString(KEY_PIN_HASH, "")
            savedHash == input.sha256()
        }
    }

    /**
     * Updates the PIN only if the old PIN is verified first.
     * Returns true if successful, false if old PIN was wrong.
     */
    fun updatePin(oldPin: String, newPin: String): Boolean {
        return if (verifyPin(oldPin)) {
            setPin(newPin)
            true
        } else {
            false
        }
    }

    /**
     * SHA-256 Hashing function to ensure the actual PIN is never stored as text.
     */
    private fun String.sha256(): String {
        return try {
            val bytes = java.security.MessageDigest
                .getInstance("SHA-256")
                .digest(this.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // Fallback for safety (though SHA-256 is standard in Android)
            ""
        }
    }
}