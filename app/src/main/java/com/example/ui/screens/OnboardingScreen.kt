package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusLockRepository
import com.example.service.BluetoothSyncManager
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyanGlow
import com.example.util.InstalledAppInfo
import com.example.util.InstalledAppsScanner
import com.example.ui.theme.FocusTeal
import com.example.ui.theme.SlateDarkBackground
import com.example.ui.theme.SlateDarkCard
import com.example.ui.theme.SlateDarkSurface
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
  onFinishOnboarding: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val repo = remember { FocusLockRepository.getInstance(context) }
  val btManager = remember { BluetoothSyncManager.getInstance(context) }

  // Step 1: Role Selection. If Parent: Step 2 is Bluetooth only! If Child: 2 (Perms), 3 (Apps), 4 (Emergency), 5 (BT / Skip)
  var selectedRole by remember { mutableStateOf("SELF") } // "SELF" or "PARENT"
  var currentStep by remember { mutableIntStateOf(1) }

  val totalSteps = if (selectedRole == "PARENT") 2 else 5

  // Child selections
  var selectedApps by remember {
    mutableStateOf(FocusLockRepository.PRESET_APPS.associate { it.packageName to true }.toMutableMap())
  }
  var emergencyMinutes by remember { mutableIntStateOf(5) }
  var emergencyDailyUses by remember { mutableIntStateOf(3) }
  var commitmentDays by remember { mutableIntStateOf(14) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(SlateDarkBackground, Color(0xFF0F1C3F), SlateDarkBackground)
        )
      )
      .windowInsetsPadding(WindowInsets.safeDrawing)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {

      // Top Stepper Bar
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (selectedRole == "PARENT") "প্যারেন্টস সেটআপ" else "চিলড্রেন সেটআপ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = FocusTeal
          )
          Text(
            text = "ধাপ $currentStep / $totalSteps",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
          progress = { currentStep.toFloat() / totalSteps },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = FocusTeal,
          trackColor = Color(0xFF1E293B)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Main Step Views
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        if (selectedRole == "PARENT") {
          when (currentStep) {
            1 -> RoleSelectionStep(
              selectedRole = selectedRole,
              onRoleSelect = { selectedRole = it }
            )
            2 -> ParentBluetoothOnlyStep(
              onConnect = {
                btManager.simulateVirtualLink("চিলড্রেন ফোন (অটো-কানেক্টেড)")
              }
            )
          }
        } else {
          when (currentStep) {
            1 -> RoleSelectionStep(
              selectedRole = selectedRole,
              onRoleSelect = { selectedRole = it }
            )
            2 -> PermissionsExplainerStep(context)
            3 -> AppSelectionStep(
              selectedApps = selectedApps,
              onToggleApp = { pkg, state ->
                selectedApps = selectedApps.toMutableMap().apply { put(pkg, state) }
              }
            )
            4 -> RulesAndCommitmentStep(
              emergencyMinutes = emergencyMinutes,
              onMinutesChange = { emergencyMinutes = it },
              emergencyUses = emergencyDailyUses,
              onUsesChange = { emergencyDailyUses = it },
              commitmentDays = commitmentDays,
              onCommitmentChange = { commitmentDays = it }
            )
            5 -> ChildParentConnectStep(
              onSkipParent = {
                // User clicked "স্কিপ কানেক্ট প্যারেন্টস"
                scope.launch {
                  saveAndFinish(repo, selectedApps, commitmentDays, emergencyMinutes, emergencyDailyUses, "SELF", onFinishOnboarding)
                }
              },
              onConnectParent = {
                btManager.simulateVirtualLink("প্যারেন্টস ফোন (অটো-কানেক্টেড)")
                scope.launch {
                  saveAndFinish(repo, selectedApps, commitmentDays, emergencyMinutes, emergencyDailyUses, "SELF", onFinishOnboarding)
                }
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Navigation Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (currentStep > 1) {
          OutlinedButton(
            onClick = { currentStep-- },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text("পিছনে")
          }
        }

        // Action button for forward navigation
        if (selectedRole == "PARENT") {
          Button(
            onClick = {
              if (currentStep == 1) {
                currentStep = 2
              } else {
                // Complete as PARENT!
                scope.launch {
                  repo.updateSettings(
                    commitmentDays = 14,
                    maxEmergencyMinutes = 5,
                    maxEmergencyUses = 3,
                    userRole = "PARENT"
                  )
                  repo.setOnboardingCompleted(true)
                  onFinishOnboarding()
                }
              }
            },
            modifier = Modifier.weight(if (currentStep > 1) 1.5f else 1f),
            colors = ButtonDefaults.buttonColors(containerColor = FocusTeal),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text(
              text = if (currentStep == 2) "প্যারেন্ট মোড চালু করুন" else "পরবর্তী ধাপ",
              fontWeight = FontWeight.Bold,
              color = Color(0xFF003828)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = if (currentStep == 2) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = Color(0xFF003828),
              modifier = Modifier.size(18.dp)
            )
          }
        } else {
          // In Child mode step 5, skip button handles completion directly
          if (currentStep < 5) {
            Button(
              onClick = { currentStep++ },
              modifier = Modifier.weight(if (currentStep > 1) 1.5f else 1f),
              colors = ButtonDefaults.buttonColors(containerColor = FocusTeal),
              shape = RoundedCornerShape(14.dp)
            ) {
              Text("পরবর্তী ধাপ", fontWeight = FontWeight.Bold, color = Color(0xFF003828))
              Spacer(modifier = Modifier.width(6.dp))
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF003828), modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }
  }
}

private suspend fun saveAndFinish(
  repo: FocusLockRepository,
  selectedApps: Map<String, Boolean>,
  commitmentDays: Int,
  emergencyMinutes: Int,
  emergencyUses: Int,
  role: String,
  onFinish: () -> Unit
) {
  selectedApps.forEach { (pkg, isRestricted) ->
    repo.toggleRestrictedApp(pkg, isRestricted)
  }
  repo.updateSettings(
    commitmentDays = commitmentDays,
    maxEmergencyMinutes = emergencyMinutes,
    maxEmergencyUses = emergencyUses,
    userRole = role
  )
  repo.setOnboardingCompleted(true)
  onFinish()
}

@Composable
fun RoleSelectionStep(
  selectedRole: String,
  onRoleSelect: (String) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    Text(
      text = "আপনার ভূমিকা বেছে নিন",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.ExtraBold,
      color = Color.White
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "প্যারেন্টস বা চিলড্রেন হিসেবে সঠিক মোড নির্বাচন করুন।",
      style = MaterialTheme.typography.bodyMedium,
      color = Color(0xFF94A3B8)
    )

    Spacer(modifier = Modifier.height(20.dp))

    // 1. প্যারেন্টস
    RoleOptionCard(
      title = "প্যারেন্টস (Parent)",
      description = "কোনো অপ্রয়োজনীয় পারমিশন লাগবে না। শুধু ব্লুটুথ দিয়ে চিলড্রেনের সাথে কানেক্ট হয়ে টাস্ক ভেরিফাই করবেন (একসেপ্ট বা রিজেক্ট)।",
      icon = Icons.Default.FamilyRestroom,
      isSelected = selectedRole == "PARENT",
      onClick = { onRoleSelect("PARENT") }
    )

    Spacer(modifier = Modifier.height(14.dp))

    // 2. চিলড্রেন
    RoleOptionCard(
      title = "চিলড্রেন (Child / Habit Builder)",
      description = "অ্যাপ ব্লকার, ক্রেডিট ইকোনমি, শিডিউল রুটিন এবং অভ্যাস গঠনের সম্পূর্ণ সিস্টেম।",
      icon = Icons.Default.Person,
      isSelected = selectedRole == "SELF",
      onClick = { onRoleSelect("SELF") }
    )
  }
}

@Composable
fun ParentBluetoothOnlyStep(
  onConnect: () -> Unit
) {
  var isLinked by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    Text(
      text = "ব্লুটুথ কানেকশন",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.ExtraBold,
      color = Color.White
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "প্যারেন্ট হিসেবে আপনার ফোনে কোনো অ্যাক্সেসিবিলিটি বা ডিভাইস অ্যাডমিন পারমিশন লাগবে না। শুধু ব্লুটুথ এর মাধ্যমে চিলড্রেনের সাথে কানেক্ট হতে হবে।",
      style = MaterialTheme.typography.bodyMedium,
      color = Color(0xFF94A3B8)
    )

    Spacer(modifier = Modifier.height(20.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
      shape = RoundedCornerShape(20.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, FocusTeal.copy(alpha = 0.3f))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(FocusTeal.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = null,
            tint = FocusTeal,
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = if (isLinked) "🟢 চিলড্রেন ডিভাইস কানেক্টেড!" else "ব্লুটুথ অটো-কানেক্ট প্রস্তুত",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "উভয় ফোনে ব্লুটুথ চালু করার সাথে সাথে অটো কানেক্ট হবে। চিলড্রেন টাস্ক জমা দিলে সরাসরি আপনার স্ক্রিনে আসবে।",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF94A3B8),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            onConnect()
            isLinked = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = FocusTeal),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF003828))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isLinked) "কানেক্ট সফল হয়েছে" else "চিলড্রেন এর সাথে অটো-কানেক্ট করুন",
            color = Color(0xFF003828),
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
fun ChildParentConnectStep(
  onSkipParent: () -> Unit,
  onConnectParent: () -> Unit
) {
  var isLinked by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    Text(
      text = "প্যারেন্টস এর সাথে কানেক্ট",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.ExtraBold,
      color = Color.White
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "টাস্ক ভেরিফিকেশন ও রিওয়ার্ড পাওয়ার জন্য প্যারেন্ট ডিভাইসের সাথে ব্লুটুথে কানেক্ট হতে পারেন।",
      style = MaterialTheme.typography.bodyMedium,
      color = Color(0xFF94A3B8)
    )

    Spacer(modifier = Modifier.height(18.dp))

    // 1-in-3 rule explainer card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
      shape = RoundedCornerShape(18.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, AmberAlert.copy(alpha = 0.3f))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = AmberAlert, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "প্যারেন্টস কানেক্ট না থাকলে নিয়ম:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = AmberAlert
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "• পরবর্তীতে প্যারেন্টস কানেক্ট না হলে টাস্কের রিওয়ার্ড লক থাকবে।\n• তবে আপনি যদি অনেকগুলো টাস্ক করেন, প্রতি ৩টি টাস্কের মধ্যে ১টি টাস্কের পেন্ডিং থাকা অবস্থাতেই অগ্রিম ক্রেডিট ব্যবহার করতে পারবেন।\n• যদি পরে প্যারেন্টস ওই টাস্কটি রিজেক্ট করে, তবে দ্বিগুণ (Double) পয়েন্ট জরিমানা কাটা হবে!",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFFCBD5E1),
          lineHeight = 20.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Action 1: Connect Bluetooth
    Button(
      onClick = {
        onConnectParent()
        isLinked = true
      },
      modifier = Modifier.fillMaxWidth(),
      colors = ButtonDefaults.buttonColors(containerColor = FocusTeal),
      shape = RoundedCornerShape(14.dp)
    ) {
      Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color(0xFF003828))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (isLinked) "প্যারেন্টস কানেক্টেড (শুরু করুন)" else "প্যারেন্টস এর সাথে ব্লুটুথ কানেক্ট করুন",
        color = Color(0xFF003828),
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Action 2: "স্কিপ কানেক্ট প্যারেন্টস" Button as explicitly requested!
    OutlinedButton(
      onClick = onSkipParent,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4464748B))
    ) {
      Text(
        text = "স্কিপ কানেক্ট প্যারেন্টস (Skip Connect Parent)",
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
fun PermissionsExplainerStep(context: Context) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    Text(
      text = "প্রয়োজনীয় পারমিশন (Permissions)",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.ExtraBold,
      color = Color.White
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "সোশ্যাল মিডিয়া স্ক্রোলিং আটকানো এবং অভ্যাস সুরক্ষার জন্য চিলড্রেন ডিভাইসে এই পারমিশনগুলো জরুরি।",
      style = MaterialTheme.typography.bodyMedium,
      color = Color(0xFF94A3B8)
    )

    Spacer(modifier = Modifier.height(16.dp))

    PermissionTrustCard(
      title = "অ্যাক্সেসিবিলিটি সার্ভিস (Accessibility)",
      description = "সোশ্যাল মিডিয়া অ্যাপ ওপেন করার সাথে সাথে শনাক্ত করে ইন্টারসেপশন গেট চালু করে।",
      icon = Icons.Default.Accessibility,
      actionLabel = "অনুমতি দিন",
      onClick = {
        try {
          context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (_: Exception) {}
      }
    )

    Spacer(modifier = Modifier.height(12.dp))

    PermissionTrustCard(
      title = "ওভারলে পারমিশন (Display over other apps)",
      description = "স্ক্রোলিং করার আগেই ডিসট্রাকশন স্ক্রিনের উপরে সচেতনতামূলক গেট ও স্পনসর ফিড দেখায়।",
      icon = Icons.Default.Layers,
      actionLabel = "অনুমতি দিন",
      onClick = {
        try {
          context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        } catch (_: Exception) {}
      }
    )

    Spacer(modifier = Modifier.height(12.dp))

    PermissionTrustCard(
      title = "ডিভাইস অ্যাডমিন (Commitment Guard)",
      description = "আপনার নির্ধারিত কমিটমেন্ট চলাকালীন ঝোঁকের মাথায় অ্যাপ আনইনস্টল করা প্রতিরোধ করে।",
      icon = Icons.Default.AdminPanelSettings,
      actionLabel = "অনুমতি দিন",
      onClick = {}
    )

    Spacer(modifier = Modifier.height(12.dp))

    PermissionTrustCard(
      title = "ইউসেজ অ্যাক্সেস ও নোটিফিকেশন",
      description = "টাইমার সচল রাখতে এবং আনলক সময়ের নোটিফিকেশন প্রদর্শনের জন্য প্রয়োজন।",
      icon = Icons.Default.QueryStats,
      actionLabel = "অনুমতি দিন",
      onClick = {
        try {
          context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } catch (_: Exception) {}
      }
    )
  }
}

@Composable
fun PermissionTrustCard(
  title: String,
  description: String,
  icon: ImageVector,
  actionLabel: String,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2264748B))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(FocusTeal.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = FocusTeal, modifier = Modifier.size(22.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
      }
    }
  }
}

@Composable
fun AppSelectionStep(
  selectedApps: Map<String, Boolean>,
  onToggleApp: (packageName: String, isRestricted: Boolean) -> Unit
) {
  val context = LocalContext.current
  var installedApps by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }
  var isScanning by remember { mutableStateOf(true) }

  LaunchedEffect(Unit) {
    val scanned = InstalledAppsScanner.getInstalledUserApps(context)
    installedApps = scanned
    isScanning = false
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    Text(
      text = "যে অ্যাপগুলো ব্লক করবেন",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.ExtraBold,
      color = Color.White
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "আপনার ডিভাইসের ইনস্টল করা অ্যাপগুলো স্ক্যান করে দেখানো হচ্ছে। যে অ্যাপগুলো ব্লক করতে চান সিলেক্ট করুন:",
      style = MaterialTheme.typography.bodyMedium,
      color = Color(0xFF94A3B8)
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (isScanning) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(color = FocusTeal)
          Spacer(modifier = Modifier.height(12.dp))
          Text("ডিভাইসের অ্যাপস স্ক্যান করা হচ্ছে...", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
        }
      }
    } else {
      // List all scanned installed apps, or fallback to PRESET_APPS if empty
      val displayApps = if (installedApps.isNotEmpty()) {
        installedApps.map {
          it.packageName to Pair(it.appName, it.category)
        }
      } else {
        FocusLockRepository.PRESET_APPS.map {
          it.packageName to Pair(it.appName, it.category)
        }
      }

      displayApps.forEach { (pkg, details) ->
        val (name, cat) = details
        val isChecked = selectedApps[pkg] ?: (pkg.contains("tiktok") || pkg.contains("instagram") || pkg.contains("facebook") || pkg.contains("youtube") || pkg.contains("twitter") || pkg.contains("snapchat"))
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggleApp(pkg, !isChecked) },
          colors = CardDefaults.cardColors(
            containerColor = if (isChecked) SlateDarkSurface else SlateDarkCard
          ),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isChecked) FocusTeal.copy(alpha = 0.5f) else Color(0x22475569)
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = null,
                  tint = if (isChecked) FocusTeal else Color(0xFF64748B),
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = name,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = cat,
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF94A3B8)
                )
              }
            }

            Checkbox(
              checked = isChecked,
              onCheckedChange = { onToggleApp(pkg, it) },
              colors = CheckboxDefaults.colors(
                checkedColor = FocusTeal,
                checkmarkColor = Color(0xFF003828)
              )
            )
          }
        }
      }
    }
  }
}

@Composable
fun RulesAndCommitmentStep(
  emergencyMinutes: Int,
  onMinutesChange: (Int) -> Unit,
  emergencyUses: Int,
  onUsesChange: (Int) -> Unit,
  commitmentDays: Int,
  onCommitmentChange: (Int) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    Text(
      text = "জরুরি অ্যাক্সেস ও কমিটমেন্ট",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.ExtraBold,
      color = Color.White
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "আপনার সেফটি ভালভ ও প্রতিজ্ঞার সময়সীমা নির্ধারণ করুন।",
      style = MaterialTheme.typography.bodyMedium,
      color = Color(0xFF94A3B8)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Timer, contentDescription = null, tint = AmberAlert, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "জরুরি সেশন সময়সীমা (সর্বোচ্চ ১০ মি)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(3, 5, 10).forEach { min ->
            val isSelected = emergencyMinutes == min
            OutlinedButton(
              onClick = { onMinutesChange(min) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isSelected) AmberAlert.copy(alpha = 0.2f) else Color.Transparent,
                contentColor = if (isSelected) AmberAlert else Color.White
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AmberAlert else Color(0x3364748B))
            ) {
              Text("$min মি", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Shield, contentDescription = null, tint = FocusTeal, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "কমিটমেন্ট চ্যালেঞ্জ সময়কাল",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(7, 14, 30).forEach { days ->
            val isSelected = commitmentDays == days
            OutlinedButton(
              onClick = { onCommitmentChange(days) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isSelected) FocusTeal.copy(alpha = 0.2f) else Color.Transparent,
                contentColor = if (isSelected) FocusTeal else Color.White
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) FocusTeal else Color(0x3364748B))
            ) {
              Text("$days দিন", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun RoleOptionCard(
  title: String,
  description: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) SlateDarkSurface else SlateDarkCard
    ),
    shape = RoundedCornerShape(18.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.5.dp,
      if (isSelected) FocusTeal else Color(0x22475569)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(if (isSelected) FocusTeal.copy(alpha = 0.2f) else Color(0xFF1E293B)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isSelected) FocusTeal else Color(0xFF94A3B8),
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
      }
      RadioButton(
        selected = isSelected,
        onClick = onClick,
        colors = RadioButtonDefaults.colors(selectedColor = FocusTeal)
      )
    }
  }
}
