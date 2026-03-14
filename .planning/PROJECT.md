# 加薪 / OvertimeCalculator

## What This Is

加薪是一个面向个人使用的原生 Android 加班工资计算器。用户用月历记录每天的加班或调休时长，应用按工作日、休息日、节假日规则自动计算当月金额，并提供主题、导出、节假日更新和应用内更新等配套能力。

## Core Value

用户可以稳定记录每天的加班或调休，并即时得到可信的当月加班工资结果。

## Requirements

### Validated

- ✓ 用户可以按月历查看整月记录、净时长和累计加班工资 - existing
- ✓ 用户可以按工作日、休息日、节假日规则计算加班工资 - existing
- ✓ 用户可以录入工作日调休并按余额抵扣 - existing
- ✓ 用户可以手动输入时薪，或按已发加班工资反推时薪 - existing
- ✓ 用户可以手动覆盖某一天的日期类型 - existing
- ✓ 用户可以管理主题、动态取色、导出 CSV，并检查应用更新 - existing

### Active

- [ ] 为当前 brownfield 仓库建立 GSD 项目元数据，使计划与 quick workflow 可以直接运行
- [ ] 用规划文档准确记录当前已交付能力、核心约束和主要架构决策
- [ ] 让后续小型审计或调查任务可以通过 `.planning/quick/` 独立追踪，而不污染里程碑路线图

### Out of Scope

- 在初始化规划文档时引入 Hilt、多模块拆分或大规模架构改造 - 这次工作只补齐规划基线，不改变现有产品实现
- 在初始化规划文档时重做 UI 或新增产品功能 - 目标是建立项目记忆与流程入口，不是扩展功能范围

## Context

- 当前仓库是单 `:app` 模块的原生 Android 应用，技术栈以 Kotlin、Jetpack Compose、Material 3、Navigation Compose、Room、DataStore 和 WorkManager 为主。
- 代码库已经存在 `.planning/codebase/` 地图文档，说明仓库是 brownfield 项目，但此前没有完成 GSD 的 `PROJECT.md`、`REQUIREMENTS.md`、`ROADMAP.md`、`STATE.md` 初始化。
- 应用采用手写 `AppContainer` 作为组合根，`MainActivity` 和 ViewModel 工厂也仍是手写依赖装配；现阶段不引入 Hilt，也不拆多模块。
- 当前最需要解决的是规划层缺口：没有 `ROADMAP.md` 和 `.planning/STATE.md` 会直接阻塞 `quick` workflow。

## Constraints

- **Tech stack**: 保持单模块 Kotlin/Compose Android 架构 - 这是现有代码和 README 明确写定的边界
- **Architecture**: 继续沿用手写 `AppContainer` 依赖装配 - 当前规模可控，初始化文档不应趁机改架构
- **Workflow**: 规划文档需纳入 git 跟踪 - quick workflow 依赖 `.planning` 文档持续存在并可回溯
- **Safety**: 初始化阶段不修改任何产品代码路径 - 这次工作只建立流程前置条件，避免引入功能回归

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 将当前仓库作为 brownfield GSD 项目初始化 | 仓库已有真实代码和 codebase map，只缺项目级规划文档 | ✓ Good |
| 将现有已发布能力记为 Validated requirements | README 与代码都能证明这些能力已经存在且被依赖 | ✓ Good |
| 用单独的 quick task 追踪临时审计 | quick 工作不应改写里程碑路线图，只应写入 `.planning/quick/` 和 STATE | ✓ Good |

---
*Last updated: 2026-03-14 after GSD initialization*
