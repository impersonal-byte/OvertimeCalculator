# Phase 2: No handwritten input - Research

**Researched:** 2026-03-15
**Domain:** Jetpack Compose UI, Material 3 Slider, Android accessibility
**Confidence:** HIGH

## Summary

Phase 2 requires replacing the current stepper-based day editor (`DurationStepperButton` +/- controls) with a centered slider that handles signed duration values (negative for comp-time, positive for overtime). The key findings are:

1. **Material 3 has no built-in centered slider** - Must implement using standard `Slider` composable with custom range (-480 to 960 minutes) and step configuration
2. **Domain constants already centralized** - `OvertimeEntryValidator` in domain layer defines `MAX_OVERTIME_MINUTES` (960) and `MIN_COMP_MINUTES` (-480), but `DayEditorSheet.kt` duplicates these values
3. **Step/snapping via `steps` parameter** - 30-minute increments = 48 discrete values (from -480 to 960 = 1440 range / 30 step = 48 steps)
4. **Accessibility built-in** - Material 3 Slider provides TalkBack support; custom semantics needed for value announcements
5. **Existing test coverage** - `MainFlowTest.kt` covers the day editor flow, but slider-specific assertions need to replace old preset-chip interactions

**Primary recommendation:** Build centered slider using Compose `Slider` with `valueRange = -480f..960f`, `steps = 47`, and custom track drawing for center indicator. Reuse domain constants from `OvertimeEntryValidator` and add test tags for slider thumb and value display.

---

<user_constraints>

## User Constraints (from CONTEXT.md)

### Locked Decisions
- Do NOT add handwriting or stylus-style input for day entry
- Use a centered slider as the primary daily duration control
- Slider maps negative values to the left (comp time) and positive to the right (overtime)
- The centered slider replaces the current primary duration stepper interaction in the day editor
- Keep the day editor focused on the centered slider plus clear/reset behavior
- Do not keep preset chips alongside the slider in the day editor
- Daily overtime entry should be low-friction and highly structured
- Prefer a single focused editor surface over multiple manual numeric fields
- Keep exact numeric text fields for settings flows (hourly rate, reverse pay, multipliers)
- Prevent invalid daily-entry ranges directly in the control
- Keep sign semantics explicit in the UI: left = negative/comp time, right = positive/overtime

### Claude's Discretion
- Exact centered-slider visual design, labels, and tick-mark treatment
- Snapping interval and haptic behavior
- Exact helper text wording and whether the current clear action remains a button or becomes a slider reset affordance

### Deferred Ideas (OUT OF SCOPE)
- Replacing settings-page numeric fields (hourly rate, reverse pay, multipliers) with slider controls
- Reworking the broader settings input model around non-text controls

</user_constraints>

---

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Jetpack Compose BOM | 2024.02.00+ | UI framework | Native Android modern UI |
| Material 3 | from BOM | Slider component | Official Material Design |
| Compose Foundation | from BOM | Gestures, custom drawing | Slider customization |
| AndroidX Lifecycle | 2.7.0+ | ViewModel | State management |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Compose Material3 Slider | from BOM | Base slider component | Primary duration input |
| LocalHapticFeedback | Foundation | Haptic feedback | Step/snapping feedback |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Custom drawing | RangeSlider | M3 RangeSlider is for dual-thumb, not bipolar |
| Third-party library | Material3 Extended Slider | No mature centered slider exists; custom needed |
| TextField replacement | Manual entry | Explicitly rejected in CONTEXT.md - keep slider |

---

## Architecture Patterns

### Recommended Project Structure
```
app/src/main/java/com/peter/overtimecalculator/
├── domain/
│   ├── Validation.kt           # Already has MAX_OVERTIME_MINUTES, MIN_COMP_MINUTES
│   └── OvertimeEntryValidator  # Central validation logic
├── ui/
│   ├── DayEditorSheet.kt        # Replace stepper with slider HERE
│   ├── OvertimeViewModel.kt     # Already has saveOvertimeMinutes
│   └── components/              # New: slider components
│       └── CenteredDurationSlider.kt
```

### Pattern 1: Centered Slider with Step Snapping
**What:** A slider that allows signed values (-8h to +16h) with discrete 30-minute snapping
**When to use:** Daily overtime/comp-time entry in day editor
**Example:**
```kotlin
// Source: Adapted from Material3 Slider docs + custom centered implementation
@Composable
fun CenteredDurationSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = -480f..960f,
    modifier: Modifier = Modifier,
) {
    val steps = 47 // (960 - (-480)) / 30 - 1 = 47 steps for 30-min increments
    
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        modifier = modifier.testTag("duration_slider"),
    )
}
```

### Pattern 2: Domain Constants Reuse
**What:** UI references domain layer constants instead of defining its own
**When to use:** Any numeric boundary values
**Example:**
```kotlin
// In DayEditorSheet.kt - BEFORE (duplicated)
private const val MaxOvertimeMinutes = 16 * 60
private const val MinCompMinutes = -8 * 60

// AFTER - use domain constants
import com.peter.overtimecalculator.domain.OvertimeEntryValidator

val sliderRange = OvertimeEntryValidator.MIN_COMP_MINUTES.toFloat().. 
                  OvertimeEntryValidator.MAX_OVERTIME_MINUTES.toFloat()
```

### Pattern 3: Preserving Bottom Sheet Flow
**What:** Slider replaces stepper but stays within existing ModalBottomSheet
**When to use:** Day editor remains in `CompTimeDayEditorSheet`
**Example:**
```kotlin
// DayEditorSheet.kt - existing structure preserved
ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(...) {
        // Slider replaces DurationStepperButton row
        CenteredDurationSlider(
            value = totalMinutes.toFloat(),
            onValueChange = { totalMinutes = it.toInt() },
        )
        // Clear button remains, preset chips removed
        TextButton(...) { /* clear */ }
    }
}
```

### Anti-Patterns to Avoid
- **Defining domain constants in UI layer:** Already happening in DayEditorSheet.kt - should use OvertimeEntryValidator constants
- **Changing save path:** OvertimeViewModel.saveOvertimeMinutes already works; slider should emit to same path

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Slider component | Build from scratch | Compose Material3 Slider | Provides accessibility, gestures, theming out of box |
| Value range validation | Custom coercion | OvertimeEntryValidator.validate | Domain layer already enforces -8h..16h |
| Haptic feedback | Custom haptic engine | LocalHapticFeedback.current.performHapticFeedback | Native Android haptic API |
| Accessibility | Build custom TalkBack | Slider built-ins + semantics | M3 Slider announces values automatically |

**Key insight:** The centered aspect is purely visual (track coloring, center indicator). The functional behavior uses standard Slider with negative-to-positive range.

---

## Common Pitfalls

### Pitfall 1: Float Precision Drift
**What goes wrong:** Slider value becomes 119.99998 instead of 120 after repeated adjustments
**Why it happens:** Float arithmetic imprecisions accumulate
**How to avoid:** Round to Int on `onValueChange` and coerce to step boundaries
**Warning signs:** Test shows "2.0h" becomes "1.99h" or similar

### Pitfall 2: Steps Calculation Error
**What goes wrong:** Slider doesn't snap to expected values (e.g., 30, 60, 90)
**Why it happens:** Steps parameter is "gaps between values", not "number of values" - formula is `(range / step) - 1`
**How to avoid:** For -480 to 960 range with 30-min steps: (1440/30) - 1 = 47 steps
**Warning signs:** Slider positions or saved values drift from 30-minute snapping expectations

### Pitfall 3: Non-Workday Negative Values
**What goes wrong:** Slider allows negative values on rest days/holidays
**Why it happens:** Domain validation enforces this but slider might not reflect in UI
**How to avoid:** Adjust `valueRange` based on `effectiveDayType` - clamp min to 0 for non-workdays
**Warning signs:** User can slide to -2h on a weekend, save succeeds, but domain rejects

### Pitfall 4: Duplicate Domain Constants
**What goes wrong:** UI and domain define same values differently
**Why it happens:** Copy-paste from old implementation
**How to avoid:** Import `OvertimeEntryValidator.MAX_OVERTIME_MINUTES` and `MIN_COMP_MINUTES`
**Warning signs:** Two constants with same value but different names

### Pitfall 5: Accessibility Value Announcements
**What goes wrong:** TalkBack announces raw float "负四百八十" instead of readable format
**Why it happens:** Default slider announces numeric value without formatting
**How to avoid:** Add `stateDescription` or custom semantics for formatted duration
**Warning signs:** Screen reader test fails or user confusion

---

## Code Examples

### Centered Slider Implementation (Verified Pattern)
```kotlin
// Source: sinasamaki/centered-slider + Material3 Slider docs
@Composable
fun CenteredDurationSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    minMinutes: Int, // From OvertimeEntryValidator.MIN_COMP_MINUTES or 0
    maxMinutes: Int = OvertimeEntryValidator.MAX_OVERTIME_MINUTES,
    modifier: Modifier = Modifier,
) {
    val stepSize = 30
    val steps = ((maxMinutes - minMinutes) / stepSize) - 1
    
    Slider(
        value = value,
        onValueChange = { newValue ->
            // Snap to nearest step
            val snapped = ((newValue / stepSize).roundToInt() * stepSize).toFloat()
            onValueChange(snapped.coerceIn(minMinutes.toFloat(), maxMinutes.toFloat()))
        },
        valueRange = minMinutes.toFloat()..maxMinutes.toFloat(),
        steps = steps,
        modifier = modifier.testTag("duration_slider"),
    )
}
```

### Value Display with Formatting (Reuses Existing)
```kotlin
// Source: DayEditorSheet.kt line 109-116 (existing pattern)
Text(
    text = formatStepperDuration(totalMinutes),
    style = MaterialTheme.typography.headlineLarge,
    modifier = Modifier.testTag("editor_duration_value"),
)
```

### Domain Constants Import (Should Migrate)
```kotlin
// Source: Validation.kt line 34-35 (existing domain constants)
import com.peter.overtimecalculator.domain.OvertimeEntryValidator

// Use these instead of DayEditorSheet.kt lines 45-47
val DurationStepMinutes = 30 // Can stay in UI - presentation only
val MaxOvertimeMinutes = OvertimeEntryValidator.MAX_OVERTIME_MINUTES
val MinCompMinutes = OvertimeEntryValidator.MIN_COMP_MINUTES
```

---

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| DurationStepperButton (+/-) | Centered Slider | Phase 2 | Single control for full range, visual sign indication |
| Duplicate constants in UI | Domain constants | Phase 2 (recommend) | Single source of truth for limits |
| Manual haptic (detectTapGestures) | Native slider haptics | Phase 2 | Built-in step feedback |

**Deprecated/outdated:**
- `DurationStepperButton` with press-hold repeat logic - replaced by slider's native gesture handling

---

## Open Questions

1. **Center track visual indicator**
   - What we know: Material3 guidelines mention centered sliders but no native implementation
   - What's unclear: Should the center (0) have a visual marker/tick?
   - Recommendation: Add a vertical line/divider at center position in custom track

2. **Haptic feedback timing**
   - What we know: Slider provides basic haptics, custom implementation can add step-specific feedback
   - What's unclear: Should haptics fire on every step crossing, or only on release?
   - Recommendation: Fire on step change during drag for immediate feedback

3. **Slider-only interaction density**
   - What we know: Preset chips were removed after manual testing because they conflicted with the slider
   - What's unclear: Whether helper copy alone is enough guidance for first-time users
   - Recommendation: Keep the slider-only layout and rely on sparse major ticks plus clear/reset affordance

4. **Test tag migration**
   - What we know: Existing tests used `duration_stepper`, `editor_duration_value`, and `preset_120`
   - What's unclear: Which old tags are still worth preserving after the preset removal
   - Recommendation: Keep `editor_duration_value`, add `duration_slider`, and remove preset tag dependencies from tests

---

## Validation Architecture

> Skip this section entirely if workflow.nyquist_validation is explicitly set to false in .planning/config.json. If the key is absent, treat as enabled.

### Test Framework
| Property | Value |
|----------|-------|
| Framework | AndroidJUnit4 + Compose UI Test |
| Config file | `app/build.gradle.kts` (androidTest dependencies) |
| Quick run command | `./gradlew :app:testDebugAndroidUnitTest` |
| Full suite command | `./gradlew :app:testDebugAndroidTest` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| SLIDER-01 | Slider displays correct range (-8h to +16h) | Unit | N/A - visual | ❌ Need new |
| SLIDER-02 | Slider snaps to 30-minute increments | Unit | N/A - visual | ❌ Need new |
| SLIDER-03 | Slider value displays formatted (e.g., "2.0h") | Unit | `onNodeWithTag("editor_duration_value").assertTextContains("2.0h")` | ✅ Exists |
| SLIDER-04 | Negative values show comp-time context | Unit | N/A - visual | ❌ Need new |
| SLIDER-05 | Non-workdays clamp to 0 minimum | Unit | Save and verify | ❌ Need new |
| SLIDER-06 | Preset chips are absent from the day editor | Integration | `onAllNodesWithTag("preset_120").assertCountEquals(0)` | ✅ Exists |
| SLIDER-07 | Save via slider value works | Integration | Full flow test | ✅ Exists (MainFlowTest) |

### Sampling Rate
- **Per task commit:** N/A - Android instrumented tests require emulator
- **Per wave merge:** `./gradlew :app:testDebugAndroidTest`
- **Phase gate:** Full suite green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `app/src/androidTest/java/com/peter/overtimecalculator/SliderDayEditorTest.kt` — covers SLIDER-01, SLIDER-02, SLIDER-04, SLIDER-05
- [ ] Test tags added to slider: `duration_slider`, `slider_thumb`, `slider_track`
- [ ] Verify domain constants are imported (not duplicated) in DayEditorSheet.kt

---

## Sources

### Primary (HIGH confidence)
- Material3 Sliders Guidelines (m3.material.io/components/sliders/guidelines) - Centered slider variant documentation
- Compose Material3 Slider API (composables.com/docs/androidx.compose.material3/material3/components/Slider) - Parameter reference
- OvertimeEntryValidator (Validation.kt lines 34-35) - Domain constants source

### Secondary (MEDIUM confidence)
- Sinasamaki Centered Slider (sinasamaki.com/centered-slider-in-jetpack-compose) - Custom implementation pattern
- PickMe Engineering Custom Slider (medium.com/pickme-engineering-blog/building-a-production-ready-custom-slider) - Step snapping, haptics

### Tertiary (LOW confidence)
- Various Medium articles on Compose accessibility - patterns generalizable but not specific to this use case

---

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Compose Material3 Slider is well-documented and stable
- Architecture: HIGH - Domain constants already centralized, existing save path works
- Pitfalls: MEDIUM - Float precision and step calculation are known issues but documented

**Research date:** 2026-03-15
**Valid until:** 2026-04-15 (30 days for stable API)
