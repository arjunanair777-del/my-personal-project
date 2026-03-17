# Building Noir Music APK

This guide explains how to build the Noir Music Android debug APK.

## Prerequisites

1. **Android Studio** (Recommended) - Latest stable version
   - Download from: https://developer.android.com/studio

2. **OR Command Line Build Tools**:
   - Java JDK 11 or higher
   - Android SDK with:
     - Android SDK Platform 34
     - Android SDK Build-Tools 34.0.0
     - Android SDK Platform-Tools

## Method 1: Build with Android Studio (Easiest)

1. **Open Project**:
   ```
   - Launch Android Studio
   - Click "Open" and select the project folder
   - Wait for Gradle sync to complete
   ```

2. **Build Debug APK**:
   ```
   - Go to: Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Wait for the build to complete
   - Click "locate" in the notification to find the APK
   ```

3. **APK Location**:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

## Method 2: Command Line Build

1. **Set ANDROID_HOME** (if not set):
   ```bash
   export ANDROID_HOME=$HOME/Android/Sdk
   export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
   ```

2. **Make gradlew executable**:
   ```bash
   chmod +x gradlew
   ```

3. **Build**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Find APK**:
   ```bash
   ls -lh app/build/outputs/apk/debug/app-debug.apk
   ```

## Method 3: GitHub Actions (Automated)

If you want to automate the build process, you can add a GitHub Actions workflow:

Create `.github/workflows/build-apk.yml`:

```yaml
name: Build Android APK

on:
  push:
    branches: [ main, claude/* ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Build Debug APK
      run: ./gradlew assembleDebug

    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

## Troubleshooting

### Gradle Build Failed
- Ensure Android SDK is properly installed
- Check that `ANDROID_HOME` is set correctly
- Try: `./gradlew clean` then `./gradlew assembleDebug`

### Permission Denied on gradlew
- Run: `chmod +x gradlew`

### SDK Not Found
- Install Android SDK via Android Studio or command line tools
- Set `ANDROID_HOME` environment variable

### Missing Dependencies
- Make sure you have an internet connection
- Run: `./gradlew --refresh-dependencies assembleDebug`

## Installing the APK

1. **Enable Unknown Sources**:
   - Go to Settings → Security
   - Enable "Install from Unknown Sources"
   - On Android 8+: Allow your browser or file manager to install apps

2. **Transfer APK to Device**:
   - Via USB cable
   - Via email/messaging
   - Via cloud storage (Google Drive, Dropbox)

3. **Install**:
   - Tap the APK file on your device
   - Follow installation prompts
   - Grant storage permissions when requested

## First Launch

1. App will show splash screen with "Made by ARJUN A NAIR"
2. Grant storage permission when requested
3. App will scan for music files
4. Enjoy your music with the premium dark interface!

## Features to Test

- ✅ Play/Pause music
- ✅ Skip next/previous tracks
- ✅ Seek to position in track
- ✅ Background playback (press Home button)
- ✅ Screen-off playback (lock device)
- ✅ Sleep timer (tap clock icon)
- ✅ Dark theme with gold accents

## Notes

- This is a **debug APK** - suitable for testing
- For production/release, you need to:
  - Create a keystore
  - Configure signing in `app/build.gradle`
  - Use `./gradlew assembleRelease`
  - Enable ProGuard optimization

## Support

For issues or questions about building, check:
- Android Studio documentation
- Gradle build documentation
- GitHub repository issues
