# Codebase Concerns

**Analysis Date:** 2026-03-15

## Intentional Tradeoffs To Keep In Mind

**Single module and manual DI are deliberate for now:**
- `README.md` states the app remains a single `:app` module and keeps a handwritten `AppContainer`
- Relevant files: `README.md`, `app/src/main/java/com/peter/overtimecalculator/data/AppContainer.kt`
- Concern: future features must work with manual wiring and a growing composition root unless the project intentionally revisits that decision

**SharedPreferences still carries state that docs already want to contain:**
- Appearance settings remain in `app/src/main/java/com/peter/overtimecalculator/data/AppearancePreferencesRepository.kt`
- Update session state remains in `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateSessionStore.kt`
- `docs/storage-boundaries.md` explicitly says new preference-style growth should prefer DataStore and that SharedPreferences should not keep expanding

## Operational Risks

**Holiday remote refresh failures are not surfaced to the user interface:**
- `OvertimeApplication.kt` triggers `refreshIfStale()` at startup and ignores `HolidayRefreshResult.Failed`
- `HolidayRulesRepository.kt` catches remote fetch problems and returns `Failed(retryable = true)`
- `HolidaySyncWorker.kt` retries or fails background work, but there is no matching UI notification path
- Impact: the app can silently fall back to stale/baseline holiday data without an in-app explanation

**Holiday rules depend on a third-party public API:**
- Remote source: `https://api.haoshenqi.top/holiday?date=%d`
- Files: `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesRepository.kt`, `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRemoteClient.kt`
- Mitigation already present: bundled baseline asset at `app/src/main/assets/holidays/cn_mainland.json`
- Concern: format or availability changes in the remote API can still affect freshness of rule updates

**In-app update flow depends on several Android/platform boundaries at once:**
- Release check: `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateReleaseChecker.kt`
- Download/install orchestration: `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateManager.kt`, `UpdateDownloadGateway.kt`, `UpdateInstallGateway.kt`
- Manifest permissions/hooks: `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/file_paths.xml`
- Concern: this feature crosses GitHub API, DownloadManager, FileProvider, and install-permission state, so regressions can span multiple layers

## Maintainability / Scaling Pressure

**Month observation recalculates from broad upstream flows:**
- `app/src/main/java/com/peter/overtimecalculator/data/repository/OvertimeRepository.kt:39` combines all configs, current-range entries, current-range overrides, and holiday rules
- Concern: any upstream change forces month recomputation, which is simple now but becomes more expensive as history and feature surface grow

**Config resolution scans the full config history:**
- `app/src/main/java/com/peter/overtimecalculator/data/repository/OvertimeRepository.kt:174` sorts and searches all configs to resolve the active month
- `ensureMaterializedConfig()` also fetches all configs inside transactions
- Concern: this is acceptable for small local history, but it is not optimized for long-running datasets

**Release hardening is currently lightweight:**
- `app/build.gradle.kts` sets `isMinifyEnabled = false` for release
- `app/proguard-rules.pro` is effectively empty
- Concern: release APKs currently skip shrinking/obfuscation and do not yet use hardened keep-rule tuning

**Update session store is instantiated repeatedly:**
- `AndroidUpdateManager` creates separate `SharedPreferencesUpdateSessionStore.create(...)` instances for its own field, the download gateway, and the install gateway
- File: `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateManager.kt`
- Concern: all instances point at the same prefs file, so behavior is consistent today, but the wiring is easy to drift or duplicate further

## Testing Gaps Seen During Mapping

**No direct tests were found for several high-leverage classes:**
- Search found no direct test references for `OvertimeRepository`, `OvertimeViewModel`, or `Migration1To2`
- Evidence: repository-wide grep over `app/src/test/**/*.kt`
- `ConfigPropagationPlanner` is directly exercised in `app/src/test/java/com/peter/overtimecalculator/DomainLogicTest.kt`
- Concern: write-path, state-transition, and migration behavior are important enough that indirect coverage may be thin

**Core screens are exercised more by flow tests than by screen-focused test files:**
- Existing Android tests include `MainSmokeTest.kt`, `MainFlowTest.kt`, and `DayEditorSliderTest.kt`
- No direct Android test matches were found for `HomeScreen`, `DayEditorSheet`, `ThemeSettingsScreen`, or `SettingsMainScreen`
- Concern: full-flow tests cover important paths, but isolated screen regressions may still slip through

## Storage Boundary Risks Already Documented In Repo Docs

**Backup and migration behavior spans multiple storage systems:**
- `docs/storage-boundaries.md` notes that Room, DataStore, SharedPreferences, and files may all participate in backup/device transfer
- Manifest backup config is enabled in `app/src/main/AndroidManifest.xml`
- Concern: future storage migrations must consider restore/duplicate-state behavior, not only local reads and writes

**SharedPreferences migration work is still pending:**
- `docs/storage-boundaries.md` explicitly recommends a future migration of appearance preferences to a dedicated DataStore repository
- Concern: new preference-related work should avoid deepening the current legacy boundary

**Release metadata in docs is currently out of sync with app build config:**
- `README.md` still shows release metadata for `v2.0.0` / version code `13`
- `app/build.gradle.kts` currently defines `appVersionName = "2.1.0"` and `appVersionCode = 14`
- Concern: readers cross-checking planning docs against repo docs may see a version mismatch until the next release sync updates `README.md`

## Safe Areas To Treat As Baseline

**Not a confirmed bug list:**
- This document only records grounded risks and tradeoffs seen during mapping
- It intentionally avoids claiming crashes or broken behavior unless a concrete failing path was verified in source or tests

---

*Concerns audit: 2026-03-15*
