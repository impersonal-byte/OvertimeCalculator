# OvertimeCalculator

一个原生 Android 加班工资计算器，面向个人离线使用场景。

项目使用 Kotlin、Jetpack Compose、Material 3、Room 和 Navigation Compose 构建，核心目标是让你按月快速记录每天加班时长，并实时看到当月累计加班工资。

仓库地址：[impersonal-byte/OvertimeCalculator](https://github.com/impersonal-byte/OvertimeCalculator)

## 项目简介

这个应用解决的是一个很具体的问题：

- 用月历查看某个月每天的加班情况
- 为某一天快速录入加班时长
- 根据工作日、休息日、法定节假日自动套用不同工资倍率
- 支持修改倍率
- 支持手动设置时薪
- 支持根据“已发加班工资 + 当月加班明细”反推时薪
- 在首页显眼位置持续显示当月累计加班工资和总时长

目前这是一个纯本地、单用户、离线优先的 Android 应用，不包含登录、云同步、报表导出或桌面小组件。

## 主要功能

- 月历首页
  - 展示当前月份全部日期
  - 每天显示加班时长
  - 区分工作日、休息日、节假日
- 每日加班录入
  - 点击某天弹出底部编辑面板
  - 输入小时和分钟
  - 可手动覆盖当天日期类型
- 加班工资计算
  - 按“时薪 x 加班小时 x 日期倍率”计算
  - 首页实时汇总当月工资和总工时
- 时薪来源支持两种模式
  - 手动输入时薪
  - 根据某月“已发加班工资”与“倍率加权工时”反推时薪
- 月份配置快照
  - 某月配置可以独立保存
  - 新配置会传播到后续未锁定月份
  - 已锁定月份不会被覆盖

## 计算规则

应用当前采用以下规则：

- 日期类型优先级
  - 用户手动覆盖
  - 内置节假日 / 调休数据
  - 周末判定
  - 普通工作日
- 默认倍率
  - 工作日：`1.5`
  - 休息日：`2.0`
  - 法定节假日：`3.0`
- 反推时薪公式
  - `时薪 = 已发加班工资 / 倍率加权小时数`

说明：

- 反推时薪依赖“某月每日加班明细”
- 如果只有月总时长，没有每天的日期类型分布，应用不会做模糊估算

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Android ViewModel
- Room
- JUnit4
- Compose UI Test

## 项目结构

```text
app/src/main/java/com/peter/overtimecalculator
|- data
|  |- db
|  \- repository
|- domain
\- ui
```

主要职责如下：

- `ui`
  - Compose 页面、导航、状态展示、交互入口
- `domain`
  - 日期类型判定、工资计算、时薪反推、配置传播规则
- `data`
  - Room 数据库存储、仓库层协调、配置持久化

## 本地运行

环境建议：

- Android Studio
- Android SDK
- JDK 17

构建调试包：

```powershell
.\gradlew.bat assembleDebug
```

运行单元测试：

```powershell
.\gradlew.bat testDebugUnitTest
```

运行设备或模拟器测试：

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

调试 APK 默认输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 当前状态

当前版本已经完成以下主流程：

- 首页月历展示
- 设置页导航
- 每日加班录入与保存
- 首页汇总实时刷新
- 手动设置时薪
- 反推时薪
- Room 本地存储
- 基础单元测试
- 基础 Compose UI 测试

## 节假日说明

项目内置了中国大陆节假日与调休数据，并在超出预置范围时退化为周末规则。

这意味着：

- 已覆盖的年份会按内置节假日数据判定
- 超出明确配置范围的日期，默认按“周末 / 工作日”规则判断
- 用户仍然可以手动覆盖某一天的日期类型

如果后续需要更高精度，适合把节假日数据改成独立数据源或可更新配置。

## 已知限制

- 当前数据只保存在本地设备
- 暂无云同步
- 暂无导出 Excel / CSV / PDF
- 暂无通知提醒和桌面组件
- 节假日数据仍有后续完善空间

## 后续计划

- 完善 Compose UI 回归测试
- 优化反推时薪结果的解释性展示
- 补充更完整的节假日与调休数据
- 增加 README 截图和使用示例
- 视需要加入导出与备份能力

## License

当前仓库尚未添加许可证文件。

如果你准备公开接受他人使用或二次开发，建议尽快补一个 `LICENSE`。
