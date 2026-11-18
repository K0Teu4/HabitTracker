# Habit Tracker — debug build + install via ADB
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

Write-Host "==> Habit Tracker 0.2.0 — build & install" -ForegroundColor Cyan

# local.properties if missing
$localProps = Join-Path $Root "local.properties"
if (-not (Test-Path $localProps)) {
    $sdk = $env:ANDROID_SDK_ROOT
    if (-not $sdk) { $sdk = $env:ANDROID_HOME }
    if (-not $sdk) {
        $default = Join-Path $env:LOCALAPPDATA "Android\Sdk"
        if (Test-Path $default) { $sdk = $default }
    }
    if ($sdk) {
        "sdk.dir=$($sdk -replace '\\','/')" | Set-Content -Path $localProps -Encoding UTF8
        Write-Host "Created local.properties -> $sdk"
    } else {
        Write-Warning "ANDROID_SDK_ROOT / ANDROID_HOME not set. Create local.properties manually."
    }
}

$gradlew = Join-Path $Root "gradlew.bat"
if (-not (Test-Path $gradlew)) {
    Write-Host "gradlew.bat not found. Trying system gradle wrapper..." -ForegroundColor Yellow
    if (Get-Command gradle -ErrorAction SilentlyContinue) {
        & gradle wrapper --gradle-version 8.11.1
    } else {
        Write-Error "No gradlew.bat and no gradle in PATH. Open project once in Android Studio to generate wrapper."
    }
}

Write-Host "==> assembleDebug"
& .\gradlew.bat --no-daemon clean assembleDebug
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed" }

$apk = Join-Path $Root "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apk)) { throw "APK not found: $apk" }

Write-Host "==> adb devices"
adb devices

Write-Host "==> install"
adb install -r $apk
if ($LASTEXITCODE -ne 0) { throw "adb install failed" }

Write-Host "==> launch"
adb shell am start -n ru.habittracker/.MainActivity

Write-Host "Done." -ForegroundColor Green
