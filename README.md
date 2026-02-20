# ADesk

一个轻量级 Android 桌面启动器，专为低端设备和 Android 手表设计。

> **适用于 Android 4.1+ (API 16)**

## 📥 下载体验

**如果您是普通用户，想要直接下载体验：**

👉 [**点击下载最新版本**](https://github.com/2489742701/ADesk/releases)

进入 Release 页面后，下载 APK 文件即可安装使用。

---

## 特性

- **轻量设计** - 支持 Android 4.1+ (API 16)
- **三页布局** - 主页、应用列表、搜索页
- **高度自定义**
  - 自定义壁纸（黑色、纯色、自定义图片）
  - 时钟位置（居中、顶部、隐藏）
  - 应用列表排序和布局
  - 收藏应用和隐藏应用管理
- **天气小组件** - 使用 Open-Meteo API
  - 支持自动定位或手动输入城市
  - 天气缓存机制，可配置更新间隔
  - 支持自定义天气 API
  - 双击刷新天气
- **日历事件显示** - 显示今日日程
- **滑动粒子效果** - 魔法棒般的视觉体验
- **插件系统** - 完全开放的 API，支持扩展功能（开发中）
- **多语言支持** - 中文、英文

## 截图

| 主页 | 应用列表 | 设置 |
|:---:|:---:|:---:|
| 主页时钟和快捷应用 | 按字母分组的应用列表 | 丰富的自定义选项 |

## 构建

```bash
git clone https://github.com/2489742701/ADesk.git
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
