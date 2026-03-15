# Coding Conventions

**Analysis Date:** 2026-03-15

## Source-of-Truth Signals

**Explicit config found:**
- `gradle.properties` sets `kotlin.code.style=official`
- No `.editorconfig`, Detekt, Ktlint, Spotless, lint XML, or coverage config was found in the repository root

**Practical implication:**
- Formatting and style are driven mainly by existing Kotlin code patterns rather than an external enforcement toolchain

## Naming Patterns

**Files and types:**
- Entry points at package root: `MainActivity.kt`, `OvertimeApplication.kt`, `AppDependencies.kt`
- View models use `*ViewModel`: `OvertimeViewModel.kt`, `AppUpdateViewModel.kt`
- Screens use `*Screen` or `*Sheet`: `HomeScreen.kt`, `ThemeSettingsScreen.kt`, `DayEditorSheet.kt`
- Settings support files use `*Sections`, `*Graphs`, `*Contract`, `*RouteEntries`
- Database classes use Room-oriented suffixes: `AppDatabase.kt`, `OvertimeDao.kt`, `Entities.kt`
- Domain helpers use descriptive nouns: `Calculators.kt`, `Validation.kt`, `ConfigPropagationPlanner.kt`

**Members:**
- Functions use camelCase verbs such as `saveOvertime`, `updateMultipliers`, `refreshRemoteRules`, `checkLatestRelease`
- Private mutable state in view models commonly uses underscore-prefixed backing properties such as `_events`
- Enum constants use screaming snake case, for example `WORKDAY`, `REST_DAY`, `HOLIDAY`

## Layering Conventions

**Package split:**
- `ui/` holds Compose screens, navigation, and Android ViewModels
- `domain/` holds calculations, validators, models, and write-side interfaces/use cases
- `data/` holds persistence, update flow, holiday sync, and repository implementations

**Dependency style:**
- Manual wiring through `app/src/main/java/com/peter/overtimecalculator/data/AppContainer.kt`
- No Hilt or Dagger setup is present
- `app/src/main/java/com/peter/overtimecalculator/AppDependencies.kt` exposes ViewModel factory helpers from the application container

## State Management Conventions

**Reactive state:**
- View models expose `StateFlow` to Compose, for example `OvertimeViewModel.uiState` and `AppUpdateViewModel.uiState`
- Internal state uses `MutableStateFlow` and `Channel`
- Derived UI state is built with `combine`, `flatMapLatest`, and `stateIn`
- Lifecycle-aware collection happens in Compose via `collectAsStateWithLifecycle`

**State hoisting in Compose:**
- Business state stays in view models instead of local composables
- Screens receive state objects and callback lambdas rather than mutating shared global state directly

## Error Handling Conventions

**Business validation:**
- `app/src/main/java/com/peter/overtimecalculator/domain/Validation.kt` defines `DomainResult.Success` / `DomainResult.Failure`
- Validators return user-facing Chinese failure messages for expected domain errors
- `domainResultOf` converts `IllegalArgumentException` into `DomainResult.Failure`

**Remote and system boundaries:**
- Remote clients use explicit timeouts and defensive parsing, for example `HolidayRemoteClient.kt` and `UpdateReleaseChecker.kt`
- Update checks use `runCatching` and return UI-safe failure messages rather than throwing through the UI layer
- Background refresh distinguishes `Updated`, `Skipped`, and `Failed(retryable)` through `HolidayRefreshResult`

## Compose / UI Conventions

**Navigation:**
- Root navigation starts in `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeNavigation.kt`
- Settings uses a nested graph under `app/src/main/java/com/peter/overtimecalculator/ui/settings/`

**Semantics and testability:**
- Compose UI uses `Modifier.testTag(...)` for test hooks
- Reusable components expose semantics such as `contentDescription` and `stateDescription`, notably in `ui/components/CenteredDurationSlider.kt`

**Theme organization:**
- Theme code is isolated under `app/src/main/java/com/peter/overtimecalculator/ui/theme/`
- Seed-color palette data lives in `ThemePaletteSpec.kt`

## Testing-Driven Quality Habits Seen In Code

**Pure logic first:**
- Mapping and calculator helpers are isolated into testable functions and objects, for example `DurationSliderMapping.kt`, `Calculators.kt`, and `Validation.kt`

**Manual fakes over mocking frameworks:**
- Tests use local fake implementations instead of MockK or Mockito

## Logging And Comments

**Logging:**
- No dedicated logging framework is configured
- No `println(` usage was found in Kotlin sources during verification

**Comments/KDoc:**
- Most production files rely on naming and structure instead of heavy inline commentary
- Documentation is concentrated in repo docs such as `README.md` and `docs/storage-boundaries.md`

## Constraints Worth Preserving

**Repository-level conventions documented elsewhere:**
- `README.md` documents the current single-module, single-activity, manual-DI posture
- `docs/storage-boundaries.md` documents where Room, DataStore, SharedPreferences, and file outputs are allowed to grow

**When adding new code:**
- Match the existing package split instead of inventing parallel feature directories
- Prefer extending existing repositories/use cases/view models before introducing a second architectural pattern

---

*Convention analysis: 2026-03-15*
