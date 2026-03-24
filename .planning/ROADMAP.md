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
- [x] **Phase 4: Animeko visual migration** - Reference the in-repo `animeko` implementation and migrate its interface and color language into this app, replacing prior custom UI styling, including merged real-device light-theme hierarchy follow-up after rollout (completed 2026-03-21)
- [ ] **Phase 5: 细节体验优化** - Polish post-migration interaction fidelity in day entry, settings chrome, data-management actions, and holiday classification

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
Phases execute in numeric order: 1 -> 2 -> 3 -> 4 -> 5

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Planning Baseline | 0/1 | Not started | - |
| 2. No handwritten input | 2/2 | Complete | 2026-03-15 |
| 3. Data backup and restore | 4/4 | Complete    | 2026-03-15 |
| 4. Animeko visual migration | 6/6 | Complete | 2026-03-21 |
| 5. 细节体验优化 | 0/3 | Not started | - |

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

### Phase 4: Animeko visual migration

**Goal:** Learn how interface and color systems are implemented in the repository's `animeko` folder, then migrate that visual language into this app and replace the previous custom styling.
**Requirements**: [Context-driven - animeko-inspired UI language, theme/token parity strategy, replace old styling paths]
**Depends on:** Phase 3
**Plans:** 6/6 plans complete

**Success Criteria** (what must be TRUE):
  1. The app has a documented migration direction for applying Animeko-inspired color/theme behavior to this project's Compose theme system.
  2. Existing visual surfaces that currently define the app's look-and-feel (theme settings, shell-level containers, core cards) have a clear replacement path instead of incremental one-off styling.
  3. Context decisions make downstream planning unambiguous about what is being replaced versus reused.
  4. Real-device light-theme validation confirms that card hierarchy remains readable after rollout, without dark-theme regressions or border-only patching.

Plans:
- [x] 04-01: Capture phase context and implementation decisions for Animeko visual migration
- [x] 04-02: Build HCT semantic theme foundation and dynamic-color precedence
- [x] 04-03: Migrate shell and home surfaces to semantic wrapper styling
- [x] 04-04: Migrate day editor and theme settings surfaces without behavior changes
- [x] 04-05: Complete remaining settings migration and remove legacy style paths
- [x] 04-06: Merge post-device light-theme hierarchy follow-up back into Phase 04 and close with guardrail notes

### Phase 5: 细节体验优化

**Goal:** Tighten post-migration detail quality so day entry feels more precise, settings controls are faster to use, settings chrome feels visually consistent, data-management actions feel coordinated, and holiday pay classification matches the app's intended 1.5x/2x/3x semantics.
**Requirements**: [UXP-01, UXP-02, UXP-03, UXP-04, UXP-05]
**Depends on:** Phase 4
**Plans:** 3 plans

**Success Criteria** (what must be TRUE):
  1. Workday entry uses a tighter slider range that improves half-hour precision without regressing comp-time editing or silently destroying existing higher-hour records.
  2. Theme mode switching shows all three choices at once and no longer relies on sideways scrolling in settings.
  3. The status-bar/top-bar area on settings screens looks like part of the same surface system instead of a mismatched strip.
  4. Data-management backup, restore, and CSV export actions look intentionally related instead of mixing unrelated emphasis styles.
  5. Only statutory 3x-pay dates resolve as `HOLIDAY`, while other official days off resolve as `REST_DAY` and makeup workdays still resolve as `WORKDAY`.

Plans:
- [ ] 05-01: Reclassify holiday day types around 3x-only statutory dates
- [ ] 05-02: Tighten day-entry precision and align data-management actions
- [ ] 05-03: Compact theme switching and harmonize settings chrome


