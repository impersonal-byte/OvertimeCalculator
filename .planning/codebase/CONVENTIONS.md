# Conventions

## Naming And File Patterns
- Types use PascalCase with explicit suffixes like `ViewModel`, `Repository`, `UseCase`, `Worker`, `Manager`, `Entity`, and `Dao`.
- Examples include `OvertimeViewModel.kt`, `AppUpdateViewModel.kt`, `OvertimeRepository.kt`, `HolidaySyncWorker.kt`, and `OvertimeDao.kt`.
- Settings screens follow `*Screen.kt` naming in `app/src/main/java/com/peter/overtimecalculator/ui/settings/`.
- Navigation route constants are grouped in `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsGraphs.kt`.

## Package Organization
- The repo follows a coarse layer split: `data`, `domain`, and `ui`.
- Infrastructure details stay in `data/**`.
- Business logic and validation stay in `domain/**`.
- State holders and composables stay in `ui/**`.

## State Management
- Long-lived screen state is kept in `MutableStateFlow` and exposed as `StateFlow`, as seen in `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt` and `app/src/main/java/com/peter/overtimecalculator/ui/AppUpdateViewModel.kt`.
- Derived UI state is built with `combine(...)` and `stateIn(...)`.
- One-off UI actions use a `Channel`, then are collected by Compose in `app/src/main/java/com/peter/overtimecalculator/ui/Screens.kt`.
- Compose-local transient form state uses `remember` and `rememberSaveable` in `Screens.kt` and settings screens.

## Error And Result Handling
- Domain validation prefers explicit result types via `DomainResult`, visible in `app/src/main/java/com/peter/overtimecalculator/domain/WriteUseCases.kt`.
- User-facing failures are usually converted into snackbar messages in view models rather than thrown up the stack.
- Remote and IO integrations often use `runCatching` or `try/catch` at the boundary, as seen in `HolidayRulesRepository.kt` and `UpdateManager.kt`.

## Persistence Patterns
- Room entities map closely to domain models through extension functions in `app/src/main/java/com/peter/overtimecalculator/data/db/Entities.kt`.
- Repository writes are wrapped in `database.withTransaction` in `app/src/main/java/com/peter/overtimecalculator/data/repository/OvertimeRepository.kt`.
- The project uses both DataStore and `SharedPreferences`, but for different responsibilities rather than a full migration.

## UI Composition Patterns
- `app/src/main/java/com/peter/overtimecalculator/ui/Screens.kt` acts as the home-screen shell and event bridge.
- Settings navigation is nested under a dedicated graph in `SettingsGraphs.kt`.
- UI tests rely heavily on semantic tags through `Modifier.testTag(...)`, visible across `Screens.kt`, `SettingsMainScreen.kt`, `PreferencesScreen.kt`, and `ThemeSettingsScreen.kt`.
- Theme selection and app appearance are modeled explicitly through `AppTheme`, `SeedColor`, and `ThemePaletteSpec` types under `app/src/main/java/com/peter/overtimecalculator/ui/theme/`.

## Formatting Tendencies
- Kotlin formatting generally follows the official style indicated by `gradle.properties`.
- Trailing commas are commonly used in multiline calls and declarations.
- Functions are usually small and named for intent, but some coordination files are large.
- Comments are sparse; code usually relies on descriptive identifiers instead.

## Testing Style
- JVM tests use plain JUnit assertions and handwritten fixtures, as seen in `app/src/test/java/com/peter/overtimecalculator/DomainLogicTest.kt`.
- The test suite appears to prefer handwritten fakes and real parsers over mocking frameworks.
- Instrumentation tests use Compose test APIs and semantic tags in `app/src/androidTest/java/com/peter/overtimecalculator/MainFlowTest.kt`.

## Mixed Or Evolving Patterns
- Manual dependency injection through `AppContainer.kt` is consistent, but it increases boilerplate around `provideFactory(...)` in view models.
- `Screens.kt` is much more monolithic than most other files, so the repo is partly layered and partly centralized.
- Storage conventions are split between Room, DataStore, and `SharedPreferences`, which is workable but should be documented carefully.
