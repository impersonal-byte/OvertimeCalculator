# 04-03 Summary - Shell and Home Visual Migration

## Outcome

Wave 2 is completed: shell and home surfaces now use the semantic theme wrapper path, with gradients limited to shell atmosphere and the overview summary card.

## What Changed

### 1) Shell-level atmosphere and hierarchy
- Updated `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppShell.kt`.
- Added shell-level gradient overlay on top of `defaults.pageBackground`.
- Set scaffold container to transparent and content color to `defaults.pageForeground`.
- Top app bar now uses semantic layer roles (`navigationContainer`, `sectionContainer`, `pageForeground`).

### 2) Home summary and month switcher migration
- Updated `app/src/main/java/com/peter/overtimecalculator/ui/HomeSummarySections.kt`.
- Kept gradient only in `SummaryCard` via semantic roles (`cardElevatedContainer`, `cardContainer`).
- Migrated summary warnings, metric text, month switcher surface, month switch buttons, and trend bar color to `OvertimeTheme.defaults`.

### 3) Home calendar and root surface migration
- Updated `app/src/main/java/com/peter/overtimecalculator/ui/HomeCalendarSections.kt`.
- Replaced old `MaterialTheme.colorScheme`-based day-card colors with semantic token tiers (`cardContainer`, `sectionContainer`, `accent`, `warningTint`, `outline`).
- Preserved day card behavior (future-day disable, today border emphasis, semantics, test tags).
- Updated `app/src/main/java/com/peter/overtimecalculator/ui/HomeScreen.kt` root background to `defaults.pageBackground`.

## Verification Evidence

Executed and passed on 2026-03-21:
- `.\gradlew.bat :app:assembleDebug --stacktrace`
- `.\gradlew.bat :app:testDebugUnitTest --stacktrace`

Additional review evidence:
- 04-03 scoped code review agent reported no findings in:
  - `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppShell.kt`
  - `app/src/main/java/com/peter/overtimecalculator/ui/HomeSummarySections.kt`
  - `app/src/main/java/com/peter/overtimecalculator/ui/HomeCalendarSections.kt`
  - `app/src/main/java/com/peter/overtimecalculator/ui/HomeScreen.kt`

## Notes

- This wave is visual-system migration only; business flow semantics were intentionally kept unchanged.
- Next planned wave is 04-04 (day editor + theme settings surfaces).
