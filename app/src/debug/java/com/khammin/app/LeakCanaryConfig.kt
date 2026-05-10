package com.khammin.app

import leakcanary.LeakCanary
import shark.AndroidReferenceMatchers

internal fun configureLeakCanary() {
    LeakCanary.config = LeakCanary.config.copy(
        referenceMatchers = AndroidReferenceMatchers.appDefaults +
                AndroidReferenceMatchers.ignoredInstanceField(
                    "com.google.android.gms.dynamic.ObjectWrapper", "a"
                )
    )
}
