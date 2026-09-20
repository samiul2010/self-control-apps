package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    RestrictedApp::class,
    CreditAccount::class,
    UnlockedSession::class,
    RoutineSchedule::class,
    VerificationRequest::class
  ],
  version = 1,
  exportSchema = false
)
abstract class FocusLockDatabase : RoomDatabase() {
  abstract fun restrictedAppDao(): RestrictedAppDao
  abstract fun creditAccountDao(): CreditAccountDao
  abstract fun unlockedSessionDao(): UnlockedSessionDao
  abstract fun routineScheduleDao(): RoutineScheduleDao
  abstract fun verificationRequestDao(): VerificationRequestDao

  companion object {
    @Volatile
    private var INSTANCE: FocusLockDatabase? = null

    fun getDatabase(context: Context): FocusLockDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          FocusLockDatabase::class.java,
          "focus_lock_db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
