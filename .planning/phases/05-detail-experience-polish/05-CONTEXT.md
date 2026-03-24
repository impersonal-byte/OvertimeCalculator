# Phase 5: 细节体验优化 - Context

**Gathered:** 2026-03-24
**Status:** Ready for planning
**Source:** Direct user request during `/gsd-plan-phase`

<domain>
## Phase Boundary

Polish detail-level interaction and visual consistency after the Phase 04 visual migration. This phase is limited to the currently requested refinements around day-entry precision, theme switching density, settings chrome harmony, data-management action styling, and holiday/rest-day classification accuracy.

</domain>

<decisions>
## Implementation Decisions

### Day-entry precision
- Workday overtime input should prioritize realistic precise entry instead of stretching the positive slider all the way to 16 hours.
- The slider experience for workday editing should target a 6-hour positive range while keeping the existing comp-time negative path intact.

### Theme mode switching
- Theme mode controls should no longer require large preview cards or sideways scrolling for quick switching.
- Show the three theme choices in a compact side-by-side arrangement for faster access.

### Settings chrome harmony
- The settings screen status-bar/top-bar area should visually match the settings screen rather than reading like a mismatched strip.
- Preserve the already-good home-screen result; this request is specifically about settings consistency.

### Data-management action styling
- Backup/restore/export actions in data management should look intentionally coordinated.
- Restore should no longer be the odd one out purely because of button color/emphasis.

### Holiday classification semantics
- Only 3x-pay dates should be treated as `HOLIDAY`.
- Remaining official days off during long holiday blocks should be treated as `REST_DAY` to match the app's 2x pay expectation.
- Makeup workdays must continue to resolve as `WORKDAY`.

### Claude's Discretion
- Exact handling for previously saved workday records above the new 6-hour-focused editor range.
- Exact compact control used for theme switching, as long as all three choices remain visible simultaneously.
- Exact Compose/system-bar implementation used to make settings chrome feel coordinated.
- Exact shared button style strategy used in data management.
- Exact holiday data source or schema evolution needed to support the 3x-vs-2x distinction.

</decisions>

<specifics>
## Specific Ideas

- User examples to implement now:
  1. “工作日一般不可能加班16小时…建议使 slider 改为 6 小时使用户更精确的输入。”
  2. “主题模式的显示卡过大，需要侧向滑动…建议做成三联并排方便快速更改。”
  3. “设置界面的顶部状态栏颜色与设置界面不符…需要优化使视觉更协调。”
  4. “数据管理中的备份恢复按钮颜色与创建和导出按钮不同，需要优化协调。”
  5. “节假日判定，需要只翻3倍的日子定为节假日，剩余时间应该为休息日更符合2倍的习惯。”

</specifics>

<deferred>
## Deferred Ideas

- “还有更多细节我们多加讨论” indicates more polish requests may come later, but they are not part of this phase plan unless directly listed above.
- No broader redesign of unrelated screens, navigation, or business features in this phase.

</deferred>

---

*Phase: 05-detail-experience-polish*
*Context gathered: 2026-03-24*
