package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RestrictedAppDao {
  @Query("SELECT * FROM restricted_apps ORDER BY appName ASC")
  fun getAll(): Flow<List<RestrictedApp>>

  @Query("SELECT packageName FROM restricted_apps WHERE isRestricted = 1")
  suspend fun getActiveRestrictedPackageNames(): List<String>

  @Query("SELECT * FROM restricted_apps WHERE packageName = :packageName LIMIT 1")
  suspend fun getByPackage(packageName: String): RestrictedApp?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(apps: List<RestrictedApp>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(app: RestrictedApp)

  @Update
  suspend fun update(app: RestrictedApp)

  @Delete
  suspend fun delete(app: RestrictedApp)
}

@Dao
interface CreditAccountDao {
  @Query("SELECT * FROM credit_account WHERE id = 1 LIMIT 1")
  fun getAccountFlow(): Flow<CreditAccount?>

  @Query("SELECT * FROM credit_account WHERE id = 1 LIMIT 1")
  suspend fun getAccount(): CreditAccount?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(account: CreditAccount)
}

@Dao
interface UnlockedSessionDao {
  @Query("SELECT * FROM unlocked_sessions ORDER BY unlockedUntilTimestamp DESC")
  fun getAllSessionsFlow(): Flow<List<UnlockedSession>>

  @Query("SELECT * FROM unlocked_sessions WHERE packageName = :packageName AND unlockedUntilTimestamp > :currentTime LIMIT 1")
  suspend fun getActiveSession(packageName: String, currentTime: Long): UnlockedSession?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: UnlockedSession)

  @Query("DELETE FROM unlocked_sessions WHERE packageName = :packageName")
  suspend fun deleteSession(packageName: String)

  @Query("DELETE FROM unlocked_sessions WHERE unlockedUntilTimestamp <= :currentTime")
  suspend fun purgeExpired(currentTime: Long)
}

@Dao
interface RoutineScheduleDao {
  @Query("SELECT * FROM routines ORDER BY startHour ASC, startMinute ASC")
  fun getAllRoutinesFlow(): Flow<List<RoutineSchedule>>

  @Query("SELECT * FROM routines WHERE routineDate = :date ORDER BY startHour ASC, startMinute ASC")
  fun getRoutinesForDate(date: String): Flow<List<RoutineSchedule>>

  @Query("SELECT * FROM routines WHERE routineDate = :date AND isCompleted = 0")
  suspend fun getUncompletedRoutines(date: String): List<RoutineSchedule>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(routine: RoutineSchedule): Long

  @Update
  suspend fun update(routine: RoutineSchedule)

  @Delete
  suspend fun delete(routine: RoutineSchedule)
}

@Dao
interface VerificationRequestDao {
  @Query("SELECT * FROM verification_requests ORDER BY submittedTimestamp DESC")
  fun getAllRequestsFlow(): Flow<List<VerificationRequest>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(request: VerificationRequest): Long

  @Update
  suspend fun update(request: VerificationRequest)
}
