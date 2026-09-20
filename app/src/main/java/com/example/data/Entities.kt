package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restricted_apps")
data class RestrictedApp(
  @PrimaryKey val packageName: String,
  val appName: String,
  val category: String = "Social",
  val isRestricted: Boolean = true,
  val iconResName: String = ""
)

@Entity(tableName = "credit_account")
data class CreditAccount(
  @PrimaryKey val id: Int = 1,
  val payAsYouGoCredits: Int = 0,
  val bankedCredits: Int = 0,
  val emergencyUsesLeftToday: Int = 3,
  val maxEmergencyUsesPerDay: Int = 3,
  val maxEmergencyMinutesPerUse: Int = 5,
  val lastEmergencyResetDate: String = "",
  val commitmentDays: Int = 14,
  val commitmentStartTimestamp: Long = 0L,
  val cooldownRequestedTimestamp: Long = 0L,
  val userRole: String = "SELF", // "SELF" or "PARENT"
  val isOnboardingCompleted: Boolean = false,
  val isParentConnected: Boolean = false,
  val pairedParentMac: String = "",
  val pairedParentName: String = "",
  val totalTasksSubmitted: Int = 0,
  val provisionalTasksGranted: Int = 0,
  val activeStudyBlockName: String = "",
  val isStudyBlockActive: Boolean = false,
  val customMotivationQuote: String = "Focus on what matters most. Future you will thank you.",
  val customMotivationAuthor: String = "FocusLock"
)

@Entity(tableName = "unlocked_sessions")
data class UnlockedSession(
  @PrimaryKey val packageName: String,
  val appName: String,
  val unlockedUntilTimestamp: Long,
  val durationMinutes: Int,
  val unlockType: String // "AD_CREDIT", "BANKED_CREDIT", "EMERGENCY"
)

@Entity(tableName = "routines")
data class RoutineSchedule(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val startHour: Int,
  val startMinute: Int,
  val endHour: Int,
  val endMinute: Int,
  val taskDescription: String,
  val isCompleted: Boolean = false,
  val isVerifiedByParent: Boolean = false,
  val isProvisionalCreditUsed: Boolean = false,
  val rewardCredits: Int = 30,
  val routineDate: String // YYYY-MM-DD
)

@Entity(tableName = "verification_requests")
data class VerificationRequest(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val childDeviceName: String = "Child Device",
  val taskTitle: String,
  val taskDescription: String = "",
  val submittedTimestamp: Long = System.currentTimeMillis(),
  val rewardCredits: Int = 50,
  val status: String = "PENDING", // "PENDING", "ACCEPTED", "REJECTED"
  val isProvisionalUsed: Boolean = false,
  val penaltyOnReject: Int = 60
)

