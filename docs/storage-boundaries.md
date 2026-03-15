# 存储边界约定

## 目的

这份文档用于明确 `D:\Android.calculator` 中不同数据应该落在哪一类存储里，避免后续重构时继续把业务数据、偏好设置、缓存元数据和临时文件混在一起。

当前项目同时使用了 Room、DataStore、`SharedPreferences` 和文件输出。它们都合理，但职责必须收口。

## 当前存储现状

### 1. Room：业务核心数据

Room 是当前业务数据的唯一主存储。

- 数据库入口：`app/src/main/java/com/peter/overtimecalculator/data/AppContainer.kt`
- 数据库定义：`app/src/main/java/com/peter/overtimecalculator/data/db/AppDatabase.kt`
- 表结构：`app/src/main/java/com/peter/overtimecalculator/data/db/Entities.kt`
- 读写仓库：`app/src/main/java/com/peter/overtimecalculator/data/repository/OvertimeRepository.kt`

当前由 Room 持久化的内容：

- `monthly_config`：月份级时薪、倍率、来源、锁定状态
- `overtime_entry`：按日期记录的加班/调休分钟数
- `holiday_override`：用户手动覆盖的日期类型

这些数据具有共同特征：

- 是应用的业务主数据
- 需要按月查询、组合计算或事务更新
- 会直接影响月历、汇总、反推时薪和规则计算结果

结论：只要数据会参与业务计算、列表查询、跨实体关联或事务更新，就应该进入 Room，而不是 DataStore 或 `SharedPreferences`。

### 2. DataStore：轻量持久化配置和缓存元数据

DataStore 当前只用于节假日规则远程缓存。

- 实现位置：`app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesRepository.kt`

当前落在 DataStore 的内容：

- `remote_json`
- `fetched_at_epoch_millis`
- `remote_updated_at`

这些数据的特点是：

- 不是主业务记录表
- 不需要复杂查询或事务关系
- 更像“轻量配置 + 缓存快照 + 拉取元数据”

结论：DataStore 适合保存轻量级、键值型、结构简单、没有关系查询需求的数据，例如应用偏好、缓存元数据、同步时间戳、功能开关等。

### 3. SharedPreferences：遗留兼容与临时状态

`SharedPreferences` 目前仍然存在两处使用，但不应继续扩张。

#### 外观和偏好设置

- 位置：`app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt`
- 文件名：`overtime-preferences`

当前保存的 key：

- `calendar_start_day`
- `app_theme`
- `use_dynamic_color`
- `seed_color`

这些本质上是“应用偏好设置”，长期来看更适合迁移到 DataStore。

#### 应用内更新流程状态

- 位置：`app/src/main/java/com/peter/overtimecalculator/data/update/UpdateManager.kt`
- 文件名：`app-update-prefs`

当前保存的 key：

- `download_id`
- `remote_version`
- `awaiting_permission`

这些数据属于“系统交互流程状态”，与 `DownloadManager` 和安装权限流程绑定较紧。目前保留在 `SharedPreferences` 是可以接受的，但不建议把更多更新逻辑状态继续堆进去。

结论：`SharedPreferences` 在本项目中只允许承担两类职责：

- 旧偏好数据的兼容存储
- 和系统服务强耦合、暂时未迁移的流程状态

除这两类情况外，不再新增新的 `SharedPreferences` 使用点。

### 4. 文件输出：临时产物，不是主存储

项目里还有文件输出，但这些都不应被视为业务真相来源。

- CSV 导出：`app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt`
  - 输出到 `cacheDir/exports`
- APK 下载：`app/src/main/java/com/peter/overtimecalculator/data/update/UpdateManager.kt`
  - 输出到 `Environment.DIRECTORY_DOWNLOADS` 对应的 app 外部文件目录

结论：导出文件、下载包、缓存文件都属于派生产物或临时文件，不应该承载业务唯一真相，也不应该替代 Room / DataStore。

## 新增数据时的存储规则

### 放进 Room 的情况

满足任一条件就优先考虑 Room：

- 数据直接参与工资、工时、日期类型等业务计算
- 数据需要按日期、月份或多条件查询
- 数据需要和其他业务记录一起事务更新
- 数据是用户录入后的长期主数据

示例：

- 新增“月度备注”并需要和月度配置一起展示
- 新增“加班分类”并参与报表统计
- 新增“历史导入记录”并需要和当天记录关联

### 放进 DataStore 的情况

满足以下特征时优先考虑 DataStore：

- 数据是简单键值配置
- 数据是应用偏好，而不是业务主记录
- 数据是缓存元数据、同步状态、最近一次拉取时间等轻量状态
- 不需要关系查询和事务建模

示例：

- 默认首页展示方式
- 新手引导是否已看过
- 节假日规则最后同步时间
- 某个纯客户端开关是否启用

### 暂时允许留在 SharedPreferences 的情况

只有以下情况才允许继续保留或新增到 `SharedPreferences`：

- 为兼容已有旧 key，需要在迁移期保留读逻辑
- 与 Android 系统服务强绑定，且迁移成本明显高于当前收益

如果必须使用 `SharedPreferences`，代码里应明确写出这是临时方案，并为后续迁移预留出口。

## 迁移规则

### 偏好设置迁移规则

外观和日历相关偏好未来应迁移到 DataStore，迁移目标包括：

- `calendar_start_day`
- `app_theme`
- `use_dynamic_color`
- `seed_color`

迁移时必须遵守：

- 新实现先读取旧 `SharedPreferences` key，避免用户设置丢失
- 处理旧值兼容，例如 `seed_color` 已经存在旧枚举值到新值的映射逻辑
- 完成首次迁移后，再逐步收敛写路径
- 不要在同一个 PR 里同时改偏好存储和大规模 UI 结构

### 更新流程状态迁移规则

`download_id`、`remote_version`、`awaiting_permission` 暂时允许留在 `SharedPreferences`，直到更新流程边界被整理清楚。

在此之前：

- 不要新增更多更新状态 key
- 不要把普通 UI 偏好混入 `app-update-prefs`
- 不要把下载文件本身当作恢复状态的唯一依据

## 明确禁止项

- 不要把业务核心数据写进 DataStore
- 不要把应用偏好继续新增到 `SharedPreferences`
- 不要把导出文件或下载文件当作业务真相来源
- 不要让同一类数据同时在 Room 和键值存储中各存一份而没有主从关系

## 备份与恢复注意事项

- `app/src/main/AndroidManifest.xml` 当前启用了 `allowBackup`、`fullBackupContent` 和 `dataExtractionRules`
- `app/src/main/res/xml/backup_rules.xml` 当前已配置细粒度排除规则
- `app/src/main/res/xml/data_extraction_rules.xml` 当前为 cloud-backup 和 device-transfer 配置了明确规则

### 平台自动备份范围（Android Auto-Backup / Device Transfer）

以下数据会通过 Android 平台机制自动备份或迁移：

| 存储类型 | 文件/路径 | 备份范围 | 说明 |
|---------|----------|---------|------|
| Room | `overtime-calculator.db` | **包含** | 业务主数据：月份配置、加班记录、日期类型覆盖 |
| SharedPreferences | `overtime-preferences.xml` | **包含** | 用户外观偏好：主题、颜色、起始日期 |

以下数据**主动排除**于平台备份范围：

| 存储类型 | 文件/路径 | 排除原因 |
|---------|----------|---------|
| SharedPreferences | `app-update-prefs.xml` | 更新流程状态（download_id, remote_version, awaiting_permission），与下载管理器强绑定，恢复后状态会失效 |
| DataStore | `datastore/` | 节假日缓存元数据（remote_json, fetched_at_epoch_millis），每次启动会自动从网络刷新 |

### 手动备份范围（.obackup 文件）

手动备份（`.obackup` 文件）仅包含 Room 业务主数据，与当前实现一致：

- **包含：** Room 数据库（月份配置、加班记录、节假日覆盖）
- **不包含：** 用户外观偏好、节假日缓存（从网络动态拉取）、更新会话状态

### 排除数据的恢复预期

| 数据类型 | 恢复后行为 |
|---------|-----------|
| UpdateSessionStore (`app-update-prefs.xml`) | 不恢复 — 用户需要重新下载并安装更新 |
| HolidayRulesRepository (`datastore/`) | 自动重新从网络拉取 — 与全新安装行为一致 |

### 关键设计决策

1. **排除更新会话状态** (`UpdateSessionStore`): 该状态记录的是「用户是否在等待某个 APK 下载完成」，恢复后该 download_id 指向的下载任务已失效，必须清除。

2. **排除节假日缓存** (`HolidayRulesRepository`): 该数据从网络动态获取，且有 24 小时过期机制。恢复时重新拉取能确保使用最新节假日数据。

3. **用户偏好保留** (`overtime-preferences.xml`): 外观主题、种子颜色等属于用户个人设置，应该随设备迁移。

这意味着 Room、DataStore 和 `SharedPreferences` 中的内容，默认都可能进入备份或设备迁移范围。后续如果调整存储位置，不能只考虑“本地读写是否正确”，还要同时考虑：

- 老数据是否会在恢复后出现重复或冲突
- 迁移后的新旧存储是否会在设备迁移场景下同时存在
- 是否需要对某些缓存型数据增加排除规则，而不是让它们跟着备份走

## 当前建议落地顺序

1. 保持 Room 继续承载业务主数据，不做存储层混改
2. 新的偏好类数据默认进入 DataStore
3. `OvertimeViewModel` 中现有外观偏好读写，后续迁移到单独的 DataStore repository
4. `UpdateManager` 中的 `SharedPreferences` 暂时保留，等更新链路收口后再评估迁移

## 相关代码入口

- `app/src/main/java/com/peter/overtimecalculator/data/AppContainer.kt`
- `app/src/main/java/com/peter/overtimecalculator/data/db/AppDatabase.kt`
- `app/src/main/java/com/peter/overtimecalculator/data/db/Entities.kt`
- `app/src/main/java/com/peter/overtimecalculator/data/repository/OvertimeRepository.kt`
- `app/src/main/java/com/peter/overtimecalculator/data/holiday/HolidayRulesRepository.kt`
- `app/src/main/java/com/peter/overtimecalculator/ui/OvertimeViewModel.kt`
- `app/src/main/java/com/peter/overtimecalculator/data/update/UpdateManager.kt`
