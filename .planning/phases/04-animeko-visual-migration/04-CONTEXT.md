# Phase 4: Animeko visual migration - Context

**Gathered:** 2026-03-16
**Status:** Ready for planning

<domain>
## Phase Boundary

Reference the in-repo `animeko` implementation to migrate its UI language and color hierarchy into this project, and replace prior custom styling in scoped surfaces. This phase changes visual system and presentation consistency only; it does not add new product capabilities or alter established business interaction semantics.

</domain>

<decisions>
## Implementation Decisions

### Migration compliance and replication approach
- Use clean-room reimplementation as the default migration path.
- Allow visual reference to include concrete color-value mapping, but do not copy animeko source code directly.
- Similarity target is high semantic and hierarchy parity (color roles, layer depth, component state language), not source-level code parity.
- Allow migration of neutral visual assets only; do not migrate animeko brand assets.
- Keep this project's own product copy and naming style.
- When visual style conflicts with current business behavior, business semantics take priority.
- Execute in phased delivery with shippable increments.
- User preference: do not add explicit migration audit-trace artifacts beyond normal phase documentation.

### Dynamic color and background strategy
- Dynamic color defaults to OFF, with manual opt-in supported.
- Apply animeko-style gradient atmosphere only to shell-level and overview-card surfaces, not as a global full-page background.
- Do not introduce AMOLED pure-black high-contrast mode in this phase.
- Do not adopt image-derived theme extraction (`MaterialThemeFromImage`) in this phase.

### Theme semantic mapping strategy
- Replace current palette strategy with HCT-generated palette approach.
- Introduce full semantic token layering (navigation/page/container/card tiers and state layers), not minimal aliases.
- Keep typography unchanged for this phase (no global custom font migration yet).
- Use edge-level migration behavior where old token paths are removed as each area is migrated.
- Resolve dynamic-color vs fixed-palette precedence with dynamic color first when enabled.
- Organize semantic tokens centrally via a `ThemeDefaults`-style access surface (animeko-inspired structure).
- Dark theme target is layered deep-gray surfaces (not pure black).
- Require semantic wrappers/defaults for new style usage; avoid ad-hoc direct color access in feature screens.

### Surface replacement scope and rollout
- Batch 1 must cover: global theme/token foundation + app shell + home visible surfaces.
- Batch 2 prioritized: `DayEditorSheet` visual replacement while preserving Phase 2 slider interaction semantics.
- Settings rollout: fully replace theme-related settings surfaces first, then expand to rules/preferences/data/about.
- Completion standard for each batch: zero legacy-style paths remain within batch target surfaces.

### Claude's Discretion
- Exact HCT parameterization details (hue/chroma/tone generation rules) as long as semantic parity decisions are honored.
- Exact gradient stop values, alphas, and per-surface tuning.
- Exact wrapper API shape and naming under the centralized theme defaults surface.
- Exact wave sizing and file-by-file order inside each approved batch.

</decisions>

<code_context>
## Existing Code Insights

### Reusable Assets
- `app/src/main/java/com/peter/overtimecalculator/ui/theme/Theme.kt`: global Compose theme entrypoint to host new semantic layer.
- `app/src/main/java/com/peter/overtimecalculator/ui/theme/ThemePaletteSpec.kt`: current palette source to be replaced/refactored toward HCT generation.
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeSettingsScreen.kt`: current theme control surface for dynamic toggle and palette selection UX migration.
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeModeSections.kt`: existing theme mode preview cards; direct integration point for animeko-like semantic preview language.
- `app/src/main/java/com/peter/overtimecalculator/ui/settings/ThemeColorSections.kt`: current palette picker surface to migrate into new token semantics.
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeAppShell.kt`: shell-level scaffold and top bar integration point for global atmosphere and container tokens.
- `app/src/main/java/com/peter/overtimecalculator/ui/HomeSummarySections.kt` and `app/src/main/java/com/peter/overtimecalculator/ui/HomeCalendarSections.kt`: core high-visibility home surfaces for first-batch replacement.
- `app/src/main/java/com/peter/overtimecalculator/ui/DayEditorSheet.kt`: second-batch core editor surface (visual only, interaction semantics preserved).

### External Reference Surfaces (animeko)
- `animeko/app/shared/ui-foundation/src/commonMain/kotlin/ui/foundation/theme/AppTheme.kt`: semantic theme defaults and role-based container/card usage model.
- `animeko/app/shared/ui-foundation/src/commonMain/kotlin/ui/foundation/theme/MaterialThemeFromImage.kt`: image-derived theme approach explicitly excluded for this phase.
- `animeko/app/shared/ui-foundation/src/commonMain/kotlin/ui/foundation/Background.kt`: gradient layering reference for shell/overview atmosphere.
- `animeko/app/shared/ui-settings/src/commonMain/kotlin/ui/settings/tabs/theme/ThemePreferences.kt`: dynamic toggle + palette interaction grouping reference.
- `animeko/app/shared/ui-settings/src/commonMain/kotlin/ui/settings/tabs/theme/ThemePreviewPanel.kt`: preview-card hierarchy reference.
- `animeko/app/shared/app-data/src/commonMain/kotlin/data/models/preference/ThemeSettings.kt`: dynamic/use-black/seed preference model reference for scope decisions.

### Established Patterns and Constraints
- This project currently uses `MaterialTheme(colorScheme = ...)` without dedicated custom typography/shapes layer, so semantic-token introduction is a net-new theme architecture step.
- Appearance settings are persisted through `AppearancePreferencesRepository` and surfaced in `OvertimeViewModel`, enabling migration without changing business data boundaries.
- Settings navigation and composition are already modularized (`SettingsGraphs.kt`, `SettingsRouteEntries.kt`), supporting phased replacement by route.
- Prior phase decisions enforce stable business semantics in day-entry and data-management flows; visual migration must not alter those behaviors.

### Integration Points
- Root integration chain: `MainActivity.kt` -> `Screens.kt` -> `OvertimeAppShell.kt` -> `OvertimeNavigation.kt`.
- Theme-state action chain: settings routes -> `SettingsGraphActions` -> `OvertimeViewModel.updateTheme/updateUseDynamicColor/updateSeedColor`.
- First-batch visual closure points: shell scaffold + home summary/calendar + theme setting surfaces.

</code_context>

<specifics>
## Specific Ideas

- User target: "学习 animeko 的界面和色彩实现，并完美迁移到当前项目，替换以前写的样式。"
- "完美迁移" is defined as semantic and visual hierarchy parity, not direct source-level replication.
- Dynamic color should remain user-controlled and opt-in; baseline visual identity should remain deterministic.
- Migration should feel complete within each batch: no visible mix of old/new style in a finished batch scope.

</specifics>

<deferred>
## Deferred Ideas

- AMOLED pure-black background mode (`useBlackBackground`-style capability)
- Image-derived dynamic theming from content bitmaps
- Global typography/font-family redesign

</deferred>

---

*Phase: 04-animeko-visual-migration*
*Context gathered: 2026-03-16*
