package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.FocusLockRepository
import com.example.data.VerificationRequest
import com.example.service.BluetoothState
import com.example.service.BluetoothSyncManager
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.FocusTeal
import com.example.ui.theme.SlateDarkBackground
import com.example.ui.theme.SlateDarkCard
import com.example.ui.theme.SlateDarkSurface
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParentModeScreen(
  requests: List<VerificationRequest>,
  onSwitchToSelfMode: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val repo = remember { FocusLockRepository.getInstance(context) }
  val btManager = remember { BluetoothSyncManager.getInstance(context) }
  val btState by btManager.state.collectAsState()

  val pendingRequests = requests.filter { it.status == "PENDING" }

  // Auto-connect trigger on screen open
  LaunchedEffect(Unit) {
    btManager.tryAutoConnect()
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SlateDarkBackground)
      .padding(horizontal = 20.dp, vertical = 16.dp)
  ) {

    // 1. Minimal Header & Bluetooth Auto-Connect Status
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "প্যারেন্ট মোড (Parent)",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White
        )
        Text(
          text = "চিলড্রেন টাস্ক ভেরিফিকেশন প্যানেল",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF94A3B8)
        )
      }

      TextButton(onClick = onSwitchToSelfMode) {
        Text("চিলড্রেন মোড", color = CyanGlow, style = MaterialTheme.typography.labelMedium)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Bluetooth Connection Box
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        if (btState is BluetoothState.Connected) FocusTeal.copy(alpha = 0.5f) else Color(0x33475569)
      )
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
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
                .background(if (btState is BluetoothState.Connected) FocusTeal.copy(alpha = 0.2f) else AmberAlert.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (btState is BluetoothState.Connected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                contentDescription = null,
                tint = if (btState is BluetoothState.Connected) FocusTeal else AmberAlert,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              when (val s = btState) {
                is BluetoothState.Connected -> {
                  Text(
                    text = "🟢 কানেক্টেড: ${s.deviceName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = FocusTeal
                  )
                  Text(
                    text = "ব্লুটুথ চালু হলেই স্বয়ংক্রিয়ভাবে কানেক্ট থাকবে",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                  )
                }
                is BluetoothState.Connecting -> {
                  Text(
                    text = "কানেক্ট হচ্ছে...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AmberAlert
                  )
                  Text(text = s.deviceName, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                }
                else -> {
                  Text(
                    text = "ব্লুটুথ অটো-কানেক্ট চালু আছে",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Text(
                    text = "উভয় ফোনে ব্লুটুথ চালু করার সাথে সাথে অটো কানেক্ট হবে",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                  )
                }
              }
            }
          }

          if (btState !is BluetoothState.Connected) {
            Button(
              onClick = {
                // Simulates or initiates the connection
                btManager.simulateVirtualLink("চিলড্রেন ফোন (কানেক্টেড)")
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyanGlow),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("কানেক্ট লিংক", color = Color(0xFF003828), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 2. The Main Box of Pending Tasks (পেন্ডিং মার্ক / টাস্ক বক্স)
    Text(
      text = "পেন্ডিং টাস্ক (${pendingRequests.size})",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = Color.White
    )

    Spacer(modifier = Modifier.height(8.dp))

    if (pendingRequests.isEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2264748B))
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .background(FocusTeal.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = FocusTeal,
              modifier = Modifier.size(32.dp)
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "বর্তমানে কোনো পেন্ডিং টাস্ক নেই",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "চিলড্রেন তার ফোন থেকে কোনো টাস্ক শেষ করে জমা দিলে এই বক্সে তাৎক্ষণিক প্রদর্শিত হবে।",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(pendingRequests) { request ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(
              1.5.dp,
              if (request.isProvisionalUsed) AmberAlert else Color(0x33475569)
            )
          ) {
            Column(modifier = Modifier.padding(18.dp)) {

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = request.childDeviceName,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = CyanGlow
                )

                val timeStr = SimpleDateFormat("h:mm a", Locale.US).format(Date(request.submittedTimestamp))
                Text(text = timeStr, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = request.taskTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )

              Spacer(modifier = Modifier.height(8.dp))

              // If Provisional credit was granted in advance
              if (request.isProvisionalUsed) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AmberAlert.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AmberAlert, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "অগ্রিম ক্রেডিট ব্যবহৃত! রিজেক্ট করলে দ্বিগুণ (${request.rewardCredits * 2}) পয়েন্ট কাটা যাবে।",
                      style = MaterialTheme.typography.bodySmall,
                      color = AmberAlert,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
                Spacer(modifier = Modifier.height(14.dp))
              }

              // ONLY TWO ACTION BUTTONS (একসেপ্ট এবং রিজেক্ট)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                // একসেপ্ট বাটন (Accept)
                Button(
                  onClick = {
                    scope.launch {
                      repo.resolveVerification(request.id, accepted = true)
                    }
                  },
                  modifier = Modifier.weight(1f),
                  colors = ButtonDefaults.buttonColors(containerColor = FocusTeal),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF003828), modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "একসেপ্ট (+${request.rewardCredits})",
                    color = Color(0xFF003828),
                    fontWeight = FontWeight.ExtraBold
                  )
                }

                // রিজেক্ট বাটন (Reject)
                val penaltyText = if (request.isProvisionalUsed) "-${request.rewardCredits * 2}" else "-${request.penaltyOnReject}"
                OutlinedButton(
                  onClick = {
                    scope.launch {
                      repo.resolveVerification(request.id, accepted = false)
                    }
                  },
                  modifier = Modifier.weight(1f),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralWarning),
                  border = androidx.compose.foundation.BorderStroke(1.5.dp, CoralWarning),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = CoralWarning, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "রিজেক্ট ($penaltyText)",
                    color = CoralWarning,
                    fontWeight = FontWeight.ExtraBold
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
