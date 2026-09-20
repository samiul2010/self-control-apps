package com.example.service

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.example.FocusLockApp
import com.example.ui.gate.InterceptionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object InterceptionManager {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val activeUnlocks = ConcurrentHashMap<String, Long>() // packageName -> unlockUntilMs
  private val restrictedPackagesCache = ConcurrentHashMap<String, String>() // packageName -> appName

  // Debounce tracking to prevent repeated rapid startActivity triggers
  private var lastInterceptedPackage: String? = null
  private var lastInterceptTimeElapsedMs: Long = 0L
  private const val INTERCEPT_DEBOUNCE_MS = 3500L

  // Ignore system & critical packages
  private val SYSTEM_WHITELIST = setOf(
    "com.aistudio.focuslock.qzrk",
    "com.example",
    "com.android.systemui",
    "com.android.settings",
    "com.google.android.apps.nexuslauncher",
    "com.android.launcher3",
    "com.sec.android.app.launcher",
    "com.google.android.inputmethod.latin",
    "com.android.phone",
    "com.android.dialer",
    "com.google.android.dialer",
    "android"
  )

  private val _lastInterceptedApp = MutableStateFlow<String?>(null)
  val lastInterceptedApp: StateFlow<String?> = _lastInterceptedApp.asStateFlow()

  fun init(context: Context) {
    val repo = FocusLockApp.get().repository
    scope.launch {
      repo.restrictedApps.collect { apps ->
        restrictedPackagesCache.clear()
        apps.filter { it.isRestricted }.forEach {
          restrictedPackagesCache[it.packageName] = it.appName
        }
      }
    }
    scope.launch {
      repo.activeSessions.collect { sessions ->
        activeUnlocks.clear()
        val now = System.currentTimeMillis()
        sessions.filter { it.unlockedUntilTimestamp > now }.forEach {
          activeUnlocks[it.packageName] = it.unlockedUntilTimestamp
        }
      }
    }
  }

  fun isWhitelisted(packageName: String): Boolean {
    return SYSTEM_WHITELIST.contains(packageName) || packageName.startsWith("com.android.internal")
  }

  fun shouldIntercept(packageName: String): Boolean {
    if (isWhitelisted(packageName)) return false
    val appName = restrictedPackagesCache[packageName] ?: return false

    val unlockExpiry = activeUnlocks[packageName]
    if (unlockExpiry != null && unlockExpiry > System.currentTimeMillis()) {
      // Currently unlocked!
      return false
    }
    return true
  }

  fun getAppName(packageName: String): String {
    return restrictedPackagesCache[packageName] ?: "Distracting App"
  }

  fun triggerInterception(context: Context, packageName: String, appName: String) {
    val nowElapsed = SystemClock.elapsedRealtime()
    if (packageName == lastInterceptedPackage && (nowElapsed - lastInterceptTimeElapsedMs) < INTERCEPT_DEBOUNCE_MS) {
      // Debounce: Already intercepted within the cooldown window
      return
    }
    lastInterceptedPackage = packageName
    lastInterceptTimeElapsedMs = nowElapsed

    _lastInterceptedApp.value = appName
    val intent = Intent(context, InterceptionActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra(InterceptionActivity.EXTRA_PACKAGE_NAME, packageName)
      putExtra(InterceptionActivity.EXTRA_APP_NAME, appName)
    }
    context.startActivity(intent)
  }

  fun recordLocalUnlock(packageName: String, untilMs: Long) {
    activeUnlocks[packageName] = untilMs
  }

  fun recordLocalLock(packageName: String) {
    activeUnlocks.remove(packageName)
  }
}
