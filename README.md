# Noir Music 🎵

A premium Android music player app with a dark, elegant interface for streaming local audio files.

## Features

### 🎨 Premium Design
- **Dark Black Theme**: Sleek black background with gold accents
- **Rich Interface**: Material Design components with smooth animations
- **Beautiful Splash Screen**: Elegant loading screen displaying "Made by ARJUN A NAIR"
- **Custom Logo**: Gold music note icon with premium styling

### 🎵 Music Playback
- **Local File Streaming**: Play all your audio files from device storage
- **Background Playback**: Continue listening when the app is in the background
- **Screen-Off Playback**: Music keeps playing even when the screen is locked
- **Foreground Service**: Persistent notification for music controls

### 🎛️ Controls
- **Play/Pause**: Full playback control
- **Skip Next/Previous**: Navigate through your playlist
- **Seek Bar**: Jump to any position in the track
- **Now Playing Bar**: Always visible player controls at the bottom

### ⚡ Unique Features
- **Sleep Timer**: Auto-pause music after 15 min, 30 min, or 1 hour
- **Premium UI Elements**: Card-based layout with elevation and shadows
- **Smooth Scrolling**: RecyclerView with optimized performance
- **Audio Focus Management**: Properly handles interruptions (calls, notifications)
- **Wake Lock**: Keeps playback active during screen-off

### 🎯 Technical Highlights
- **Kotlin**: Modern Android development
- **Material Components**: Latest Material Design guidelines
- **MediaPlayer API**: Robust audio playback
- **Content Provider**: Efficient media scanning
- **Service Architecture**: Background music service with foreground notification
- **Lifecycle-Aware**: Proper activity and service lifecycle management

## Requirements

- Android 7.0 (API 24) or higher
- Storage permission for accessing music files
- Recommended: Android 13 (API 33) for best experience with new audio permissions

## Permissions

- `READ_EXTERNAL_STORAGE`: For Android 12 and below
- `READ_MEDIA_AUDIO`: For Android 13 and above
- `FOREGROUND_SERVICE`: For background playback
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: For media-specific foreground service
- `WAKE_LOCK`: For screen-off playback

## Building the APK

### Debug APK
```bash
./gradlew assembleDebug
```

The debug APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (requires signing)
```bash
./gradlew assembleRelease
```

## Installation

1. Enable "Install from Unknown Sources" in Android settings
2. Transfer the APK to your device
3. Open the APK file and follow installation prompts
4. Grant storage permission when requested
5. Enjoy your music!

## App Structure

```
app/
├── src/main/
│   ├── java/com/noirmusic/
│   │   ├── adapter/
│   │   │   └── SongAdapter.kt          # RecyclerView adapter
│   │   ├── model/
│   │   │   └── Song.kt                 # Song data model
│   │   ├── repository/
│   │   │   └── MusicRepository.kt      # Media content provider
│   │   ├── service/
│   │   │   └── MusicService.kt         # Background music service
│   │   ├── MainActivity.kt             # Main activity
│   │   └── SplashActivity.kt           # Splash screen
│   ├── res/
│   │   ├── drawable/                   # Vector icons and backgrounds
│   │   ├── layout/                     # XML layouts
│   │   ├── values/                     # Colors, strings, styles
│   │   └── mipmap/                     # App icons
│   └── AndroidManifest.xml
└── build.gradle
```

## Credits

**Made by ARJUN A NAIR**

## License

This is a personal project.
