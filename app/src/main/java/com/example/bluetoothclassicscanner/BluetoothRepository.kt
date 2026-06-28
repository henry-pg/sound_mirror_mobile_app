package com.example.bluetoothclassicscanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothRepository(
    private val appContext: Context,
) {

    private val bluetoothManager: BluetoothManager? =
        appContext.getSystemService(BluetoothManager::class.java)

    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceUiModel>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceUiModel>> = _pairedDevices.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDeviceUiModel>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDeviceUiModel>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    @Volatile
    private var isReceiverRegistered = false

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isScanning.value = true
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.bluetoothDeviceExtra() ?: return
                    addDiscoveredDevice(device)
                }
            }
        }
    }

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        if (!BluetoothPermissionHandler.canReadPairedDevices(appContext)) {
            _pairedDevices.value = emptyList()
            return
        }

        val adapter = bluetoothAdapter ?: run {
            _pairedDevices.value = emptyList()
            return
        }

        try {
            _pairedDevices.value = adapter.bondedDevices
                .orEmpty()
                .map(::deviceToUiModel)
                .sortedBy { it.name.lowercase() }
        } catch (_: SecurityException) {
            _pairedDevices.value = emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery(): String? {
        val adapter = bluetoothAdapter ?: return "Bluetooth is not supported on this device"

        BluetoothPermissionHandler.discoveryUnavailableReason(appContext)?.let { reason ->
            return reason
        }

        if (!adapter.isEnabled) {
            return "Bluetooth is disabled"
        }

        return try {
            registerReceiverIfNeeded()
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            _discoveredDevices.value = emptyList()
            if (adapter.startDiscovery()) {
                null
            } else {
                _isScanning.value = false
                unregisterReceiverIfNeeded()
                "Failed to start Bluetooth discovery"
            }
        } catch (_: SecurityException) {
            _isScanning.value = false
            unregisterReceiverIfNeeded()
            "Bluetooth permissions were denied"
        }
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        val adapter = bluetoothAdapter ?: return
        try {
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
        } catch (_: SecurityException) {
            // Ignore missing permission failures and let the UI reflect state on next refresh.
        } finally {
            _isScanning.value = false
            unregisterReceiverIfNeeded()
        }
    }

    fun release() {
        stopDiscovery()
        unregisterReceiverIfNeeded()
    }

    private fun addDiscoveredDevice(device: BluetoothDevice) {
        val deviceModel = try {
            deviceToUiModel(device)
        } catch (_: SecurityException) {
            return
        }

        val updated = _discoveredDevices.value
            .associateBy { it.address }
            .toMutableMap()
            .apply {
                put(deviceModel.address, deviceModel)
            }
            .values
            .sortedBy { it.name.lowercase() }

        _discoveredDevices.value = updated
    }

    @SuppressLint("MissingPermission")
    private fun deviceToUiModel(device: BluetoothDevice): BluetoothDeviceUiModel {
        val displayName = device.name?.takeIf { it.isNotBlank() } ?: "Unknown device"
        return BluetoothDeviceUiModel(
            name = displayName,
            address = device.address ?: "Unavailable",
            bondState = bondStateLabel(device.bondState),
            deviceType = deviceTypeLabel(device.type),
        )
    }

    private fun bondStateLabel(bondState: Int): String {
        return when (bondState) {
            BluetoothDevice.BOND_BONDED -> "Bonded"
            BluetoothDevice.BOND_BONDING -> "Bonding"
            BluetoothDevice.BOND_NONE -> "Not bonded"
            else -> "Unknown"
        }
    }

    private fun deviceTypeLabel(deviceType: Int): String {
        return when (deviceType) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
            BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual"
            BluetoothDevice.DEVICE_TYPE_LE -> "Low Energy"
            BluetoothDevice.DEVICE_TYPE_UNKNOWN -> "Unknown"
            else -> "Unknown"
        }
    }

    private fun registerReceiverIfNeeded() {
        if (isReceiverRegistered) return

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(
                discoveryReceiver,
                filter,
                Context.RECEIVER_EXPORTED,
            )
        } else {
            @Suppress("DEPRECATION")
            appContext.registerReceiver(discoveryReceiver, filter)
        }
        isReceiverRegistered = true
    }

    private fun unregisterReceiverIfNeeded() {
        if (!isReceiverRegistered) return

        runCatching {
            appContext.unregisterReceiver(discoveryReceiver)
        }
        isReceiverRegistered = false
    }

    private fun Intent.bluetoothDeviceExtra(): BluetoothDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
    }
}
