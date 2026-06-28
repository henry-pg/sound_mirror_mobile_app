package com.example.bluetoothclassicscanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat

object BluetoothPermissionHandler {

    fun runtimePermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun missingRuntimePermissions(context: Context): List<String> {
        return runtimePermissions().filterNot { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun canReadPairedDevices(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun canStartDiscovery(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            missingRuntimePermissions(context).isEmpty()
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && isLocationEnabled(context)
        }
    }

    fun hasRequiredPermissions(context: Context): Boolean {
        return missingRuntimePermissions(context).isEmpty()
    }

    fun discoveryUnavailableReason(context: Context): String? {
        val missingPermissions = missingRuntimePermissions(context)
        if (missingPermissions.isNotEmpty()) {
            return "Bluetooth permissions are required before scanning"
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !isLocationEnabled(context)) {
            return "Location services must be enabled for Bluetooth discovery on Android 11 and lower"
        }

        return null
    }

    fun permissionSummary(context: Context): String {
        val missing = missingRuntimePermissions(context)
        return if (missing.isEmpty()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !isLocationEnabled(context)) {
                "Permissions granted, but Location services are off"
            } else {
                "All required Bluetooth permissions granted"
            }
        } else {
            "Missing: ${missing.joinToString()}"
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }
}
