package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledAppInfo(
  val packageName: String,
  val appName: String,
  val category: String,
  val isSystemApp: Boolean,
  val icon: Drawable? = null
)

object InstalledAppsScanner {

  /**
   * Scans all launchable third-party & user installed applications on the actual physical device.
   */
  suspend fun getInstalledUserApps(context: Context): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
      addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val resolveInfos = try {
      pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
    } catch (_: Exception) {
      emptyList()
    }

    val apps = mutableListOf<InstalledAppInfo>()
    val seenPackages = mutableSetOf<String>()
    val currentPackageName = context.packageName

    for (resolveInfo in resolveInfos) {
      val pkg = resolveInfo.activityInfo?.packageName ?: continue
      if (pkg == currentPackageName || seenPackages.contains(pkg)) {
        continue
      }
      seenPackages.add(pkg)

      val appInfo = resolveInfo.activityInfo.applicationInfo ?: continue
      val appName = try {
        resolveInfo.loadLabel(pm).toString()
      } catch (_: Exception) {
        pkg
      }

      val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
      val category = categorizeApp(appName, pkg)

      apps.add(
        InstalledAppInfo(
          packageName = pkg,
          appName = appName,
          category = category,
          isSystemApp = isSystem
        )
      )
    }

    // Sort: Third party / non-system first, then alphabetically
    apps.sortedWith(
      compareBy<InstalledAppInfo> { it.isSystemApp }
        .thenBy { it.appName.lowercase() }
    )
  }

  private fun categorizeApp(name: String, pkg: String): String {
    val lower = (name + " " + pkg).lowercase()
    return when {
      lower.contains("tiktok") || lower.contains("musically") || lower.contains("reels") || lower.contains("shorts") -> "Short Video"
      lower.contains("instagram") || lower.contains("facebook") || lower.contains("twitter") || lower.contains("snapchat") || lower.contains("reddit") || lower.contains("threads") -> "Social Media"
      lower.contains("youtube") || lower.contains("netflix") || lower.contains("hotstar") || lower.contains("prime") || lower.contains("twitch") -> "Video & Streaming"
      lower.contains("game") || lower.contains("pubg") || lower.contains("freefire") || lower.contains("candy") || lower.contains("clash") -> "Mobile Gaming"
      lower.contains("chat") || lower.contains("messenger") || lower.contains("whatsapp") || lower.contains("telegram") || lower.contains("discord") -> "Instant Messaging"
      lower.contains("browser") || lower.contains("chrome") || lower.contains("firefox") || lower.contains("opera") -> "Web Browser"
      else -> "Installed Application"
    }
  }
}
