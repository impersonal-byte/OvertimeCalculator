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
  <a href="https://github.com/impersonal-byte/OvertimeCalculator/releases/tag/v1.6.0">下载 v1.6.0</a>
  ·
  <a href="#功能亮点">功能亮点</a>
  ·
  <a href="#本地运行">本地运行</a>
  ·
  <a href="#github-actions-自动发版">GitHub Actions 自动发版</a>
</p>

## 功能亮点

- 月历首页展示整月记录、净时长和累计加班工资
- 支持工作日、休息日、节假日不同倍率
- 支持工作日调休录入，自动按月度余额抵扣
- 支持手动输入时薪，也支持按已发加班工资反推时薪
- 支持手动覆盖某一天的日期类型
- 支持中国节假日基线数据，并通过 Timor API 静默刷新
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

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Android ViewModel
- Room
- DataStore
- WorkManager

## 本地运行

环境要求：

- Android Studio
- Android SDK
- JDK 17

常用命令：

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat packageReleaseApk
```

产物位置：

```text
调试包: app/build/outputs/apk/debug/app-debug.apk
正式包: app/build/dist/OvertimeCalculator-<version>-universal.apk
```

## GitHub Actions 自动发版

仓库现在包含两套工作流：

- `Android CI`
  在 `main` 提交和指向 `main` 的 PR 上运行，仅做校验，不生成正式发布包
- `Android Release`
  仅在推送 `v*` 标签时运行，自动做正式签名构建、创建/更新 GitHub Release、上传 APK

需要在 GitHub 仓库 `Settings > Secrets and variables > Actions` 中配置这 4 个 Secrets：

- `SIGNING_KEY`
- `ALIAS`
- `KEY_STORE_PASSWORD`
- `KEY_PASSWORD`

Windows PowerShell 生成 `SIGNING_KEY`：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes(".\\OvertimeCalculator.jks"))
```

发版流程：

```powershell
git add .
git commit -m "Release v1.6.0"
git tag v1.6.0
git push origin main
git push origin v1.6.0
```

注意：

- tag 去掉前缀 `v` 后，必须与 `app/build.gradle.kts` 中的 `appVersionName` 完全一致
- 若存在 `docs/releases/v1.6.0.md`，工作流会优先用它作为 Release 正文
- 若不存在对应文档，工作流会回退到 GitHub 自动生成 Release notes

## 已知限制

- 当前数据仅保存在本地设备
- 暂无云同步
- 暂无 PDF 导出
- 暂无桌面组件

## License

本项目采用 [MIT License](./LICENSE)。
