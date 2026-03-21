# 04-02 Summary - Theme Foundation and HCT Migration

## Outcome

Wave 1 is completed: the app now has an HCT-driven palette generator, a centralized semantic `ThemeDefaults` surface, and dynamic-color default switched to OFF while keeping opt-in precedence behavior.

## What Changed

### 1) HCT-based palette generation
- Refactored `app/src/main/java/com/peter/overtimecalculator/ui/theme/ThemePaletteSpec.kt`.
- Replaced hardcoded per-palette `lightColorScheme`/`darkColorScheme` values with generated schemes from HCT (`com.google.android.material.color.utilities.Hct`).
- Kept `ThemePaletteSpec` shape compatible for settings usage (`displayName`, `swatchColors`, preview accents).
- Ensured dark background uses deep-gray layering rather than pure black.

### 2) Centralized semantic theme defaults
- Added `app/src/main/java/com/peter/overtimecalculator/ui/theme/ThemeDefaults.kt`.
- Introduced semantic role tokens (`pageBackground`, `navigationContainer`, `sectionContainer`, `cardContainer`, `accent`, `positiveTint`, etc.).
- Updated `app/src/main/java/com/peter/overtimecalculator/ui/theme/Theme.kt` to provide `LocalThemeDefaults` via `CompositionLocalProvider`.

### 3) Dynamic-color baseline policy
- Updated `app/src/main/java/com/peter/overtimecalculator/data/AppearancePreferencesRepository.kt`:
  - `loadUseDynamicColor()` default from `true` to `false`.
- Updated `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt`:
  - `AppUiState.empty().useDynamicColor` default from `true` to `false`.

### 4) Tests
- Updated `app/src/test/java/com/peter/overtimecalculator/ThemePaletteSpecTest.kt` with dark-layer and non-pure-black assertions.
- Added `app/src/test/java/com/peter/overtimecalculator/ThemeDefaultsTest.kt` for semantic-token invariants.
- Kept `app/src/test/java/com/peter/overtimecalculator/ThemeOverviewStateTest.kt` valid under the new baseline.

## Verification Evidence

Executed and passed:
- `./gradlew.bat :app:compileDebugKotlin`
- `./gradlew.bat :app:testDebugUnitTest --tests "com.peter.overtimecalculator.ThemePaletteSpecTest"`
- `./gradlew.bat :app:testDebugUnitTest --tests "com.peter.overtimecalculator.ThemeDefaultsTest"`
- `./gradlew.bat :app:testDebugUnitTest --tests "com.peter.overtimecalculator.ThemeOverviewStateTest"`
- `./gradlew.bat :app:assembleDebug`

## Notes

- This wave establishes theme infrastructure only; screen-level visual replacement is intentionally deferred to 04-03 and later.
