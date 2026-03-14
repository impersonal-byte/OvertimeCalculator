# Integrations

## Snapshot
- The app is mostly self-contained, but it integrates with Android platform services, one public holiday API, and GitHub releases.
- Most integrations are wired from `app/src/main/java/com/peter/overtimecalculator/data/**`.

## Local Storage
- Room stores monthly config, overtime entries, and holiday overrides through `app/src/main/java/com/peter/overtimecalculator/data/db/AppDatabase.kt`.
- Table models live in `app/src/main/java/com/peter/overtimecalculator/data/db/Entities.kt`.
- Query and write access live in `app/src/main/java/com/peter/overtimecalculator/data/db/OvertimeDao.kt`.
- Repository orchestration over Room lives in `app/src/main/java/com/peter/overtimecalculator/data/repository/OvertimeRepository.kt`.

## Preference Storage
- DataStore Preferences is used in `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesRepository.kt` to cache remote holiday JSON and fetch metadata.
- `SharedPreferences` is used in `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt` for appearance and calendar preferences.
- `SharedPreferences` is also used in `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateManager.kt` for pending download and install-permission state.

## Remote Holiday Rules
- The holiday subsystem starts with bundled baseline data in `app/src/main/assets/holidays/cn_mainland.json`.
- Remote refresh logic lives in `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesRepository.kt`.
- The remote endpoint template is `https://api.haoshenqi.top/holiday?date=%d` in `HolidayRulesRepository.kt`.
- Parsing support lives in `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayHaoshenqiApiParser.kt` and `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesJsonParser.kt`.

## Background Work
- Periodic holiday refresh is scheduled through WorkManager in `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidaySyncWorker.kt`.
- Startup scheduling is triggered from `app/src/main/java/com/peter/overtimecalculator/OvertimeApplication.kt` via `appContainer.scheduleHolidaySync()`.
- The worker uses a connected-network constraint and `enqueueUniquePeriodicWork` in `HolidaySyncWorker.kt`.

## App Update Flow
- GitHub release checks are implemented in `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateManager.kt`.
- The latest-release endpoint is `https://api.github.com/repos/impersonal-byte/OvertimeCalculator/releases/latest` in `UpdateManager.kt`.
- Download/install orchestration surfaces through `app/src/main/java/com/peter/overtimecalculator/ui/AppUpdateViewModel.kt`.
- About/update UI entry points live in `app/src/main/java/com/peter/overtimecalculator/ui/settings/AboutScreen.kt` and `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsGraphs.kt`.

## File Sharing And Export
- CSV export file generation happens in `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt`.
- Share intent dispatch happens in `app/src/main/java/com/peter/overtimecalculator/ui/Screens.kt`.
- `FileProvider` is declared in `app/src/main/AndroidManifest.xml` and configured by `app/src/main/res/xml/file_paths.xml`.

## Android Platform Services
- `DownloadManager` is used in `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateManager.kt`.
- Install permission routing uses `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES` in `UpdateManager.kt`.
- Vibration capability is requested in `app/src/main/AndroidManifest.xml`, with UI haptic hooks under `app/src/main/java/com/peter/overtimecalculator/ui/TickHapticFeedback.kt`.

## CI And Release Infrastructure
- Manual verification workflow lives in `.github/workflows/ci.yml`.
- Tag-based release workflow lives in `.github/workflows/release.yml`.
- Release notes can be sourced from `docs/releases/*.md`, selected in `.github/workflows/release.yml`.
- Release signing expects secrets and generates `local.properties` in CI according to `.github/workflows/release.yml`.

## Sensitive Integration Edges
- `app/build.gradle.kts` reads signing values from `local.properties`.
- The repo root also contains `OvertimeCalculator.jks` and `OvertimeCalculator.jks.base64.txt`, which should be treated as high-risk operational artifacts.
- No cloud backend, auth provider, or analytics SDK is visible in the current codebase.
