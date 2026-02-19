# ADesk

一个轻量级 Android 桌面启动器，专为低端设备和 Android 手表设计。

## 特性

- **轻量设计** - 支持 Android 4.1+ (API 16)
- **三页布局** - 主页、应用列表、搜索页
- **高度自定义**
  - 自定义壁纸（黑色、纯色、自定义图片）
  - 时钟位置（居中、顶部、隐藏）
  - 应用列表排序和布局
  - 收藏应用和隐藏应用管理
- **天气小组件** - 使用 Open-Meteo API
- **滑动粒子效果** - 魔法棒般的视觉体验
- **插件系统** - 完全开放的 API，支持扩展功能
- **多语言支持** - 中文、英文

## 截图

| 主页 | 应用列表 | 设置 |
|:---:|:---:|:---:|
| 主页时钟和快捷应用 | 按字母分组的应用列表 | 丰富的自定义选项 |

## 下载

从 [Releases](https://github.com/your-username/adesk/releases) 页面下载最新版本。

## 构建

```bash
git clone https://github.com/your-username/adesk.git
cd adesk
./gradlew assembleDebug
```

## 插件开发

ADesk 支持三种类型的插件：

1. **WidgetPlugin** - 小部件插件
2. **EffectPlugin** - 特效插件
3. **ActionPlugin** - 动作插件

详见 [PLUGIN_DEVELOPMENT.md](PLUGIN_DEVELOPMENT.md)

## 技术栈

- Kotlin
- Android SDK 16+
- ViewPager2
- RecyclerView
- Coroutines
- SharedPreferences

## 开源协议

MIT License

## 致谢

- 天气数据来自 [Open-Meteo](https://open-meteo.com/)
- 图标使用 Android 内置资源
