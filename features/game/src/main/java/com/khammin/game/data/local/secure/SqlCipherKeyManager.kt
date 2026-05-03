package com.khammin.game.data.local.secure

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SqlCipherKeyManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    companion object {
        private const val KEYSTORE_ALIAS = "sqlcipher_keystore_key"
        private val ENCRYPTED_KEY = stringPreferencesKey("encrypted_key")
        private val ENCRYPTION_IV  = stringPreferencesKey("encryption_iv")
    }

    var wasKeyRecovered: Boolean = false
        private set

    init {
        initialize()
    }

    private fun initialize() {
        generateKeystoreKeyIfNeeded()
        runBlocking {
            val preferences = dataStore.data.first()
            if (preferences[ENCRYPTED_KEY] == null) {
                generateAndEncryptSqlCipherKey()
            }
        }
    }

    private fun generateKeystoreKeyIfNeeded() {
        if (keyStore.containsAlias(KEYSTORE_ALIAS)) return
        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply { init(keyGenSpec) }
            .generateKey()
    }

    // suspend — called from within the runBlocking in initialize(), avoiding nested runBlocking
    private suspend fun generateAndEncryptSqlCipherKey() {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(KEYSTORE_ALIAS))

        val sqlCipherKey = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val encryptedKey = cipher.doFinal(sqlCipherKey)
        val iv = cipher.iv

        dataStore.edit { prefs ->
            prefs[ENCRYPTED_KEY] = Base64.encodeToString(encryptedKey, Base64.NO_WRAP)
            prefs[ENCRYPTION_IV]  = Base64.encodeToString(iv, Base64.NO_WRAP)
        }

        sqlCipherKey.fill(0) // zero out plaintext key from memory
    }

    private fun getDecryptedSqlCipherKey(encryptedKeyB64: String, ivB64: String): ByteArray {
        val encryptedKey = Base64.decode(encryptedKeyB64, Base64.NO_WRAP)
        val ivBytes      = Base64.decode(ivB64, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(KEYSTORE_ALIAS), GCMParameterSpec(128, ivBytes))
        return cipher.doFinal(encryptedKey)
    }

    private fun getSecretKey(keyAlias: String): SecretKey =
        (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey

    private suspend fun recoverFromKeyMismatch() {
        dataStore.edit { prefs ->
            prefs.remove(ENCRYPTED_KEY)
            prefs.remove(ENCRYPTION_IV)
        }
        if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            keyStore.deleteEntry(KEYSTORE_ALIAS)
        }
        generateKeystoreKeyIfNeeded()
        generateAndEncryptSqlCipherKey()
        wasKeyRecovered = true
    }

    fun getSupportFactory(): SupportOpenHelperFactory = runBlocking {

        val prefs = dataStore.data.first()
        val encryptedKey = prefs[ENCRYPTED_KEY] ?: error("Encrypted SQLCipher key missing")
        val iv           = prefs[ENCRYPTION_IV]  ?: error("SQLCipher IV missing")
        val decryptedKey = try {
            getDecryptedSqlCipherKey(encryptedKey, iv)
        } catch (_: AEADBadTagException) {
            // Keystore key was wiped (e.g. app reinstall with auto-backup restore).
            // Regenerate everything and signal the caller to delete the old database.
            recoverFromKeyMismatch()
            val recoveredPrefs = dataStore.data.first()
            getDecryptedSqlCipherKey(
                recoveredPrefs[ENCRYPTED_KEY] ?: error("Encrypted SQLCipher key missing after recovery"),
                recoveredPrefs[ENCRYPTION_IV]  ?: error("SQLCipher IV missing after recovery"),
            )
        }
        // clearPassphrase = true: SQLCipher zeros the key array after opening the database
        SupportOpenHelperFactory(decryptedKey, null, true)

    }
}
