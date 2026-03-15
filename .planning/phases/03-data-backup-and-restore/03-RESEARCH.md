# Phase 3: Data backup and restore - Research

**Researched:** 2026-03-15
**Domain:** Android backup/restore UX, Storage Access Framework, Room snapshot portability
**Confidence:** HIGH

## Summary

Phase 3 should not treat the existing monthly CSV export as the restore contract. The current app exports a derived, single-month share file from `OvertimeViewModel.exportMonthlyCsv()` and shares it through `Intent.ACTION_SEND`, while the durable business truth spans multiple Room tables and month-propagation rules.

Key findings:

1. **Current export is intentionally lossy** - `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt` writes a monthly CSV from current UI state and `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppEffects.kt` only shares it; it does not encode the full restorable business state.
2. **Restorable truth spans multiple tables and rules** - `monthly_config`, `overtime_entry`, and `holiday_override` live in `app/src/main/java/com/peter/overtimecalculator/data/db/Entities.kt`, while `ConfigPropagationPlanner.kt` means restore must preserve month-level config semantics, not only day rows.
3. **Data-management UI already exists as the right surface** - `app/src/main/java/com/peter/overtimecalculator/ui/settings/DataManagementScreen.kt` and `DataManagementSections.kt` are currently export-only, but release notes already frame this area as the home for future backup/restore.
4. **Android SAF is the right import/export entry model** - Official Android guidance recommends `ACTION_OPEN_DOCUMENT` + `CATEGORY_OPENABLE` for user-chosen files, and `ACTION_OPEN_DOCUMENT_TREE` only when the app needs durable directory access.
5. **System Auto Backup is useful but insufficient** - Android Auto Backup/device transfer can preserve app data, but official guidance and practical UX both distinguish OS disaster recovery from an explicit in-app backup/restore workflow.

**Primary recommendation:** plan around a structured snapshot backup format with explicit in-app export/import UX in the data-management area. Keep monthly CSV export as a separate lightweight share feature. Restore should validate the snapshot, require explicit confirmation before destructive replacement, and write through repository/Room boundaries in a controlled transaction-oriented path.

---

<user_constraints>

## User Constraints (from CONTEXT.md)

### Locked Decisions
- Treat the current export-only state as a real product gap.
- Do NOT use the current monthly CSV export as the primary restore format.
- Preserve `monthly_config`, `overtime_entry`, and `holiday_override` semantics during restore.
- Add backup/restore from the existing data-management area.
- Keep existing CSV export as a lightweight share/export feature.
- Restore needs visible validation, explicit outcome messaging, and deliberate replace/merge/conflict handling.

### Claude's Discretion
- Exact backup file format and extension.
- Exact restore UX shape (replace-only vs limited merge).
- Exact confirmation and preview copy.

### Deferred Ideas (OUT OF SCOPE)
- Cloud sync or account-based replication.
- Turning CSV into the full-fidelity portability contract.
- Broad storage-architecture rewrites unrelated to shipping backup/restore safely.

</user_constraints>

---

## Standard Stack

### Core
| Library / API | Version | Purpose | Why Standard |
|---------------|---------|---------|--------------|
| Android Storage Access Framework | Platform | User-selected import/export file access | Official Android file-picking model |
| `Intent.ACTION_OPEN_DOCUMENT` | Platform | Pick backup file to restore | Supports user-visible, provider-backed file access |
| Room | `2.8.4` | Durable business-state persistence | Existing source of truth |
| Compose + Material 3 | existing | Data-management UI and restore UX | Existing app UI stack |

### Supporting
| Library / API | Purpose | When to Use |
|---------------|---------|-------------|
| `ActivityResultContracts.OpenDocument` or equivalent intent launcher | File selection | Restore/import flow |
| `ActivityResultContracts.CreateDocument` or explicit output stream via chosen Uri | Backup file creation | User-controlled backup export |
| `contentResolver.openInputStream/openOutputStream` | Read/write structured snapshot | SAF-backed backup I/O |
| Room transactions / repository write path | Atomic restore application | Replace or staged restore |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Structured snapshot backup | Reuse monthly CSV | Too lossy; cannot reconstruct full month truth |
| App-controlled restore | Rely on OS Auto Backup only | Not explicit, not user-driven, not testable in-app |
| File picker import | Hard-coded app file path | Breaks user expectations and provider interoperability |

---

## Architecture Patterns

### Pattern 1: Snapshot backup separate from CSV share
**What:** Maintain two export concepts: lightweight CSV share vs full-fidelity backup snapshot.
**When to use:** Existing CSV remains for interoperability/share, while backup/restore handles real app portability.
**Project evidence:** `OvertimeViewModel.exportMonthlyCsv()` already emits CSV from UI state only.

### Pattern 2: Explicit data-management restore flow
**What:** Extend `DataManagementScreen.kt` with backup and restore actions instead of adding hidden or system-only restore behavior.
**When to use:** User-initiated portability and recovery.
**Project evidence:** `docs/releases/v1.5.0.md` already reserved this area for export/backup/restore.

### Pattern 3: Repository/transaction-backed restore
**What:** Add a restore path that validates a parsed snapshot and applies durable state through Room/repository logic, ideally in a single controlled operation.
**When to use:** Full replace restore or conflict-aware import.
**Project evidence:** current durable data and month semantics are split across `Entities.kt`, `OvertimeDao.kt`, `OvertimeRepository.kt`, and `ConfigPropagationPlanner.kt`.

### Pattern 4: Validation before destructive write
**What:** Parse and inspect the selected backup before replacement, then show user-visible confirmation of what will change.
**When to use:** Any restore that could overwrite local durable data.
**Why:** Android backup guidance warns that custom backup/export flows increase leakage and data-loss risk if treated casually.

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| File chooser | Custom file browser | SAF / `ACTION_OPEN_DOCUMENT` | Official provider-compatible picker |
| Restore source of truth | Reconstruct from CSV | Structured snapshot format | CSV is lossy for this domain |
| Hidden background restore | Silent file-path magic | Explicit user-driven restore flow | Safer and auditable |
| Testing only by manual QA | Unverified restore path | Unit + Robolectric/integration + androidTest coverage | Restore bugs are destructive |

---

## Common Pitfalls

### Pitfall 1: Treating CSV as a backup
**What goes wrong:** Users believe exported CSV can fully restore app state.
**Why it happens:** Export exists first, restore arrives later, and naming gets blurred.
**How to avoid:** Keep CSV wording scoped to share/export; create a separate backup artifact and copy.

### Pitfall 2: Ignoring month propagation semantics
**What goes wrong:** Imported months look present, but future unlocked configs no longer reflect the original business history.
**Why it happens:** `ConfigPropagationPlanner` propagates config values forward outside day-entry rows.
**How to avoid:** Backup/restore must preserve config state explicitly, not infer it from daily rows.

### Pitfall 3: Partial restore across tables
**What goes wrong:** Entries restore but overrides/config do not, producing incorrect day types or pay calculations.
**Why it happens:** Business truth is split across multiple tables and preferences.
**How to avoid:** Model snapshot contents and restore validation around the full data bundle.

### Pitfall 4: Unsafe overwrite UX
**What goes wrong:** A selected file silently replaces local data without preview or confirmation.
**Why it happens:** Restore implemented as a direct DAO write path.
**How to avoid:** Add validation summary + explicit confirmation before destructive write.

### Pitfall 5: Under-testing provider-backed file access
**What goes wrong:** Restore works with local temp files but fails with real SAF Uris/providers.
**Why it happens:** Tests only cover plain File I/O.
**How to avoid:** Include androidTest coverage around document-picker style flows or provider-like inputs.

---

## Code Examples / Verified Directions

### SAF file picking (official direction)
```kotlin
val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
    type = "application/*"
    addCategory(Intent.CATEGORY_OPENABLE)
}
```

### Current export boundary (project evidence)
```kotlin
fun exportMonthlyCsv() {
    val uiStateSnapshot = uiState.value
    viewModelScope.launch {
        val exportResult = withContext(Dispatchers.IO) {
            runCatching { createCsvFile(uiStateSnapshot) }
        }
        // Shares file, does not create restorable app snapshot
    }
}
```

### Durable data that backup must cover
```kotlin
@Entity(tableName = "monthly_config")
data class MonthlyConfigEntity(...)

@Entity(tableName = "overtime_entry")
data class OvertimeEntryEntity(...)

@Entity(tableName = "holiday_override")
data class HolidayOverrideEntity(...)
```

---

## State of the Art

| Old Approach | Recommended Phase 3 Approach | Why |
|--------------|------------------------------|-----|
| Monthly CSV share only | Separate CSV share + structured backup snapshot | Distinguishes convenience export from durable restore |
| Implicit OS restore only | Explicit in-app backup/restore flow plus OS backup coexistence | Users need a deterministic path |
| No restore validation | Parse/validate/confirm before write | Prevents destructive surprises |

---

## Open Questions

1. **Snapshot format choice**
   - What we know: CSV is insufficient.
   - What's unclear: JSON snapshot vs database-file copy vs hybrid manifest + payload.
   - Recommendation: favor a structured, versioned snapshot format that is readable and testable.

2. **Restore semantics**
   - What we know: blind overwrite is risky.
   - What's unclear: full replace only vs merge for some records.
   - Recommendation: scope first release to validated full replace with explicit confirmation, unless repo constraints strongly favor merge.

3. **Non-Room state inclusion**
   - What we know: business truth is in Room, while preferences/cache live elsewhere.
   - What's unclear: whether Phase 3 should back up appearance prefs and holiday cache metadata.
   - Recommendation: prioritize durable business truth first; treat preferences/cache as secondary unless required by UX.

4. **ViewModel ownership**
   - What we know: current export lives in `OvertimeViewModel`, but restore may deserve a narrower data-management owner.
   - What's unclear: whether to extend `OvertimeViewModel` or add dedicated state/logic for data management.
   - Recommendation: plan explicit contracts first, then choose the ownership model that minimizes unrelated churn.

---

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric + AndroidX Test + Compose UI Test |
| Config file | `app/build.gradle.kts` |
| Quick run command | `./gradlew.bat :app:testDebugUnitTest` |
| Full suite command | `./gradlew.bat :app:testDebugUnitTest :app:connectedDebugAndroidTest` |
| Estimated runtime | ~60-180 seconds |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| DATA-01 | Backup snapshot captures full durable business state | unit/integration | `./gradlew.bat :app:testDebugUnitTest --tests "*Backup*"` | ❌ Need new |
| DATA-02 | Restore validates file and reports success/failure clearly | unit/androidTest | `./gradlew.bat :app:testDebugUnitTest --tests "*Restore*"` | ❌ Need new |
| DATA-03 | Restored state preserves config/entries/overrides semantics | integration | `./gradlew.bat :app:testDebugUnitTest --tests "*Repository*"` | ❌ Need new |
| DATA-04 | CSV export remains separate from backup/restore UI | androidTest | `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.peter.overtimecalculator.MainFlowTest` | ⚠️ Partial |

### Sampling Rate
- **After every task commit:** run the targeted unit/integration command for the files touched.
- **After every plan wave:** run `./gradlew.bat :app:testDebugUnitTest`.
- **Before `/gsd-verify-work`:** run full unit + connected androidTest coverage for backup/restore flows.
- **Max feedback latency:** < 180 seconds for quick checks.

### Wave 0 Gaps
- [ ] Add backup snapshot unit tests for round-trip serialization/parsing.
- [ ] Add repository/database integration tests for restore application semantics.
- [ ] Add androidTest coverage for data-management backup/restore UI and picker-result handling.

---

## Sources

### Primary (HIGH confidence)
- Android Developers: `ACTION_OPEN_DOCUMENT` and Storage Access Framework guidance
- Android Developers: Auto Backup and backup-options guidance
- Project files: `OvertimeViewModel.kt`, `OvertimeAppEffects.kt`, `DataManagementScreen.kt`, `DataManagementSections.kt`, `Entities.kt`, `OvertimeDao.kt`, `ConfigPropagationPlanner.kt`, `docs/storage-boundaries.md`

### Secondary (MEDIUM confidence)
- CommonsWare backup/import-export discussion
- GitHub examples using `ACTION_OPEN_DOCUMENT` and document-tree permissions for Android apps

---

## Metadata

**Confidence breakdown:**
- Stack and APIs: HIGH
- Data-model fidelity requirements: HIGH
- Restore UX specifics: MEDIUM

**Research date:** 2026-03-15
**Valid until:** 2026-04-15
