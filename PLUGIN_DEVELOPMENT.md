# ADesk 插件开发指南

## 概述

ADesk 支持三种类型的插件：

1. **WidgetPlugin** - 小部件插件，可以在主页显示自定义内容
2. **EffectPlugin** - 特效插件，可以自定义视觉效果
3. **ActionPlugin** - 动作插件，响应手势触发自定义操作

## 插件接口

### 基础接口 (ADeskPlugin)

```kotlin
interface ADeskPlugin {
    val pluginId: String          // 插件唯一ID
    val pluginName: String        // 插件名称
    val pluginVersion: String     // 插件版本
    val pluginDescription: String // 插件描述
    val pluginIcon: Drawable?     // 插件图标
    val author: String            // 作者
    
    fun onInit(context: Context, config: Bundle?)  // 初始化
    fun onDestroy()                                 // 销毁
}
```

### 小部件插件 (WidgetPlugin)

```kotlin
interface WidgetPlugin : ADeskPlugin {
    val widgetLayout: Int   // 布局资源ID
    val widgetWidth: Int    // 宽度 (dp)
    val widgetHeight: Int   // 高度 (dp)
    
    fun onUpdate(context: Context)      // 更新内容
    fun onClick(context: Context)       // 点击事件
}
```

### 特效插件 (EffectPlugin)

```kotlin
interface EffectPlugin : ADeskPlugin {
    fun onDraw(canvas: Canvas)                              // 绘制效果
    fun onTouch(x: Float, y: Float, action: Int)           // 触摸事件
    fun onScroll(dx: Float, dy: Float, x: Float, y: Float) // 滚动事件
}
```

### 动作插件 (ActionPlugin)

```kotlin
interface ActionPlugin : ADeskPlugin {
    val triggerType: TriggerType  // 触发类型
    
    fun onTrigger(context: Context, data: Bundle?)  // 触发时调用
}

enum class TriggerType {
    SWIPE_LEFT, SWIPE_RIGHT, SWIPE_UP, SWIPE_DOWN,
    DOUBLE_TAP, LONG_PRESS, SHAKE
}
```

## 开发步骤

### 1. 创建 Android 项目

创建一个新的 Android 项目，最小 SDK 为 16 (Android 4.1)。

### 2. 添加依赖

在 `build.gradle` 中添加：

```groovy
dependencies {
    compileOnly 'com.thanksplay:adesk-plugin-api:1.0'
}
```

### 3. 实现插件接口

```kotlin
class MyPlugin : ADeskPlugin.WidgetPlugin {
    override val pluginId = "com.example.myplugin"
    override val pluginName = "我的插件"
    override val pluginVersion = "1.0.0"
    override val pluginDescription = "这是一个示例插件"
    override val pluginIcon: Drawable? = null
    override val author = "Your Name"
    
    override val widgetLayout = R.layout.my_widget
    override val widgetWidth = 200
    override val widgetHeight = 100
    
    override fun onInit(context: Context, config: Bundle?) {
        // 初始化代码
    }
    
    override fun onDestroy() {
        // 清理代码
    }
    
    override fun onUpdate(context: Context) {
        // 更新小部件内容
    }
    
    override fun onClick(context: Context) {
        // 处理点击
    }
}
```

### 4. 注册服务

在 `AndroidManifest.xml` 中：

```xml
<service android:name=".MyPluginService"
    android:exported="true">
    <intent-filter>
        <action android:name="com.thanksplay.adesk.PLUGIN" />
    </intent-filter>
    <meta-data
        android:name="adesk_plugin_id"
        android:value="com.example.myplugin" />
    <meta-data
        android:name="adesk_plugin_name"
        android:value="我的插件" />
    <meta-data
        android:name="adesk_plugin_version"
        android:value="1.0.0" />
    <meta-data
        android:name="adesk_plugin_class"
        android:value="com.example.MyPlugin" />
    <meta-data
        android:name="adesk_plugin_description"
        android:value="这是一个示例插件" />
    <meta-data
        android:name="adesk_plugin_author"
        android:value="Your Name" />
</service>
```

## 注意事项

1. 插件运行在 ADesk 进程中，注意内存使用
2. 避免在主线程执行耗时操作
3. 插件应该轻量，适配低端设备
4. 支持 Android 4.1 及以上版本

## 示例项目

参考 ADesk 源码中的 `widget/ParticleView.kt` 了解特效实现。
