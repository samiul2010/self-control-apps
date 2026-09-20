package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.gate.MotivationalVideoDialog
import com.example.data.FocusLockRepository
import com.example.data.RoutineSchedule
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.FocusTeal
import com.example.ui.theme.SlateDarkCard
import com.example.ui.theme.SlateDarkSurface
import kotlinx.coroutines.launch

@Composable
fun ScheduleScreen(
  routines: List<RoutineSchedule>
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val repo = remember { FocusLockRepository.getInstance(context) }

  var showAddDialog by remember { mutableStateOf(false) }
  var showMotivationalDialog by remember { mutableStateOf(false) }
  var motivationalDialogTaskTitle by remember { mutableStateOf("") }
  var newTitle by remember { mutableStateOf("") }
  var newTaskDesc by remember { mutableStateOf("") }
  var startHour by remember { mutableIntStateOf(18) }
  var endHour by remember { mutableIntStateOf(20) }

  val uncompletedCount = routines.count { !it.isCompleted }
  val hasBacklog = uncompletedCount > 0

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item { Spacer(modifier = Modifier.height(6.dp)) }

    // Header with Anti-Backlog Rule Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDarkSurface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22475569))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Habit & Study Blocks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Scheduled gates & banked credit rewards",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
              )
            }

            Button(
              onClick = { showAddDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = FocusTeal),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF003828), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add Block", color = Color(0xFF003828), fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Anti-Backlog Banner
          if (hasBacklog) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AmberAlert.copy(alpha = 0.15f))
                .border(1.dp, AmberAlert.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(12.dp)
            ) {
              Row(verticalAlignment = Alignment.Top) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AmberAlert)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Anti-Backlog Rule Active ($uncompletedCount Pending)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AmberAlert
                  )
                  Text(
                    text = "Distracting app credit unlocks remain locked until today's scheduled tasks are completed or submitted. This prevents ignoring study blocks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1)
                  )
                }
              }
            }
          } else {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(FocusTeal.copy(alpha = 0.12f))
                .padding(12.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = FocusTeal)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "All scheduled tasks cleared! Full credit spending unlocked.",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }
    }

    item {
      Text(
        text = "Today's Schedule & Routines",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }

    if (routines.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("No Scheduled Focus Blocks", style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Add a study or deep work routine to build habits.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
          }
        }
      }
    }

    items(routines) { routine ->
      RoutineCard(
        routine = routine,
        onCompleteHonest = {
          scope.launch {
            repo.completeRoutineHonest(routine.id, routine.rewardCredits)
          }
        },
        onRequestParentVerification = {
          scope.launch {
            val isProvisional = repo.requestParentVerification(routine.id, routine.taskDescription, routine.rewardCredits + 25)
          }
        },
        onWatchMotivationalVideo = {
          motivationalDialogTaskTitle = "${routine.title}: ${routine.taskDescription}"
          showMotivationalDialog = true
        },
        onDelete = {
          scope.launch {
            repo.deleteRoutine(routine)
          }
        }
      )
    }


    item { Spacer(modifier = Modifier.height(20.dp)) }
  }

  // Motivational Video Dialog
  if (showMotivationalDialog) {
    MotivationalVideoDialog(
      taskTitle = motivationalDialogTaskTitle.ifBlank { "Study / Deep Work Session" },
      onDismiss = { showMotivationalDialog = false }
    )
  }

  // Add Routine Dialog
  if (showAddDialog) {
    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Schedule Focus Block") },
      text = {
        Column {
          OutlinedTextField(
            value = newTitle,
            onValueChange = { newTitle = it },
            label = { Text("Block Title (e.g. Evening Study)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = newTaskDesc,
            onValueChange = { newTaskDesc = it },
            label = { Text("Associated Task (e.g. Read Chapter 4)") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(text = "Hours: 18:00 - 20:00 (Evening block)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newTitle.isNotBlank()) {
              scope.launch {
                val newRoutine = RoutineSchedule(
                  title = newTitle.trim(),
                  startHour = startHour,
                  startMinute = 0,
                  endHour = endHour,
                  endMinute = 0,
                  taskDescription = newTaskDesc.ifBlank { "Focused study session" },
                  isCompleted = false,
                  rewardCredits = 40,
                  routineDate = FocusLockRepository.getTodayDateString()
                )
                repo.addRoutine(newRoutine)
                newTitle = ""
                newTaskDesc = ""
                showAddDialog = false
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = FocusTeal)
        ) {
          Text("Save Routine", color = Color(0xFF003828), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun RoutineCard(
  routine: RoutineSchedule,
  onCompleteHonest: () -> Unit,
  onRequestParentVerification: () -> Unit,
  onWatchMotivationalVideo: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = SlateDarkCard),
    shape = RoundedCornerShape(18.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (routine.isCompleted) FocusTeal.copy(alpha = 0.4f) else Color(0x22475569)
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
              .background(if (routine.isCompleted) FocusTeal.copy(alpha = 0.2f) else AmberAlert.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (routine.isCompleted) Icons.Default.CheckCircle else Icons.Default.Timer,
              contentDescription = null,
              tint = if (routine.isCompleted) FocusTeal else AmberAlert,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = routine.title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            val startTime = String.format("%02d:%02d", routine.startHour, routine.startMinute)
            val endTime = String.format("%02d:%02d", routine.endHour, routine.endMinute)
            Text(
              text = "$startTime - $endTime",
              style = MaterialTheme.typography.bodySmall,
              color = CyanGlow
            )
          }
        }

        IconButton(onClick = onDelete) {
          Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Task: ${routine.taskDescription}",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFFCBD5E1)
      )

      Spacer(modifier = Modifier.height(14.dp))

      if (routine.isCompleted) {
        val isProvisional = routine.isProvisionalCreditUsed
        val containerBg = if (isProvisional) AmberAlert.copy(alpha = 0.15f) else FocusTeal.copy(alpha = 0.15f)
        val tintColor = if (isProvisional) AmberAlert else FocusTeal

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(containerBg)
            .padding(10.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isProvisional) Icons.Default.Warning else Icons.Default.TaskAlt,
              contentDescription = null,
              tint = tintColor,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isProvisional) {
                "⚡ অগ্রিম ক্রেডিট যোগ হয়েছে (+${routine.rewardCredits + 25} CR) • প্যারেন্ট রিজেক্ট করলে দ্বিগুণ কাটা যাবে"
              } else {
                "টাস্ক সম্পন্ন • প্যারেন্ট ভেরিফিকেশন বা যোগ হয়েছে"
              },
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = tintColor
            )
          }
        }
      } else {

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onCompleteHonest,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = FocusTeal),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF003828), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Honest Done (+${routine.rewardCredits})",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF003828)
            )
          }

          OutlinedButton(
            onClick = onRequestParentVerification,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanGlow),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanGlow.copy(alpha = 0.5f))
          ) {
            Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Parent Check (+${routine.rewardCredits + 25})",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
          onClick = onWatchMotivationalVideo,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberAlert),
          border = androidx.compose.foundation.BorderStroke(1.dp, AmberAlert.copy(alpha = 0.5f))
        ) {
          Icon(imageVector = Icons.Default.SelfImprovement, contentDescription = null, tint = AmberAlert, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "মোটিভেশনাল ভিডিও দেখুন (Watch Motivational Clip)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AmberAlert
          )
        }
      }
    }
  }
}
