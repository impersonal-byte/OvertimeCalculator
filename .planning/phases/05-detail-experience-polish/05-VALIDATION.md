---
phase: 05
slug: detail-experience-polish
status: planned
nyquist_compliant: true
wave_0_complete: true
created: 2026-03-24
---

# Phase 05 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric + AndroidX Test + Compose UI Test |
| **Config file** | `app/build.gradle.kts` |
| **Quick run command** | `./gradlew.bat :app:testDebugUnitTest` |
| **Full suite command** | `./gradlew.bat :app:testDebugUnitTest :app:connectedDebugAndroidTest` |
| **Estimated runtime** | ~180 seconds |

---

## Sampling Rate

- **After every task commit:** Run the task's targeted command
- **After every plan wave:** Run `./gradlew.bat :app:testDebugUnitTest`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 180 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 05-01-01 | 01 | 1 | UXP-05 holiday/source contract | unit | `./gradlew.bat :app:testDebugUnitTest --tests "com.peter.overtimecalculator.HolidayRulesJsonParserTest" --tests "com.peter.overtimecalculator.TimorHolidayApiParserTest"` | ⚠️ extend + 1 new | ⬜ pending |
| 05-01-02 | 01 | 1 | UXP-05 day-type resolution + pay math | unit/integration | `./gradlew.bat :app:testDebugUnitTest --tests "com.peter.overtimecalculator.DomainLogicTest" --tests "com.peter.overtimecalculator.HolidayRulesRepositoryTest"` | ✅ | ⬜ pending |
| 05-02-01 | 02 | 1 | UXP-01 workday slider precision | unit | `./gradlew.bat :app:testDebugUnitTest --tests "com.peter.overtimecalculator.CenteredDurationSliderMappingTest" --tests "com.peter.overtimecalculator.CenteredDurationSliderTest"` | ✅ | ⬜ pending |
| 05-02-02 | 02 | 1 | UXP-01 editor integration and UXP-04 data-management action consistency | androidTest | `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.peter.overtimecalculator.DayEditorSliderTest && ./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.peter.overtimecalculator.DataManagementBackupRestoreTest` | ✅ | ⬜ pending |
| 05-03-01 | 03 | 1 | UXP-02 compact theme selector | androidTest | `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.peter.overtimecalculator.MainFlowTest` | ✅ | ⬜ pending |
| 05-03-02 | 03 | 1 | UXP-03 settings chrome harmony | build + unit | `./gradlew.bat :app:assembleDebug && ./gradlew.bat :app:testDebugUnitTest --tests "com.peter.overtimecalculator.ThemeDefaultsTest"` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- Existing infrastructure covers all phase requirements.
- If a new wage-aware holiday parser is introduced, add one focused parser test file before implementation.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Settings status-bar/top-bar visual harmony in light/dark mode | UXP-03 | System-bar appearance is difficult to assert robustly without screenshot/device infrastructure | Open Theme, Rules, Data, and About screens in light and dark mode; confirm the top inset/status-bar area reads as part of the same settings chrome instead of a mismatched strip |
| Data-management action color coordination | UXP-04 | Existing test suite does not assert rendered colors | Open the data-management screen and confirm backup / restore / CSV actions look intentionally related while retaining distinct labels and behaviors |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 180s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
