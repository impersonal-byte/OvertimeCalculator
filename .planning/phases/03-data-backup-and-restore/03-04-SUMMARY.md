---
phase: 03-data-backup-and-restore
plan: 04
subsystem: ui
tags: [backup, restore, saf, compose, android-test]

# Dependency graph
requires:
  - phase: 03-data-backup-and-restore
    provides: BackupRestoreRepository, RestorePreview, backup file contract, platform backup boundaries
provides:
  - Data-management UI with separate backup / restore / CSV actions
  - ViewModel + app-effects orchestration for SAF backup create / restore pick flows
  - ViewModel unit coverage for backup / preview / confirm / CSV rejection
  - Android UI coverage for data-management action separation
affects: [data-backup-and-restore]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - compose-settings-actions
    - saf-document-create-and-open
    - preview-before-destructive-restore

key-files:
  created:
    - app/src/test/java/com/peter/overtimecalculator/BackupRestoreViewModelTest.kt
    - app/src/androidTest/java/com/peter/overtimecalculator/DataManagementBackupRestoreTest.kt
  modified:
    - app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppEffects.kt
    - app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppShell.kt
    - app/src/main/java/com/peter/overtimecalculator/ui/OvertimeNavigation.kt
    - app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt
    - app/src/main/java/com/peter/overtimecalculator/ui/Screens.kt
    - app/src/main/java/com/peter/overtimecalculator/ui/settings/DataManagementScreen.kt
    - app/src/main/java/com/peter/overtimecalculator/ui/settings/DataManagementSections.kt
    - app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsGraphContract.kt
    - app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsRouteEntries.kt

key-decisions:
  - "Keep CSV export as a lightweight share path and clearly separate it from full backup/restore"
  - "Run restore through preview first, then require explicit confirmation before destructive apply"
  - "Handle SAF file create/open in app effects, not in composables or hard-coded file paths"
  - "Reject CSV content before attempting destructive restore"

patterns-established:
  - "Data management exposes three distinct actions: backup, restore, CSV export"
  - "Restore confirmation is driven by RestorePreview-backed UI state"
  - "ViewModel tests cover backup/restore orchestration without booting full app side effects"

requirements-completed: [DATA-02, DATA-04]

# Metrics
duration: multi-session
completed: 2026-03-15
---

# Phase 3 Plan 4: Data-management Backup/Restore UI Summary

**User-facing backup/restore workflow shipped in settings, with CSV export kept clearly separate and guarded by tests**

## Accomplishments

- Expanded data management from one CSV export action into three distinct actions: full backup, restore, and CSV export
- Wired backup/restore commands through `OvertimeViewModel` and `OvertimeAppEffects`
- Added SAF-backed create/open document flows for `.obackup` files
- Added restore confirmation UI driven by `RestorePreview` counts before destructive apply
- Rejected CSV payloads before restore and surfaced clear error messaging
- Added `BackupRestoreViewModelTest` for create / pick / preview / confirm paths
- Added `DataManagementBackupRestoreTest` for UI action separation and copy checks
- Hardened `.obackup` decoding to tolerate BOM / whitespace / trailing null bytes from SAF-backed document providers
- Updated restore success flow to jump the UI to a month contained in the restored backup instead of leaving the user on a stale month
- Wrapped destructive restore in a real Room transaction via `AppContainer` + `database.withTransaction`

## Verification

- ✅ `./gradlew.bat :app:assembleDebug`
- ✅ `./gradlew.bat :app:testDebugUnitTest`
- ⚠ `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.peter.overtimecalculator.DataManagementBackupRestoreTest` compiled the test APK but could not run because there is no connected device in this environment
- ✅ Follow-up regression: app-generated backup content can be previewed by the same restore flow in `BackupRestoreViewModelTest`

## Files Created/Modified

- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppEffects.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppShell.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeNavigation.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/Screens.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/DataManagementScreen.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/DataManagementSections.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsGraphContract.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsRouteEntries.kt`
- `app/src/test/java/com/peter/overtimecalculator/BackupRestoreViewModelTest.kt`
- `app/src/androidTest/java/com/peter/overtimecalculator/DataManagementBackupRestoreTest.kt`

## Issues Encountered

- The original 03-04 executor was interrupted by network / certificate failures and left partial UI work in the working tree.
- The interrupted partial implementation used unsafe effect logic (`GlobalScope`) and had no tests; it was replaced with a stable, test-backed implementation.
- Android instrumentation execution is still environment-blocked without a connected device, so that verification remains pending for a device-enabled run.
- Manual testing then exposed a restore failure on device before the confirmation dialog. Follow-up fixes hardened JSON parsing for document-provider content, made restore transactional, and switched the UI to a restored month after success so restore does not look like a no-op.
- Additional manual testing exposed that auto-materialized future config months could pull the post-restore landing month forward. Follow-up fixes changed landing-month selection to prefer the latest meaningful business month (entries/overrides, then locked config months) instead of the latest config row.

## Next Phase Readiness

- Phase 3 execution is complete from a code perspective.
- Final phase-level verification should decide whether the missing on-device androidTest run is a human-needed check or a remaining gap.

---
*Phase: 03-data-backup-and-restore*
*Completed: 2026-03-15*
