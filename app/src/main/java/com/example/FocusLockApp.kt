package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.FocusLockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FocusLockApp : Application() {

  private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  lateinit var repository: FocusLockRepository
    private set

  companion object {
    const val CHANNEL_ID_FOCUS = "focuslock_channel"
    const val CHANNEL_NAME_FOCUS = "FocusLock Status & Alerts"
    private lateinit var instance: FocusLockApp

    fun get(): FocusLockApp = instance
  }

  override fun onCreate() {
    super.onCreate()
    instance = this
    repository = FocusLockRepository.getInstance(this)

    // Initialize Start.io Ads using App ID from Secret start_ad
    com.example.ads.StartIoAdManager.initialize(this)

    createNotificationChannels()

    applicationScope.launch {
      repository.initializeDefaultsIfEmpty()
    }
  }

  private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID_FOCUS,
        CHANNEL_NAME_FOCUS,
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Shows active unlocked app timers and habit reminders"
      }
      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.createNotificationChannel(channel)
    }
  }
}
