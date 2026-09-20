package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.data.FocusLockRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ParentModeScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.FocusLockTheme
import com.example.ui.theme.FocusTeal
import com.example.ui.theme.SlateDarkBackground
import com.example.ui.theme.SlateDarkSurface

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val repo = FocusLockApp.get().repository

    setContent {
      FocusLockTheme(darkTheme = true) {
        val scope = rememberCoroutineScope()
        val account by repo.creditAccount.collectAsState(initial = null)
        val restrictedApps by repo.restrictedApps.collectAsState(initial = emptyList())
        val activeSessions by repo.activeSessions.collectAsState(initial = emptyList())
        val routines by repo.routines.collectAsState(initial = emptyList())
        val verificationRequests by repo.verificationRequests.collectAsState(initial = emptyList())

        val isOnboardingDone = account?.isOnboardingCompleted == true

        if (!isOnboardingDone) {
          OnboardingScreen(
            onFinishOnboarding = {
              // Automatically triggers recomposition to the chosen role
            }
          )
        } else if (account?.userRole == "PARENT") {
          // Ultra-minimal Parent Screen: Only Bluetooth connection & Pending Tasks Box with Accept/Reject
          ParentModeScreen(
            requests = verificationRequests,
            onSwitchToSelfMode = {
              scope.launch {
                repo.setUserRole("SELF")
              }
            }

          )
        } else {
          // Child Mode: Full habit system WITHOUT Parent transfer options
          FocusLockMainApp(
            account = account,
            restrictedApps = restrictedApps,
            activeSessions = activeSessions,
            routines = routines
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusLockMainApp(
  account: com.example.data.CreditAccount?,
  restrictedApps: List<com.example.data.RestrictedApp>,
  activeSessions: List<com.example.data.UnlockedSession>,
  routines: List<com.example.data.RoutineSchedule>
) {
  var selectedTab by remember { mutableStateOf("dashboard") }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = SlateDarkBackground,
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(FocusTeal.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = FocusTeal,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "FocusLock",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Habit Architecture",
                style = MaterialTheme.typography.bodySmall,
                color = FocusTeal
              )
            }
          }
        },
        actions = {
          // Total Credits Chip
          val totalCredits = (account?.payAsYouGoCredits ?: 0) + (account?.bankedCredits ?: 0)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF1E293B))
              .padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Text(
              text = "$totalCredits Credits",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = FocusTeal
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = SlateDarkBackground
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = SlateDarkSurface,
        tonalElevation = 8.dp
      ) {
        NavigationBarItem(
          selected = selectedTab == "dashboard",
          onClick = { selectedTab = "dashboard" },
          icon = { Icon(Icons.Default.Shield, contentDescription = "Dashboard") },
          label = { Text("Guard") },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF003828),
            selectedTextColor = FocusTeal,
            indicatorColor = FocusTeal,
            unselectedIconColor = Color(0xFF94A3B8),
            unselectedTextColor = Color(0xFF94A3B8)
          )
        )

        NavigationBarItem(
          selected = selectedTab == "routines",
          onClick = { selectedTab = "routines" },
          icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Routines") },
          label = { Text("Routines") },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF003828),
            selectedTextColor = FocusTeal,
            indicatorColor = FocusTeal,
            unselectedIconColor = Color(0xFF94A3B8),
            unselectedTextColor = Color(0xFF94A3B8)
          )
        )

        NavigationBarItem(
          selected = selectedTab == "settings",
          onClick = { selectedTab = "settings" },
          icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
          label = { Text("Settings") },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF003828),
            selectedTextColor = FocusTeal,
            indicatorColor = FocusTeal,
            unselectedIconColor = Color(0xFF94A3B8),
            unselectedTextColor = Color(0xFF94A3B8)
          )
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        "dashboard" -> DashboardScreen(
          account = account,
          restrictedApps = restrictedApps,
          activeSessions = activeSessions
        )
        "routines" -> ScheduleScreen(
          routines = routines
        )
        "settings" -> SettingsScreen(
          account = account,
          onRoleChanged = { /* Handled via repository */ }
        )
      }
    }
  }
}


