package com.example.bluetoothclassicscanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private data class BluetoothStatusSnapshot(
    val isBluetoothSupported: Boolean,
    val isBluetoothEnabled: Boolean,
    val permissionSummary: String,
    val arePermissionsGranted: Boolean,
    val statusMessage: String,
)

data class BluetoothScannerUiState(
    val isBluetoothSupported: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
    val permissionSummary: String = "",
    val arePermissionsGranted: Boolean = false,
    val pairedDevices: List<BluetoothDeviceUiModel> = emptyList(),
    val discoveredDevices: List<BluetoothDeviceUiModel> = emptyList(),
    val isScanning: Boolean = false,
    val statusMessage: String = "",
)

class BluetoothViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val repository = BluetoothRepository(application.applicationContext)
    private val appContext = application.applicationContext

    private val statusMessage = MutableStateFlow("")
    private val bluetoothSupport = MutableStateFlow(false)
    private val bluetoothEnabled = MutableStateFlow(false)
    private val permissionSummary = MutableStateFlow("")
    private val permissionsGranted = MutableStateFlow(false)

    private val statusSnapshot = combine(
        bluetoothSupport,
        bluetoothEnabled,
        permissionSummary,
        permissionsGranted,
        statusMessage,
    ) { isSupported, isEnabled, permissionText, granted, message ->
        BluetoothStatusSnapshot(
            isBluetoothSupported = isSupported,
            isBluetoothEnabled = isEnabled,
            permissionSummary = permissionText,
            arePermissionsGranted = granted,
            statusMessage = message,
        )
    }

    val uiState: StateFlow<BluetoothScannerUiState> = combine(
        statusSnapshot,
        repository.pairedDevices,
        repository.discoveredDevices,
        repository.isScanning,
    ) { status, pairedDevices, discoveredDevices, isScanning ->
        BluetoothScannerUiState(
            isBluetoothSupported = status.isBluetoothSupported,
            isBluetoothEnabled = status.isBluetoothEnabled,
            permissionSummary = status.permissionSummary,
            arePermissionsGranted = status.arePermissionsGranted,
            pairedDevices = pairedDevices,
            discoveredDevices = discoveredDevices,
            isScanning = isScanning,
            statusMessage = status.statusMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BluetoothScannerUiState(),
    )

    init {
        refresh()
    }

    fun refresh(updateMessage: Boolean = true) {
        bluetoothSupport.value = repository.isBluetoothSupported()
        bluetoothEnabled.value = repository.isBluetoothEnabled()
        permissionsGranted.value = BluetoothPermissionHandler.hasRequiredPermissions(appContext)
        permissionSummary.value = BluetoothPermissionHandler.permissionSummary(appContext)
        repository.refreshPairedDevices()
        if (updateMessage) {
            statusMessage.value = buildStatusMessage()
        }
    }

    fun onPermissionsUpdated() {
        refresh()
    }

    fun startScan() {
        statusMessage.value = repository.startDiscovery() ?: "Scanning for nearby discoverable Bluetooth Classic devices"
        refresh(updateMessage = false)
    }

    fun stopScan() {
        repository.stopDiscovery()
        statusMessage.value = if (repository.isBluetoothEnabled()) {
            "Bluetooth discovery stopped"
        } else {
            buildStatusMessage()
        }
        refresh(updateMessage = false)
    }

    override fun onCleared() {
        repository.release()
        super.onCleared()
    }

    private fun buildStatusMessage(): String {
        return when {
            !repository.isBluetoothSupported() -> "Bluetooth is not supported on this device"
            !repository.isBluetoothEnabled() -> "Bluetooth is disabled. Enable it in system settings to scan."
            BluetoothPermissionHandler.discoveryUnavailableReason(appContext) != null ->
                BluetoothPermissionHandler.discoveryUnavailableReason(appContext)
                    ?: "Grant Bluetooth permissions to view paired devices and scan."
            else -> "Bluetooth is ready"
        }
    }
}
