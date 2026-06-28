package com.example.bluetoothclassicscanner

data class BluetoothDeviceUiModel(
    val name: String,
    val address: String,
    val bondState: String,
    val deviceType: String,
)
