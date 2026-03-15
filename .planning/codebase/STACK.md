# Technology Stack

**Analysis Date:** 2026-03-15

## Languages

**Primary:**
- Kotlin 2.2.10 - All application code (UI, domain, data layers)

**Build Configuration:**
- Kotlin DSL - Gradle build scripts (`build.gradle.kts`, `settings.gradle.kts`)

## Runtime

**Environment:**
- Android SDK 36 (compileSdk, targetSdk)
- Android SDK 26 (minSdk)
- JDK 17 (source/target compatibility)

**Build Tool:**
- Gradle 9.3.1 with wrapper
- Gradle wrapper properties: `gradle/wrapper/gradle-wrapper.properties`

**Package Manager:**
- Gradle (no separate package manager, dependencies in `build.gradle.kts`)

## Frameworks

**Core:**
- Jetpack Compose (BOM 2026.01.01) - UI framework
- Material 3 (material3:1.3.0) - Design system
- Navigation Compose 2.9.7 - Navigation
- AndroidX Core KTX 1.17.0 - Kotlin extensions

**Architecture:**
- AndroidX Lifecycle 2.8.6 - ViewModel, runtime-compose, viewmodel-compose
- AndroidX Activity Compose 1.12.4 - Single Activity architecture

**Data Persistence:**
- Room 2.8.4 - SQLite ORM with Kotlin coroutines support
- DataStore Preferences 1.1.1 - Key-value preferences storage

**Background Processing:**
- WorkManager 2.10.1 - Background sync worker for holiday data

**Testing:**
- JUnit4 4.13.2 - Unit test framework
- Robolectric 4.14.1 - Android unit test runner
- AndroidX Test Core 1.6.1 - Android testing utilities
- AndroidX Compose UI Test JUnit4 - Compose UI testing
- kotlinx-coroutines-test 1.10.2 - Coroutine testing utilities
- androidx.arch.core:core-testing 2.2.0 - Architecture testing utilities

## Key Dependencies

**Compose (BOM 2026.01.01):**
- `androidx.compose.ui:ui` - Core Compose UI
- `androidx.compose.ui:ui-tooling-preview` - Preview support
- `androidx.compose.foundation:foundation` - Foundation components
- `androidx.compose.material3:material3` - Material 3 components
- `androidx.compose.material:material-icons-extended` - Material icons

**AndroidX:**
- `androidx.core:core-ktx:1.17.0` - Kotlin extensions
- `androidx.lifecycle:lifecycle-runtime-ktx:2.8.6` - Lifecycle support
- `androidx.lifecycle:lifecycle-runtime-compose:2.8.6` - Compose integration
- `androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6` - ViewModel Compose
- `androidx.activity:activity-compose:1.12.4` - Compose Activity
- `androidx.navigation:navigation-compose:2.9.7` - Navigation
- `androidx.room:room-runtime:2.8.4` - Room database
- `androidx.room:room-ktx:2.8.4` - Room coroutines
- `androidx.datastore:datastore-preferences:1.1.1` - DataStore
- `androidx.work:work-runtime-ktx:2.10.1` - WorkManager
- `com.google.android.material:material:1.13.0` - Material components

**Testing:**
- `junit:junit:4.13.2` - JUnit framework
- `org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2` - Coroutine test
- `androidx.arch.core:core-testing:2.2.0` - Arch test helpers
- `androidx.test:core:1.6.1` - Android test core
- `org.json:json:20240303` - JSON parsing for tests
- `org.robolectric:robolectric:4.14.1` - Android test runner

## Configuration

**Environment:**
- `local.properties` - SDK directory configuration (`sdk.dir`)
- Signing configuration in `local.properties` for release builds:
  - `signing.storeFile`, `signing.keyAlias`, `signing.storePassword`, `signing.keyPassword`

**Build Configuration:**
- `build.gradle.kts` - Root project configuration
- `app/build.gradle.kts` - App module configuration (version 2.1.0, versionCode 14)
- `settings.gradle.kts` - Project settings
- `gradle.properties` - Gradle JVM args, AndroidX flags, Kotlin code style

**Android Config:**
- Namespace: `com.peter.overtimecalculator`
- Application ID: `com.peter.overtimecalculator` (debug: `.debug` suffix)

## Platform Requirements

**Development:**
- Android Studio with Android SDK
- JDK 17
- Gradle 9.3.1+ (via wrapper)

**Production:**
- Android 8.0+ (API 26+)
- No special runtime requirements

---

*Stack analysis: 2026-03-15*
