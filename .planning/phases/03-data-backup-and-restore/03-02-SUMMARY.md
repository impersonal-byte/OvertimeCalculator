---
phase: 03-data-backup-and-restore
plan: 02
subsystem: data
tags: [backup, restore, room, persistence]

# Dependency graph
requires:
  - phase: 03-data-backup-and-restore
    provides: BackupSnapshot contract, BackupSnapshotCodec from plan 03-01
provides:
  - BackupRestoreRepository with exportSnapshot, previewRestore, restoreSnapshot
  - Bulk DAO operations for full snapshot read/replace
  - Wired backup service via AppContainer
affects: [backup-ui, restore-ui]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - Repository pattern for data boundary
    - Snapshot export/restore transaction
    - Preview-before-restore validation

key-files:
  created:
    - app/src/main/java/com/peter/overtimecalculator/data/backup/BackupRestoreRepository.kt
    - app/src/test/java/com/peter/overtimecalculator/BackupRestoreDaoTest.kt
    - app/src/test/java/com/peter/overtimecalculator/BackupRestoreRepositoryTest.kt
  modified:
    - app/src/main/java/com/peter/overtimecalculator/data/db/OvertimeDao.kt
    - app/src/main/java/com/peter/overtimecalculator/data/AppContainer.kt

key-decisions:
  - "Restore is true clear-and-replace: deletes all old rows before inserting snapshot"
  - "Restore preserves exact durable rows - no replay of write use cases"
  - "PreviewRestore is non-destructive - validates before any write"
  - "Backup excludes update-session and holiday-cache metadata per contract"

patterns-established:
  - "Pattern: Bulk DAO operations for snapshot export/import"
  - "Pattern: Non-destructive preview before destructive restore"

requirements-completed: [DATA-01, DATA-03]

# Metrics
duration: 10min
completed: 2026-03-15
---

# Phase 3 Plan 2: Backup/Restore Repository Summary

**Room-backed persistence layer with snapshot export/restore and preview validation**

## Performance

- **Duration:** 10 min
- **Started:** 2026-03-15T13:30:00Z
- **Completed:** 2026-03-15T13:40:00Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments
- Added bulk DAO operations for full snapshot read and replace (getAllEntries, getAllOverrides, upsertEntries, upsertOverrides)
- Implemented BackupRestoreRepository with exportSnapshot, previewRestore, restoreSnapshot
- Wired BackupRestoreRepository through AppContainer for UI access
- Excluded update-session and holiday-cache metadata from restore contract
- Added comprehensive tests for DAO contract and repository round-trips

## Task Commits

Each task was committed atomically:

1. **Task 1: Add bulk snapshot read and replace primitives** - `0da4503` (feat)
2. **Task 2: Implement backup/restore repository** - `e76480f` (feat)

## Files Created/Modified
- `app/src/main/java/com/peter/overtimecalculator/data/db/OvertimeDao.kt` - Added bulk read/replace operations
- `app/src/main/java/com/peter/overtimecalculator/data/backup/BackupRestoreRepository.kt` - New repository
- `app/src/main/java/com/peter/overtimecalculator/data/AppContainer.kt` - Wired backup service
- `app/src/test/java/com/peter/overtimecalculator/BackupRestoreDaoTest.kt` - DAO contract tests
- `app/src/test/java/com/peter/overtimecalculator/BackupRestoreRepositoryTest.kt` - Repository tests

## Decisions Made
- Restore preserves exact durable rows rather than replaying write use cases month-by-month
- previewRestore is non-destructive - validates snapshot before any write
- Backup/restore excludes update-session and holiday-cache metadata per the backup contract

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed restore not performing true clear-and-replace**
- **Found during:** Orchestrator verification after plan completion
- **Issue:** Initial implementation used upsert (INSERT REPLACE) which only replaces matching primary keys, but doesn't clear old rows not in the snapshot. This is NOT true replace semantics.
- **Fix:** Added `deleteAllConfigs()`, `deleteAllEntries()`, `deleteAllOverrides()` to DAO. Updated repository to call delete-all before insert to ensure true clear-and-replace behavior.
- **Files modified:** `app/src/main/java/com/peter/overtimecalculator/data/db/OvertimeDao.kt`, `app/src/main/java/com/peter/overtimecalculator/data/backup/BackupRestoreRepository.kt`
- **Verification:** Added test `restoreSnapshot_clearsOldDataNotInSnapshot_trueReplaceBehavior` that proves old data is cleared when restoring a subset snapshot
- **Committed in:** `f4a2b1c` (fix commit)

---

**Total deviations:** 1 auto-fixed (1 bug fix)
**Impact on plan:** Fix ensures restore has true clear-and-replace semantics as specified in plan. Tests now verify this behavior.

## Issues Encountered
None

## Next Phase Readiness
- Backup/restore service is wired through AppContainer and ready for UI consumption
- The service provides snapshot export from Room truth and restore with preview validation
- Phase 3 Wave 2 (this plan) builds on Wave 1 (03-01) artifacts successfully
