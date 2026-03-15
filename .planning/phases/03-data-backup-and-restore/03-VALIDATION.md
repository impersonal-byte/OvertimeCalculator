---
phase: 03
slug: data-backup-and-restore
status: draft
nyquist_compliant: true
wave_0_complete: false
created: 2026-03-15
---

# Phase 03 — Validation Strategy

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

- **After every task commit:** Run `./gradlew.bat :app:testDebugUnitTest`
- **After every plan wave:** Run `./gradlew.bat :app:testDebugUnitTest :app:connectedDebugAndroidTest`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 180 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 03-01-01 | 01 | 1 | DATA-01 | unit | `./gradlew.bat :app:testDebugUnitTest --tests "*Backup*"` | ❌ W0 | ⬜ pending |
| 03-01-02 | 01 | 1 | DATA-02 | unit | `./gradlew.bat :app:testDebugUnitTest --tests "*Restore*"` | ❌ W0 | ⬜ pending |
| 03-02-01 | 02 | 2 | DATA-03 | integration | `./gradlew.bat :app:testDebugUnitTest --tests "*Repository*"` | ❌ W0 | ⬜ pending |
| 03-02-02 | 02 | 2 | DATA-04 | androidTest | `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.peter.overtimecalculator.MainFlowTest` | ⚠️ partial | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `app/src/test/java/com/peter/overtimecalculator/BackupSnapshotTest.kt` — serialization/parsing and snapshot schema stubs
- [ ] `app/src/test/java/com/peter/overtimecalculator/RestoreUseCaseTest.kt` — restore validation and replace semantics stubs
- [ ] `app/src/test/java/com/peter/overtimecalculator/BackupRestoreRepositoryTest.kt` — repository/database round-trip stubs
- [ ] `app/src/androidTest/java/com/peter/overtimecalculator/DataManagementBackupRestoreTest.kt` — UI flow and picker-result stubs

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Backup/restore copy makes sense to users | DATA-02 | UX clarity and destructive-action comprehension depend on final wording | Open data management, trigger backup and restore, confirm labels clearly distinguish CSV export vs full backup and explain replacement scope |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 180s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
