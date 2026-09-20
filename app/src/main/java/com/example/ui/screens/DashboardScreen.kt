package com.example.ui.screens

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import com.example.ads.StartIoAdManager
import com.example.ads.StartIoBannerAd
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CreditAccount
import com.example.data.FocusLockRepository
import com.example.data.RestrictedApp
import com.example.data.UnlockedSession
import com.example.service.InterceptionManager
import com.example.ui.gate.RewardedAdSimulatorDialog
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.FocusTeal
import com.example.ui.theme.SlateDarkCard
import com.example.ui.theme.SlateDarkSurface
import com.example.util.InstalledAppInfo
import com.example.util.InstalledAppsScanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
  account: CreditAccount?,
  restrictedApps: List<RestrictedApp>,
  activeSessions: List<UnlockedSession>
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val repo = remember { FocusLockRepository.getInstance(context) }

  var showAdModal by remember { mutableStateOf(false) }
  var showAddAppDialog by remember { mutableStateOf(false) }
  var showDeviceAppsScannerDialog by remember { mutableStateOf(false) }
  var scannedInstalledApps by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }
  var isScanningInstalledApps by remember { mutableStateOf(false) }
  var newAppName by remember { mutableStateOf("") }
  var newPackageName by remember { mutableStateOf("") }

  // Periodic ticker for active unlocks
  var currentTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
  LaunchedEffect(Unit) {
    while (true) {
      delay(1000L)
      currentTimestamp = System.currentTimeMillis()
    }
  }

  // Check Accessibility Service status
  fun isAccessibilityServiceEnabled(ctx: Context): Boolean {
    val am = ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
    return enabledServices.any { it.resolveInfo.serviceInfo.packageName == ctx.packageName }
  }

  var isServiceEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item { Spacer(modifier = Modifier.height(6.dp)) }

    // Status Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FocusTeal.copy(alpha = 0.3f))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(FocusTeal.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Shield,
                  contentDescription = null,
                  tint = FocusTeal,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "FocusLock Active",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = "${restrictedApps.count { it.isRestricted }} Apps Intercepted",
                  style = MaterialTheme.typography.bodySmall,
                  color = FocusTeal
                )
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(FocusTeal.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = "ONLINE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = FocusTeal
              )
            }
          }

          if (!isServiceEnabled) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AmberAlert.copy(alpha = 0.15f))
                .border(1.dp, AmberAlert.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(12.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AmberAlert)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Accessibility Service Needed",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AmberAlert
                  )
                  Text(
                    text = "Tap to enable FocusLock in Android Accessibility Settings for automatic app interception.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1)
                  )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                  onClick = {
                    try {
                      context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    } catch (_: Exception) {}
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = AmberAlert),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Enable", color = Color(0xFF432C00), fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    // Credit Economy Card
    item {
      val payCredits = account?.payAsYouGoCredits ?: 0
      val bankedCredits = account?.bankedCredits ?: 0
      val totalCredits = payCredits + bankedCredits

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22475569))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Credit Economy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Earned by habits & ads • Spent for access",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
              )
            }

            val activity = context as? Activity
            Button(
              onClick = {
                val configuredAppId = StartIoAdManager.getResolvedAppId()
                if (activity != null && configuredAppId.isNotBlank()) {
                  StartIoAdManager.showRewardedVideo(
                    activity = activity,
                    onRewardEarned = {
                      scope.launch {
                        repo.earnAdCredits(20)
                        Toast.makeText(context, "🎉 অভিনন্দন! Start.io বিজ্ঞাপন দেখার জন্য +২০ ক্রেডিট যোগ হয়েছে!", Toast.LENGTH_LONG).show()
                      }
                    },
                    onFailed = { _ ->
                      // Graceful fallback to Fullscreen Video Ad Player
                      showAdModal = true
                    }
                  )
                } else {
                  // Direct instant fullscreen video ad player
                  showAdModal = true
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = FocusTeal),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF003828), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Watch Ad (+20)", color = Color(0xFF003828), fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "$payCredits", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = FocusTeal)
              Text(text = "Pay-As-You-Go", style = MaterialTheme.typography.labelSmall, color = Color.White)
              Text(text = "From Ad Views", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = Color(0xFF64748B))
            }

            Box(modifier = Modifier.height(36.dp).width(1.dp).background(Color(0x2264748B)))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "$bankedCredits", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = CyanGlow)
              Text(text = "Banked Credits", style = MaterialTheme.typography.labelSmall, color = Color.White)
              Text(text = "From Study Tasks", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = Color(0xFF64748B))
            }

            Box(modifier = Modifier.height(36.dp).width(1.dp).background(Color(0x2264748B)))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "$totalCredits", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = AmberAlert)
              Text(text = "Total Available", style = MaterialTheme.typography.labelSmall, color = Color.White)
              Text(text = "Redeemable", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = Color(0xFF64748B))
            }
          }
        }
      }
    }

    // Real Start.io Banner Ad Unit
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22475569))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Sponsored • Start.io Ads",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF64748B)
            )
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF334155))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(text = "AD", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontSize = 9.sp)
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          StartIoBannerAd(modifier = Modifier.fillMaxWidth())
        }
      }
    }

    // Active Unlocks Banner (if any)
    val validActiveSessions = activeSessions.filter { it.unlockedUntilTimestamp > currentTimestamp }
    if (validActiveSessions.isNotEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF13283E)),
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanGlow)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, tint = CyanGlow)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Active Unlocked Access",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
              Text(
                text = "${validActiveSessions.size} active",
                style = MaterialTheme.typography.labelSmall,
                color = CyanGlow
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            validActiveSessions.forEach { session ->
              val secondsLeft = ((session.unlockedUntilTimestamp - currentTimestamp) / 1000L).coerceAtLeast(0)
              val minutes = secondsLeft / 60
              val seconds = secondsLeft % 60
              val timeString = String.format("%02d:%02d", minutes, seconds)

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFF0B192A))
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = session.appName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Text(
                    text = "Remaining: $timeString (${session.unlockType})",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanGlow,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                Button(
                  onClick = {
                    scope.launch {
                      repo.relockSession(session.packageName)
                      InterceptionManager.recordLocalLock(session.packageName)
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = CoralWarning.copy(alpha = 0.8f)),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Relock", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
              }
              Spacer(modifier = Modifier.height(6.dp))
            }
          }
        }
      }
    }

    // Emergency Access Pool Summary
    item {
      val passes = account?.emergencyUsesLeftToday ?: 0
      val maxPasses = account?.maxEmergencyUsesPerDay ?: 3
      val maxMin = account?.maxEmergencyMinutesPerUse ?: 5

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AmberAlert.copy(alpha = 0.2f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(AmberAlert.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = AmberAlert, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Emergency Passes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "$maxMin min per pass • Resets midnight",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
              )
            }
          }

          Text(
            text = "$passes / $maxPasses Left",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = AmberAlert
          )
        }
      }
    }

    // Restricted Apps Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Managed Apps",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        Row {
          TextButton(
            onClick = {
              showDeviceAppsScannerDialog = true
              isScanningInstalledApps = true
              scope.launch {
                scannedInstalledApps = InstalledAppsScanner.getInstalledUserApps(context)
                isScanningInstalledApps = false
              }
            }
          ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = FocusTeal, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("স্ক্যান ডিভাইস", color = FocusTeal)
          }

          TextButton(onClick = { showAddAppDialog = true }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = FocusTeal, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Custom", color = FocusTeal)
          }
        }
      }
    }

    // Restricted Apps List
    items(restrictedApps) { app ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(14.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (app.isRestricted) CoralWarning.copy(alpha = 0.15f) else Color(0xFF334155)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = if (app.isRestricted) CoralWarning else Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = app.category,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
              )
            }
          }

          Switch(
            checked = app.isRestricted,
            onCheckedChange = { isChecked ->
              scope.launch {
                repo.toggleRestrictedApp(app.packageName, isChecked)
              }
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = FocusTeal,
              checkedTrackColor = Color(0xFF003828)
            )
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(20.dp)) }
  }

  // Rewarded Ad Modal
  if (showAdModal) {
    RewardedAdSimulatorDialog(
      onAdFinished = { credits ->
        scope.launch {
          repo.earnAdCredits(credits)
          showAdModal = false
        }
      },
      onDismiss = { showAdModal = false }
    )
  }

  // Add Custom App Dialog
  if (showAddAppDialog) {
    AlertDialog(
      onDismissRequest = { showAddAppDialog = false },
      title = { Text("Add App to Intercept") },
      text = {
        Column {
          OutlinedTextField(
            value = newAppName,
            onValueChange = { newAppName = it },
            label = { Text("App Name (e.g. Threads)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = newPackageName,
            onValueChange = { newPackageName = it },
            label = { Text("Package Name (e.g. com.instagram.barcelona)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newAppName.isNotBlank() && newPackageName.isNotBlank()) {
              scope.launch {
                repo.addCustomRestrictedApp(newPackageName.trim(), newAppName.trim())
                newAppName = ""
                newPackageName = ""
                showAddAppDialog = false
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = FocusTeal)
        ) {
          Text("Add", color = Color(0xFF003828), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddAppDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Device Apps Real Scanner Dialog
  if (showDeviceAppsScannerDialog) {
    AlertDialog(
      onDismissRequest = { showDeviceAppsScannerDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = FocusTeal)
          Spacer(modifier = Modifier.width(8.dp))
          Text("ডিভাইসের রিয়েল অ্যাপস স্ক্যান")
        }
      },
      text = {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
        ) {
          if (isScanningInstalledApps) {
            Column(
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              CircularProgressIndicator(color = FocusTeal)
              Spacer(modifier = Modifier.height(14.dp))
              Text("আপনার ডিভাইসের অ্যাপস স্ক্যান করা হচ্ছে...", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
            }
          } else if (scannedInstalledApps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text("কোনো অ্যাপ পাওয়া যায়নি বা পারমিশন প্রয়োজন।", color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(scannedInstalledApps) { installedApp ->
                val alreadyAdded = restrictedApps.any { it.packageName == installedApp.packageName }
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(installedApp.appName, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                      Text(installedApp.packageName, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                    }

                    Button(
                      onClick = {
                        scope.launch {
                          repo.addCustomRestrictedApp(installedApp.packageName, installedApp.appName)
                          repo.toggleRestrictedApp(installedApp.packageName, !alreadyAdded)
                        }
                      },
                      colors = ButtonDefaults.buttonColors(
                        containerColor = if (alreadyAdded) CoralWarning.copy(alpha = 0.8f) else FocusTeal
                      ),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Text(
                        if (alreadyAdded) "রিমুভ" else "ব্লক তালিকায় যোগ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (alreadyAdded) Color.White else Color(0xFF003828)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showDeviceAppsScannerDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = FocusTeal)
        ) {
          Text("সম্পন্ন", color = Color(0xFF003828), fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}
