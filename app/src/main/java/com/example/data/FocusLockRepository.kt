package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FocusLockRepository(private val database: FocusLockDatabase) {

  val restrictedApps: Flow<List<RestrictedApp>> = database.restrictedAppDao().getAll()
  val creditAccount: Flow<CreditAccount?> = database.creditAccountDao().getAccountFlow()
  val activeSessions: Flow<List<UnlockedSession>> = database.unlockedSessionDao().getAllSessionsFlow()
  val routines: Flow<List<RoutineSchedule>> = database.routineScheduleDao().getAllRoutinesFlow()
  val verificationRequests: Flow<List<VerificationRequest>> = database.verificationRequestDao().getAllRequestsFlow()

  companion object {
    @Volatile
    private var INSTANCE: FocusLockRepository? = null

    fun getInstance(context: Context): FocusLockRepository {
      return INSTANCE ?: synchronized(this) {
        val db = FocusLockDatabase.getDatabase(context)
        val instance = FocusLockRepository(db)
        INSTANCE = instance
        instance
      }
    }

    val PRESET_APPS = listOf(
      RestrictedApp("com.zhiliaoapp.musically", "TikTok", "Short Video", isRestricted = true),
      RestrictedApp("com.instagram.android", "Instagram", "Social Feed", isRestricted = true),
      RestrictedApp("com.facebook.katana", "Facebook", "Social Media", isRestricted = true),
      RestrictedApp("com.google.android.youtube", "YouTube", "Video Streaming", isRestricted = true),
      RestrictedApp("com.twitter.android", "X (Twitter)", "Social Media", isRestricted = true),
      RestrictedApp("com.reddit.frontpage", "Reddit", "Forum & Discussion", isRestricted = true),
      RestrictedApp("com.snapchat.android", "Snapchat", "Messaging & Stories", isRestricted = true)
    )

    fun getTodayDateString(): String {
      val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
      return sdf.format(Date())
    }
  }

  suspend fun initializeDefaultsIfEmpty() {
    val currentAccount = database.creditAccountDao().getAccount()
    val today = getTodayDateString()

    if (currentAccount == null) {
      val initialAccount = CreditAccount(
        id = 1,
        payAsYouGoCredits = 15, // Welcome credits
        bankedCredits = 50,
        emergencyUsesLeftToday = 3,
        maxEmergencyUsesPerDay = 3,
        maxEmergencyMinutesPerUse = 5,
        lastEmergencyResetDate = today,
        commitmentDays = 14,
        commitmentStartTimestamp = System.currentTimeMillis(),
        userRole = "SELF",
        isOnboardingCompleted = false
      )
      database.creditAccountDao().insertOrUpdate(initialAccount)
      database.restrictedAppDao().insertAll(PRESET_APPS)

      // Add starter routine
      val starterRoutine = RoutineSchedule(
        title = "Deep Focus / Study Block",
        startHour = 18,
        startMinute = 0,
        endHour = 20,
        endMinute = 0,
        taskDescription = "Study or read for 45 minutes without phone distraction",
        isCompleted = false,
        rewardCredits = 35,
        routineDate = today
      )
      database.routineScheduleDao().insert(starterRoutine)
    } else {
      // Check midnight emergency reset
      if (currentAccount.lastEmergencyResetDate != today) {
        database.creditAccountDao().insertOrUpdate(
          currentAccount.copy(
            emergencyUsesLeftToday = currentAccount.maxEmergencyUsesPerDay,
            lastEmergencyResetDate = today
          )
        )
      }
    }
  }

  suspend fun isAppRestricted(packageName: String): Boolean {
    val app = database.restrictedAppDao().getByPackage(packageName)
    return app?.isRestricted == true
  }

  suspend fun isAppCurrentlyUnlocked(packageName: String): Boolean {
    val now = System.currentTimeMillis()
    val activeSession = database.unlockedSessionDao().getActiveSession(packageName, now)
    return activeSession != null
  }

  suspend fun shouldIntercept(packageName: String): Boolean {
    if (!isAppRestricted(packageName)) return false
    return !isAppCurrentlyUnlocked(packageName)
  }

  suspend fun earnAdCredits(amount: Int = 15): Int {
    val account = database.creditAccountDao().getAccount() ?: return 0
    val newPay = account.payAsYouGoCredits + amount
    database.creditAccountDao().insertOrUpdate(account.copy(payAsYouGoCredits = newPay))
    return newPay
  }

  suspend fun spendCreditsToUnlock(
    packageName: String,
    appName: String,
    durationMinutes: Int,
    creditCost: Int
  ): Boolean {
    val account = database.creditAccountDao().getAccount() ?: return false
    val totalCredits = account.payAsYouGoCredits + account.bankedCredits
    if (totalCredits < creditCost) return false

    var remCost = creditCost
    var newPay = account.payAsYouGoCredits
    var newBanked = account.bankedCredits

    if (newPay >= remCost) {
      newPay -= remCost
    } else {
      remCost -= newPay
      newPay = 0
      newBanked -= remCost
    }

    database.creditAccountDao().insertOrUpdate(
      account.copy(
        payAsYouGoCredits = newPay,
        bankedCredits = newBanked
      )
    )

    val unlockTimestamp = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
    val session = UnlockedSession(
      packageName = packageName,
      appName = appName,
      unlockedUntilTimestamp = unlockTimestamp,
      durationMinutes = durationMinutes,
      unlockType = "CREDIT"
    )
    database.unlockedSessionDao().insertSession(session)
    return true
  }

  suspend fun useEmergencyPass(packageName: String, appName: String): Boolean {
    val account = database.creditAccountDao().getAccount() ?: return false
    if (account.emergencyUsesLeftToday <= 0) return false

    val updatedAccount = account.copy(
      emergencyUsesLeftToday = account.emergencyUsesLeftToday - 1
    )
    database.creditAccountDao().insertOrUpdate(updatedAccount)

    val minutes = account.maxEmergencyMinutesPerUse
    val unlockTimestamp = System.currentTimeMillis() + (minutes * 60 * 1000L)
    val session = UnlockedSession(
      packageName = packageName,
      appName = appName,
      unlockedUntilTimestamp = unlockTimestamp,
      durationMinutes = minutes,
      unlockType = "EMERGENCY"
    )
    database.unlockedSessionDao().insertSession(session)
    return true
  }

  suspend fun relockSession(packageName: String) {
    database.unlockedSessionDao().deleteSession(packageName)
  }

  suspend fun purgeExpiredSessions() {
    database.unlockedSessionDao().purgeExpired(System.currentTimeMillis())
  }

  suspend fun toggleAppRestricted(packageName: String, restricted: Boolean) {
    val app = database.restrictedAppDao().getByPackage(packageName)
    if (app != null) {
      database.restrictedAppDao().update(app.copy(isRestricted = restricted))
    }
  }

  suspend fun toggleRestrictedApp(packageName: String, restricted: Boolean) {
    toggleAppRestricted(packageName, restricted)
  }


  suspend fun addCustomRestrictedApp(packageName: String, appName: String, category: String = "Distracting App") {
    database.restrictedAppDao().insert(
      RestrictedApp(
        packageName = packageName,
        appName = appName,
        category = category,
        isRestricted = true
      )
    )
  }

  suspend fun completeRoutineHonest(routineId: Long, reward: Int = 30) {
    val routines = database.routineScheduleDao().getAllRoutinesFlow().firstOrNull() ?: emptyList()
    val r = routines.find { it.id == routineId } ?: return
    database.routineScheduleDao().update(r.copy(isCompleted = true))

    val account = database.creditAccountDao().getAccount() ?: return
    database.creditAccountDao().insertOrUpdate(
      account.copy(bankedCredits = account.bankedCredits + reward)
    )
  }

  suspend fun requestParentVerification(routineId: Long, taskTitle: String, reward: Int = 50): Boolean {
    val account = database.creditAccountDao().getAccount() ?: return false
    val newTotalSubmitted = account.totalTasksSubmitted + 1

    // Check 1-in-3 rule: Every 3rd task gives provisional advance credit immediately!
    val isProvisional = (newTotalSubmitted % 3 == 0)
    val doublePenalty = reward * 2

    val req = VerificationRequest(
      childDeviceName = "Child Device",
      taskTitle = taskTitle,
      rewardCredits = reward,
      status = "PENDING",
      isProvisionalUsed = isProvisional,
      penaltyOnReject = if (isProvisional) doublePenalty else 35
    )
    database.verificationRequestDao().insert(req)

    // Mark routine completed
    val routines = database.routineScheduleDao().getAllRoutinesFlow().firstOrNull() ?: emptyList()
    val r = routines.find { it.id == routineId }
    if (r != null) {
      database.routineScheduleDao().update(
        r.copy(isCompleted = true, isVerifiedByParent = false, isProvisionalCreditUsed = isProvisional)
      )
    }

    val updatedBanked = if (isProvisional) account.bankedCredits + reward else account.bankedCredits
    val updatedProvisionalCount = if (isProvisional) account.provisionalTasksGranted + 1 else account.provisionalTasksGranted

    database.creditAccountDao().insertOrUpdate(
      account.copy(
        totalTasksSubmitted = newTotalSubmitted,
        provisionalTasksGranted = updatedProvisionalCount,
        bankedCredits = updatedBanked
      )
    )
    return isProvisional
  }

  suspend fun resolveVerification(requestId: Long, accepted: Boolean) {
    val reqs = database.verificationRequestDao().getAllRequestsFlow().firstOrNull() ?: emptyList()
    val req = reqs.find { it.id == requestId } ?: return
    val newStatus = if (accepted) "ACCEPTED" else "REJECTED"
    database.verificationRequestDao().update(req.copy(status = newStatus))

    val account = database.creditAccountDao().getAccount() ?: return

    val newBanked: Int = if (accepted) {
      if (req.isProvisionalUsed) {
        // Child already received credit upfront; confirm approval
        account.bankedCredits
      } else {
        // Normal approval: Add earned credit
        account.bankedCredits + req.rewardCredits
      }
    } else {
      // REJECTED!
      if (req.isProvisionalUsed) {
        // Double deduction penalty since provisional credit was spent in advance!
        (account.bankedCredits - (req.rewardCredits * 2)).coerceAtLeast(0)
      } else {
        // Heavy deduction penalty for uncompleted task
        (account.bankedCredits - req.penaltyOnReject).coerceAtLeast(0)
      }
    }

    database.creditAccountDao().insertOrUpdate(account.copy(bankedCredits = newBanked))
  }


  suspend fun addRoutine(routine: RoutineSchedule) {
    database.routineScheduleDao().insert(routine)
  }

  suspend fun deleteRoutine(routine: RoutineSchedule) {
    database.routineScheduleDao().delete(routine)
  }

  suspend fun updateSettings(
    commitmentDays: Int,
    maxEmergencyMinutes: Int,
    maxEmergencyUses: Int,
    userRole: String,
    customQuote: String? = null
  ) {
    val account = database.creditAccountDao().getAccount() ?: return
    database.creditAccountDao().insertOrUpdate(
      account.copy(
        commitmentDays = commitmentDays,
        maxEmergencyMinutesPerUse = maxEmergencyMinutes,
        maxEmergencyUsesPerDay = maxEmergencyUses,
        userRole = userRole,
        customMotivationQuote = customQuote ?: account.customMotivationQuote
      )
    )
  }

  suspend fun setUserRole(userRole: String) {
    val account = database.creditAccountDao().getAccount() ?: return
    database.creditAccountDao().insertOrUpdate(account.copy(userRole = userRole))
  }

  suspend fun setOnboardingCompleted(completed: Boolean) {
    val account = database.creditAccountDao().getAccount() ?: return
    database.creditAccountDao().insertOrUpdate(account.copy(isOnboardingCompleted = completed))
  }

  suspend fun requestCooldown() {
    val account = database.creditAccountDao().getAccount() ?: return
    database.creditAccountDao().insertOrUpdate(
      account.copy(cooldownRequestedTimestamp = System.currentTimeMillis())
    )
  }

  suspend fun cancelCooldown() {
    val account = database.creditAccountDao().getAccount() ?: return
    database.creditAccountDao().insertOrUpdate(
      account.copy(cooldownRequestedTimestamp = 0L)
    )
  }

  suspend fun hasTodayBacklog(): Boolean {
    val today = getTodayDateString()
    val uncompleted = database.routineScheduleDao().getUncompletedRoutines(today)
    return uncompleted.isNotEmpty()
  }
}
