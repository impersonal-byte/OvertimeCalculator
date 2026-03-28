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
- [x] **DATA-02**: User can restore a backup through an explicit in-app import flow with validation and clear outcome messaging
- [ ] **DATA-03**: Restored data preserves `monthly_config`, `overtime_entry`, and `holiday_override` semantics, including month-level configuration propagation behavior
- [x] **DATA-04**: CSV export remains available as a lightweight share/export feature and is clearly separate from backup/restore

### UX Detail Polish

- [x] **UXP-01**: Workday day-entry uses a 6-hour-focused slider range for finer control without breaking valid comp-time editing or legacy overtime records
- [x] **UXP-02**: Theme mode switching is available as a compact 3-option control with all choices visible without horizontal scrolling
- [x] **UXP-03**: Settings screens present status-bar/top-bar chrome that visually matches the settings surface language in light and dark themes
- [x] **UXP-04**: Data-management backup, restore, and CSV export actions use coordinated button emphasis so no single action looks visually out of family
- [x] **UXP-05**: Only 3x-pay statutory dates resolve as `HOLIDAY`; other official days off resolve as `REST_DAY`, while makeup workdays remain `WORKDAY`

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
| DATA-01 | Phase 3 | Complete |
| DATA-02 | Phase 3 | Complete |
| DATA-03 | Phase 3 | Complete |
| DATA-04 | Phase 3 | Complete |
| UXP-01 | Phase 5 | Complete |
| UXP-02 | Phase 5 | Complete |
| UXP-03 | Phase 5 | Complete |
| UXP-04 | Phase 5 | Complete |
| UXP-05 | Phase 5 | Complete |

**Coverage:**
- v1 requirements: 13 total
- Mapped to phases: 13
- Unmapped: 0 ✓

---
*Requirements defined: 2026-03-14*
*Last updated: 2026-03-28 after Phase 05 completion*
