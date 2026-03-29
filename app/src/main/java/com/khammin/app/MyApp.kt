package com.khammin.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.onesignal.OneSignal
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        OneSignal.initWithContext(this, "837992e0-4742-4dfc-ab23-13c939090da2")
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }
    }
}