---
phase: 02
slug: no-handwritten-input
status: draft
nyquist_compliant: false
wave_0_complete: false
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
| 02-01-01 | 01 | 1 | centered day-entry control | unit | `./gradlew :app:testDebugUnitTest` | ⚠️ partial | ⬜ pending |
| 02-01-02 | 01 | 1 | slider snapping and range rules | unit | `./gradlew :app:testDebugUnitTest` | ❌ W0 | ⬜ pending |
| 02-02-01 | 02 | 2 | day-editor interaction flow | androidTest | `./gradlew :app:connectedDebugAndroidTest` | ✅ | ⬜ pending |
| 02-02-02 | 02 | 2 | presets remain available alongside slider | androidTest | `./gradlew :app:connectedDebugAndroidTest` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `app/src/test/java/com/peter/overtimecalculator/CenteredDurationSliderMappingTest.kt` - pure mapping and snapping coverage for centered slider conversion
- [ ] `app/src/androidTest/java/com/peter/overtimecalculator/DayEditorSliderTest.kt` - slider semantics and day-editor interaction coverage
- [ ] Slider semantics/test tags for `duration_slider`, `duration_slider_value`, and center marker visibility assertions

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Mobile visual density of major ticks and zero marker | readable slider markers | Visual crowding judgment depends on handset width and design feel | Open the day editor on a phone-sized device and verify major ticks remain readable while dense per-step indicators are not rendered |
| Drag feel and haptic cadence | low-friction structured input | Touch feel and haptic comfort are experiential rather than binary | Drag across negative and positive ranges, confirm detents feel stable and haptics do not feel noisy |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all missing references
- [ ] No watch-mode flags
- [ ] Feedback latency < 90s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
