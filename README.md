# Bluetooth Classic Scanner

A native Android app in Kotlin that scans for nearby Bluetooth Classic devices and displays:

- Paired devices from `bondedDevices`
- Nearby discoverable Bluetooth Classic devices found through `BluetoothAdapter.startDiscovery()`

The app uses:

- Kotlin
- Jetpack Compose
- MVVM
- Coroutines and `StateFlow`
- Native Android Bluetooth APIs

## Requirements

- Android Studio Koala or newer
- Android SDK installed
- A real Android phone

Bluetooth Classic discovery should be tested on a real device. Emulators generally do not support this workflow reliably.

## Project Structure

- `app/src/main/java/com/example/bluetoothclassicscanner/MainActivity.kt`
- `app/src/main/java/com/example/bluetoothclassicscanner/BluetoothViewModel.kt`
- `app/src/main/java/com/example/bluetoothclassicscanner/BluetoothRepository.kt`
- `app/src/main/java/com/example/bluetoothclassicscanner/BluetoothPermissionHandler.kt`
- `app/src/main/java/com/example/bluetoothclassicscanner/BluetoothDeviceUiModel.kt`
- `app/src/main/java/com/example/bluetoothclassicscanner/BluetoothScannerScreen.kt`

## Open The Project

1. Open Android Studio.
2. Choose `Open`.
3. Select the project root folder.

4. Let Android Studio sync Gradle.

## Build The App

From Android Studio:

1. Wait for Gradle sync to finish.
2. Use `Build > Make Project`.

From the command line:

```bash
./gradlew assembleDebug
```

If your Android SDK is not already configured in your shell or Android Studio, set `ANDROID_SDK_ROOT` and `ANDROID_HOME` first.

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Install On A Test Phone

### Option 1: Android Studio

1. Enable Developer Options on the phone.
2. Enable `USB debugging`.
3. Connect the phone by USB.
4. Accept the debugging prompt on the phone.
5. In Android Studio, select the connected device.
6. Click `Run`.

Android Studio will build and install the app automatically.

### Option 2: ADB

Enable `USB debugging`, connect the phone, then run:

```bash
~/Library/Android/sdk/platform-tools/adb devices
```

If the device appears, install the app:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If you get a signature conflict, uninstall first:

```bash
adb uninstall com.example.bluetoothclassicscanner
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Runtime Notes

On first launch:

1. Grant the requested Bluetooth permissions.
2. If testing on Android 11 or lower, make sure Location services are enabled.
3. Make sure Bluetooth is enabled in system settings.

Nearby devices found through discovery are limited to discoverable Bluetooth Classic devices. Not every nearby Bluetooth device will appear.

## Basic Test Plan

1. Launch the app.
2. Confirm the app shows whether Bluetooth is supported and enabled.
3. Confirm paired devices appear in the `Paired devices` section.
4. Tap `Start Scan`.
5. Confirm the scanning state changes.
6. Confirm nearby discoverable Bluetooth Classic devices appear in `Discovered nearby devices`.
7. Tap `Stop Scan`.
8. Confirm scanning stops without a crash.
9. Disable Bluetooth and reopen the app to verify the disabled-state message.
10. Deny permissions and confirm the app does not crash.

## Permissions

Android 12 and higher:

- `BLUETOOTH_SCAN`
- `BLUETOOTH_CONNECT`

Android 11 and lower:

- `BLUETOOTH`
- `BLUETOOTH_ADMIN`
- `ACCESS_FINE_LOCATION`

`BLUETOOTH_ADVERTISE` is not used in this app.

## Known Limitations

- This app scans Bluetooth Classic devices only.
- BLE scanning APIs are intentionally not used.
- iOS is not supported.
- Discovery results depend on device visibility and OS behavior.
