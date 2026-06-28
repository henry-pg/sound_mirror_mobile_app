# Bluetooth Classic Scanner

This project is a simple Android app that scans for Bluetooth Classic devices.

It shows:

- paired Bluetooth devices already saved on the phone
- nearby discoverable Bluetooth Classic devices found during a scan

## Important First Note

This GitHub repo contains the source code.

It does not contain a built APK file in git.

So if you want to test the app on a phone, you must do one of these:

1. Build the APK from this repo
2. Get an APK file from someone who already built it

## Easiest Way For Most People

If you do not already know Android development tools, use Android Studio.

Android Studio can:

- download the Android tools you need
- build the app
- install the app onto your phone

## What You Need

- an Android phone
- a USB cable
- a computer
- this project downloaded from GitHub

For the easiest path, also install Android Studio:

`https://developer.android.com/studio`

If you want to install the app from the command line instead of through Android Studio, you also need `adb` installed on your computer.

`adb` is part of Android platform tools and is usually installed along with Android Studio.

## Download The Project

Repo URL:

`https://github.com/henry-pg/sound_mirror_mobile_app`

You can either:

- download the repo ZIP from GitHub and extract it
- or clone it with git

## Option 1: Build And Install With Android Studio

This is the recommended path if you are not familiar with command-line Android tools.

### Step 1: Install Android Studio

Download and install Android Studio from:

`https://developer.android.com/studio`

During installation, allow it to install the Android SDK and related tools.

### Step 2: Open The Project

1. Open Android Studio.
2. Click `Open`.
3. Select the project folder you downloaded from GitHub.
4. Wait for Android Studio to finish syncing the project.

### Step 3: Turn On Developer Mode On The Phone

On the Android phone:

1. Open `Settings`.
2. Open `About phone`.
3. Find `Build number`.
4. Tap `Build number` 7 times.
5. Go back to `Settings`.
6. Open `Developer options`.
7. Turn on `USB debugging`.

### Step 4: Connect The Phone

1. Connect the phone to the computer with a USB cable.
2. If the phone shows a prompt asking whether to allow USB debugging, approve it.

### Step 5: Run The App

1. In Android Studio, find the device selector near the top of the window.
2. Select your phone.
3. Click the `Run` button.

Android Studio will:

- build the app
- install it on the phone
- launch it

## Option 2: Build The APK Yourself With The Command Line

Use this only if you are comfortable with terminal commands.

Before using this option, make sure `adb` is installed.

### Step 1: Build The App

Open a terminal in the project folder and run:

```bash
./gradlew assembleDebug
```

If the Android SDK is not configured on your machine, you may need to install Android Studio first or set Android SDK environment variables.

If the build succeeds, the APK will be created here:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Step 2: Install The App On The Phone

You also need `adb`, which comes with Android platform tools.

If you installed Android Studio, you probably already have it.

Then:

1. Turn on `USB debugging` on the phone
2. Connect the phone by USB
3. Approve the USB debugging prompt on the phone
4. Run:

```bash
adb devices
```

If your phone appears in the list, install the app:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If you get a signature conflict error, run:

```bash
adb uninstall com.example.bluetoothclassicscanner
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Option 3: Install A Prebuilt APK

If someone already built the APK and sent it to you, you do not need to build the project yourself.

Before using this option, make sure `adb` is installed.

You can install it with:

```bash
adb install -r path/to/app-debug.apk
```

You still need:

- a phone with `USB debugging` enabled
- `adb` installed on your computer

## First-Time App Setup On The Phone

When the app opens:

1. Allow the Bluetooth permission prompts
2. Turn on Bluetooth if it is off
3. If the phone is Android 11 or lower, make sure Location services are also turned on

## How To Test The App

1. Open the app
2. Confirm the title says `Bluetooth Classic Scanner`
3. Check the Bluetooth status section
4. Check the permission status section
5. Confirm paired devices appear if the phone already has any
6. Tap `Start Scan`
7. Watch the nearby devices list for discoverable Bluetooth Classic devices
8. Tap `Stop Scan`
9. Confirm the app does not crash if Bluetooth is off or permissions are denied

## What To Expect

- The app scans Bluetooth Classic devices only
- It does not use BLE-only scanning APIs
- Not every nearby Bluetooth device will appear
- Only discoverable Bluetooth Classic devices should be expected to show up
- Testing should be done on a real Android phone, not an emulator

## Main Source Files

- `app/src/main/java/com/example/bluetoothclassicscanner/MainActivity.kt`
- `app/src/main/java/com/example/bluetoothclassicscanner/BluetoothViewModel.kt`
- `app/src/main/java/com/example/bluetoothclassicscanner/BluetoothRepository.kt`
- `app/src/main/java/com/example/bluetoothclassicscanner/BluetoothPermissionHandler.kt`
- `app/src/main/java/com/example/bluetoothclassicscanner/BluetoothDeviceUiModel.kt`
- `app/src/main/java/com/example/bluetoothclassicscanner/BluetoothScannerScreen.kt`
