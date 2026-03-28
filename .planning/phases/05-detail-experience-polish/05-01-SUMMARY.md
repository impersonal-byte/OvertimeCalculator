---
phase: 05-detail-experience-polish
plan: 01
subsystem: domain
tags: [holidays, payroll, timor, datastore]

requires:
  - phase: 04-animeko-visual-migration
    provides: Shared app shell and settings surfaces that Phase 05 continues polishing
provides:
  - Wage-aware holiday day typing with separate statutory holidays, official-off rest days, and makeup workdays
  - Timor-backed remote holiday parsing for yearly refresh
  - Payroll regression coverage for 3x/2x/1x holiday semantics
affects: [calendar, payroll, holiday-refresh]

tech-stack:
  added: [Timor holiday API payload parser]
  patterns: [backward-compatible additive holiday schema, resolve-day-type as single source of truth]

key-files:
  created: [app/src/main/java/com/peter/overtimecalculator/data/holiday/TimorHolidayApiParser.kt, app/src/test/java/com/peter/overtimecalculator/TimorHolidayApiParserTest.kt]
  modified: [app/src/main/java/com/peter/overtimecalculator/domain/HolidayRules.kt, app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesJsonParser.kt, app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesJsonSerializer.kt, app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesRepository.kt, app/src/main/assets/holidays/cn_mainland.json, app/src/test/java/com/peter/overtimecalculator/DomainLogicTest.kt, app/src/test/java/com/peter/overtimecalculator/HolidayRulesRepositoryTest.kt, app/src/test/java/com/peter/overtimecalculator/HolidayRulesJsonParserTest.kt]

key-decisions:
  - "Keep schemaVersion at 1 and add optional restDates for backward-compatible bundled/cache parsing"
  - "Make HolidayCalendar.resolveDayType() the only place that translates statutory holidays, official-off rest days, and makeup workdays"

patterns-established:
  - "Holiday rules snapshots can grow additively without forcing an asset/cache version bump when the old data still parses safely"
  - "Remote holiday refresh should consume a wage-aware source instead of flattening public-off days into one bucket"

requirements-completed: [UXP-05]
duration: interrupted-session
completed: 2026-03-28
---

# Phase 05 Plan 01: Holiday day-type reclassification summary

**Wage-aware holiday rules now distinguish statutory holidays, official-off rest days, and makeup workdays without calculator-specific exceptions.**

## Performance

- **Duration:** Interrupted session
- **Started:** Prior interrupted implementation session (exact timestamp not preserved)
- **Completed:** 2026-03-28T15:12:00Z
- **Tasks:** 2
- **Files modified:** 10

## Accomplishments
- Added `restDates` to the holiday rules model and taught `HolidayCalendar.resolveDayType()` to return `REST_DAY` for official-off dates.
- Replaced the active remote holiday parsing path with `TimorHolidayApiParser`, which maps `wage` values into `HOLIDAY` / `REST_DAY` / `WORKDAY` semantics.
- Regenerated the bundled 2026 baseline holiday asset so statutory days and official-off rest days are no longer flattened together.

## Task Commits

No implementation-time git commits were created for this plan because the user did not request code commits during Phase 05 execution.

Plan metadata commit was also not created in this execution session.

## Files Created/Modified
- `app/src/main/java/com/peter/overtimecalculator/domain/HolidayRules.kt` - adds `restDates` and resolves them as `REST_DAY`
- `app/src/main/java/com/peter/overtimecalculator/data/holiday/TimorHolidayApiParser.kt` - parses wage-aware yearly holiday payloads
- `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesRepository.kt` - refreshes yearly overlays from Timor and preserves meaningful overrides
- `app/src/main/assets/holidays/cn_mainland.json` - stores separated `holidayDates` and `restDates` for 2026 baseline data
- `app/src/test/java/com/peter/overtimecalculator/TimorHolidayApiParserTest.kt` - locks Timor parsing behavior with focused tests

## Decisions Made
- Kept the snapshot schema at version 1 and made `restDates` optional in JSON parsing so legacy cached/bundled data keeps loading cleanly.
- Preserved `HolidayCalendar.resolveDayType()` as the single source of truth so payroll logic inherits the new semantics automatically.

## Deviations from Plan

None - plan executed within the intended richer holiday-model approach.

## Issues Encountered
- Early RED coverage in `DomainLogicTest` referenced a non-existent `days` property; the test was corrected to assert against `ObservedMonth.dayCells` before production changes were written.
- Oracle review favored additive schema growth over a version bump, so the implementation stayed backward-compatible while still meeting the requirement.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Holiday classification now matches the intended 1.5x/2x/3x business semantics.
- Downstream UI and verification work can treat holiday typing as stable shared infrastructure.

---
*Phase: 05-detail-experience-polish*
*Completed: 2026-03-28*
