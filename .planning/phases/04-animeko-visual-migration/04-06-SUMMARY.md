# 04-06 Summary - Post-device light-theme hierarchy follow-up

## Outcome

Phase 04 closed with a merged real-device follow-up: light-theme hierarchy was strengthened after actual device validation exposed flattened card layers on home and settings surfaces.

## What Changed

### 1) Light-theme token separation was reinforced
- Updated `app/src/main/java/com/peter/overtimecalculator/ui/theme/ThemeDefaults.kt`.
- Increased tonal separation between `navigationContainer`, `sectionContainer`, `cardContainer`, and `cardElevatedContainer` in light mode.
- Kept the dark-mode branch stable to avoid regressions.

### 2) Key cards moved from patch fixes to semantic layering
- Updated `app/src/main/java/com/peter/overtimecalculator/ui/HomeSummarySections.kt`.
- Updated `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsCommon.kt`.
- Updated `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsMainSections.kt`.
- The merged direction favors tonal layer differences and semantic containers over visible border patches.

### 3) Guardrails were added for future maintenance
- Updated `app/src/test/java/com/peter/overtimecalculator/ThemeDefaultsTest.kt`.
- Added luminance-delta assertions so later token tuning cannot collapse light-theme card hierarchy back toward the page background.

## Verification Evidence

Passed on 2026-03-21:
- `./gradlew.bat :app:assembleDebug --stacktrace`
- `./gradlew.bat :app:testDebugUnitTest --stacktrace`
- Real-device light-theme validation for home summary/month switcher and settings cards

## Residual Cleanup Notes

- `app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeModeSections.kt` still contains hardcoded preview colors for theme thumbnails.
- `app/src/main/java/com/peter/overtimecalculator/ui/theme/Color.kt` appears to be an unused legacy palette file.
- `app/src/main/res/values/themes.xml` still inherits from `Theme.MaterialComponents.DayNight.NoActionBar` and should be reviewed for long-term alignment with the Compose Material 3 shell.
- Some screen-level gradients and alpha blends remain local because `ThemeDefaults` does not yet define richer roles for hero atmospheres, subtle text, and preview-line tokens.

## Notes

- This file absorbs the former `4.1` real-device follow-up into the main Phase 04 reading path.
- Historical `4.1` planning docs remain as archive material only; future maintenance should start from the `04-animeko-visual-migration` folder.
