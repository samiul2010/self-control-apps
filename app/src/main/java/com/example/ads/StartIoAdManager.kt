package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.ads.banner.BannerListener
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener

object StartIoAdManager {
  private const val TAG = "StartIoAdManager"

  private var isInitialized = false
  var currentAppId: String = ""
    private set

  /**
   * Resolves the Start.io App ID from BuildConfig (injected via AI Studio Secrets: start_ad)
   * or environment variables.
   */
  fun getResolvedAppId(): String {
    val fromSecret = try {
      val direct = BuildConfig.start_ad
      if (!direct.isNullOrBlank() && direct != "YOUR_START_IO_APP_ID") direct else ""
    } catch (_: Throwable) {
      ""
    }

    if (fromSecret.isNotBlank()) return fromSecret

    val fromUpper = try {
      val directUpper = BuildConfig.START_AD
      if (!directUpper.isNullOrBlank() && directUpper != "YOUR_START_IO_APP_ID") directUpper else ""
    } catch (_: Throwable) {
      ""
    }
    if (fromUpper.isNotBlank()) return fromUpper

    val fromEnv = System.getenv("start_ad") ?: System.getenv("START_AD") ?: ""
    if (fromEnv.isNotBlank() && fromEnv != "YOUR_START_IO_APP_ID") return fromEnv

    return ""
  }

  fun initialize(context: Context) {
    if (isInitialized) return

    val appId = getResolvedAppId()
    currentAppId = appId

    if (appId.isNotBlank()) {
      Log.i(TAG, "Initializing Start.io SDK with configured App ID: $appId")
      try {
        // Initialize Start.io SDK with the real App ID, disable auto return ads
        StartAppSDK.init(context, appId, false)
        StartAppSDK.enableReturnAds(false)
        isInitialized = true
        Log.i(TAG, "Start.io SDK successfully initialized.")
      } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Start.io SDK", e)
      }
    } else {
      Log.w(TAG, "No valid start_ad App ID found in Secrets. Start.io SDK will initialize when configured.")
    }
  }

  /**
   * Loads and displays a Rewarded Video Ad.
   * When video completes, executes onRewardEarned callback.
   */
  fun showRewardedVideo(
    activity: Activity,
    onRewardEarned: () -> Unit,
    onFailed: (String) -> Unit = {}
  ) {
    val appId = getResolvedAppId()
    if (!isInitialized && appId.isNotBlank()) {
      initialize(activity.applicationContext)
    }

    val rewardedAd = StartAppAd(activity)
    rewardedAd.setVideoListener(object : VideoListener {
      override fun onVideoCompleted() {
        Log.i(TAG, "Start.io Rewarded Video completed! Granting credits.")
        activity.runOnUiThread {
          onRewardEarned()
        }
      }
    })

    rewardedAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
      override fun onReceiveAd(ad: Ad) {
        Log.d(TAG, "Start.io Rewarded Ad loaded successfully. Showing now.")
        rewardedAd.showAd(object : AdDisplayListener {
          override fun adHidden(ad: Ad?) {
            Log.d(TAG, "Rewarded Ad hidden")
          }
          override fun adDisplayed(ad: Ad?) {
            Log.d(TAG, "Rewarded Ad displayed")
          }
          override fun adClicked(ad: Ad?) {
            Log.d(TAG, "Rewarded Ad clicked")
          }
          override fun adNotDisplayed(ad: Ad?) {
            Log.w(TAG, "Rewarded Ad not displayed: ${ad?.errorMessage}")
            activity.runOnUiThread {
              onFailed(ad?.errorMessage ?: "Ad could not be displayed")
            }
          }
        })
      }

      override fun onFailedToReceiveAd(ad: Ad?) {
        val error = ad?.errorMessage ?: "Network error / Ad not ready"
        Log.e(TAG, "Start.io Rewarded Ad load failed: $error")
        activity.runOnUiThread {
          onFailed(error)
        }
      }
    })
  }

  /**
   * Displays an Interstitial ad (e.g. before unlocking an emergency session or on completion)
   */
  fun showInterstitial(
    activity: Activity,
    onClosed: () -> Unit = {}
  ) {
    val appId = getResolvedAppId()
    if (!isInitialized && appId.isNotBlank()) {
      initialize(activity.applicationContext)
    }

    val interstitialAd = StartAppAd(activity)
    interstitialAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
      override fun onReceiveAd(ad: Ad) {
        interstitialAd.showAd(object : AdDisplayListener {
          override fun adHidden(ad: Ad?) {
            activity.runOnUiThread { onClosed() }
          }
          override fun adDisplayed(ad: Ad?) {}
          override fun adClicked(ad: Ad?) {}
          override fun adNotDisplayed(ad: Ad?) {
            activity.runOnUiThread { onClosed() }
          }
        })
      }

      override fun onFailedToReceiveAd(ad: Ad?) {
        activity.runOnUiThread { onClosed() }
      }
    })
  }
}

/**
 * Jetpack Compose Composable for displaying a Start.io Banner Ad
 */
@Composable
fun StartIoBannerAd(
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentSize(Alignment.Center)
  ) {
    AndroidView(
      modifier = Modifier.fillMaxWidth(),
      factory = { context ->
        Banner(context).apply {
          setBannerListener(object : BannerListener {
            override fun onReceiveAd(banner: android.view.View?) {
              Log.d("StartIoBanner", "Start.io Banner ad received and displayed")
            }
            override fun onFailedToReceiveAd(banner: android.view.View?) {
              Log.w("StartIoBanner", "Start.io Banner failed to receive ad")
            }
            override fun onClick(banner: android.view.View?) {
              Log.d("StartIoBanner", "Start.io Banner clicked")
            }
            override fun onImpression(banner: android.view.View?) {
              Log.d("StartIoBanner", "Start.io Banner impression recorded")
            }
          })
        }
      }
    )
  }
}
