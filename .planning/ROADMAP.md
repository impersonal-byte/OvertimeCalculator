# Roadmap: 加薪 / OvertimeCalculator

## Overview

This roadmap starts by establishing a durable GSD planning baseline for an already-shipping brownfield Android app. Once the planning layer is in place, milestone work and quick tasks can proceed with shared context, persistent state, and consistent execution history.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: Planning Baseline** - Initialize GSD project memory and quick-task support for the existing app
- [x] **Phase 2: No handwritten input** - Define a non-handwritten, structured day-entry interaction for overtime and comp time
- [x] **Phase 3: Data backup and restore** - Close the export-only data gap with app-controlled full-fidelity backup and restore (completed 2026-03-15)

## Phase Details

### Phase 1: Planning Baseline
**Goal**: Establish `PROJECT.md`, `REQUIREMENTS.md`, `ROADMAP.md`, `STATE.md`, and quick-task conventions so future work can run through GSD without re-discovering repo context.
**Depends on**: Nothing (first phase)
**Requirements**: [PLAN-01, PLAN-02, PLAN-03, QTK-01]
**Success Criteria** (what must be TRUE):
  1. Maintainer can open `.planning/PROJECT.md` and understand the app's validated capabilities, current constraints, and key decisions.
  2. Maintainer can open `.planning/STATE.md` and know the current focus, most recent activity, blockers, and quick-task history.
  3. Quick workflow prerequisites exist in this repo because `.planning/ROADMAP.md` and `.planning/STATE.md` are present and consistent.
**Plans**: 1 plan

Plans:
- [ ] 01-01: Initialize brownfield GSD planning artifacts and enable quick-task tracking

## Progress

**Execution Order:**
Phases execute in numeric order: 1 -> 2 -> 3

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Planning Baseline | 0/1 | Not started | - |
| 2. No handwritten input | 2/2 | Complete | 2026-03-15 |
| 3. Data backup and restore | 3/4 | Complete    | 2026-03-15 |

### Phase 2: No handwritten input

**Goal:** Replace the current stepper-led day editor with a centered, non-handwritten, structured signed-duration input that uses slider-only entry and preserves the existing signed-minute save flow.
**Requirements**: [Context-driven - centered slider day entry, chips removed, settings numeric forms unchanged]
**Depends on:** Phase 1
**Plans:** 2 plans

**Success Criteria** (what must be TRUE):
  1. The day editor uses a centered signed-duration input with a visually clear zero point and sparse major tick markers that remain readable on phone-sized screens.
  2. Daily entry still saves through the existing signed-minute pipeline, with negative values remaining restricted to valid day types.
  3. The centered slider is the sole duration control, while reset/clear remains available and settings numeric forms stay unchanged.

Plans:
- [x] 02-01: Create centered duration slider with TDD (pure mapping helper + composable)
- [x] 02-02: Integrate centered slider into day editor sheet

### Phase 3: Data backup and restore

**Goal:** Add an app-controlled backup and restore workflow that can round-trip the full overtime business state across devices or reinstalls without relying on lossy CSV exports or opaque OS-level migration.
**Requirements**: [DATA-01, DATA-02, DATA-03, DATA-04]
**Depends on:** Phase 2
**Plans:** 4/4 plans complete

**Success Criteria** (what must be TRUE):
  1. Users can create a backup from the data-management area that captures the full business state required to reconstruct overtime months, not just a derived CSV view.
  2. Users can restore a backup through an explicit in-app workflow with validation, clear conflict/replace behavior, and visible success or failure feedback.
  3. Restored data preserves monthly configuration, overtime entries, holiday overrides, and month-propagation semantics so calculations remain trustworthy after import.
  4. Existing CSV export remains available as a lightweight share/export feature and is not mislabeled as a full restore path.

Plans:
- [x] 03-01: Define versioned backup snapshot contract and codec with TDD
- [x] 03-02: Implement persistence-layer backup and restore engine
- [x] 03-03: Align Android auto-backup scope with manual restore rules
- [x] 03-04: Ship data-management backup/restore UI and flow tests
