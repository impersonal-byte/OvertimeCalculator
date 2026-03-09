# OvertimeCalculator

一个用于记录每日加班时长并自动计算当月加班工资的原生 Android 应用。

![首页月历](./docs/images/settings.png)

## 功能特性

- 月历视图查看整月加班记录
- 点击日期快速录入加班时长
- 按工作日、休息日、法定节假日自动计算不同倍率
- 支持手动修改倍率
- 支持手动设置时薪
- 支持根据“已发加班工资”反推时薪
- 首页实时显示当月累计加班工资和总时长
- 所有数据本地保存，可离线使用

## 界面预览

| 首页月历 | 日期录入 | 参数设置 |
| --- | --- | --- |
| ![首页月历](./docs/images/settings.png) | ![日期录入](./docs/images/day-editor.png) | ![参数设置](./docs/images/config.png) |

## 适用场景

适合希望按月管理加班记录、快速估算加班工资的个人用户。

## 计算规则

- 日期类型优先级
  - 用户手动覆盖
  - 内置节假日 / 调休数据
  - 周末判定
  - 普通工作日
- 默认倍率
  - 工作日：`1.5`
  - 休息日：`2.0`
  - 法定节假日：`3.0`
- 时薪反推公式
  - `时薪 = 已发加班工资 / 倍率加权小时数`

说明：

- 反推时薪依赖当月每日加班明细
- 如果只有月总时长，没有每天的日期类型分布，应用不会做模糊估算

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Android ViewModel
- Room

## 运行方式

环境要求：

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

调试 APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 当前状态

当前版本已支持：

- 月历首页展示
- 设置页导航
- 每日加班录入与保存
- 首页汇总实时刷新
- 手动设置时薪
- 反推时薪
- Room 本地存储
- 基础单元测试
- 基础 Compose UI 测试

## 节假日说明

项目内置了中国大陆节假日与调休数据，并在超出预置范围时回退到周末规则。用户也可以手动覆盖某一天的日期类型。

## 已知限制

- 当前数据只保存在本地设备
- 暂无云同步
- 暂无导出 Excel / CSV / PDF
- 暂无通知提醒和桌面组件
- 节假日数据仍有继续完善空间

## License

本项目采用 [MIT License](./LICENSE)。
