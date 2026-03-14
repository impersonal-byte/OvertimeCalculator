# Concerns

## 1. Sensitive Signing Material Appears In The Repo
- Repo root files `OvertimeCalculator.jks` and `OvertimeCalculator.jks.base64.txt` look like release-signing artifacts.
- `.github/workflows/release.yml` expects signing secrets to be provided securely at runtime, so checked-in signing material is a major operational and security concern.
- Even if these files are no longer active, their presence increases accidental exposure risk and makes repository hygiene harder.

## 2. UI Coordination Is Highly Centralized
- `app/src/main/java/com/peter/overtimecalculator/ui/Screens.kt` is very large and combines navigation, home UI, editor behavior, event collection, CSV share flow, and presentation logic.
- This makes seemingly small UI changes more likely to create merge conflicts or regress adjacent behavior.
- The file is also harder to test in focused units than smaller screen-specific components.

## 3. Manual Dependency Wiring Does Not Scale Gracefully
- `app/src/main/java/com/peter/overtimecalculator/data/AppContainer.kt` is clean for the current size, but every new dependency threads through manual construction.
- `MainActivity.kt`, `OvertimeViewModel.kt`, and `AppUpdateViewModel.kt` also carry factory boilerplate because there is no DI framework.
- This is not broken today, but it increases friction as features and test seams grow.

## 4. Networking And Install Paths Are Hand-Rolled
- `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateManager.kt` uses raw `HttpURLConnection`, manual JSON parsing, `DownloadManager`, and install-permission routing.
- `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesRepository.kt` also performs raw HTTP calls.
- These are legitimate choices for a small app, but they concentrate failure handling, retry behavior, and API drift risk in custom code.

## 5. Persistence Strategy Is Split Across Three Mechanisms
- Room is used for main business data in `app/src/main/java/com/peter/overtimecalculator/data/db/`.
- DataStore is used for holiday cache metadata in `HolidayRulesRepository.kt`.
- `SharedPreferences` is still used in `OvertimeViewModel.kt` and `UpdateManager.kt`.
- The split is understandable, but future changes need clear rules to avoid accidental duplication or partial migrations.

## 6. Database Migration Visibility Is Limited
- `app/src/main/java/com/peter/overtimecalculator/data/db/AppDatabase.kt` sets `exportSchema = false`.
- That reduces schema-history visibility and makes future migration review or debugging harder than it needs to be.
- The project already has at least one explicit migration, so schema tracking matters.

## 7. CI Does Not Cover Instrumentation Flows
- `.github/workflows/ci.yml` only runs unit tests and a debug build.
- `.github/workflows/release.yml` also stops at unit tests before packaging.
- `app/src/androidTest/java/com/peter/overtimecalculator/MainFlowTest.kt` covers important navigation and settings flows, but those checks are not part of automated remote verification.

## 8. Repo Hygiene Looks Mixed
- Repo root includes build outputs and logs such as `build.log`, `build2.log`, `manifest_error.log`, `manifest_utf8.log`, and `hs_err_pid*.log`.
- The checked-in `android-sdk/` directory also suggests local tooling may be living inside the repo.
- None of this breaks the app directly, but it increases noise and can make source-of-truth boundaries less clear.

## 9. Documentation And Runtime Need To Stay In Sync
- `README.md` is generally strong, but operational details like signing, update flow, storage split, and test coverage can drift as the app evolves.
- The new `.planning/codebase/*.md` docs should be refreshed after changes to build, release, storage, or navigation structure.
