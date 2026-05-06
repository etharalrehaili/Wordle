package com.khammin.core.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.khammin.core.BuildConfig
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun preload(context: Context) {
        if (rewardedAd != null || isLoading) return
        isLoading = true
        // Use applicationContext so the loaded RewardedAd never captures an Activity reference.
        RewardedAd.load(
            context.applicationContext,
            BuildConfig.AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                }
            }
        )
    }

    fun showHintAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onNotReady: () -> Unit,
    ) {
        val ad = rewardedAd
        if (ad == null) {
            onNotReady()
            preload(activity)
            return
        }
        ad.show(activity) {
            onRewarded()
        }
        rewardedAd = null
        preload(activity)
    }

    /**
     * Call from Activity.onDestroy(). Drops the loaded ad so the singleton
     * does not hold a reference chain into the destroyed Activity.
     */
    fun destroy() {
        rewardedAd = null
        isLoading = false
    }
}