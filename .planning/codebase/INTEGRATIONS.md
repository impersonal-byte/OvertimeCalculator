# External Integrations

**Analysis Date:** 2026-03-15

## APIs & External Services

**Holiday data API:**
- Service: `https://api.haoshenqi.top/holiday?date=%d`
- Call site: `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesRepository.kt`
- HTTP client: `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRemoteClient.kt`
- Parsing: `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayHaoshenqiApiParser.kt`
- Auth: none
- Purpose: fetch current year and next year holiday overrides, then merge them over the bundled baseline asset

**GitHub Releases API:**
- Service: `https://api.github.com/repos/impersonal-byte/OvertimeCalculator/releases/latest`
- Call site: `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateReleaseChecker.kt`
- Headers:
  - `Accept: application/vnd.github+json`
  - `X-GitHub-Api-Version: 2022-11-28`
  - `User-Agent: OvertimeCalculator/{currentVersionName}`
- Auth: none
- Purpose: check whether a newer APK release exists and extract release notes plus download URL

**GitHub release asset downloads:**
- Download target comes from the release asset selected in `app/src/main/java/com/peter/overtimecalculator/domain/AppUpdateModels.kt`
- Download orchestration lives in `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateDownloadGateway.kt`
- APK installation handoff lives in `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateInstallGateway.kt`

## Local Platform Integrations

**Room / SQLite:**
- Database definition: `app/src/main/java/com/peter/overtimecalculator/data/db/AppDatabase.kt`
- DAO: `app/src/main/java/com/peter/overtimecalculator/data/db/OvertimeDao.kt`
- Entities: `app/src/main/java/com/peter/overtimecalculator/data/db/Entities.kt`
- Exported schema directory: `app/schemas/com.peter.overtimecalculator.data.db.AppDatabase/`
- Purpose: persist monthly config, overtime entries, and manual holiday overrides

**DataStore Preferences:**
- Implementation: `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesRepository.kt`
- Keys:
  - `remote_json`
  - `fetched_at_epoch_millis`
  - `remote_updated_at`
- Purpose: cache remote holiday overlay JSON and refresh metadata

**SharedPreferences:**
- Appearance preferences: `app/src/main/java/com/peter/overtimecalculator/data/AppearancePreferencesRepository.kt`
- Update session state: `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateSessionStore.kt`
- Purpose: keep theme/calendar preferences and APK download/install session state

**WorkManager:**
- Worker: `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidaySyncWorker.kt`
- Startup scheduling: `app/src/main/java/com/peter/overtimecalculator/OvertimeApplication.kt`
- Purpose: enqueue periodic holiday refresh work with network constraint

**Android system services and framework hooks:**
- `DownloadManager`: `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateManager.kt`, `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateDownloadGateway.kt`
- `FileProvider`: `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/file_paths.xml`
- Install permission flow: `android.permission.REQUEST_INSTALL_PACKAGES` in `app/src/main/AndroidManifest.xml`

## CI/CD & Repository Automation

**Manual verification workflow:**
- File: `.github/workflows/ci.yml`
- Trigger: `workflow_dispatch`
- Steps:
  - run `testDebugUnitTest`
  - build debug APK with `assembleDebug`
  - run `MainSmokeTest` on an emulator via `reactivecircus/android-emulator-runner@v2`

**Release workflow:**
- File: `.github/workflows/release.yml`
- Trigger: push tag `v*` or manual dispatch
- Responsibilities:
  - validate signing secrets exist
  - generate CI `local.properties`
  - validate Git tag matches `appVersionName`
  - run unit tests
  - build signed release APK via `packageReleaseApk`
  - publish GitHub Release
  - sync version metadata back into `README.md`

**Release secrets referenced by workflow:**
- `SIGNING_KEY`
- `ALIAS`
- `KEY_STORE_PASSWORD`
- `KEY_PASSWORD`

## Authentication & Identity

**Application auth:**
- None found
- The app is local-first and does not define user accounts, login flows, or token exchange

## Incoming/Outgoing Boundaries

**Outgoing calls:**
- Holiday API requests to `api.haoshenqi.top`
- GitHub API requests to `api.github.com`
- APK download requests through `DownloadManager`

**Incoming callbacks / webhooks:**
- None found in repository code
- No server endpoints, webhook handlers, or background push receivers are defined

## Configuration Surface

**Local development:**
- `local.properties` must provide `sdk.dir`
- Release signing values can also live in `local.properties` for local packaging

**Build configuration:**
- Root plugins: `build.gradle.kts`
- App module config and dependency versions: `app/build.gradle.kts`
- Repository settings: `settings.gradle.kts`
- Gradle flags: `gradle.properties`

---

*Integration audit: 2026-03-15*
