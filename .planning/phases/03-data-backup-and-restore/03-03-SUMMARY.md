---
phase: 03-data-backup-and-restore
plan: 03
subsystem: infra
tags: [android, backup, restore, datastore, sharedpreferences]

# Dependency graph
requires:
  - phase: 03-data-backup-and-restore
    provides: Manual backup/restore repository from 03-02
provides:
  - Narrowed Android platform backup rules (backup_rules.xml, data_extraction_rules.xml)
  - Documented storage boundaries for backup/restore behavior
affects: [data-backup-and-restore]

# Tech tracking
tech-stack:
  added: []
  patterns: [android-backup-rules, explicit-include-exclude]

key-files:
  created: []
  modified:
    - app/src/main/res/xml/backup_rules.xml
    - app/src/main/res/xml/data_extraction_rules.xml
    - docs/storage-boundaries.md

key-decisions:
  - "Exclude UpdateSessionStore from platform backup (volatile download state)"
  - "Exclude HolidayRulesRepository cache from platform backup (network-refreshed)"
  - "Manual .obackup scope matches platform backup (Room data only, same as 03-02 contract)"

patterns-established:
  - "Platform auto-backup: Room DB + user preferences only"
  - "Manual backup: same as platform (Room data only, excludes holiday cache)"
  - "Volatile session state excluded from both backup types"

requirements-completed: [DATA-03]

# Metrics
duration: 7min
completed: 2026-03-15
---

# Phase 3 Plan 3: Align Android Backup Rules Summary

**Narrowed Android platform backup to durable user state, excluding volatile update-session and holiday cache, with documented boundaries**

## Performance

- **Duration:** 7 min
- **Started:** 2026-03-15T06:45:05Z
- **Completed:** 2026-03-15T06:45:12Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- Replaced wide-open `backup_rules.xml` with explicit include/exclude rules
- Aligned `data_extraction_rules.xml` for cloud-backup and device-transfer
- Documented backup/restore boundaries in storage-boundaries.md with rationale

## Task Commits

Each task was committed atomically:

1. **Task 1: Narrow Android backup rules to durable user state** - `6f2ce0f` (feat)
2. **Task 2: Document manual backup scope and exclusions** - `764858a` (docs)

**Plan metadata:** (commit at final step)

## Files Created/Modified
- `app/src/main/res/xml/backup_rules.xml` - Explicit backup include/exclude rules
- `app/src/main/res/xml/data_extraction_rules.xml` - Cloud-backup and device-transfer rules
- `docs/storage-boundaries.md` - Backup/restore boundary documentation

## Decisions Made
- Exclude UpdateSessionStore (app-update-prefs.xml) from platform backup - download state becomes invalid after restore
- Exclude HolidayRulesRepository cache (datastore/) - automatically refreshes from network
- Include Room database and user appearance preferences - these are durable user state

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

Phase 3 backup/restore complete. All 3 plans executed:
- 03-01: Manual backup feature
- 03-02: Manual restore with clear-and-replace semantics  
- 03-03: Platform backup alignment

Ready for next roadmap decision.

---
*Phase: 03-data-backup-and-restore*
*Completed: 2026-03-15*
