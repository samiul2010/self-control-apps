package com.example.ui.gate

import android.app.Activity
import android.content.Intent
import com.example.ads.StartIoAdManager
import com.example.ads.StartIoBannerAd
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CreditAccount
import com.example.data.FocusLockRepository
import com.example.service.InterceptionManager
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.FocusTeal
import com.example.ui.theme.FocusTealDark
import com.example.ui.theme.SlateDarkBackground
import com.example.ui.theme.SlateDarkCard
import com.example.ui.theme.SlateDarkSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun InterceptionGateContent(
  packageName: String,
  appName: String,
  account: CreditAccount?,
  hasBacklog: Boolean,
  onUnlockSuccess: (durationMinutes: Int) -> Unit,
  onDismissToHome: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val repo = remember { FocusLockRepository.getInstance(context) }

  var showAdPlayerModal by remember { mutableStateOf(false) }
  var showMotivationalVideoModal by remember { mutableStateOf(false) }
  var successMessage by remember { mutableStateOf<String?>(null) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val payCredits = account?.payAsYouGoCredits ?: 0
  val bankedCredits = account?.bankedCredits ?: 0
  val totalCredits = payCredits + bankedCredits
  val emergencyPassesLeft = account?.emergencyUsesLeftToday ?: 0
  val maxEmergencyMin = account?.maxEmergencyMinutesPerUse ?: 5
  val isStudyActive = account?.isStudyBlockActive == true

  // Background Gradient
  val gateBrush = Brush.verticalGradient(
    colors = listOf(
      SlateDarkBackground,
      Color(0xFF0F1B38),
      Color(0xFF0A1226)
    )
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(gateBrush)
      .windowInsetsPadding(WindowInsets.safeDrawing)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      // Top Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(FocusTeal.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = "FocusLock Shield",
              tint = FocusTeal,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "FocusLock Intercept",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        IconButton(onClick = onDismissToHome) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss to home",
            tint = Color(0xFF94A3B8)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Central Urge Gate Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33475569))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(CircleShape)
              .background(CoralWarning.copy(alpha = 0.15f))
              .border(2.dp, CoralWarning.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Gated App",
              tint = CoralWarning,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "$appName is Gated",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Replace the urge to scroll with mindful action. Earn credits or spend what you have banked.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Motivational Quote Pill
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF1E293B).copy(alpha = 0.8f))
              .padding(horizontal = 14.dp, vertical = 10.dp)
          ) {
            Text(
              text = "“${account?.customMotivationQuote ?: "A year from now you will wish you had started today."}”",
              style = MaterialTheme.typography.bodySmall,
              color = CyanGlow,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Real Start.io Banner Unit
          StartIoBannerAd(modifier = Modifier.fillMaxWidth())
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Lockout or Backlog Warning Banners
      if (isStudyActive) {
        WarningBanner(
          title = "Study Block Active (${account.activeStudyBlockName.ifEmpty { "Focus Session" }})",
          description = "No social media or credit spending allowed during scheduled study blocks. Only emergency access is available.",
          isRed = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
          onClick = { showMotivationalVideoModal = true },
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = AmberAlert),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Watch Motivational Video", tint = Color(0xFF432C00))
          Spacer(modifier = Modifier.width(8.dp))
          Text("কাজের মোটিভেশনাল ভিডিও দেখুন", fontWeight = FontWeight.Bold, color = Color(0xFF432C00))
        }
        Spacer(modifier = Modifier.height(12.dp))
      } else if (hasBacklog) {
        WarningBanner(
          title = "Anti-Backlog Lock Active",
          description = "You have uncompleted scheduled tasks from earlier today. Clear your backlog in the Routines tab before you can spend credits!",
          isRed = false
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
          onClick = { showMotivationalVideoModal = true },
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = CyanGlow),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Watch Motivational Video", tint = Color(0xFF003544))
          Spacer(modifier = Modifier.width(8.dp))
          Text("মোটিভেশনাল ভিডিও দেখুন (Focus Booster)", fontWeight = FontWeight.Bold, color = Color(0xFF003544))
        }
        Spacer(modifier = Modifier.height(12.dp))
      }

      // Live Credit Economy Status
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300C896))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          CreditStatPill(
            label = "Pay-As-You-Go",
            amount = payCredits,
            sublabel = "From Ads",
            color = FocusTeal
          )
          Box(
            modifier = Modifier
              .height(36.dp)
              .width(1.dp)
              .background(Color(0x3364748B))
          )
          CreditStatPill(
            label = "Banked Credits",
            amount = bankedCredits,
            sublabel = "From Tasks",
            color = CyanGlow
          )
          Box(
            modifier = Modifier
              .height(36.dp)
              .width(1.dp)
              .background(Color(0x3364748B))
          )
          CreditStatPill(
            label = "Total Pool",
            amount = totalCredits,
            sublabel = "Available",
            color = AmberAlert
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Section: Earn Credits (Ad Watching)
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(20.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Option A: Watch Sponsored Ad",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Earn +15 pay-as-you-go credits immediately",
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
              Text(
                text = "+15 CR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = FocusTeal
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = {
              val act = context as? Activity
              val configuredAppId = StartIoAdManager.getResolvedAppId()
              if (act != null && configuredAppId.isNotBlank()) {
                StartIoAdManager.showRewardedVideo(
                  activity = act,
                  onRewardEarned = {
                    scope.launch {
                      repo.earnAdCredits(20)
                      successMessage = "সফল! Start.io বিজ্ঞাপন দেখে +২০ ক্রেডিট যুক্ত হয়েছে।"
                    }
                  },
                  onFailed = { _ ->
                    // Fallback to simulator if real ad fails to load
                    showAdPlayerModal = true
                  }
                )
              } else {
                showAdPlayerModal = true
              }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = FocusTeal),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Play ad",
              tint = Color(0xFF003828)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Watch Rewarded Ad Now (+20 CR)",
              fontWeight = FontWeight.Bold,
              color = Color(0xFF003828)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Section: Spend Credits to Unlock
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(20.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Text(
            text = "Option B: Spend Banked Credits",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = if (isStudyActive || hasBacklog) "Credit spending currently locked by study block or backlog."
            else "Unlock access for a controlled time window.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Unlock Option 1: 5 Min for 15 Credits
          UnlockOptionRow(
            durationMin = 5,
            creditCost = 15,
            canAfford = totalCredits >= 15 && !isStudyActive && !hasBacklog,
            onUnlock = {
              scope.launch {
                val ok = repo.spendCreditsToUnlock(packageName, appName, 5, 15)
                if (ok) {
                  InterceptionManager.recordLocalUnlock(packageName, System.currentTimeMillis() + 5 * 60 * 1000L)
                  onUnlockSuccess(5)
                } else {
                  errorMessage = "Insufficient credits to unlock."
                }
              }
            }
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Unlock Option 2: 15 Min for 30 Credits
          UnlockOptionRow(
            durationMin = 15,
            creditCost = 30,
            canAfford = totalCredits >= 30 && !isStudyActive && !hasBacklog,
            onUnlock = {
              scope.launch {
                val ok = repo.spendCreditsToUnlock(packageName, appName, 15, 30)
                if (ok) {
                  InterceptionManager.recordLocalUnlock(packageName, System.currentTimeMillis() + 15 * 60 * 1000L)
                  onUnlockSuccess(15)
                } else {
                  errorMessage = "Insufficient credits to unlock."
                }
              }
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Section: Emergency Access
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AmberAlert.copy(alpha = 0.3f))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Option C: Emergency Access",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AmberAlert
              )
              Text(
                text = "Fixed safety valve ($maxEmergencyMin min). Resets midnight.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AmberAlert.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "$emergencyPassesLeft left today",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AmberAlert
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedButton(
            onClick = {
              val act = context as? Activity
              act?.let { StartIoAdManager.showInterstitial(it) }
              scope.launch {
                val ok = repo.useEmergencyPass(packageName, appName)
                if (ok) {
                  InterceptionManager.recordLocalUnlock(packageName, System.currentTimeMillis() + maxEmergencyMin * 60 * 1000L)
                  onUnlockSuccess(maxEmergencyMin)
                } else {
                  errorMessage = "No emergency passes remaining for today."
                }
              }
            },
            enabled = emergencyPassesLeft > 0,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberAlert),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (emergencyPassesLeft > 0) AmberAlert else Color.Gray),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(imageVector = Icons.Default.Bolt, contentDescription = "Emergency")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (emergencyPassesLeft > 0) "Use Emergency Pass ($maxEmergencyMin Min)" else "0 Passes Left Today",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Stay Focused / Exit Button
      Button(
        onClick = onDismissToHome,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Stay Focused & Return Home", color = Color.White, fontWeight = FontWeight.SemiBold)
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Rewarded Ad Simulation Player Modal
  if (showAdPlayerModal) {
    RewardedAdSimulatorDialog(
      onAdFinished = { earnedCredits ->
        scope.launch {
          repo.earnAdCredits(earnedCredits)
          showAdPlayerModal = false
          successMessage = "Success! +$earnedCredits Pay-as-you-go credits added."
        }
      },
      onDismiss = { showAdPlayerModal = false }
    )
  }

  // Motivational Video Modal (During study schedule / focus time)
  if (showMotivationalVideoModal) {
    MotivationalVideoDialog(
      taskTitle = account?.activeStudyBlockName?.ifEmpty { "ফোকাস স্টাডি সেশন" } ?: "ফোকাস সেশন",
      onDismiss = { showMotivationalVideoModal = false }
    )
  }
}

@Composable
fun CreditStatPill(label: String, amount: Int, sublabel: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = "$amount",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.ExtraBold,
      color = color
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = Color.White,
      fontWeight = FontWeight.SemiBold
    )
    Text(
      text = sublabel,
      style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
      color = Color(0xFF64748B)
    )
  }
}

@Composable
fun WarningBanner(title: String, description: String, isRed: Boolean) {
  val bgColor = if (isRed) CoralWarning.copy(alpha = 0.15f) else AmberAlert.copy(alpha = 0.15f)
  val borderColor = if (isRed) CoralWarning.copy(alpha = 0.4f) else AmberAlert.copy(alpha = 0.4f)
  val iconColor = if (isRed) CoralWarning else AmberAlert

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(bgColor)
      .border(1.dp, borderColor, RoundedCornerShape(14.dp))
      .padding(14.dp)
  ) {
    Row(verticalAlignment = Alignment.Top) {
      Icon(
        imageVector = if (isRed) Icons.Default.Block else Icons.Default.Warning,
        contentDescription = "Warning",
        tint = iconColor,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = iconColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFFCBD5E1)
        )
      }
    }
  }
}

@Composable
fun UnlockOptionRow(
  durationMin: Int,
  creditCost: Int,
  canAfford: Boolean,
  onUnlock: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF1E293B))
      .padding(horizontal = 14.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Icons.Default.Timer,
        contentDescription = "Timer",
        tint = CyanGlow,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = "$durationMin Minutes Access",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "Cost: $creditCost Credits",
          style = MaterialTheme.typography.bodySmall,
          color = if (canAfford) FocusTeal else Color(0xFFEF4444)
        )
      }
    }

    Button(
      onClick = onUnlock,
      enabled = canAfford,
      colors = ButtonDefaults.buttonColors(
        containerColor = FocusTeal,
        disabledContainerColor = Color(0xFF334155)
      ),
      shape = RoundedCornerShape(10.dp)
    ) {
      Text(
        text = if (canAfford) "Unlock" else "Need $creditCost",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = if (canAfford) Color(0xFF003828) else Color(0xFF94A3B8)
      )
    }
  }
}

@Composable
fun RewardedAdSimulatorDialog(
  onAdFinished: (credits: Int) -> Unit,
  onDismiss: () -> Unit
) {
  val totalVideoSeconds = 15
  var secondsRemaining by remember { mutableIntStateOf(totalVideoSeconds) }
  var isCompleted by remember { mutableStateOf(false) }
  var isMuted by remember { mutableStateOf(false) }

  // Sponsors
  val sponsors = remember {
    listOf(
      Triple("Mindful Brain: Daily Habits & Focus", "Master deep focus, eliminate phone addiction, and build sustainable habits.", "Install Now • Free"),
      Triple("Peak Language Mastery", "Learn 1,000 active vocabulary words in 15 focused minutes a day.", "Get App • 4.8 ★"),
      Triple("Calm Flow Soundscapes & Sleep", "Calming binaural frequencies and guided audio breathing loops.", "Try Free Today")
    )
  }
  val currentSponsor = remember { sponsors.random() }

  LaunchedEffect(Unit) {
    while (secondsRemaining > 0) {
      delay(1000L)
      secondsRemaining--
    }
    isCompleted = true
  }

  Dialog(
    onDismissRequest = {
      if (isCompleted) onDismiss()
    },
    properties = DialogProperties(
      dismissOnBackPress = isCompleted,
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF030712))
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, FocusTeal)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Video Ad Top Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(FocusTeal.copy(alpha = 0.2f))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "SPONSORED VIDEO AD",
                  style = MaterialTheme.typography.labelSmall,
                  color = FocusTeal,
                  fontWeight = FontWeight.Bold
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                  contentDescription = "Mute / Unmute",
                  tint = Color(0xFF94A3B8),
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            if (isCompleted) {
              IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
              }
            } else {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(AmberAlert.copy(alpha = 0.2f))
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "Reward in ${secondsRemaining}s",
                  style = MaterialTheme.typography.labelSmall,
                  color = AmberAlert,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Simulated High-Production Fullscreen Video Player Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(280.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(
                Brush.verticalGradient(
                  listOf(Color(0xFF0F172A), Color(0xFF022C22), Color(0xFF0F172A))
                )
              )
              .border(1.dp, FocusTeal.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(20.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(72.dp)
                  .clip(CircleShape)
                  .background(FocusTeal.copy(alpha = 0.2f))
                  .border(2.dp, FocusTeal.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                  contentDescription = "Video Ad Streaming",
                  tint = FocusTeal,
                  modifier = Modifier.size(44.dp)
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              Text(
                text = currentSponsor.first,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = currentSponsor.second,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
              )

              Spacer(modifier = Modifier.height(12.dp))

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(FocusTealDark)
                  .padding(horizontal = 14.dp, vertical = 6.dp)
              ) {
                Text(
                  text = currentSponsor.third,
                  style = MaterialTheme.typography.labelSmall,
                  color = FocusTeal,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Video Playback Progress Bar
          val progress = (totalVideoSeconds - secondsRemaining).toFloat() / totalVideoSeconds.toFloat()
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(7.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = if (isCompleted) FocusTeal else CyanGlow,
            trackColor = Color(0xFF1E293B)
          )

          Spacer(modifier = Modifier.height(10.dp))

          if (!isCompleted) {
            Text(
              text = "পুরো ভিডিও শেষ হওয়া পর্যন্ত অপেক্ষা করুন (+২০ ক্রেডিট পাবেন)",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8),
              textAlign = TextAlign.Center
            )
          } else {
            Text(
              text = "🎉 ভিডিও সমাপ্ত! এখন আপনার ক্রেডিট বুঝে নিন।",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = FocusTeal,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
              onClick = { onAdFinished(20) },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(containerColor = FocusTeal),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Claim Reward", tint = Color(0xFF003828))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Claim +20 Focus Credits",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF003828)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MotivationalVideoDialog(
  taskTitle: String,
  onDismiss: () -> Unit
) {
  var secondsRemaining by remember { mutableIntStateOf(10) }
  var isCompleted by remember { mutableStateOf(false) }

  val motivationalQuotes = remember {
    listOf(
      "“সময় অমূল্য। এখন সোশ্যাল মিডিয়ায় হারিয়ে না গিয়ে নিজের লক্ষ্যে মনোনিবেশ করুন।”",
      "“আজকের প্রতিটি কঠিন পরিশ্রম আগামীকালের সাফল্য তৈরি করে। চালিয়ে যান!”",
      "“সাফল্যের মূল চাবিকাঠি হলো ধারাবাহিকভাবে কাজ করে যাওয়া, বিভ্রান্তি এড়িয়ে চলা।”"
    )
  }
  val quote = remember { motivationalQuotes.random() }

  LaunchedEffect(Unit) {
    while (secondsRemaining > 0) {
      delay(1000L)
      secondsRemaining--
    }
    isCompleted = true
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      dismissOnBackPress = true,
      dismissOnClickOutside = true,
      usePlatformDefaultWidth = false
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF070B14))
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberAlert)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(AmberAlert.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = "কাজের সময় মোটিভেশনাল ভিডিও",
                style = MaterialTheme.typography.labelSmall,
                color = AmberAlert,
                fontWeight = FontWeight.Bold
              )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Simulated Motivational Video Frame
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(
                Brush.linearGradient(
                  listOf(Color(0xFF2C1810), Color(0xFF1E293B), Color(0xFF0F172A))
                )
              )
              .border(1.dp, AmberAlert.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(16.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(60.dp)
                  .clip(CircleShape)
                  .background(AmberAlert.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = "Video Playing",
                  tint = AmberAlert,
                  modifier = Modifier.size(38.dp)
                )
              }
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = taskTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = quote,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          val progress = (10 - secondsRemaining) / 10f
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = AmberAlert,
            trackColor = Color(0xFF334155)
          )

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = if (isCompleted) "মোটিভেশন ভিডিও সমাপ্ত! এখন কাজে ফিরে যান 🎯" else "ভিডিও চলমান (${secondsRemaining}s বাকি)",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(14.dp))
          Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("কাজে ফিরে যান (Stay Focused)", color = Color(0xFF432C00), fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
