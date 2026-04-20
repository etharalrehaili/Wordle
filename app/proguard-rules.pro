# ─────────────────────────────────────────────────────────────────────────────
# App-level ProGuard rules
# Applied on top of the consumer rules from every feature module.
# ─────────────────────────────────────────────────────────────────────────────

# ── Crash-report readability ──────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Lazy { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlin.**

# ── Kotlin Coroutines ─────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keepclasseswithmembers class * { @dagger.hilt.* <methods>; }
-dontwarn dagger.**

# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ── OneSignal ─────────────────────────────────────────────────────────────────
-keep class com.onesignal.** { *; }
-dontwarn com.onesignal.**

# ── OkHttp ────────────────────────────────────────────────────────────────────
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ── Retrofit ──────────────────────────────────────────────────────────────────
-keep class retrofit2.** { *; }
-keepattributes Signature, Exceptions, InnerClasses
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# ── Gson ──────────────────────────────────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-dontwarn sun.misc.**

# ── WorkManager ───────────────────────────────────────────────────────────────
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ── Coil ──────────────────────────────────────────────────────────────────────
-keep class coil.** { *; }
-dontwarn coil.**

# ── AndroidX ──────────────────────────────────────────────────────────────────
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.**
