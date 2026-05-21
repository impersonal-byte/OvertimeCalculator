---
phase: 06-material3-interaction-system-upgrade
type: phase-plan
depends_on:
  - 05-detail-experience-polish
context:
  - .planning/phases/06-material3-interaction-system-upgrade/06-CONTEXT.md
autonomous: true
execution_mode: wave-based
---

<objective>
Execute a Material 3-first interaction-system upgrade that improves clarity, confidence, and daily-use efficiency without expanding product scope.

Purpose: move from "post-polish consistency" to a stable, reusable UX system where state feedback, token hierarchy, accessibility semantics, and restrained expressive detail all work together.

Output: four-wave execution blueprint with file targets, verification gates, and acceptance criteria aligned to UXS-01..UXS-06.
</objective>

<constraints>
- Keep product scope unchanged: no new user-facing capabilities and no large information-architecture redesign in this phase.
- Preserve existing business semantics (overtime calculation, holiday typing, backup/restore semantics).
- Follow MD3 as the baseline system; md3E is selective enhancement, not visual takeover.
- Prefer existing architecture patterns (`ViewModel + StateFlow`, `ThemeDefaults`, settings section composition) over introducing a second UI architecture.
- Maintain "compact but breathable" density and "refined utility" tone.
</constraints>

<execution_context>
@D:/Android.calculator/.planning/phases/06-material3-interaction-system-upgrade/06-CONTEXT.md
@D:/Android.calculator/.planning/ROADMAP.md
@D:/Android.calculator/.planning/REQUIREMENTS.md
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/HomeScreen.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/HomeSummarySections.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/HomeCalendarSections.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/DayEditorSheet.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppEffects.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/theme/ThemeDefaults.kt
@D:/Android.calculator/app/src/main/java/com/peter/overtimecalculator/ui/settings/
</execution_context>

<wave_plan>

## Wave 1 - Explicit state feedback model (Plan 06-01)

**Goal:** Establish explicit UI-state semantics and clear async feedback so users can always tell whether data is loading, empty, failed, or completed.

**Requirements:** UXS-01, UXS-02

**Target files (expected):**
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/HomeScreen.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/HomeSummarySections.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppEffects.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/DataManagementSections.kt`
- related unit/android tests covering state rendering and long-running operations

**Implementation tasks:**
1. Introduce explicit state model for home/month switching (`Loading | Content | Empty | Error`).
2. Replace ambiguous placeholder-only first render with full-coverage skeleton (summary + calendar) and guided empty state behavior.
3. Use a slower, steadier skeleton animation tempo to reduce visual fatigue.
4. Keep locked empty-state copy stable and avoid further copy iterations in this phase.
5. Replace explicit save button flow in day editor with close-to-save behavior (tap outside sheet to close and auto-save).
6. Fix auto-save trigger policy to "tap-outside close only" (no save-on-change and no debounce-save path).
7. Add guardrails for auto-save: no-op when nothing changed; treat changed-to-zero as clearing the day record.
8. Ensure close-save path completes before same-day editor can be reopened (eliminate close/reopen save race by logic, not by copy).
9. Define swipe-down close as cancel path (no save), and keep gesture semantics distinct from tap-outside close-save.
10. Handle auto-save failure with top red hint copy fixed to "保存失败，点这里重试" and immediate retry action.
11. After user-triggered retry, perform one additional automatic retry at most (no infinite loops).
12. Add 2-second undo entry after successful auto-save to let users quickly revert accidental close-save.
13. Use "修改已保存" as the success copy and "已取消保存" after undo.
14. Keep user on current page after undo (do not auto-reopen day editor sheet).
15. If undo is tapped after a month switch, perform rollback in current month view with silent refresh (no forced jump back).
16. Keep auto-save failure red hint persistent until user action (retry or dismiss).
17. Ignore rapid repeat taps for same-day entry within a short window; first tap wins.
18. Keep clear action one-step (no second confirm dialog) for day-record clear to preserve high-frequency speed.
19. Add save feedback choreography as "light motion first, then top mini status strip with '修改已保存'" to balance confidence and low interruption.
20. On month switch with unsaved edits, enforce "save first, then switch month" to avoid losing edits.
21. If user taps another date while current date is still saving, queue the new target and auto-open it after save completes.
22. Queue policy uses last-intent-wins: if multiple dates are tapped during save, keep only the latest target.
23. If "save-first" fails during month switch, stay on current month and provide in-place retry.
24. When user leaves immediately after close-save trigger (back/home), hold navigation briefly until critical write is durably persisted.
25. If retry still fails at cap, keep local draft marked pending to guarantee no data loss and allow later resubmit.
26. For pending drafts, support dual resubmit paths: auto-retry when network/conditions recover and explicit manual submit entry.
27. If save exceeds threshold (e.g., 6s) while queue has next target, offer explicit split: continue waiting OR abandon current save and move to queued target.
28. If app is killed/crashes after close-save trigger, restore pending critical writes on next launch before normal flow and ask user to continue submit.
29. Add clear in-progress/complete feedback contract for backup/restore/export/update actions, standardized as bottom progress sheet behavior with minimize-to-background support.
30. Preserve current business flow and side-effect wiring while improving observability.

**Verification gate:**
- `./gradlew.bat :app:testDebugUnitTest`
- targeted instrumentation tests for home render states and data-management/update flows

**Done criteria:**
- Users no longer encounter silent/ambiguous first-load calendar states.
- Long-running operations visibly communicate in-progress and final outcome.

---

## Wave 2 - MD3 token baseline unification (Plan 06-02)

**Goal:** Remove ad-hoc spacing/shape/elevation/emphasis drift by introducing a reusable token baseline.

**Requirements:** UXS-03

**Target files (expected):**
- `app/src/main/java/com/peter/overtimecalculator/ui/theme/ThemeDefaults.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppShell.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/HomeSummarySections.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/HomeCalendarSections.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsCommon.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/*Sections.kt`
- token guardrail unit tests (new or extended)

**Implementation tasks:**
1. Define shared spacing/shape/elevation/action-emphasis token surfaces.
2. Lock spacing baseline to 4dp grid with key 8-step anchors for primary layout rhythm.
3. Place high-frequency actions in fixed lower-area positions for thumb-first reachability.
4. Set high-frequency action size target to 52dp for reliable tapping without over-expanding layout.
5. Move low-frequency actions into secondary area instead of sharing the primary action row.
6. Keep settings entry in top-right but expand hit area to 52dp to improve reach reliability.
7. Make DayCard fully tappable (whole-card hit area) for faster day-edit entry.
8. Keep key editor actions (close/undo) in a fixed corner position across states.
9. Replace mixed literal dp/radius/elevation values in target scope with token references.
10. Keep MD3 semantics first (tonal hierarchy + restrained emphasis).
11. Ensure light/dark and dynamic-color compatibility remains intact.

**Verification gate:**
- `./gradlew.bat :app:assembleDebug`
- `./gradlew.bat :app:testDebugUnitTest`

**Done criteria:**
- Targeted surfaces no longer depend on scattered one-off spacing/shape/elevation values.
- Token usage is understandable and enforceable in future plans.

---

## Wave 3 - Interaction affordance + accessibility hardening (Plan 06-03)

**Goal:** Improve perceived tap confidence and semantic accessibility for high-frequency interactions.

**Requirements:** UXS-04, UXS-05

**Target files (expected):**
- `app/src/main/java/com/peter/overtimecalculator/ui/HomeCalendarSections.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/HomeSummarySections.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/DayEditorSheet.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/DurationFormatting.kt`
- related accessibility-focused tests and UI test tags/assertions

**Implementation tasks:**
1. Strengthen day-card press/tap affordance and semantics descriptions.
2. Use tonal press-state + micro-scale as the default DayCard tap confirmation behavior.
3. Ensure icons and actionable controls have meaningful accessibility labels/state descriptions.
4. Consolidate number-first typography hierarchy for key figures with selective emphasis: keep amount hierarchy stable and slightly strengthen time readability.
5. Implement TalkBack semantics for DayCard as "date + day-type + duration", with compact single-character day-type labels (工/休/节).
6. Remove confusing dual-format presentation where it harms readability.

**Verification gate:**
- `./gradlew.bat :app:testDebugUnitTest`
- targeted instrumentation tests for day entry, semantics tags, and summary rendering

**Done criteria:**
- High-frequency actions are easier to perceive and verify.
- Accessibility semantics are explicit enough for assistive technologies.

---

## Wave 4 - Settings/data-management efficiency + restrained md3E polish (Plan 06-04)

**Goal:** Finalize phase-level UX cohesion by tightening settings efficiency and applying measured expressive polish.

**Requirements:** UXS-02, UXS-06

**Target files (expected):**
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsMainScreen.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/SettingsMainSections.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/DataManagementSections.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/AboutSections.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppShell.kt`
- targeted UI tests covering settings/data-management interaction continuity

**Implementation tasks:**
1. Align settings and data-management action rhythm with compact-but-breathable density.
2. Apply subtle expressive motion/hierarchy cues where they improve clarity and confirmation.
3. Keep motion restrained and utility-oriented (no decorative overload).
4. Prioritize completion behavior over copy tweaks: operation done -> state refresh -> user returned to correct context.
5. Provide a top mini status strip as the re-entry point when bottom progress sheet is minimized.
6. For concurrent tasks, top mini strip shows latest task + remaining task count by default.
7. Style the top mini status strip as page-matched background with a thin outline for low-distraction visibility.
8. Make top mini status strip tap open a task-list panel first, then route to selected task progress.
9. On restore completion, show numeric result (imported count) and jump user back to current-month view.
10. Show "网络较慢，仍在处理中" reassurance when progress stalls under slow network.
11. If app returns from background during long-running task, auto-restore current progress state.
12. Allow editor entry during long-running tasks, but temporarily disable conflicting actions to prevent state overwrite.
13. For disabled conflicting actions, provide immediate reason and quick jump path to progress entry.
14. Validate that polish remains compatible with existing Phase 04/05 visual foundations.

**Verification gate:**
- `./gradlew.bat :app:assembleDebug`
- `./gradlew.bat :app:testDebugUnitTest`
- `./gradlew.bat :app:connectedDebugAndroidTest` (or targeted settings/data classes)

**Done criteria:**
- Settings + data-management feel like one coherent interaction system.
- md3E accents are noticeable but controlled, consistent with refined utility tone.

</wave_plan>

<acceptance>
1. Home/month flows have explicit UI states and no misleading placeholder-first ambiguity.
2. Save and long-running operations provide clear progress/outcome feedback.
3. Tokenized spacing/shape/elevation/emphasis baseline is applied in targeted surfaces.
4. Accessibility semantics and touch affordances improve for core high-frequency interactions.
5. Numeric hierarchy is clearer while preserving compact-but-breathable layout density.
6. Expressive effects remain restrained and utility-aligned rather than decorative.
</acceptance>

<risk_controls>
- **Scope creep risk:** Reject new feature ideas and IA rewrites; record them as deferred backlog only.
- **Behavior regression risk:** Keep business logic unchanged; confine this phase to interaction/presentation contracts.
- **Consistency risk:** Enforce token usage over local literal values in migrated scope.
- **Motion overuse risk:** Gate md3E additions with usability criterion (clarity/confidence first).
- **Verification gap risk:** Require build + unit tests + targeted instrumentation/UI checks per wave.
</risk_controls>

<output>
Create execution summaries after each wave:
- `.planning/phases/06-material3-interaction-system-upgrade/06-01-SUMMARY.md`
- `.planning/phases/06-material3-interaction-system-upgrade/06-02-SUMMARY.md`
- `.planning/phases/06-material3-interaction-system-upgrade/06-03-SUMMARY.md`
- `.planning/phases/06-material3-interaction-system-upgrade/06-04-SUMMARY.md`
</output>
