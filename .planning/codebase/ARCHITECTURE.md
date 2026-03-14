# Architecture

## High-Level Shape
- This is a single-module Android app with package-level layering rather than separate feature modules.
- The main package root is `app/src/main/java/com/peter/overtimecalculator/`.
- The most visible layers are `data`, `domain`, and `ui` under that package root.

## Startup Chain
- Android bootstraps through `app/src/main/AndroidManifest.xml`.
- The application class is `app/src/main/java/com/peter/overtimecalculator/OvertimeApplication.kt`.
- `OvertimeApplication` creates `AppContainer`, schedules periodic holiday sync, and performs a startup freshness refresh.
- The only activity entry point is `app/src/main/java/com/peter/overtimecalculator/MainActivity.kt`.
- `MainActivity` creates `OvertimeViewModel` and `AppUpdateViewModel` using manual factories, then renders `OvertimeCalculatorApp`.

## Composition Root
- `app/src/main/java/com/peter/overtimecalculator/data/AppContainer.kt` is the composition root.
- It wires Room, holiday rules, update manager, repository, calculators, and use cases.
- This project does not use Hilt, Dagger, or Koin; object graph assembly is manual.

## UI Layer
- The main app shell and navigation live in `app/src/main/java/com/peter/overtimecalculator/ui/Screens.kt`.
- Settings-specific navigation lives in `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsGraphs.kt`.
- The UI is single-activity and Compose-based, with a `NavHost` routing between home and a nested settings graph.
- Feature screens under `app/src/main/java/com/peter/overtimecalculator/ui/settings/` include theme, rules, preferences, data management, and about/update areas.

## ViewModel Layer
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt` owns the main app state.
- It combines repository data, selected month, editor state, and appearance preferences into a single `StateFlow<AppUiState>`.
- One-off UI events are emitted through a `Channel` and collected in `Screens.kt`.
- `app/src/main/java/com/peter/overtimecalculator/ui/AppUpdateViewModel.kt` separately manages update checking, download state, and install handoff.

## Domain Layer
- Business rules live under `app/src/main/java/com/peter/overtimecalculator/domain/`.
- Core calculations are concentrated in `app/src/main/java/com/peter/overtimecalculator/domain/Calculators.kt`.
- Validation and write orchestration use focused use-case wrappers such as `app/src/main/java/com/peter/overtimecalculator/domain/WriteUseCases.kt`.
- Domain models also capture monthly config, holiday rules, update state, and formatting helpers.

## Data Layer
- Room schema and access live in `app/src/main/java/com/peter/overtimecalculator/data/db/`.
- `app/src/main/java/com/peter/overtimecalculator/data/repository/OvertimeRepository.kt` bridges database state, holiday calendars, config propagation, and domain calculators.
- Holiday refresh logic lives in `app/src/main/java/com/peter/overtimecalculator/data/holiday/`.
- App update and APK install logic lives in `app/src/main/java/com/peter/overtimecalculator/data/update/`.

## Core Data Flow
- Main app flow is: Room flows + holiday rules -> `OvertimeRepository.observeMonth()` -> `OvertimeViewModel.uiState` -> Compose screens in `Screens.kt`.
- Write flow is: UI input -> `OvertimeViewModel` -> use case in `WriteUseCases.kt` -> `OvertimeRepository` transaction -> Room -> observed flows -> recomposed UI.
- Update flow is: About screen -> `AppUpdateViewModel` -> `UpdateManager.checkLatestRelease()` -> `DownloadManager` -> install intent.
- Holiday flow is: bundled asset + remote refresh -> `HolidayRulesRepository.rules` -> `HolidayCalendar` -> repository calculations -> UI summaries and day cells.

## Background And Lifecycle Behavior
- Periodic holiday refresh is a WorkManager job in `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidaySyncWorker.kt`.
- Foreground resume behavior is observed in `app/src/main/java/com/peter/overtimecalculator/ui/Screens.kt` so update install state can continue after permission changes.

## Architectural Characteristics
- The architecture is pragmatic and testable for a small app, but strongly centralized.
- `OvertimeViewModel.kt` and `Screens.kt` act as major coordination hubs.
- Package layering is clear, but there is still significant coupling through the shared `AppUiState` and manual dependency graph.
