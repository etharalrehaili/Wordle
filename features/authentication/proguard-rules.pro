# ─────────────────────────────────────────────────────────────────────────────
# authentication module — consumer rules (applied when the app is built)
# ─────────────────────────────────────────────────────────────────────────────

# ── Firebase Auth ─────────────────────────────────────────────────────────────
-keep class com.google.firebase.auth.** { *; }
-keepclassmembers class com.google.firebase.auth.** { *; }
-dontwarn com.google.firebase.auth.**

# ── Domain layer ─────────────────────────────────────────────────────────────
-keep class com.khammin.authentication.domain.** { *; }

# ── Hilt injection targets ────────────────────────────────────────────────────
-keep class com.khammin.authentication.data.di.** { *; }
