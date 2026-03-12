# Music Player (Android Studio Lab Activity)

This repository contains a simple **Music Player app** built for Android Studio lab work.

## Features
- Play / Pause / Stop controls
- Next / Previous song buttons
- Options menu with:
  - Refresh songs
  - Stop playback
  - About
- Loads songs from device storage using `MediaStore`
- Runtime permission handling for Android 13+ and older versions

## Project Structure
- `app/src/main/java/com/example/musicplayer/MainActivity.kt`: Main logic for loading songs and controlling playback
- `app/src/main/java/com/example/musicplayer/Song.kt`: Song model
- `app/src/main/res/layout/activity_main.xml`: UI with multiple buttons
- `app/src/main/res/menu/player_menu.xml`: App menu items

## How to Run
1. Open this project in **Android Studio**.
2. Let Gradle sync.
3. Run on an emulator/device.
4. Allow storage/audio permission when prompted.
5. Add audio files to the emulator/device and tap **Refresh songs** from the menu.

## Notes for Lab Submission
- Demonstrates use of multiple files and UI components.
- Demonstrates menu creation and handling.
- Includes toast-based feedback for actions.
