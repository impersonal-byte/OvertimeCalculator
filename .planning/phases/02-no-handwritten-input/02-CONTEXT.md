# Phase 2: No handwritten input - Context

**Gathered:** 2026-03-14
**Status:** Ready for planning

<domain>
## Phase Boundary

Replace handwritten-style or manual freeform day-entry expectations with a structured input flow for daily overtime and comp-time recording. This phase focuses on the day editor interaction for entering signed duration values and does not expand into replacing every numeric settings form in the app.

</domain>

<decisions>
## Implementation Decisions

### Input mode
- Do not add handwriting or stylus-style input for day entry.
- Use a centered slider as the primary daily duration control.
- The slider maps negative values to the left for comp time and positive values to the right for overtime.
- The centered slider is intended to replace the current primary duration stepper interaction in the day editor.

### Quick entry
- Keep the day-entry interaction slider-first rather than splitting attention across multiple quick-entry surfaces.
- Preserve reset/clear behavior, but remove preset chips from the day editor so the centered slider is the sole duration control.
- Optimize the day-entry flow for quick repeated use from the calendar, not for long-form editing.

### Input density
- Make daily overtime entry low-friction and highly structured.
- Prefer a single focused editor surface over multiple manual numeric fields for the day-entry flow.
- Keep exact numeric text fields for settings flows that require precision, such as hourly rate, reverse-engineered pay input, and multipliers.

### Validation feedback
- Prevent invalid daily-entry ranges directly in the control instead of allowing invalid values and rejecting them later.
- Keep the sign semantics explicit in the UI: left means negative/comp time, right means positive/overtime.
- Retain lightweight explanatory copy and save feedback, with snackbar-style messaging reserved for exceptional failures rather than normal range guidance.

### Slider markers
- Do not render stop indicators for every 30-minute detent on phone-sized screens.
- Keep 30-minute snapping internally, but show only sparse major markers and a clear center-zero indicator.
- Prioritize readability of direction and zero position over exhaustive visual ticks.

### Claude's Discretion
- Exact centered-slider visual design, labels, and tick-mark treatment.
- Snapping interval and haptic behavior.
- Exact helper text wording and whether the current clear action remains a button or becomes a slider reset affordance.

</decisions>

<specifics>
## Specific Ideas

- "使用 Centered sliders，向左表示负，向右表示正数。"
- The user explicitly questioned whether this can completely replace the current interaction; the chosen default is to apply full replacement only to daily duration entry, not to all settings forms.
- The user explicitly raised crowding concerns about stop indicators on mobile; the chosen default is sparse major ticks only, not one marker per step.
- After manual testing, the user chose to remove preset chips entirely because they conflict with the slider interaction.

</specifics>

<code_context>
## Existing Code Insights

### Reusable Assets
- `app/src/main/java/com/peter/overtimecalculator/ui/DayEditorSheet.kt`: current day-entry sheet, existing integration point for replacing the primary duration control.
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt`: already supports signed-minute saving through `saveOvertimeMinutes`, so the control can emit total minutes directly.
- `app/src/androidTest/java/com/peter/overtimecalculator/MainFlowTest.kt`: existing flow tests around day entry and settings provide a baseline for updating behavior verification.

### Established Patterns
- Daily editing currently uses a bottom sheet launched from calendar day cards, so the new input should stay inside that existing sheet-based flow.
- The app already distinguishes signed duration semantics in the day editor: workdays allow negative values for comp time, while non-workdays clamp to zero or above.
- Settings flows use `OutlinedTextField` for precise numeric values, which is a separate pattern from the day-entry editor and should remain distinct.

### Integration Points
- `HomeCalendarSections.kt` day cards open the editor through `OvertimeViewModel.openEditor`.
- `Screens.kt` and `OvertimeAppShell.kt` already pass `onSaveEditor = viewModel::saveOvertimeMinutes`, so a slider-based editor can plug into the same save path.
- `RulesSections.kt` remains the home for exact numeric configuration and should not be treated as part of the slider replacement scope.

</code_context>

<deferred>
## Deferred Ideas

- Replacing settings-page numeric fields such as hourly rate, reverse pay amount, or multipliers with slider-based controls — future phase if desired.
- Reworking the broader settings input model around non-text controls — outside the scope of the day-entry interaction phase.

</deferred>

---

*Phase: 02-no-handwritten-input*
*Context gathered: 2026-03-14*
