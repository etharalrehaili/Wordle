package com.khammin.game.data.local.secure

import android.content.Context
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseEncryptionMigrator @Inject constructor() {

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
