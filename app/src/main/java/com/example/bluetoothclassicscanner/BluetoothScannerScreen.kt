package com.example.bluetoothclassicscanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BluetoothScannerScreen(
    uiState: BluetoothScannerUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onRequestPermissions: () -> Unit,
) {
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "Bluetooth Classic Scanner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            item {
                StatusCard(
                    title = "Bluetooth status",
                    body = uiState.statusMessage,
                )
            }

            item {
                StatusCard(
                    title = "Permission status",
                    body = uiState.permissionSummary,
                    footer = {
                        if (!uiState.arePermissionsGranted) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(onClick = onRequestPermissions) {
                                Text("Grant Permissions")
                            }
                        }
                    },
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onStartScan,
                        enabled = uiState.isBluetoothSupported && uiState.isBluetoothEnabled && !uiState.isScanning,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Start Scan")
                    }

                    OutlinedButton(
                        onClick = onStopScan,
                        enabled = uiState.isScanning,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Stop Scan")
                    }
                }
            }

            item {
                Text(
                    text = if (uiState.isScanning) "Scanning in progress..." else "Not currently scanning",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            item {
                DeviceSectionHeader(
                    title = "Paired devices",
                    emptyMessage = "No paired Bluetooth devices found",
                    isEmpty = uiState.pairedDevices.isEmpty(),
                )
            }

            deviceItems(uiState.pairedDevices)

            item {
                DeviceSectionHeader(
                    title = "Discovered nearby devices",
                    emptyMessage = "No discoverable Bluetooth Classic devices found yet",
                    isEmpty = uiState.discoveredDevices.isEmpty(),
                )
            }

            deviceItems(uiState.discoveredDevices)
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    body: String,
    footer: @Composable (() -> Unit)? = null,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
            footer?.invoke()
        }
    }
}

@Composable
private fun DeviceSectionHeader(
    title: String,
    emptyMessage: String,
    isEmpty: Boolean,
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isEmpty) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun LazyListScope.deviceItems(devices: List<BluetoothDeviceUiModel>) {
    items(
        count = devices.size,
        key = { index -> devices[index].address },
    ) { index ->
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DeviceRow(device = devices[index])
                if (index != devices.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(device: BluetoothDeviceUiModel) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = device.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        Text(text = "MAC: ${device.address}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Bond state: ${device.bondState}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Type: ${device.deviceType}", style = MaterialTheme.typography.bodyMedium)
    }
}
