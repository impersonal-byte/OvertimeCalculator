# OvertimeCalculator

Android native overtime pay calculator built with Kotlin, Jetpack Compose, Material 3, Room, and Navigation Compose.

## Features

- Monthly calendar home screen
- Daily overtime entry with bottom sheet editor
- Automatic overtime pay calculation by workday, rest day, and holiday multipliers
- Manual hourly rate configuration
- Reverse-engineered hourly rate from monthly overtime payout and weighted overtime hours
- Local offline storage with Room

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Android ViewModel
- Room

## Build

```powershell
.\gradlew.bat assembleDebug
```

## Test

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

## Notes

- The app currently includes built-in mainland China holiday and adjusted workday data, with weekend fallback for dates outside the predefined range.
- Data is stored locally on device. No login or cloud sync is included in the current version.
