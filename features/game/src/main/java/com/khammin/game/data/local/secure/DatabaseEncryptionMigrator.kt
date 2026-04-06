package com.khammin.game.data.local.secure

import android.content.Context
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseEncryptionMigrator @Inject constructor() {

    /**
     * Checks whether [dbName] on-disk is a plain-text SQLite file.
     * If it is, the file is deleted so Room can create a fresh encrypted
     * database in its place. All app data is re-synced from the server
     * on the next launch.
     *
     * Plain SQLite files always begin with the 16-byte ASCII magic string
     * "SQLite format 3\u0000". SQLCipher-encrypted files start with random
     * cipher-text, so the check is reliable.
     */
    fun migrateIfNeeded(context: Context, dbName: String) {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) return

        val header = ByteArray(16)
        try {
            FileInputStream(dbFile).use { it.read(header) }
        } catch (_: Exception) {
            return // can't read the file — let Room handle it
        }

        if (String(header, Charsets.UTF_8).startsWith("SQLite format 3")) {
            context.deleteDatabase(dbName)
        }
    }
}
