package com.khammin.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.khammin.core.data.extentions.languageDataStore
import com.khammin.core.data.extentions.themeDataStore
import com.onesignal.OneSignal
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
        FirebaseApp.initializeApp(this)

        val appCheckFactory = PlayIntegrityAppCheckProviderFactory.getInstance()
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(appCheckFactory)

        // OneSignal must be initialized on the main thread.
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)

        CoroutineScope(Dispatchers.IO).launch {
            // Pre-warm DataStore caches so the first runBlocking read in MainActivity
            // finds data already in memory instead of hitting disk.
            languageDataStore.data.first()
            themeDataStore.data.first()

            // MobileAds init is slow (~300ms). Safe to run off the main thread —
            // ads will not load until after init completes regardless.
            MobileAds.initialize(this@MyApp) {}

            // Notification permission prompt — no reason to block the main thread.
            val prefs = getSharedPreferences("app_settings_prefs", MODE_PRIVATE)
            if (!prefs.getBoolean("notification_dialog_shown", false)) {
                prefs.edit().putBoolean("notification_dialog_shown", true).apply()
                OneSignal.Notifications.requestPermission(false)
            }
        }
    }
}