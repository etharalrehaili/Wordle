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
import net.sqlcipher.database.SupportFactory
import java.security.KeyStore
import java.security.SecureRandom
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
        private val ENCRYPTED_KEY = stringPreferencesKey("encrypted_key")
        private val ENCRYPTION_IV = stringPreferencesKey("encryption_iv")
    }

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
        if (!keyStore.containsAlias("sqlcipher_keystore_key")) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenSpec = KeyGenParameterSpec.Builder(
                "sqlcipher_keystore_key",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
    }

    private fun generateAndEncryptSqlCipherKey() {
        val secretKey = getSecretKey("sqlcipher_keystore_key")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val sqlCipherKey = ByteArray(32)
        SecureRandom().nextBytes(sqlCipherKey)

        val encryptedKey = cipher.doFinal(sqlCipherKey)
        val iv = cipher.iv

        runBlocking {
            dataStore.edit { preferences ->
                preferences[ENCRYPTED_KEY] = Base64.encodeToString(encryptedKey, Base64.NO_WRAP)
                preferences[ENCRYPTION_IV] = Base64.encodeToString(iv, Base64.NO_WRAP)
            }
        }

        // Zero out the key in memory
        sqlCipherKey.fill(0)
    }

    private fun getDecryptedSqlCipherKey(keyAlias: String, key: String, iv: String): ByteArray {
        val encryptedKey = Base64.decode(key, Base64.NO_WRAP)
        val ivBytes = Base64.decode(iv, Base64.NO_WRAP)

        val secretKey = getSecretKey(keyAlias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, ivBytes))

        return cipher.doFinal(encryptedKey)
    }

    private fun getSecretKey(keyAlias: String): SecretKey =
        (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey

    fun getSupportFactory(): SupportFactory = runBlocking {
        val preferences = dataStore.data.first()

        val encryptedKey = preferences[ENCRYPTED_KEY]
            ?: error("Encrypted key missing")

        val iv = preferences[ENCRYPTION_IV]
            ?: error("IV missing")

        val decryptedKey = getDecryptedSqlCipherKey(
            "sqlcipher_keystore_key",
            encryptedKey,
            iv
        )

        SupportFactory(decryptedKey, null, false)
    }

}