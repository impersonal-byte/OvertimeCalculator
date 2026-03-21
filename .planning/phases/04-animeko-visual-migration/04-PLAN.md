---
phase: 04-animeko-visual-migration
type: phase-plan
depends_on:
  - 03-data-backup-and-restore
context:
  - .planning/phases/04-animeko-visual-migration/04-CONTEXT.md
autonomous: true
execution_mode: wave-based
---

<objective>
Execute an Animeko-inspired visual migration for this app by replacing legacy styling with a centralized semantic theme system and phased surface rollout.

Purpose: deliver high semantic parity (color roles, layer depth, component state language) through clean-room implementation while preserving existing business interaction semantics.

Output: wave-by-wave execution blueprint with file targets, verification gates, and completion rules.
</objective>

<constraints>
- Clean-room implementation only. Do not copy animeko source code directly.
- Business behavior remains unchanged (especially Phase 2 day-entry interaction semantics and Phase 3 backup/restore semantics).
- Keep this project's existing product copy and naming language.
- Dynamic color defaults to OFF and remains user opt-in.
- No pure-black AMOLED mode and no image-derived theming in this phase.
- Enforce semantic wrappers/defaults instead of ad-hoc feature-level direct color usage.
</constraints>

<execution_context>
@D:/Android.calculator/.planning/phases/04-animeko-visual-migration/04-CONTEXT.md
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/theme/Theme.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/theme/ThemePaletteSpec.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppShell.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/HomeSummarySections.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/HomeCalendarSections.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/DayEditorSheet.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeSettingsScreen.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeModeSections.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeColorSections.kt
</execution_context>

<wave_plan>

## Wave 1 - Theme foundation and semantic token system (Plan 04-02)

**Goal:** Replace static palette wiring with HCT-backed theme generation and centralized semantic token defaults.

**Target files:**
- `app/src/main/java/com/peter/overtimecalculator/ui/theme/Theme.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/theme/ThemePaletteSpec.kt`
- New theme support files under `app/src/main/java/com/peter/overtimecalculator/ui/theme/`
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt` (only if required for preference plumbing)

**Implementation tasks:**
1. Introduce HCT-driven light/dark palette generation path for configured seed colors.
2. Add centralized `ThemeDefaults`-style semantic token surface (navigation/page/container/card tiers and state variants).
3. Implement dynamic-color precedence: when enabled and supported, dynamic color overrides fixed HCT palette.
4. Keep typography unchanged and keep existing settings semantics intact.

**Verification gate:**
- `./gradlew.bat :app:assembleDebug`
- `./gradlew.bat :app:testDebugUnitTest`

**Done criteria:**
- No primary feature screen needs to consume raw hardcoded colors directly for migrated surfaces.
- Theme layer exposes reusable semantic tokens for downstream waves.

---

## Wave 2 - Shell and Home visual migration (Plan 04-03)

**Goal:** Deliver first user-visible style closure by migrating shell and home surfaces to the new semantic language.

**Target files:**
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppShell.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/HomeSummarySections.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/HomeCalendarSections.kt`
- Optional helper components in `app/src/main/java/com/peter/overtimecalculator/ui/components/`

**Implementation tasks:**
1. Apply gradient atmosphere only to shell-level and overview-card surfaces.
2. Replace legacy card/container color usage with semantic token tiers.
3. Align top-bar/surface hierarchy with new deep-gray dark theme layering.
4. Remove old style paths in migrated shell/home scope immediately (no dual styling in target surfaces).

**Verification gate:**
- `./gradlew.bat :app:assembleDebug`
- `./gradlew.bat :app:testDebugUnitTest`

**Done criteria:**
- Shell + home surfaces show coherent new visual language.
- No legacy styling path remains in migrated shell/home files.

---

## Wave 3 - Day editor and theme settings migration (Plan 04-04)

**Goal:** Migrate the highest-impact editor/settings surfaces while preserving behavior semantics.

**Target files:**
- `app/src/main/java/com/peter/overtimecalculator/ui/DayEditorSheet.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeSettingsScreen.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeModeSections.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeColorSections.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeOverviewSections.kt`

**Implementation tasks:**
1. Update `DayEditorSheet` visuals to semantic wrappers without changing slider-only interaction behavior from Phase 2.
2. Apply new token language to theme settings surfaces, including previews and section containers.
3. Keep dynamic-color default OFF while preserving user opt-in toggle behavior.
4. Remove replaced legacy style usage inside wave scope files.

**Verification gate:**
- `./gradlew.bat :app:assembleDebug`
- `./gradlew.bat :app:testDebugUnitTest`
- Targeted UI tests that cover day editor and theme settings if present.

**Done criteria:**
- Day editor behavior is unchanged but visual system is migrated.
- Theme settings screens are fully aligned with new semantic hierarchy.

---

## Wave 4 - Remaining settings surfaces and legacy cleanup (Plan 04-05)

**Goal:** Finish settings-area migration and enforce zero-legacy-style completion in target batch scope.

**Target files (expected):**
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/RulesSettingsScreen.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/PreferencesScreen.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/DataManagementScreen.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/AboutScreen.kt`
- Shared settings components under `app/src/main/java/com/peter/overtimecalculator/ui/settings/`

**Implementation tasks:**
1. Extend semantic wrappers/tokens to remaining settings pages.
2. Remove obsolete style helpers/tokens that are no longer referenced.
3. Ensure all migrated target surfaces avoid ad-hoc direct color access.
4. Perform final consistency pass for light/dark + dynamic on/off combinations.

**Verification gate:**
- `./gradlew.bat :app:assembleDebug`
- `./gradlew.bat :app:testDebugUnitTest`
- `./gradlew.bat :app:connectedDebugAndroidTest` (or targeted class-level instrumentation for settings flow)

**Done criteria:**
- Targeted phase surfaces have zero remaining legacy-style paths.
- Theme behavior is consistent across supported theme mode and dynamic-color combinations.

</wave_plan>

<acceptance>
1. The app theme system is HCT-based with centralized semantic defaults and dynamic-color precedence when enabled.
2. Shell, home, day editor, and theme settings surfaces are migrated to semantic wrappers/tokens.
3. Phase-targeted surfaces satisfy batch-level zero-legacy-style completion.
4. Migration preserves existing business behaviors and user-facing semantic flows.
</acceptance>

<risk_controls>
- **License/compliance:** Maintain clean-room implementation and avoid source copy from `animeko` files.
- **Behavior regression:** Validate day editor interaction behavior and settings action semantics after each wave.
- **Visual drift:** Apply centralized tokens only; reject one-off ad-hoc color usage during review.
- **Rollout risk:** Ship by wave with build + tests passing at each gate.
</risk_controls>

<output>
Create execution summaries after each wave:
- `.planning/phases/04-animeko-visual-migration/04-02-SUMMARY.md`
- `.planning/phases/04-animeko-visual-migration/04-03-SUMMARY.md`
- `.planning/phases/04-animeko-visual-migration/04-04-SUMMARY.md`
- `.planning/phases/04-animeko-visual-migration/04-05-SUMMARY.md`
</output>
