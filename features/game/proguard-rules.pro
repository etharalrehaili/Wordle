# ─────────────────────────────────────────────────────────────────────────────
# game module — consumer rules (applied when the app is built)
# ─────────────────────────────────────────────────────────────────────────────

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-dontwarn androidx.room.**

# ── Retrofit / Gson API models ────────────────────────────────────────────────
# These data classes have NO @SerializedName annotations — Gson maps JSON keys
# directly to field names at runtime, so field names must not be obfuscated.
-keep class com.khammin.game.data.remote.model.** { *; }

# ── Domain models ─────────────────────────────────────────────────────────────
-keep class com.khammin.game.domain.model.** { *; }

# ── Repository and data layer (Hilt injection targets) ───────────────────────
-keep class com.khammin.game.data.repository.** { *; }
-keep class com.khammin.game.data.di.** { *; }

# ── ProfileSyncWorker ─────────────────────────────────────────────────────────
# WorkManager instantiates workers by class name via reflection.
-keep class com.khammin.game.data.local.worker.** extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ── SQLCipher ─────────────────────────────────────────────────────────────────
-keep class net.sqlcipher.** { *; }
-keep interface net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# ── Secure local classes ──────────────────────────────────────────────────────
-keep class com.khammin.game.data.local.secure.** { *; }

# ── Use cases ─────────────────────────────────────────────────────────────────
-keep class com.khammin.game.domain.usecases.** { *; }
