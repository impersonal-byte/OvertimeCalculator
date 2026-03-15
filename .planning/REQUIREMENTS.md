# Requirements: 加薪 / OvertimeCalculator

**Defined:** 2026-03-14
**Core Value:** 用户可以稳定记录每天的加班或调休，并即时得到可信的当月加班工资结果。

## v1 Requirements

Requirements for the initial GSD planning baseline. Each maps to roadmap phases.

### Planning Foundation

- [ ] **PLAN-01**: Maintainer can read `.planning/PROJECT.md` and understand the app's purpose, core value, constraints, and validated shipped capabilities
- [ ] **PLAN-02**: Maintainer can read `.planning/ROADMAP.md` and see an initial execution phase for planning-baseline work
- [ ] **PLAN-03**: Maintainer can read `.planning/STATE.md` and immediately know current focus, last activity, blockers, and session continuity

### Quick Tasks

- [ ] **QTK-01**: Maintainer can record ad-hoc investigations under `.planning/quick/` without changing milestone roadmap structure

### Data Portability

- [ ] **DATA-01**: User can create an app-controlled backup that captures the durable overtime business state needed to reconstruct months across devices or reinstalls
- [ ] **DATA-02**: User can restore a backup through an explicit in-app import flow with validation and clear outcome messaging
- [ ] **DATA-03**: Restored data preserves `monthly_config`, `overtime_entry`, and `holiday_override` semantics, including month-level configuration propagation behavior
- [ ] **DATA-04**: CSV export remains available as a lightweight share/export feature and is clearly separate from backup/restore

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Product Evolution

- **EVOL-01**: Maintainer can roadmap UI coordination simplification for large Compose screens and app shell logic
- **EVOL-02**: Maintainer can roadmap repository hygiene and CI coverage improvements highlighted in `.planning/codebase/CONCERNS.md`
- **EVOL-03**: Maintainer can roadmap future architecture scaling decisions around manual dependency wiring and storage boundaries

## Out of Scope

| Feature | Reason |
|---------|--------|
| Introduce Hilt during initialization | Not required to establish planning baseline and would change runtime architecture |
| Split the app into multiple Gradle modules during initialization | Too large for docs bootstrap and not required for quick workflow enablement |
| Ship new end-user app features as part of planning init | This work is for workflow enablement, not product expansion |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| PLAN-01 | Phase 1 | Pending |
| PLAN-02 | Phase 1 | Pending |
| PLAN-03 | Phase 1 | Pending |
| QTK-01 | Phase 1 | Pending |
| DATA-01 | Phase 3 | Pending |
| DATA-02 | Phase 3 | Pending |
| DATA-03 | Phase 3 | Pending |
| DATA-04 | Phase 3 | Pending |

**Coverage:**
- v1 requirements: 8 total
- Mapped to phases: 8
- Unmapped: 0 ✓

---
*Requirements defined: 2026-03-14*
*Last updated: 2026-03-14 after initial definition*
