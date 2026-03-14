# Structure

## Repository Layout
- Root Gradle files: `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, and `gradle/wrapper/gradle-wrapper.properties`.
- Main Android module: `app/`.
- Human-facing docs: `README.md` and `docs/**`.
- GitHub automation: `.github/workflows/ci.yml` and `.github/workflows/release.yml`.

## Module Map
- `settings.gradle.kts` includes only `:app`.
- There are no separate feature, library, or shared utility Gradle modules.
- Architectural boundaries are package-based inside the `app` module.

## App Source Sets
- Production code: `app/src/main/`.
- JVM unit tests: `app/src/test/`.
- Instrumentation and Compose UI tests: `app/src/androidTest/`.

## Main Source Layout
- Manifest: `app/src/main/AndroidManifest.xml`.
- Kotlin code root: `app/src/main/java/com/peter/overtimecalculator/`.
- Bundled assets: `app/src/main/assets/`.
- XML resources: `app/src/main/res/`.

## Package Breakdown
- `app/src/main/java/com/peter/overtimecalculator/data/` holds infrastructure and persistence wiring.
- `app/src/main/java/com/peter/overtimecalculator/data/db/` holds Room database, DAO, entities, and converters.
- `app/src/main/java/com/peter/overtimecalculator/data/repository/` holds the main repository boundary.
- `app/src/main/java/com/peter/overtimecalculator/data/holiday/` holds holiday refresh, parsing, serialization, and worker logic.
- `app/src/main/java/com/peter/overtimecalculator/data/update/` holds release-check and install flows.
- `app/src/main/java/com/peter/overtimecalculator/domain/` holds models, calculators, validators, planners, and use cases.
- `app/src/main/java/com/peter/overtimecalculator/ui/` holds app shell, view models, presentation helpers, and theming hooks.
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/` holds settings-area screens and routes.
- `app/src/main/java/com/peter/overtimecalculator/ui/theme/` holds color and theme definitions.

## Key Entry Files
- Application bootstrap: `app/src/main/java/com/peter/overtimecalculator/OvertimeApplication.kt`.
- Activity entry point: `app/src/main/java/com/peter/overtimecalculator/MainActivity.kt`.
- Main Compose shell: `app/src/main/java/com/peter/overtimecalculator/ui/Screens.kt`.
- Settings graph: `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsGraphs.kt`.

## Resource And Asset Layout
- Strings and theme XML live in `app/src/main/res/values/`.
- Backup and file-provider XML live in `app/src/main/res/xml/`.
- Launcher assets live in `app/src/main/res/drawable/` and `app/src/main/res/mipmap-anydpi-v26/`.
- Holiday baseline data lives in `app/src/main/assets/holidays/cn_mainland.json`.

## Tests And Docs
- Domain, repository, parser, theme, and update tests live in `app/src/test/java/com/peter/overtimecalculator/`.
- End-to-end UI flow coverage lives in `app/src/androidTest/java/com/peter/overtimecalculator/MainFlowTest.kt`.
- Feature and release documentation lives in `docs/theme-settings-spec.md` and `docs/releases/*.md`.

## Operational Files At Repo Root
- Local Android SDK and tooling appear under `android-sdk/`.
- Build output and logs appear under `build/`, `build.log`, `build2.log`, `build_utf8.log`, `manifest_error.log`, `manifest_utf8.log`, and `hs_err_pid*.log`.
- Signing-related files visible at root include `OvertimeCalculator.jks` and `OvertimeCalculator.jks.base64.txt`.

## Where New Work Likely Lands
- New domain rules should usually land under `app/src/main/java/com/peter/overtimecalculator/domain/`.
- Persistence changes should usually touch `app/src/main/java/com/peter/overtimecalculator/data/db/` and `app/src/main/java/com/peter/overtimecalculator/data/repository/`.
- New screens or settings subflows should usually land under `app/src/main/java/com/peter/overtimecalculator/ui/` or `app/src/main/java/com/peter/overtimecalculator/ui/settings/`.
- Release-process updates should usually land in `app/build.gradle.kts`, `README.md`, and `.github/workflows/release.yml`.
