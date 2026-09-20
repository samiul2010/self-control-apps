package com.example.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.FocusLockApp
import com.example.data.VerificationRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.util.UUID

sealed class BluetoothState {
  object Disconnected : BluetoothState()
  object Scanning : BluetoothState()
  object Listening : BluetoothState()
  data class Connecting(val deviceName: String) : BluetoothState()
  data class Connected(val deviceName: String, val address: String) : BluetoothState()
  data class Error(val message: String) : BluetoothState()
}

class BluetoothSyncManager private constructor(private val context: Context) {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val prefs: SharedPreferences = context.getSharedPreferences("focuslock_bt_prefs", Context.MODE_PRIVATE)

  private val _state = MutableStateFlow<BluetoothState>(BluetoothState.Disconnected)
  val state: StateFlow<BluetoothState> = _state.asStateFlow()

  private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
  private var activeSocket: BluetoothSocket? = null
  private var serverSocket: BluetoothServerSocket? = null

  companion object {
    val FOCUSLOCK_UUID: UUID = UUID.fromString("e0cbf06c-cd8b-4647-bb8a-263b43f0f974")
    const val SERVICE_NAME = "FocusLockParentSync"
    const val PREF_LAST_DEVICE_ADDRESS = "last_device_address"
    const val PREF_LAST_DEVICE_NAME = "last_device_name"

    @Volatile
    private var INSTANCE: BluetoothSyncManager? = null

    fun getInstance(context: Context): BluetoothSyncManager {
      return INSTANCE ?: synchronized(this) {
        val instance = BluetoothSyncManager(context.applicationContext)
        INSTANCE = instance
        instance
      }
    }
  }

  fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

  fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

  fun getSavedDevice(): Pair<String?, String?> {
    val address = prefs.getString(PREF_LAST_DEVICE_ADDRESS, null)
    val name = prefs.getString(PREF_LAST_DEVICE_NAME, null)
    return Pair(name, address)
  }

  fun saveDevice(name: String, address: String) {
    prefs.edit()
      .putString(PREF_LAST_DEVICE_NAME, name)
      .putString(PREF_LAST_DEVICE_ADDRESS, address)
      .apply()
  }

  fun clearSavedDevice() {
    prefs.edit()
      .remove(PREF_LAST_DEVICE_NAME)
      .remove(PREF_LAST_DEVICE_ADDRESS)
      .apply()
    disconnect()
  }

  @SuppressLint("MissingPermission")
  fun getBondedDevices(): List<Pair<String, String>> {
    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return emptyList()
    return try {
      bluetoothAdapter.bondedDevices?.map { Pair(it.name ?: "Unknown Device", it.address) } ?: emptyList()
    } catch (e: Exception) {
      emptyList()
    }
  }

  /**
   * Automatically attempts to connect to saved paired device whenever Bluetooth is on
   */
  fun tryAutoConnect() {
    val (savedName, savedAddress) = getSavedDevice()
    if (savedAddress != null) {
      connectToDevice(savedName ?: "Paired Device", savedAddress)
    } else {
      // If no saved device yet, start listening as server for parent mode
      startServerListener()
    }
  }

  /**
   * Start listening for child connections (Used by Parent mode)
   */
  @SuppressLint("MissingPermission")
  fun startServerListener() {
    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
      _state.value = BluetoothState.Disconnected
      return
    }

    scope.launch {
      try {
        _state.value = BluetoothState.Listening
        serverSocket?.close()
        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, FOCUSLOCK_UUID)
        val socket = serverSocket?.accept(15000) // 15s timeout
        if (socket != null) {
          handleConnectedSocket(socket, socket.remoteDevice?.name ?: "Child Phone", socket.remoteDevice?.address ?: "")
        }
      } catch (e: Exception) {
        // Listening timeout or closed
        if (_state.value is BluetoothState.Listening) {
          _state.value = BluetoothState.Disconnected
        }
      }
    }
  }

  /**
   * Connect to child/parent device via MAC address (Used for auto-connect or manual selection)
   */
  @SuppressLint("MissingPermission")
  fun connectToDevice(name: String, address: String) {
    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
      _state.value = BluetoothState.Error("Bluetooth is turned off")
      return
    }

    scope.launch {
      try {
        _state.value = BluetoothState.Connecting(name)
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
        val socket = device.createRfcommSocketToServiceRecord(FOCUSLOCK_UUID)
        bluetoothAdapter.cancelDiscovery()
        socket.connect()
        saveDevice(name, address)
        handleConnectedSocket(socket, name, address)
      } catch (e: Exception) {
        _state.value = BluetoothState.Error("Could not connect to $name: ${e.message}")
      }
    }
  }

  private fun handleConnectedSocket(socket: BluetoothSocket, name: String, address: String) {
    activeSocket = socket
    saveDevice(name, address)
    _state.value = BluetoothState.Connected(name, address)

    scope.launch {
      try {
        val reader = BufferedReader(InputStreamReader(socket.inputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
          line?.let { processIncomingMessage(it) }
        }
      } catch (e: Exception) {
        _state.value = BluetoothState.Disconnected
      }
    }
  }

  private suspend fun processIncomingMessage(message: String) {
    val repo = FocusLockApp.get().repository
    // Simple protocol format: VERDICT:reqId:ACCEPTED/REJECTED or TASK:title:credits:provisional
    if (message.startsWith("VERDICT:")) {
      val parts = message.split(":")
      if (parts.size >= 3) {
        val reqId = parts[1].toLongOrNull() ?: return
        val accepted = parts[2] == "ACCEPTED"
        repo.resolveVerification(reqId, accepted)
      }
    } else if (message.startsWith("TASK:")) {
      val parts = message.split(":")
      if (parts.size >= 4) {
        val title = parts[1]
        val reward = parts[2].toIntOrNull() ?: 50
        val isProvisional = parts[3] == "TRUE"
        val req = VerificationRequest(
          childDeviceName = "Connected Child Phone",
          taskTitle = title,
          rewardCredits = reward,
          status = "PENDING",
          isProvisionalUsed = isProvisional,
          penaltyOnReject = if (isProvisional) reward * 2 else reward + 15
        )
        val db = com.example.data.FocusLockDatabase.getDatabase(context)
        db.verificationRequestDao().insert(req)
      }
    }
  }

  /**
   * Virtual Bluetooth Link Simulator:
   * Enables immediate testing in browser preview / emulator where second physical hardware is not present.
   */
  fun simulateVirtualLink(deviceName: String = "Child's Galaxy Phone (Virtual)") {
    saveDevice(deviceName, "00:11:22:33:AA:BB")
    _state.value = BluetoothState.Connected(deviceName, "00:11:22:33:AA:BB")
  }

  fun disconnect() {
    try {
      activeSocket?.close()
      serverSocket?.close()
    } catch (_: Exception) {}
    activeSocket = null
    serverSocket = null
    _state.value = BluetoothState.Disconnected
  }
}
