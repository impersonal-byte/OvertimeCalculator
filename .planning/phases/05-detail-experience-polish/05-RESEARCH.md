# Phase 5: 细节体验优化 - Research

**Researched:** 2026-03-24
**Domain:** Compose settings/detail polish, edge-to-edge settings chrome, wage-aware holiday classification
**Confidence:** HIGH

## Summary

Phase 5 should stay split between low-risk UI polish and one isolated business-rule/data-model change.

Key findings:

1. **The current workday slider is tuned for the old `-8h..16h` range** — `CenteredDurationSlider.kt` is continuous, but `DurationSliderMapping.kt`/major tick generation still assume wide positive anchors, so a 6-hour-focused workday range needs coordinated mapping + tick updates instead of a one-line max change.
2. **The current theme chooser is still card-heavy** — `ThemeModeSections.kt` uses large preview cards with a horizontal-scroll fallback under narrow widths, which directly matches the user's complaint. Material 3's compact single-choice segmented-button pattern is the closest standard replacement.
3. **Settings chrome is centralized** — all settings screens use `SettingsTopBar()` from `SettingsCommon.kt`, so status-bar/top-bar harmony should be fixed in shared settings chrome, not by patching individual screens.
4. **Data-management action styling is inconsistent by code, not perception** — `DataManagementSections.kt` uses filled buttons for backup/export but an outlined button for restore, which explains the mismatch the user sees.
5. **The current holiday model cannot express the requested 3x-vs-2x distinction** — `HolidayYearRules` only stores `holidayDates` and `workingDates`; baseline data and the current haoshenqi parser flatten long holiday blocks into a single `HOLIDAY` bucket.
6. **A richer holiday source already exists** — `https://timor.tech/api/holiday/year/{year}` exposes `wage` values that distinguish statutory 3x dates, official-off 2x dates, and 1x makeup workdays. That makes it a better fit than the current status-only refresh source if the app must keep remote parity.

**Primary recommendation:** plan three parallel tracks: (a) holiday model/source upgrade around wage-aware day types, (b) day-editor precision plus data-management action consistency, and (c) compact theme switching plus shared settings chrome harmony.

---

<user_constraints>

## User Constraints (from CONTEXT.md)

### Locked Decisions
- Workday day-entry should use a 6-hour-focused slider instead of the current 16-hour-positive editing feel.
- Theme mode switching must become a compact three-choice control with all choices visible side by side.
- Settings status-bar/top-bar chrome must visually match the settings surface language.
- Data-management backup/restore/export actions must look coordinated.
- Only 3x-pay dates count as `HOLIDAY`; other official days off count as `REST_DAY`; makeup days remain `WORKDAY`.

### Claude's Discretion
- Exact legacy-value handling for existing >6h workday entries.
- Exact compact control implementation (segmented buttons vs equivalent compact choice row).
- Exact system-bar styling helper strategy.
- Exact holiday schema and source migration.

### Deferred Ideas (OUT OF SCOPE)
- Additional unspecified polish items not listed in this phase context.
- Broad redesigns outside day entry, theme mode, settings chrome, data management, and holiday typing.

</user_constraints>

---

## Standard Stack

### Core
| Library / API | Version | Purpose | Why Standard |
|---------------|---------|---------|--------------|
| Compose Material 3 `Slider` | existing | Duration entry | Already used for day-entry editing |
| Compose Material 3 segmented buttons | existing | Compact mutually-exclusive theme choice | Official Compose control for single-choice option sets |
| `enableEdgeToEdge()` + `TopAppBar`/`Scaffold` | existing | Settings status-bar/top-bar harmony | Matches current app shell + modern Android system-bar behavior |
| Timor holiday API (`/api/holiday/year/{year}`) | remote HTTP | Wage-aware official-off day typing | Exposes `wage` 1/2/3 where current source does not |

### Supporting
| Library / API | Purpose | When to Use |
|---------------|---------|-------------|
| `ButtonDefaults.buttonColors` / filled tonal styles | Coordinated settings action emphasis | Backup/restore/export visual alignment |
| Existing androidTest (`MainFlowTest`, `DayEditorSliderTest`, `DataManagementBackupRestoreTest`) | UI regression coverage | Phase 5 settings/day-entry changes |
| Existing holiday unit tests (`DomainLogicTest`, `HolidayRulesRepositoryTest`) | Day-type + pay regression coverage | Holiday model/source refactor |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Compact segmented theme selector | Keep large preview cards | Fails the user's no-scroll/quick-switch request |
| Shared settings chrome fix | Per-screen status bar hacks | Higher churn and inconsistent results |
| Wage-aware remote source / richer schema | Keep current haoshenqi status-only feed | Cannot distinguish 3x statutory days from 2x official-off days |

---

## Architecture Patterns

### Pattern 1: Shared settings chrome owns settings-bar harmony
**What:** Fix status-bar/top-bar mismatch inside `SettingsTopBar()` and shared theme/system-bar wiring rather than on every settings screen.
**Project evidence:** `ThemeSettingsScreen.kt`, `RulesScreen.kt`, `DataManagementScreen.kt`, `PreferencesScreen.kt`, and `AboutScreen.kt` all call `SettingsTopBar()`.

### Pattern 2: Compact single-choice control for theme mode
**What:** Replace large swipeable theme cards with one compact mutually-exclusive control that keeps all choices visible.
**Project evidence:** `ThemeModeSections.kt` already isolates the chooser, so the compact control can change without rewriting settings routing.

### Pattern 3: Range-change work must include mapping + UI + tests together
**What:** Treat workday slider precision as a coordinated update to range calculation, major tick generation, editor integration, and regression tests.
**Project evidence:** current slider tests assert specific progress-to-hours mappings and visible ticks (`CenteredDurationSliderTest.kt`, `DayEditorSliderTest.kt`).

### Pattern 4: Holiday typing belongs in the source-of-truth model, not ad-hoc calculator exceptions
**What:** Extend holiday rule storage/parsing so `HolidayCalendar.resolveDayType()` receives enough information to distinguish statutory holidays, official-off rest days, and makeup workdays.
**Project evidence:** all pay calculators already depend on `HolidayCalendar.resolveDayType()`, so fixing the source model gives the whole app the new semantics automatically.

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Compact theme choice | Custom pixel-perfect mini-card carousel | Standard segmented single-choice control | Smaller, faster, easier to test |
| Settings status bar | Per-screen magic colors | Shared settings top-bar/system-bar coordination | Keeps all settings routes consistent |
| Holiday 3x/2x distinction | Hard-coded exception tables in calculators | Richer holiday source/schema with explicit day-type inputs | Preserves one source of truth |
| Slider precision update | One-line max constant tweak | Mapping + tick + editor + regression update | Avoids broken labels and silent clamping bugs |

---

## Common Pitfalls

### Pitfall 1: Shrinking the slider without reworking tick anchors
**What goes wrong:** labels overlap or still show large-range anchors because current positive ticks assume a 10h/16h-style range.
**How to avoid:** update range-specific tick generation and the tests that assert progress/tick behavior in the same task.

### Pitfall 2: Silently clamping existing >6h workday records on open
**What goes wrong:** old data becomes lossy when the editor opens.
**How to avoid:** explicitly decide and test how legacy values above the new focused range behave before changing the editor range.

### Pitfall 3: Fixing only one settings screen's bar color
**What goes wrong:** Theme screen looks correct but Rules/Data/About still mismatch.
**How to avoid:** apply the harmony fix in shared settings chrome.

### Pitfall 4: Keeping the current holiday feed while changing only local pay logic
**What goes wrong:** remote refresh reintroduces flattened `HOLIDAY` day types and overwrites the new semantics.
**How to avoid:** upgrade the source/schema and repository path, not just the calculator assertions.

### Pitfall 5: Treating all public-off days as statutory holidays
**What goes wrong:** Golden Week / Spring Festival blocks keep paying 3x for dates that should be 2x rest days.
**How to avoid:** map source data by wage tier (3 = `HOLIDAY`, 2 = `REST_DAY`, 1 makeup = `WORKDAY`).

---

## Code Examples / Verified Directions

### Current holiday typing bottleneck
```kotlin
data class HolidayYearRules(
    val holidayDates: Set<LocalDate>,
    val workingDates: Set<LocalDate>,
)

fun resolveDayType(date: LocalDate, override: DayType?): DayType {
    if (override != null) return override
    val yearRules = rulesProvider().years[date.year]
    if (date in yearRules.holidayDates) return DayType.HOLIDAY
    if (date in yearRules.workingDates) return DayType.WORKDAY
    return if (date.dayOfWeek.value in listOf(6, 7)) DayType.REST_DAY else DayType.WORKDAY
}
```

### Wage-aware official-off source direction
```json
{
  "10-01": { "holiday": true, "wage": 3, "name": "国庆节" },
  "10-04": { "holiday": true, "wage": 2, "name": "国庆节" },
  "10-10": { "holiday": false, "after": true, "wage": 1, "name": "国庆节后补班" }
}
```

### Existing theme chooser isolation point
```kotlin
ThemeModeChooser(
    selectedTheme = uiState.appTheme,
    activePalette = activePalette,
    onThemeSelected = onAppThemeChange,
)
```

---

## State of the Art

| Old Approach | Recommended Phase 5 Approach | Why |
|--------------|------------------------------|-----|
| Large theme preview cards + optional horizontal scroll | Compact always-visible 3-option selector | Matches the user's quick-switch request |
| Mixed filled + outlined data-management actions | Coordinated settings action emphasis | Improves visual coherence after Phase 04 migration |
| `holidayDates` vs `workingDates` only | `holidayDates` + `restDates` + `workingDates` (or equivalent explicit day typing) | Supports the requested 3x/2x distinction |
| Status-only holiday refresh source | Wage-aware holiday refresh source | Keeps remote updates aligned with payroll semantics |

---

## Open Questions

1. **Legacy >6h workday records**
   - What we know: the user wants a 6-hour-focused workday slider.
   - What's unclear: whether previously saved >6h workday entries must remain directly editable without extra handling.
   - Recommendation: plan explicit regression coverage and preserve editability instead of silently clamping.

2. **Holiday source migration scope**
   - What we know: the current source/schema is insufficient.
   - What's unclear: whether to fully replace the remote source now or keep a curated baseline plus remote fallback.
   - Recommendation: use a wage-aware source in this phase unless operational reliability requires a short-term hybrid.

---

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric + AndroidX Test + Compose UI Test |
| Config file | `app/build.gradle.kts` |
| Quick run command | `./gradlew.bat :app:testDebugUnitTest` |
| Full suite command | `./gradlew.bat :app:testDebugUnitTest :app:connectedDebugAndroidTest` |
| Estimated runtime | ~120-180 seconds |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| UXP-01 | Workday slider uses a 6-hour-focused range with safe legacy handling | unit + androidTest | `./gradlew.bat :app:testDebugUnitTest --tests "com.peter.overtimecalculator.CenteredDurationSliderMappingTest" --tests "com.peter.overtimecalculator.CenteredDurationSliderTest" && ./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.peter.overtimecalculator.DayEditorSliderTest` | ✅ extend existing |
| UXP-02 | Theme mode control shows all three choices without horizontal scroll and still switches correctly | androidTest | `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.peter.overtimecalculator.MainFlowTest` | ✅ extend existing |
| UXP-03 | Settings chrome/system-bar harmony remains build-safe and token-consistent | build + unit + manual | `./gradlew.bat :app:assembleDebug && ./gradlew.bat :app:testDebugUnitTest --tests "com.peter.overtimecalculator.ThemeDefaultsTest"` | ✅ extend existing |
| UXP-04 | Data-management actions keep separate behavior while sharing coordinated emphasis | androidTest + manual | `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.peter.overtimecalculator.DataManagementBackupRestoreTest` | ✅ extend existing |
| UXP-05 | Statutory 3x days resolve as `HOLIDAY`, 2x official-off days resolve as `REST_DAY`, makeup days stay `WORKDAY` | unit/integration | `./gradlew.bat :app:testDebugUnitTest --tests "com.peter.overtimecalculator.DomainLogicTest" --tests "com.peter.overtimecalculator.HolidayRulesRepositoryTest" --tests "com.peter.overtimecalculator.HolidayRulesJsonParserTest" --tests "com.peter.overtimecalculator.TimorHolidayApiParserTest"` | ⚠️ add one new test file |

### Sampling Rate
- **After every task commit:** run the targeted command for the touched files.
- **After every wave:** run `./gradlew.bat :app:testDebugUnitTest`.
- **Before `/gsd-verify-work`:** run full unit + connected androidTest coverage for Phase 5 regressions.
- **Max feedback latency:** < 180 seconds for quick checks.

### Wave 0 Gaps
- Existing infrastructure covers the phase; no new framework install is required.
- Add only one new focused parser/unit test file if the holiday-source migration introduces a new parser surface.

---

## Sources

### Primary (HIGH confidence)
- Project files: `DayEditorSheet.kt`, `CenteredDurationSlider.kt`, `ThemeModeSections.kt`, `ThemeSettingsScreen.kt`, `SettingsCommon.kt`, `DataManagementSections.kt`, `HolidayRules.kt`, `HolidayRulesRepository.kt`, `HolidayHaoshenqiApiParser.kt`, `cn_mainland.json`, `DomainLogicTest.kt`
- Android Developers guidance on Compose Slider / segmented buttons / edge-to-edge system bars (summarized during research)
- Timor holiday API sample payload for 2026 showing `wage` values 1/2/3 (`https://timor.tech/api/holiday/year/2026`)

### Secondary (MEDIUM confidence)
- General China overtime-pay summaries referencing Labor Law Article 44 and the distinction between statutory holidays (3x) and rest days (2x)
