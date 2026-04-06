package com.khammin.core.data.ndk

object KeyManager {

    init {
        System.loadLibrary("native-lib")
    }

    external fun getAuthToken(): String

    external fun getBaseUrl(): String

    external fun getFirebaseDbUrl(): String

    external fun getGoogleApiKey(): String

    external fun getBaseHost(): String
}
