---
phase: 03-data-backup-and-restore
plan: 01
subsystem: data-backup
tags: [backup, snapshot, json-codec, restore-preview]

# Dependency graph
requires:
  - phase: 02-data-entry
    provides: Durable business state entities (MonthlyConfigEntity, OvertimeEntryEntity, HolidayOverrideEntity)
provides:
  - Versioned backup snapshot contract (BackupSnapshot)
  - Backup file extension (.obackup) and MIME type (application/overtime-backup)
  - JSON codec for encode/decode round-trip
  - RestorePreview for pre-restore validation
affects: [03-02-restore-workflow, 03-03-backup-ui, 03-04-backup-integration]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Versioned backup contract: schema version field for future compatibility"
    - "Non-destructive preview: RestorePreview sealed class for confirmation UI"
    - "Separation of concerns: backup uses dedicated format, not CSV"

key-files:
  created:
    - app/src/main/java/com/peter/overtimecalculator/domain/BackupSnapshot.kt
    - app/src/main/java/com/peter/overtimecalculator/data/backup/BackupSnapshotCodec.kt
    - app/src/test/java/com/peter/overtimecalculator/BackupSnapshotTest.kt

key-decisions:
  - "Used .obackup extension and application/overtime-backup MIME type to distinguish from CSV"
  - "Included schema version field to enable future compatibility checks before restore"
  - "Created RestorePreview sealed class to drive confirmation UI before destructive writes"

patterns-established:
  - "Backup contract excludes update-session and holiday-cache metadata (test verifies)"
  - "Codec uses regex-based JSON parsing without external dependencies"

requirements-completed: [DATA-01]

# Metrics
duration: 25min
completed: 2026-03-15
---

# Phase 3 Plan 1: Backup Snapshot Contract Summary

**Versioned backup snapshot contract with dedicated file type, JSON codec, and non-destructive RestorePreview for confirmation UI**

## Performance

- **Duration:** 25 min
- **Started:** 2026-03-15T06:10:00Z
- **Completed:** 2026-03-15T06:35:00Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments

- Created versioned `BackupSnapshot` data class with schema version for future compatibility
- Defined dedicated backup extension (`.obackup`) and MIME type (`application/overtime-backup`) - distinct from CSV export
- Implemented `BackupSnapshotCodec` with JSON encode/decode round-trip
- Added `RestorePreview` sealed class to validate and preview restore content before destructive writes
- Tests verify: round-trip preservation, schema versioning, exclusion of update-session/holiday-cache metadata

## Task Commits

Each task was committed atomically:

1. **Task 1: Write failing snapshot contract tests** - `cd77be0` (test)
2. **Task 2: Implement versioned snapshot models and codec** - `42f5324` (feat)

**Plan metadata:** (pending docs commit)

## Files Created/Modified

- `app/src/main/java/com/peter/overtimecalculator/domain/BackupSnapshot.kt` - BackupSnapshot, BackupMonthlyConfig, BackupOvertimeEntry, BackupHolidayOverride, RestorePreview
- `app/src/main/java/com/peter/overtimecalculator/data/backup/BackupSnapshotCodec.kt` - JSON encode/decode codec
- `app/src/test/java/com/peter/overtimecalculator/BackupSnapshotTest.kt` - Round-trip and validation tests

## Decisions Made

- Used `.obackup` extension and `application/overtime-backup` MIME type to mechanically distinguish backup files from CSV exports
- Schema version field enables future restore validation before writing - prevents incompatible data from corrupting app state
- RestorePreview is a sealed class with Compatible/Incompatible subclasses to drive confirmation UI

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## Next Phase Readiness

- Backup snapshot contract is ready for downstream phases (restore workflow, backup UI, backup integration)
- RestorePreview enables non-destructive preview before any destructive write operation
- Versioned schema allows future restores to validate compatibility before proceeding

---
*Phase: 03-data-backup-and-restore*
*Completed: 2026-03-15*
