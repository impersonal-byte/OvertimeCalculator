---
phase: 05-detail-experience-polish
plan: 03
subsystem: ui
tags: [compose, settings, theme, segmented-buttons]

requires:
  - phase: 04-animeko-visual-migration
    provides: Tokenized settings surfaces and edge-to-edge shell structure used by the final chrome polish
provides:
  - Compact always-visible segmented theme selection
  - Route-aware settings chrome that removes the mismatched home-shell treatment from settings screens
  - Regression coverage for theme chooser visibility and selection tags
affects: [theme-settings, app-shell, settings-navigation]

tech-stack:
  added: []
  patterns: [segmented theme mode control, route-aware shell chrome for settings]

key-files:
  created: []
  modified: [app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeModeSections.kt, app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeSettingsScreen.kt, app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppShell.kt, app/src/androidTest/java/com/peter/overtimecalculator/MainFlowTest.kt]

key-decisions:
  - "Preserve existing theme mode test tags while replacing oversized cards with a segmented control"
  - "Centralize settings chrome harmony in the route-aware app shell instead of per-screen or per-activity hacks"

patterns-established:
  - "Settings-specific top/status-area polish should be applied where shell insets and gradients are actually owned"
  - "Theme controls can become more compact without breaking automation when existing tags are preserved"

requirements-completed: [UXP-02, UXP-03]
duration: interrupted-session
completed: 2026-03-28
---

# Phase 05 Plan 03: Theme switcher and settings chrome summary

**Theme settings now expose all three modes in a compact segmented control, and settings routes inherit a cleaner shell treatment that matches their surface language.**

## Performance

- **Duration:** Interrupted session
- **Started:** Prior interrupted implementation session (exact timestamp not preserved)
- **Completed:** 2026-03-28T15:12:00Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Replaced the oversized theme cards with a full-width `SingleChoiceSegmentedButtonRow` that keeps light, dark, and system modes visible together.
- Preserved existing `theme_mode_*` and `theme_mode_*_selected` tags so automation and QA flows still target the same semantic hooks.
- Moved settings chrome harmonization into `OvertimeAppShell.kt`, removing the home-screen gradient treatment and zeroing content insets for settings routes.

## Task Commits

No implementation-time git commits were created for this plan because the user did not request code commits during Phase 05 execution.

Plan metadata commit was also not created in this execution session.

## Files Created/Modified
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeModeSections.kt` - swaps card chooser UI for segmented buttons while preserving tags
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeSettingsScreen.kt` - hosts the new compact chooser in the theme settings surface
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppShell.kt` - applies route-aware settings chrome without disturbing the home shell
- `app/src/androidTest/java/com/peter/overtimecalculator/MainFlowTest.kt` - asserts compact chooser visibility and dark-mode selection tags

## Decisions Made
- The compact theme selector keeps all three choices visible without adding any new navigation or scroll affordance.
- The correct centralization point for settings chrome turned out to be `OvertimeAppShell.kt`, because that is where route gradients and scaffold insets are owned.

## Deviations from Plan

None - the implementation stayed within the planned shared-chrome strategy, but landed in `OvertimeAppShell.kt` instead of lower-level theme/activity files because that is the actual shell owner.

## Issues Encountered
- Android instrumentation verification could not run in this environment because `connectedDebugAndroidTest` failed with `No connected devices!`.
- A code-level visual review confirmed the current implementation is acceptable to close the phase, with only on-device final look-and-feel confirmation still pending.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Theme switching and settings chrome polish are ready for normal use.
- If a final visual sign-off is desired, the remaining work is device/emulator-based confirmation rather than more implementation.

---
*Phase: 05-detail-experience-polish*
*Completed: 2026-03-28*
