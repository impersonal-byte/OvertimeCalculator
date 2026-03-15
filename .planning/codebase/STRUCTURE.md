# Codebase Structure

**Analysis Date:** 2026-03-15

## Directory Layout

```
D:\Android.calculator\
├── app/src/main/java/com/peter/overtimecalculator/
│   ├── MainActivity.kt           # Single Activity entry point
│   ├── OvertimeApplication.kt    # Application class
│   ├── AppDependencies.kt         # ViewModel factory
│   ├── data/                      # Data layer
│   ├── domain/                    # Domain layer
│   └── ui/                        # UI layer
└── app/src/main/res/              # Android resources
```

## Directory Purposes

**Root Package (`com.peter.overtimecalculator`):**
- Purpose: Entry points and root-level configuration
- Contains: `MainActivity.kt`, `OvertimeApplication.kt`, `AppDependencies.kt`
- Key files: `app/src/main/java/com/peter/overtimecalculator/MainActivity.kt`

**Data Layer (`data/`):**
- Purpose: Persistence, external APIs, data repository implementation
- Contains: Database, repositories, holiday sync, app updates
- Key files:
  - `app/src/main/java/com/peter/overtimecalculator/data/AppContainer.kt` - DI container
  - `app/src/main/java/com/peter/overtimecalculator/data/repository/OvertimeRepository.kt` - Main repository

**Database Subdirectory (`data/db/`):**
- Purpose: Room database and entities
- Contains: Database class, DAOs, entity classes, converters
- Key files:
  - `app/src/main/java/com/peter/overtimecalculator/data/db/AppDatabase.kt`
  - `app/src/main/java/com/peter/overtimecalculator/data/db/Entities.kt`
  - `app/src/main/java/com/peter/overtimecalculator/data/db/OvertimeDao.kt`

**Holiday Subdirectory (`data/holiday/`):**
- Purpose: Holiday data management and API sync
- Contains: Repository, remote client, JSON parser, sync worker
- Key files:
  - `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesRepository.kt`

**Update Subdirectory (`data/update/`):**
- Purpose: App update management
- Contains: Update manager, release checker, download/install gateways
- Key files:
  - `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateManager.kt`

**Domain Layer (`domain/`):**
- Purpose: Pure business logic, models, and use cases
- Contains: Models, calculators, use cases, validation
- Key files:
  - `app/src/main/java/com/peter/overtimecalculator/domain/Models.kt` - Domain models
  - `app/src/main/java/com/peter/overtimecalculator/domain/Calculators.kt` - Calculation logic
  - `app/src/main/java/com/peter/overtimecalculator/domain/WriteUseCases.kt` - Write operations

**UI Layer (`ui/`):**
- Purpose: All Compose UI and presentation logic
- Contains: Screens, ViewModels, components, theme, settings
- Key files:
  - `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt` - Main ViewModel
  - `app/src/main/java/com/peter/overtimecalculator/ui/HomeScreen.kt` - Main screen
  - `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeNavigation.kt` - Navigation setup

**UI Components (`ui/components/`):**
- Purpose: Reusable Compose UI components
- Contains: `CenteredDurationSlider.kt`, `DurationSliderMapping.kt`

**UI Theme (`ui/theme/`):**
- Purpose: Compose theme configuration
- Contains: `Theme.kt`, `Color.kt`, `ThemePaletteSpec.kt`

**UI Settings (`ui/settings/`):**
- Purpose: Settings screens and related UI
- Contains: Settings screens, sections, ViewModels, navigation
- Key files:
  - `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsGraphs.kt` - Settings navigation
  - `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsMainScreen.kt`

## Key File Locations

**Entry Points:**
- `app/src/main/java/com/peter/overtimecalculator/MainActivity.kt`: Single Activity, Compose setup
- `app/src/main/java/com/peter/overtimecalculator/OvertimeApplication.kt`: App initialization

**Configuration:**
- `app/src/main/java/com/peter/overtimecalculator/data/AppContainer.kt`: Dependency injection container

**Core Logic:**
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt`: Main ViewModel (450 lines)
- `app/src/main/java/com/peter/overtimecalculator/data/repository/OvertimeRepository.kt`: Data access (194 lines)
- `app/src/main/java/com/peter/overtimecalculator/domain/Calculators.kt`: Business calculations (184 lines)

**Testing:**
- `app/src/test/java/com/peter/overtimecalculator/`: Unit tests
- `app/src/androidTest/java/com/peter/overtimecalculator/`: Android instrumentation tests

## Naming Conventions

**Files:**
- Screens: `*Screen.kt` (e.g., `HomeScreen.kt`, `SettingsMainScreen.kt`)
- ViewModels: `*ViewModel.kt` (e.g., `OvertimeViewModel.kt`, `AppUpdateViewModel.kt`)
- Sections: `*Sections.kt` (e.g., `HomeCalendarSections.kt`, `SettingsMainSections.kt`)
- Contracts: `*Contract.kt` (e.g., `SettingsGraphContract.kt`)
- Room entity classes are grouped in `app/src/main/java/com/peter/overtimecalculator/data/db/Entities.kt` rather than split into one `*Entity.kt` file per type

**Directories:**
- Singular names for feature directories: `data/`, `domain/`, `ui/`
- Plural for collections: `components/`, `settings/`

## Where to Add New Code

**New Feature (UI):**
- Implementation: `app/src/main/java/com/peter/overtimecalculator/ui/`
- Tests: `app/src/test/java/com/peter/overtimecalculator/` (unit) or `app/src/androidTest/java/com/peter/overtimecalculator/` (instrumented)

**New Data Source:**
- Implementation: `app/src/main/java/com/peter/overtimecalculator/data/`
- Repository: `app/src/main/java/com/peter/overtimecalculator/data/repository/`

**New Business Logic:**
- Implementation: `app/src/main/java/com/peter/overtimecalculator/domain/`
- Use Case: Create in `WriteUseCases.kt` or new file

**New Settings Screen:**
- Implementation: `app/src/main/java/com/peter/overtimecalculator/ui/settings/`
- Navigation: Add route in `SettingsGraphs.kt` and `SettingsRouteEntries.kt`

## Special Directories

**Assets:**
- Location: `app/src/main/assets/`
- Contains: `holidays/cn_mainland.json` - baseline holiday rules
- Generated: No (committed)
- Purpose: Pre-bundled holiday data

**Room Schema:**
- Location: `app/schemas/com.peter.overtimecalculator.data.db.AppDatabase/`
- Generated: Yes (by Room)
- Purpose: Database migration support

---

*Structure analysis: 2026-03-15*
