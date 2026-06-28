package com.example.bluetoothclassicscanner

import android.bluetooth.BluetoothManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {

    private val viewModel: BluetoothViewModel by viewModels()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.onPermissionsUpdated()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (isBluetoothSupportedOnDevice() && !BluetoothPermissionHandler.hasRequiredPermissions(this)) {
            permissionLauncher.launch(BluetoothPermissionHandler.runtimePermissions().toTypedArray())
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MaterialTheme {
                Surface {
                    BluetoothScannerScreen(
                        uiState = uiState,
                        onStartScan = {
                            if (BluetoothPermissionHandler.hasRequiredPermissions(this@MainActivity)) {
                                viewModel.startScan()
                            } else {
                                permissionLauncher.launch(
                                    BluetoothPermissionHandler.runtimePermissions().toTypedArray(),
                                )
                            }
                        },
                        onStopScan = viewModel::stopScan,
                        onRequestPermissions = {
                            permissionLauncher.launch(
                                BluetoothPermissionHandler.runtimePermissions().toTypedArray(),
                            )
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onStop() {
        viewModel.stopScan()
        super.onStop()
    }

    private fun isBluetoothSupportedOnDevice(): Boolean {
        return getSystemService(BluetoothManager::class.java)?.adapter != null
    }
}
