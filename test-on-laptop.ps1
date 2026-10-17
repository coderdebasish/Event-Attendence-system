# ==============================================================================
# Event Attendance System - Laptop Testing & Diagnosis Script
# ==============================================================================

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   Event Attendance System - Laptop Diagnostics & Runner" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Configure Java and Android SDK Paths
if (-not $env:JAVA_HOME) {
    if (Test-Path "E:\Program Files\Android\Android Studio\jbr") {
        $env:JAVA_HOME = "E:\Program Files\Android\Android Studio\jbr"
    } elseif (Test-Path "C:\Program Files\Android\Android Studio\jbr") {
        $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
    }
}

if (-not $env:ANDROID_HOME) {
    if (Test-Path "$env:LOCALAPPDATA\Android\Sdk") {
        $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
    }
}

$adb = "$env:ANDROID_HOME\platform-tools\adb.exe"

if (-not (Test-Path $adb)) {
    Write-Host "[ERROR] adb.exe not found at: $adb" -ForegroundColor Red
    Write-Host "Please ensure Android SDK Platform-Tools is installed." -ForegroundColor Yellow
    exit 1
}

Write-Host "[INFO] Using Java:    $env:JAVA_HOME" -ForegroundColor Gray
Write-Host "[INFO] Using Android: $env:ANDROID_HOME" -ForegroundColor Gray

# 2. Check Connected Devices / Emulators
Write-Host "`n[1/4] Checking connected devices or running emulators..." -ForegroundColor Yellow
$deviceOutput = & $adb devices
$lines = $deviceOutput -split "`r?`n" | Where-Object { $_.Trim() -ne "" -and $_ -notmatch "List of devices" }

$connectedDevices = @()
foreach ($line in $lines) {
    if ($line -match "^([^\s]+)\s+device$") {
        $connectedDevices += $matches[1]
    }
}

if ($connectedDevices.Count -eq 0) {
    Write-Host "`n[!] No device or emulator currently connected." -ForegroundColor Yellow
    Write-Host "----------------------------------------------------------" -ForegroundColor Gray
    Write-Host "To test the app right now on your laptop, choose one of these:" -ForegroundColor White
    Write-Host "  Option A (Fastest & Best): Connect your Android phone with a USB cable." -ForegroundColor Green
    Write-Host "            Make sure 'USB Debugging' is enabled in Developer Options on your phone." -ForegroundColor Gray
    Write-Host "  Option B (Wireless): On your phone (Android 11+), enable 'Wireless Debugging'" -ForegroundColor Green
    Write-Host "            and run: & `"$adb`" connect <phone-ip>:<port>" -ForegroundColor Gray
    Write-Host "  Option C (Emulator): Open Android Studio -> Tools -> Device Manager -> Start an Emulator ▶️." -ForegroundColor Green
    Write-Host "----------------------------------------------------------" -ForegroundColor Gray
    Write-Host "`nWaiting for device to connect... (Press Ctrl+C to cancel)" -ForegroundColor Cyan

    while ($connectedDevices.Count -eq 0) {
        Start-Sleep -Seconds 2
        $deviceOutput = & $adb devices
        $lines = $deviceOutput -split "`r?`n" | Where-Object { $_.Trim() -ne "" -and $_ -notmatch "List of devices" }
        foreach ($line in $lines) {
            if ($line -match "^([^\s]+)\s+device$") {
                $connectedDevices += $matches[1]
            }
        }
    }
}

$targetDevice = $connectedDevices[0]
Write-Host "[SUCCESS] Target device connected: $targetDevice" -ForegroundColor Green

# 3. Verify / Build Debug APK
Write-Host "`n[2/4] Verifying debug APK build..." -ForegroundColor Yellow
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "[BUILD] Compiling debug APK via Gradle..." -ForegroundColor Cyan
    .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Gradle build failed!" -ForegroundColor Red
        exit 1
    }
}

# 4. Install onto Device
Write-Host "`n[3/4] Installing app on $targetDevice..." -ForegroundColor Yellow
& $adb -s $targetDevice install -r $apkPath
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Installation failed." -ForegroundColor Red
    exit 1
}
Write-Host "[SUCCESS] App installed successfully!" -ForegroundColor Green

# 5. Launch App
Write-Host "`n[4/4] Launching Event Attendance System..." -ForegroundColor Yellow
& $adb -s $targetDevice shell am start -n "com.eventattendance.app/.MainActivity"

# 6. Stream Live Diagnostics & Logs
Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "   STREAMING LIVE LOGS & DIAGNOSTICS (Press Ctrl+C to stop)" -ForegroundColor Green
Write-Host "   Tags: EventAttendance, AndroidRuntime, System.err" -ForegroundColor Gray
Write-Host "==========================================================" -ForegroundColor Green

& $adb -s $targetDevice logcat -c
& $adb -s $targetDevice logcat -v time -s EventAttendance:V AndroidRuntime:E System.err:E
