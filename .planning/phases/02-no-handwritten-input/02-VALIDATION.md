---
phase: 02
slug: no-handwritten-input
status: planned
nyquist_compliant: true
wave_0_complete: true
created: 2026-03-15
---

# Phase 02 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | AndroidJUnit4 + Compose UI Test + JUnit4 unit tests |
| **Config file** | `app/build.gradle.kts` |
| **Quick run command** | `./gradlew :app:testDebugUnitTest` |
| **Full suite command** | `./gradlew :app:connectedDebugAndroidTest` |
| **Estimated runtime** | ~90 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew :app:testDebugUnitTest`
- **After every plan wave:** Run `./gradlew :app:connectedDebugAndroidTest`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 90 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 02-01-01 | 01 | 1 | DurationMapper pure helper | unit | `./gradlew :app:testDebugUnitTest --tests "CenteredDurationSliderMappingTest"` | ✅ W0 | ⬜ pending |
| 02-01-02 | 01 | 1 | CenteredDurationSlider composable | unit | `./gradlew :app:testDebugUnitTest --tests "CenteredDurationSliderTest"` | ✅ W0 | ⬜ pending |
| 02-01-03 | 01 | 1 | Accessibility semantics | unit | Code review | N/A | ⬜ pending |
| 02-02-01 | 02 | 2 | Replace stepper with slider | integration | `./gradlew :app:assembleDebug` | ✅ | ⬜ pending |
| 02-02-02 | 02 | 2 | Preset chips removed from day editor | integration | Code review | ✅ | ⬜ pending |
| 02-02-03 | 02 | 2 | DayEditorSliderTest | androidTest | `./gradlew :app:connectedDebugAndroidTest` | ✅ | ⬜ pending |
| 02-02-04 | 02 | 2 | Human verify | checkpoint | Manual | N/A | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements (COMPLETED)

- [x] `app/src/test/java/com/peter/overtimecalculator/CenteredDurationSliderMappingTest.kt` - pure mapping and snapping coverage for centered slider conversion
- [x] `app/src/test/java/com/peter/overtimecalculator/CenteredDurationSliderTest.kt` - slider composable tests
- [x] `app/src/androidTest/java/com/peter/overtimecalculator/DayEditorSliderTest.kt` - slider semantics and day-editor interaction coverage
- [x] Slider semantics/test tags: `duration_slider`, `duration_slider_value`, center marker

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Mobile visual density of major ticks and zero marker | readable slider markers | Visual crowding judgment depends on handset width and design feel | Open the day editor on a phone-sized device and verify major ticks remain readable while dense per-step indicators are not rendered |
| Drag feel and haptic cadence | low-friction structured input | Touch feel and haptic comfort are experiential rather than binary | Drag across negative and positive ranges, confirm detents feel stable and haptics do not feel noisy |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all missing references
- [x] No watch-mode flags
- [x] Feedback latency < 90s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
