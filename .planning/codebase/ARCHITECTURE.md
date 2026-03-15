# Architecture

**Analysis Date:** 2026-03-15

## Pattern Overview

**Overall:** Single-module layered Android app with MVVM-style presentation, manual dependency wiring, and Compose UI

**Key Characteristics:**
- Single Activity with Jetpack Compose (all UI is composable functions)
- Manual dependency injection via `AppContainer` (no Hilt/Dagger)
- Reactive data flow using Kotlin Flows and StateFlow
- Repository pattern for data access abstraction
- Use Cases for business logic encapsulation

## Layers

**UI Layer:**
- Purpose: Handle all Compose UI and user interactions
- Location: `app/src/main/java/com/peter/overtimecalculator/ui/`
- Contains: Screens (`HomeScreen.kt`, `DayEditorSheet.kt`), ViewModels (`OvertimeViewModel.kt`), components (`components/`, `theme/`)
- Depends on: Domain models/use cases plus application-provided dependencies via ViewModels
- Used by: MainActivity

**Domain Layer:**
- Purpose: Business logic, calculations, and domain models
- Location: `app/src/main/java/com/peter/overtimecalculator/domain/`
- Contains: Models (`Models.kt`), calculators (`Calculators.kt`), use cases (`WriteUseCases.kt`), validation (`Validation.kt`)
- Depends on: None (pure Kotlin)
- Used by: UI layer and data-layer implementations

**Data Layer:**
- Purpose: Data persistence and external service integration
- Location: `app/src/main/java/com/peter/overtimecalculator/data/`
- Contains: Database (`db/`), repositories (`repository/`), holiday data (`holiday/`), app updates (`update/`)
- Depends on: Domain layer (for models)
- Used by: Application/container wiring and Android-facing features

## Data Flow

**Overtime Entry Flow:**

1. User taps calendar day → `HomeScreen` → `onDayClick(date)`
2. `OvertimeViewModel.openEditor(date)` → opens `DayEditorSheet`
3. User enters duration and saves → `saveOvertime()` in ViewModel
4. ViewModel calls `saveOvertimeUseCase(date, minutes, overrideDayType)`
5. Use Case calls `OvertimeRepository.saveOvertime()`
6. Repository writes to Room database in a transaction
7. Repository's `observeMonth()` Flow emits new data
8. ViewModel's `observedMonth` StateFlow updates
9. UI recomposes via `uiState` StateFlow

**State Management:**
- `OvertimeViewModel.uiState`: StateFlow<AppUiState> - primary UI state
- `OvertimeViewModel.events`: Flow<UiEvent> backed by an internal Channel - one-time events such as snackbars and share/install actions
- `OvertimeRepository.observeMonth()`: Flow<ObservedMonth> - reactive data from Room
- Appearance preferences via `AppearancePreferencesRepository.snapshot`: StateFlow

## Key Abstractions

**OvertimeWriteGateway:**
- Purpose: Interface for all write operations to overtime data
- Examples: `app/src/main/java/com/peter/overtimecalculator/domain/WriteUseCases.kt` uses this
- Pattern: Strategy pattern - `OvertimeRepository` implements this interface

**HolidayCalendar:**
- Purpose: Resolves day types considering holidays, user overrides, and weekends
- Examples: Used by `MonthlyOvertimeCalculator`, `ReverseHourlyRateCalculator`, and `OvertimeRepository`
- Pattern: Pure domain service

**AppContainer:**
- Purpose: Manual DI container - provides all dependencies
- Examples: `app/src/main/java/com/peter/overtimecalculator/data/AppContainer.kt`
- Pattern: Service Locator / manual DI

## Entry Points

**Application:**
- Location: `app/src/main/java/com/peter/overtimecalculator/OvertimeApplication.kt`
- Triggers: Android app launch
- Responsibilities: Initialize `AppContainer`, schedule holiday sync, refresh holiday rules

**MainActivity:**
- Location: `app/src/main/java/com/peter/overtimecalculator/MainActivity.kt`
- Triggers: Activity creation
- Responsibilities: Set up Compose content, create ViewModels with factory, apply theme, render `OvertimeCalculatorApp`

**Navigation:**
- Location: `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeNavigation.kt`
- Uses: Navigation Compose with NavHost
- Routes: `HomeRoute` = "home", settings graph = "settings_graph"

## Error Handling

**Strategy:** Result objects for expected domain errors plus guarded remote/system boundaries

**Patterns:**
- `DomainResult<T>`: Sealed class with `Success<T>` and `Failure(message)` - used in use cases
- `HolidayRefreshResult`: Sealed interface for holiday sync results
- ViewModels translate failures into UI events such as `UiEvent.ShowSnackbar`
- Room transactions with rollback on failure

## Cross-Cutting Concerns

**Logging:** Not actively used in codebase (console logging absent)

**Validation:**
- Input validation in ViewModel before calling use cases
- Domain validation via `Validation.kt`
- Database constraints via Room entities

**Authentication:** Not applicable (local-only app, no auth)

**Theme/Appearance:**
- Handled in `OvertimeViewModel` via appearance preferences StateFlow
- Theme applied in `MainActivity` using `OvertimeCalculatorTheme` composable
- Theme settings stored in `AppearancePreferencesRepository` (SharedPreferences)

---

*Architecture analysis: 2026-03-15*
