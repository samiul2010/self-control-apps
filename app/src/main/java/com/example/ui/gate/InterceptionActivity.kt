package com.example.ui.gate

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.FocusLockApp
import com.example.ui.theme.FocusLockTheme
import kotlinx.coroutines.launch

class InterceptionActivity : ComponentActivity() {

  companion object {
    const val EXTRA_PACKAGE_NAME = "extra_package_name"
    const val EXTRA_APP_NAME = "extra_app_name"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: "com.zhiliaoapp.musically"
    val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Distracting App"
    val repo = FocusLockApp.get().repository

    var hasBacklog by mutableStateOf(false)
    lifecycleScope.launch {
      hasBacklog = repo.hasTodayBacklog()
    }

    setContent {
      FocusLockTheme(darkTheme = true) {
        val account by repo.creditAccount.collectAsState(initial = null)

        InterceptionGateContent(
          packageName = packageName,
          appName = appName,
          account = account,
          hasBacklog = hasBacklog,
          onUnlockSuccess = { durationMinutes ->
            Toast.makeText(
              this,
              "$appName unlocked for $durationMinutes minutes! Timer started.",
              Toast.LENGTH_LONG
            ).show()

            // Try launching the target app if installed, otherwise finish back
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
              startActivity(launchIntent)
            }
            finish()
          },
          onDismissToHome = {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
              addCategory(Intent.CATEGORY_HOME)
              flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
            finish()
          }
        )
      }
    }
  }

  override fun onBackPressed() {
    // Prevent bypass via back button: redirect to home launcher
    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
      addCategory(Intent.CATEGORY_HOME)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(homeIntent)
    finish()
  }
}
