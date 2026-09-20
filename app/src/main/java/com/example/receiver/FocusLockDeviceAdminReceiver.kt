package com.example.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class FocusLockDeviceAdminReceiver : DeviceAdminReceiver() {

  override fun onEnabled(context: Context, intent: Intent) {
    super.onEnabled(context, intent)
    Toast.makeText(context, "FocusLock Commitment Protection Activated", Toast.LENGTH_SHORT).show()
  }

  override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
    return "FocusLock commitment period is active! Deactivating removes your habit protection barriers."
  }

  override fun onDisabled(context: Context, intent: Intent) {
    super.onDisabled(context, intent)
    Toast.makeText(context, "FocusLock Commitment Protection Deactivated", Toast.LENGTH_SHORT).show()
  }
}
