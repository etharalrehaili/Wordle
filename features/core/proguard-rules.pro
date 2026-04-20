# ─────────────────────────────────────────────────────────────────────────────
# core module — consumer rules (applied when the app is built)
# ─────────────────────────────────────────────────────────────────────────────

# ── KeyManager (JNI) ──────────────────────────────────────────────────────────
# Native method names must match exactly what is compiled in native-lib.so.
# Both the class name and every native method must be preserved as-is.
-keep class com.khammin.core.data.ndk.KeyManager {
    native <methods>;
    *;
}

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
-dontwarn androidx.datastore.**

# ── Kotlin Serialization ──────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.khammin.core.**$$serializer { *; }
-keepclassmembers class com.khammin.core.** {
    *** Companion;
}
-keepclasseswithmembers class com.khammin.core.** {
    kotlinx.serialization.KSerializer serializer(...);
}
