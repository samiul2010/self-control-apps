package com.example.ui.screens

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import com.example.ads.StartIoAdManager
import com.example.ads.StartIoBannerAd
import com.example.ui.gate.RewardedAdSimulatorDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CreditAccount
import com.example.data.FocusLockRepository
import com.example.receiver.FocusLockDeviceAdminReceiver
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.FocusTeal
import com.example.ui.theme.SlateDarkCard
import com.example.ui.theme.SlateDarkSurface
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  account: CreditAccount?,
  onRoleChanged: (String) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val repo = remember { FocusLockRepository.getInstance(context) }

  val dpm = remember { context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager }
  val adminComponent = remember { ComponentName(context, FocusLockDeviceAdminReceiver::class.java) }
  val isAdminActive = remember(dpm) { dpm?.isAdminActive(adminComponent) == true }

  var customQuote by remember(account) { mutableStateOf(account?.customMotivationQuote ?: "A year from now you will wish you had started today.") }
  var isEditingQuote by remember { mutableStateOf(false) }
  var showCooldownConfirm by remember { mutableStateOf(false) }
  var showAdModal by remember { mutableStateOf(false) }

  val commitmentDays = account?.commitmentDays ?: 14
  val cooldownTime = account?.cooldownRequestedTimestamp ?: 0L
  val hasCooldownActive = cooldownTime > 0L

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item { Spacer(modifier = Modifier.height(6.dp)) }

    // Header
    item {
      Text(
        text = "Guard & Commitment Settings",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White
      )
      Text(
        text = "Configure psychological barriers & device protection",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF94A3B8)
      )
    }

    // Commitment & Device Admin
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
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
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(FocusTeal.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = FocusTeal, modifier = Modifier.size(22.dp))
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Commitment Horizon",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = "$commitmentDays Days Habit Challenge",
                  style = MaterialTheme.typography.bodySmall,
                  color = FocusTeal
                )
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isAdminActive) FocusTeal.copy(alpha = 0.15f) else AmberAlert.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = if (isAdminActive) "ADMIN ACTIVE" else "ADMIN OFF",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isAdminActive) FocusTeal else AmberAlert
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          if (!isAdminActive) {
            Button(
              onClick = {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                  putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                  putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "FocusLock commitment protection requires Device Admin to prevent impulsive uninstalls during your habit period.")
                }
                context.startActivity(intent)
              },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(containerColor = FocusTeal),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF003828), modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Enable Device Admin Protection", color = Color(0xFF003828), fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 24-Hour Cooldown Section
          if (hasCooldownActive) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AmberAlert.copy(alpha = 0.15f))
                .border(1.dp, AmberAlert.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(12.dp)
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = AmberAlert)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "24-Hour Cooldown Active",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AmberAlert
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Deliberate pause to prevent impulsive decisions. You can cancel cooldown anytime to stay committed.",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFFCBD5E1)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = {
                    scope.launch { repo.cancelCooldown() }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = FocusTeal),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Cancel Cooldown & Stay Protected", color = Color(0xFF003828), fontWeight = FontWeight.Bold)
                }
              }
            }
          } else {
            OutlinedButton(
              onClick = { showCooldownConfirm = true },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3364748B))
            ) {
              Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Request 24-Hour Cooldown to Change Barrier")
            }
          }
        }
      }
    }

    // Emergency Access Configuration
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AmberAlert.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = AmberAlert, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Emergency Access Policy",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Capped safety valve per specifications",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Session Length", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf(3, 5, 10).forEach { min ->
                val isSelected = (account?.maxEmergencyMinutesPerUse ?: 5) == min
                OutlinedButton(
                  onClick = {
                    scope.launch {
                      repo.updateSettings(
                        commitmentDays = account?.commitmentDays ?: 14,
                        maxEmergencyMinutes = min,
                        maxEmergencyUses = account?.maxEmergencyUsesPerDay ?: 3,
                        userRole = account?.userRole ?: "SELF"
                      )
                    }
                  },
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) AmberAlert.copy(alpha = 0.2f) else Color.Transparent,
                    contentColor = if (isSelected) AmberAlert else Color.White
                  ),
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AmberAlert else Color(0x3364748B))
                ) {
                  Text("${min}m", style = MaterialTheme.typography.labelSmall)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Daily Pool Uses", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf(2, 3, 4).forEach { count ->
                val isSelected = (account?.maxEmergencyUsesPerDay ?: 3) == count
                OutlinedButton(
                  onClick = {
                    scope.launch {
                      repo.updateSettings(
                        commitmentDays = account?.commitmentDays ?: 14,
                        maxEmergencyMinutes = account?.maxEmergencyMinutesPerUse ?: 5,
                        maxEmergencyUses = count,
                        userRole = account?.userRole ?: "SELF"
                      )
                    }
                  },
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) AmberAlert.copy(alpha = 0.2f) else Color.Transparent,
                    contentColor = if (isSelected) AmberAlert else Color.White
                  ),
                  border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AmberAlert else Color(0x3364748B))
                ) {
                  Text("$count", style = MaterialTheme.typography.labelSmall)
                }
              }
            }
          }
        }
      }
    }

    // Motivational Affirmation Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Personal Gate Affirmation",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )

            IconButton(onClick = { isEditingQuote = !isEditingQuote }) {
              Icon(
                imageVector = if (isEditingQuote) Icons.Default.Save else Icons.Default.Edit,
                contentDescription = null,
                tint = CyanGlow,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          if (isEditingQuote) {
            OutlinedTextField(
              value = customQuote,
              onValueChange = { customQuote = it },
              modifier = Modifier.fillMaxWidth(),
              label = { Text("Your motivational quote or goal") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = {
                scope.launch {
                  repo.updateSettings(
                    commitmentDays = account?.commitmentDays ?: 14,
                    maxEmergencyMinutes = account?.maxEmergencyMinutesPerUse ?: 5,
                    maxEmergencyUses = account?.maxEmergencyUsesPerDay ?: 3,
                    userRole = account?.userRole ?: "SELF",
                    customQuote = customQuote
                  )
                  isEditingQuote = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = FocusTeal)
            ) {
              Text("Save Quote", color = Color(0xFF003828), fontWeight = FontWeight.Bold)
            }
          } else {
            Text(
              text = "“$customQuote”",
              style = MaterialTheme.typography.bodyMedium,
              color = CyanGlow
            )
          }
        }
      }
    }

    // Parent Bluetooth Status Card (No role switch allowed for child)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FocusTeal.copy(alpha = 0.3f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "প্যারেন্টস ব্লুটুথ লিঙ্ক (Parent Bluetooth Link)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "উভয় ফোনে ব্লুটুথ চালু করার সাথে সাথে স্বয়ংক্রিয়ভাবে অটো-কানেক্ট হবে।",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(FocusTeal.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text("অটো-কানেক্ট", color = FocusTeal, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "💡 প্রতি ৩টি টাস্কের ১টিতে পেন্ডিং অবস্থায় অগ্রিম ক্রেডিট চালু থাকবে। প্যারেন্ট রিজেক্ট করলে দ্বিগুণ জরিমানা কাটা যাবে।",
            style = MaterialTheme.typography.bodySmall,
            color = AmberAlert
          )
        }
      }
    }

    // Start.io Ads Network Configuration & Test Suite
    item {
      val activity = context as? Activity
      val resolvedAppId = StartIoAdManager.getResolvedAppId()

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FocusTeal.copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Start.io রিয়েল অ্যাড নেটওয়ার্ক (Ads Network)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "AI Studio সিক্রেট 'start_ad' থেকে অটো-ইন্টিগ্রেটেড",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (resolvedAppId.isNotBlank()) FocusTeal.copy(alpha = 0.2f) else AmberAlert.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = if (resolvedAppId.isNotBlank()) "অ্যাক্টিভ (ACTIVE)" else "পেন্ডিং",
                color = if (resolvedAppId.isNotBlank()) FocusTeal else AmberAlert,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // App ID display
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF0F172A))
              .padding(12.dp)
          ) {
            Column {
              Text(
                text = "Start.io App ID (via start_ad secret):",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = if (resolvedAppId.isNotBlank()) resolvedAppId else "অটো-কনফিগারেশন মোড সক্রিয়",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = CyanGlow
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Live Banner
          Text(
            text = "লাইভ ব্যানার বিজ্ঞাপন (Live Banner):",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8)
          )
          Spacer(modifier = Modifier.height(6.dp))
          StartIoBannerAd(modifier = Modifier.fillMaxWidth())
        }
      }
    }

    // OEM Background Reliability Shortcuts
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "OEM Reliability & Permissions",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "Xiaomi, Oppo, Vivo, Samsung aggressive battery killers can terminate accessibility services. Open settings to ensure FocusLock stays active.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                try {
                  context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } catch (_: Exception) {}
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
              Text("Accessibility", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
              onClick = {
                try {
                  context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                } catch (_: Exception) {}
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
              Text("Overlay", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }

  if (showCooldownConfirm) {
    AlertDialog(
      onDismissRequest = { showCooldownConfirm = false },
      title = { Text("Request 24-Hour Cooldown?") },
      text = {
        Text("To prevent impulsive undoing of your habits, FocusLock requires a 24-hour waiting period before barriers can be relaxed. Your protection remains active during this countdown.")
      },
      confirmButton = {
        Button(
          onClick = {
            scope.launch {
              repo.requestCooldown()
              showCooldownConfirm = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = AmberAlert)
        ) {
          Text("Start 24h Cooldown", color = Color(0xFF432C00), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showCooldownConfirm = false }) {
          Text("Stay Protected")
        }
      }
    )
  }

  // Fallback Rewarded Ad Dialog
  if (showAdModal) {
    RewardedAdSimulatorDialog(
      onAdFinished = { credits ->
        scope.launch {
          repo.earnAdCredits(credits)
          showAdModal = false
          Toast.makeText(context, "🎉 টেস্ট সফল! +$credits ক্রেডিট যোগ হয়েছে।", Toast.LENGTH_LONG).show()
        }
      },
      onDismiss = { showAdModal = false }
    )
  }
}
