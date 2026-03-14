# Testing

## Test Layout
- JVM unit tests live in `app/src/test/java/com/peter/overtimecalculator/`.
- Instrumentation and Compose UI tests live in `app/src/androidTest/java/com/peter/overtimecalculator/`.
- The main instrumentation entry is `app/src/androidTest/java/com/peter/overtimecalculator/MainFlowTest.kt`.

## Declared Frameworks
- JUnit 4 is declared in `app/build.gradle.kts`.
- Coroutine testing support comes from `kotlinx-coroutines-test` in `app/build.gradle.kts`.
- Android-aware JVM tests use Robolectric from `app/build.gradle.kts`.
- AndroidX test core and arch core testing are also declared in `app/build.gradle.kts`.
- Instrumentation tests use AndroidX JUnit, Espresso, and Compose UI test support from `app/build.gradle.kts`.

## What Is Covered
- Core overtime math and business rules are covered in `app/src/test/java/com/peter/overtimecalculator/DomainLogicTest.kt`.
- Write-path validation and persistence-facing use cases are covered in `app/src/test/java/com/peter/overtimecalculator/WriteUseCaseTest.kt`.
- Holiday parsing and repository behavior are covered in `app/src/test/java/com/peter/overtimecalculator/HolidayHaoshenqiApiParserTest.kt`, `app/src/test/java/com/peter/overtimecalculator/HolidayRulesJsonParserTest.kt`, and `app/src/test/java/com/peter/overtimecalculator/HolidayRulesRepositoryTest.kt`.
- Update-related behavior is covered in `app/src/test/java/com/peter/overtimecalculator/UpdateLogicTest.kt`.
- Theme and appearance logic are covered in `app/src/test/java/com/peter/overtimecalculator/ThemePaletteSpecTest.kt` and `app/src/test/java/com/peter/overtimecalculator/ThemeOverviewStateTest.kt`.

## UI Flow Coverage
- `app/src/androidTest/java/com/peter/overtimecalculator/MainFlowTest.kt` covers home screen visibility, settings navigation, hourly rate workflows, reverse-rate flow, multiplier editing, data export entry, about/update entry, and theme settings.
- The instrumentation suite depends on `testTag` coverage in production composables.
- Device preparation in `MainFlowTest.kt` uses shell commands to wake/unlock the device before running flows.

## Test Design Patterns
- Domain tests use explicit local fixtures and helper builders rather than mocking frameworks.
- Repository and parser tests appear to favor realistic input data and direct assertions.
- Compose UI tests query by semantic tags and visible text instead of internal implementation details.

## Commands
- Local test command documented in `README.md`: `./gradlew testDebugUnitTest`.
- Debug build command documented in `README.md`: `./gradlew assembleDebug`.
- Instrumentation execution is implied by the `androidTest` source set but is not documented in detail in `README.md`.

## CI Coverage
- `.github/workflows/ci.yml` runs `testDebugUnitTest` and `assembleDebug` only.
- `.github/workflows/release.yml` also runs `testDebugUnitTest` before packaging a release APK.
- No GitHub workflow currently runs `connectedAndroidTest` or emulator-backed Compose instrumentation tests.

## Gaps And Risks
- UI flow regressions can slip through CI because `MainFlowTest.kt` is not part of the GitHub verification path.
- Large coordination files like `app/src/main/java/com/peter/overtimecalculator/ui/Screens.kt` would benefit from more focused UI or presentation-level tests.
- Update/install flows in `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateManager.kt` depend on Android system services and are harder to validate exhaustively with the current visible test mix.
