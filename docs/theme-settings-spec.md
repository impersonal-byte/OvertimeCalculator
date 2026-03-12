# 主题设置页实现说明

## 1. 页面目标与交互说明

- 新增独立设置子页“主题与外观”，入口位于设置首页。
- 页面分为三部分：
  - 主题模式：浅色、深色、自动
  - 色彩：动态色彩开关 + 固定色板
  - 帮助说明：解释动态色与固定色的关系
- 所有改动即时生效，不增加保存按钮。

## 2. 设置导航结构

- `settings/main`
  - `settings/theme`
  - `settings/rules`
  - `settings/preferences`
  - `settings/data`
  - `settings/about`
- `settings/preferences` 只保留非主题偏好项，例如日历起始日。

## 3. 主题相关类型

### `AppTheme`

- `SYSTEM`
- `LIGHT`
- `DARK`

### `SeedColor`

- `CLAY`
- `MINT_GREEN`
- `AQUA`
- `SKY_BLUE`
- `LAVENDER`
- `ORCHID`
- `LILAC`
- `ROSE`

### `ThemePaletteSpec`

- `seedColor`
- `displayName`
- `lightColorScheme`
- `darkColorScheme`
- `swatchColors`
- `lightPreviewAccent`
- `darkPreviewAccent`

`ThemePaletteSpec` 是主题设置页和实际 Material 主题映射的统一来源。

## 4. 固定色板与 token

| SeedColor | Display | Primary |
| --- | --- | --- |
| `CLAY` | `Clay` | `#9A3412` |
| `MINT_GREEN` | `Mint` | `#2C8C74` |
| `AQUA` | `Aqua` | `#1E88A8` |
| `SKY_BLUE` | `Sky` | `#4F83CC` |
| `LAVENDER` | `Lavender` | `#7E8BD8` |
| `ORCHID` | `Orchid` | `#A27AD6` |
| `LILAC` | `Lilac` | `#B58AD9` |
| `ROSE` | `Rose` | `#D777A7` |

实际预览图标使用每个色板自带的 `swatchColors`、`lightPreviewAccent`、`darkPreviewAccent`。

## 5. 动态色彩行为

- Android 12 及以上：
  - `dynamic_color_switch` 可交互
  - 打开后实际主题走 `dynamicLightColorScheme` / `dynamicDarkColorScheme`
  - 固定色板网格保留显示，但进入禁用态
- Android 12 以下：
  - `dynamic_color_switch` 显示为禁用态
  - 副标题提示“需要 Android 12 及以上版本”
  - 固定色板保持可选

关闭动态色彩后，恢复使用上一次选中的固定色板。

## 6. 预览卡绘制规则

- 预览卡使用 Compose `Canvas` 自绘，不依赖静态图片资源。
- 浅色预览使用当前色板的 light scheme。
- 深色预览使用当前色板的 dark scheme。
- 自动预览使用对角线分割：
  - 左上区域绘制浅色预览
  - 右下区域绘制深色预览
  - 中间绘制对角线分隔
- 选中态使用主色描边，并通过 `theme_mode_*_selected` 提供测试锚点。

## 7. 测试与验收清单

- 设置首页可见 `nav_theme`
- 进入主题页可见：
  - `settings_theme_screen`
  - `theme_mode_light`
  - `theme_mode_dark`
  - `theme_mode_system`
  - `dynamic_color_switch`
  - `theme_palette_grid`
  - `theme_palette_rose`
- 选择深色后应出现 `theme_mode_dark_selected`
- 选择固定色板后返回首页，主题应立即生效
- 重启 Activity 后主题模式、动态色、固定色保持不变

## 8. 当前实现约束

- 持久化继续使用现有 `SharedPreferences` 键：
  - `app_theme`
  - `use_dynamic_color`
  - `seed_color`
- 不改业务计算、数据库、导出逻辑。
- 当前未实现 onboarding 流程，组件只为后续复用预留。
