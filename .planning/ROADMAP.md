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
- [x] **Phase 4: Animeko visual migration** - Reference the in-repo `animeko` implementation and migrate its interface and color language into this app, replacing prior custom UI styling (completed 2026-03-21)
- [x] **Phase 4.1: 亮色卡片层次修复 (INSERTED)** - Urgent fix for light-theme card contrast and visual hierarchy after 04 rollout (completed 2026-03-21)

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
Phases execute in numeric order: 1 -> 2 -> 3 -> 4 -> 4.1

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Planning Baseline | 0/1 | Not started | - |
| 2. No handwritten input | 2/2 | Complete | 2026-03-15 |
| 3. Data backup and restore | 4/4 | Complete    | 2026-03-15 |
| 4. Animeko visual migration | 5/5 | Complete | 2026-03-21 |
| 4.1. 亮色卡片层次修复 (INSERTED) | 3/3 | Complete | 2026-03-21 |

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
**Plans:** 5/5 plans complete

**Success Criteria** (what must be TRUE):
  1. The app has a documented migration direction for applying Animeko-inspired color/theme behavior to this project's Compose theme system.
  2. Existing visual surfaces that currently define the app's look-and-feel (theme settings, shell-level containers, core cards) have a clear replacement path instead of incremental one-off styling.
  3. Context decisions make downstream planning unambiguous about what is being replaced versus reused.

Plans:
- [x] 04-01: Capture phase context and implementation decisions for Animeko visual migration
- [x] 04-02: Build HCT semantic theme foundation and dynamic-color precedence
- [x] 04-03: Migrate shell and home surfaces to semantic wrapper styling
- [x] 04-04: Migrate day editor and theme settings surfaces without behavior changes
- [x] 04-05: Complete remaining settings migration and remove legacy style paths

### Phase 04.1: 修复亮色主题卡片层次与对比度不足 (INSERTED)

**Goal:** 修复亮色主题下卡片边界与层次不明显的问题，确保核心卡片在浅色模式可见、可读、可辨识，同时不破坏暗色主题表现。
**Requirements**: [Context-driven - light-theme hierarchy/contrast fix, no behavior changes]
**Depends on:** Phase 4
**Plans:** 3/3 plans complete

**Success Criteria** (what must be TRUE):
  1. 亮色主题下首页与设置页关键卡片容器可清晰区分（边界、层次、分组关系明确）。
  2. 文本与卡片背景对比度提升，弱化文本仍可读，且不造成暗色主题回归。
  3. 仅调整视觉 token/层级，不改变业务交互语义与数据逻辑。
  4. 动态色开关在亮色模式下仍保持一致行为，不出现“卡片消失感”。

Plans:
- [x] 04.1-01: 调整亮色语义层级与关键卡片边界，修复卡片可见性
- [x] 04.1-02: 用语义分层色卡替换边框补丁，按真机反馈修复亮色层次
- [x] 04.1-03: Strengthen light-theme tonal layers for core cards and add luminance guardrails


