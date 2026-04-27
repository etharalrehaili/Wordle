package com.khammin.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.onesignal.OneSignal
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        FirebaseApp.initializeApp(this)

        val appCheckFactory = if (BuildConfig.DEBUG) {
            com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(appCheckFactory)

        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
        val prefs = getSharedPreferences("app_settings_prefs", MODE_PRIVATE)
        val dialogShown = prefs.getBoolean("notification_dialog_shown", false)
        if (!dialogShown) {
            prefs.edit().putBoolean("notification_dialog_shown", true).apply()
            CoroutineScope(Dispatchers.IO).launch {
                OneSignal.Notifications.requestPermission(false)
            }
        }
    }
}