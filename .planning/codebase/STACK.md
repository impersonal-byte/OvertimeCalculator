# Stack

## Snapshot
- Project type: single-module native Android app named `OvertimeCalculator`.
- Gradle module map lives in `settings.gradle.kts` and includes only `:app`.
- Root build plugin declarations live in `build.gradle.kts`.
- Main app build configuration lives in `app/build.gradle.kts`.

## Languages And Runtime
- Kotlin is the primary implementation language across `app/src/main/java/com/peter/overtimecalculator/**`.
- Gradle Kotlin DSL is used for build files in `build.gradle.kts`, `settings.gradle.kts`, and `app/build.gradle.kts`.
- Java 17 is configured in `app/build.gradle.kts`.
- Android SDK levels are `minSdk = 26`, `compileSdk = 36`, and `targetSdk = 36` in `app/build.gradle.kts`.

## Build Toolchain
- Gradle wrapper version is pinned in `gradle/wrapper/gradle-wrapper.properties` to `gradle-9.3.1-bin.zip`.
- Shared Gradle settings live in `gradle.properties` with AndroidX enabled and official Kotlin style.
- The root plugin block in `build.gradle.kts` declares `com.android.application`, `org.jetbrains.kotlin.plugin.compose`, and `com.android.legacy-kapt`.
- `app/build.gradle.kts` defines custom tasks `packageReleaseApk`, `printVersionName`, and `printVersionCode`.

## Android Frameworks And Libraries
- Jetpack Compose drives UI in files such as `app/src/main/java/com/peter/overtimecalculator/MainActivity.kt` and `app/src/main/java/com/peter/overtimecalculator/ui/Screens.kt`.
- Material 3 is used via Compose and `com.google.android.material:material` in `app/build.gradle.kts`.
- Navigation Compose is used through `app/src/main/java/com/peter/overtimecalculator/ui/Screens.kt` and `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsGraphs.kt`.
- Lifecycle ViewModel and Compose lifecycle bindings are used in `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt` and `app/src/main/java/com/peter/overtimecalculator/ui/AppUpdateViewModel.kt`.
- Room persistence is defined in `app/src/main/java/com/peter/overtimecalculator/data/db/AppDatabase.kt`, `app/src/main/java/com/peter/overtimecalculator/data/db/Entities.kt`, and `app/src/main/java/com/peter/overtimecalculator/data/db/OvertimeDao.kt`.
- DataStore Preferences is used for holiday metadata/cache in `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesRepository.kt`.
- WorkManager is used for background sync in `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidaySyncWorker.kt`.

## Internal Architectural Building Blocks
- Application bootstrap starts in `app/src/main/java/com/peter/overtimecalculator/OvertimeApplication.kt`.
- Manual dependency assembly is centralized in `app/src/main/java/com/peter/overtimecalculator/data/AppContainer.kt`.
- Business rules live under `app/src/main/java/com/peter/overtimecalculator/domain/**`.
- UI state orchestration lives in `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt` and `app/src/main/java/com/peter/overtimecalculator/ui/AppUpdateViewModel.kt`.

## Configuration Hotspots
- App identity, signing, SDK levels, dependencies, and packaging live in `app/build.gradle.kts`.
- Manifest-level permissions and `FileProvider` setup live in `app/src/main/AndroidManifest.xml`.
- Backup and file-sharing XML resources live under `app/src/main/res/xml/`.
- Bundled holiday seed data lives in `app/src/main/assets/holidays/cn_mainland.json`.

## Developer Commands
- Local commands are documented in `README.md`.
- Unit tests: `./gradlew testDebugUnitTest` or `./gradlew.bat testDebugUnitTest`.
- Debug build: `./gradlew assembleDebug` or `./gradlew.bat assembleDebug`.
- Release packaging: `./gradlew packageReleaseApk` or `./gradlew.bat packageReleaseApk`.

## Test Stack
- JVM tests use JUnit 4, `kotlinx-coroutines-test`, AndroidX core-testing, JSON helpers, and Robolectric from `app/build.gradle.kts`.
- Instrumentation tests use AndroidX JUnit, Espresso, and Compose UI test libraries from `app/build.gradle.kts`.
- Representative test entry points live in `app/src/test/java/com/peter/overtimecalculator/` and `app/src/androidTest/java/com/peter/overtimecalculator/MainFlowTest.kt`.
