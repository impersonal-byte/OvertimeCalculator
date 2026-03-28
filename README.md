<p align="center">
  <img src="./docs/icons/concept-1-calendar-clock.svg" width="120" alt="加薪应用图标" />
</p>

<h1 align="center">加薪</h1>

<p align="center">
  一个面向个人使用的原生 Android 加班工资计算器。
</p>

<p align="center">
  用月历记录每天的加班或调休时长，按工作日 / 休息日 / 节假日规则自动计算，并实时汇总当月金额。
</p>

<p align="center">
  <a href="https://github.com/impersonal-byte/OvertimeCalculator/releases/latest">下载最新版本</a>
  ·
  <!-- BEGIN:current-release-link -->
<a href="https://github.com/impersonal-byte/OvertimeCalculator/releases/tag/v2.3.1">查看 v2.3.1</a>
<!-- END:current-release-link -->
  ·
  <a href="#功能亮点">功能亮点</a>
  ·
  <a href="#本地运行">本地运行</a>
  ·
  <a href="#发布与-ci">发布与 CI</a>
</p>

## 当前版本

<!-- BEGIN:current-version -->
- 当前正式版本：`v2.3.1`
- 当前版本号：`18`
<!-- END:current-version -->
- 最新正式发布页：`https://github.com/impersonal-byte/OvertimeCalculator/releases/latest`

当前版本会在发版流程完成后自动同步到本 README；具体变更说明以 `docs/releases/` 和 GitHub Release 页面为准。

## 功能亮点

- 月历首页展示整月记录、净时长和累计加班工资
- 支持工作日、休息日、节假日不同倍率
- 支持工作日调休录入，自动按月度余额抵扣
- 支持手动输入时薪，也支持按已发加班工资反推时薪
- 支持手动覆盖某一天的日期类型
- 支持中国节假日基线数据，并通过 `timor.tech` 年度节假日接口静默刷新
- 支持应用内检查更新，从 GitHub Release 下载新版本
- 支持外观模式、动态取色与多套种子色切换
- 支持导出本月 CSV 数据并通过系统分享发送

## 计算规则

- 日期类型优先级：
  用户手动覆盖 > 节假日/调休规则 > 周末 > 普通工作日
- 默认倍率：
  工作日 `1.5`，休息日 `2.0`，节假日 `3.0`
- 调休规则：
  仅工作日允许录入负时长，且优先抵扣工作日加班，再抵扣休息日，最后抵扣节假日
- 反推公式：
  `时薪 = 已发加班工资 / 倍率加权小时数`

## 项目现状

- 单 `:app` 模块原生 Android 应用
- Jetpack Compose 单 Activity 架构
- 仍使用手写 `AppContainer` 作为组合根；经过 `v2.0.0` 架构评估，现阶段暂不引入 Hilt，也暂不拆多模块
- 业务主数据使用 Room，节假日缓存元数据使用 DataStore，系统强耦合临时状态仍保留在 `SharedPreferences`

如果你要继续了解当前存储边界，可以直接看：`docs/storage-boundaries.md`

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Android ViewModel
- Room
- DataStore Preferences
- WorkManager
- Robolectric + JUnit4
- GitHub Actions

## 下载与安装

### 直接下载

- 最新正式版本：`https://github.com/impersonal-byte/OvertimeCalculator/releases/latest`
- 当前版本详情页：`https://github.com/impersonal-byte/OvertimeCalculator/releases/tag/v2.3.1`

### 安装方式

- 从 GitHub Release 页面下载通用 APK
- 在 Android 设备上手动安装
- 如果设备首次通过浏览器或文件管理器安装 APK，系统可能要求你允许“安装未知应用”

### 应用内更新

应用内“检查更新”会从 GitHub Release 拉取最新正式版本，并通过系统下载器下载 APK。下载完成后会拉起系统安装流程；如果系统尚未授予安装权限，应用会引导你进入对应设置页。

## 本地运行

### 环境要求

- Android Studio
- Android SDK
- JDK 17
- `local.properties` 中正确配置 `sdk.dir`

### 常用命令

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.peter.overtimecalculator.MainSmokeTest
.\gradlew.bat packageReleaseApk
```

### 产物位置

```text
调试包: app/build/outputs/apk/debug/app-debug.apk
正式包: app/build/dist/OvertimeCalculator-<version>-universal.apk
```

### Room schema

当前项目已经启用 Room schema 导出，生成结果位于：

```text
app/schemas/com.peter.overtimecalculator.data.db.AppDatabase/
```

## 发布与 CI

仓库当前包含两套主要工作流：

- `Android Verify (Manual)`
  - 仅在 GitHub Actions 页面手动触发
  - 运行单元测试、调试构建和 Android UI smoke test
- `Android Release`
  - 在推送 `v*` tag 时触发
  - 自动校验 tag 与 `appVersionName`
  - 自动运行单元测试
  - 自动构建正式签名 APK
  - 自动创建 / 更新 GitHub Release

### Release Secrets

需要在 GitHub 仓库 `Settings > Secrets and variables > Actions` 中配置以下 Secrets：

- `SIGNING_KEY`
- `ALIAS`
- `KEY_STORE_PASSWORD`
- `KEY_PASSWORD`

PowerShell 生成 `SIGNING_KEY`：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes(".\OvertimeCalculator.jks"))
```

### 发版流程

```powershell
git add app/build.gradle.kts docs/releases/v<version>.md
git commit -m "Release v<version>"
git push origin main
git tag v<version>
git push origin v<version>
```

### 约束说明

- tag 去掉前缀 `v` 后，必须与 `app/build.gradle.kts` 中的 `appVersionName` 完全一致
- `docs/releases/v<version>.md` 存在时，release workflow 会优先用它作为 GitHub Release 正文
- 普通 `git push origin main` 不会触发正式发版；正式发版依赖 `v*` tag

### README 自动同步

- `Android Release` 工作流会在发版完成后回写 `README.md` 中的当前版本链接与版本号区块
- 机器维护的区块使用注释标记包围，请不要手动删除：

```text
<!-- BEGIN:... -->
<!-- END:... -->
```

## 文档索引

- 发布说明：`docs/releases/`
- 存储边界：`docs/storage-boundaries.md`
- 代码库地图：`.planning/codebase/`

## 已知限制

- 当前数据仅保存在本地设备
- 暂无云同步
- 暂无 PDF 导出
- 暂无桌面组件

## License

本项目采用 [MIT License](./LICENSE)。
