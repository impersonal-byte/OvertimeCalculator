# Phase 3: Data backup and restore - Context

**Gathered:** 2026-03-15
**Status:** Ready for planning

<domain>
## Phase Boundary

Close the current export-only data gap by adding an explicit in-app backup and restore workflow for durable overtime data. This phase focuses on full-fidelity business-state portability and restore safety, not on turning the existing monthly CSV share file into the sole long-term backup format.

</domain>

<decisions>
## Implementation Decisions

### Backup scope
- Treat the current export-only state as a product gap that needs a restore-capable workflow.
- Do not model the existing monthly CSV export as the primary restore format, because it is a lossy derived view rather than the full business truth.
- Backups must cover the durable business state required to reconstruct months correctly.

### Restore fidelity
- Preserve `monthly_config`, `overtime_entry`, and `holiday_override` semantics during restore.
- Respect month-level configuration propagation rules so restored data remains logically trustworthy after import.
- Prefer a format and pipeline that can round-trip the app's business state, not just visible calendar rows.

### User-facing workflow
- Add the backup/restore workflow from the existing data-management area rather than inventing a disconnected entry point.
- Keep the existing CSV export as a lightweight share/export feature.
- Make it explicit in the UI that CSV export is not the same as full backup/restore.

### Safety and validation
- Restore needs visible validation and clear success/failure feedback.
- Handle replace/merge/conflict behavior deliberately instead of silently overwriting durable data.
- Design the workflow so users can understand what will be restored before destructive changes happen.

### Claude's Discretion
- Exact backup file format and extension.
- Whether restore supports full replace only or offers limited merge options.
- Exact copy, screen layout, and whether restore is gated behind an extra confirmation sheet/dialog.

</decisions>

<specifics>
## Specific Ideas

- The current user concern is: "项目已经有数据导出功能，但是还没有数据导入，有严重的逻辑漏洞。"
- Existing product docs already position data management as a future home for export, backup, and restore.
- Existing monthly CSV export should remain available, but planning should avoid institutionalizing a lossy CSV round-trip as the main restore contract.

</specifics>

<code_context>
## Existing Code Insights

### Reusable Assets
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/DataManagementScreen.kt`: existing settings entry point where backup/restore actions should surface.
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/DataManagementSections.kt`: currently export-only UI section; likely extension point for backup and restore cards/actions.
- `app/src/main/java/com/peter/overtimecalculator/data/repository/OvertimeRepository.kt`: current business-state assembly and write orchestration boundary.
- `app/src/main/java/com/peter/overtimecalculator/data/db/Entities.kt`: source-of-truth tables that backup/restore must preserve.
- `app/src/main/java/com/peter/overtimecalculator/data/db/OvertimeDao.kt`: current low-level persistence API that may need bulk snapshot/restore support.

### Established Patterns
- The app uses Room as the durable business-data source, while CSV output is documented as a temporary derived file rather than business truth.
- Settings actions are currently routed through Compose screens and ViewModel callbacks rather than standalone Android activities.
- The app already uses explicit snackbar-style feedback and test tags for user-visible flows.

### Integration Points
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt`: current export action owner and likely place to coordinate backup/restore commands if this remains in the main app ViewModel.
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppEffects.kt`: existing effect layer for file sharing; similar orchestration patterns may be needed for file picking/open-document flows.
- `app/src/main/java/com/peter/overtimecalculator/domain/ConfigPropagationPlanner.kt`: restore logic must not break forward-propagated monthly config behavior.

</code_context>

<deferred>
## Deferred Ideas

- Cloud sync or account-based cross-device synchronization.
- Turning CSV into a universal full-fidelity import/export contract for all future versions.
- Broad storage-architecture refactors unrelated to shipping backup/restore safely in the current single-module app.

</deferred>

---

*Phase: 03-data-backup-and-restore*
*Context gathered: 2026-03-15*
