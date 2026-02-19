package com.thanksplay.adesk.plugin

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle

interface ADeskPlugin {
    
    val pluginId: String
    val pluginName: String
    val pluginVersion: String
    val pluginDescription: String
    val pluginIcon: Drawable?
    val author: String
    
    fun onInit(context: Context, config: Bundle?)
    
    fun onDestroy()
    
    interface WidgetPlugin : ADeskPlugin {
        val widgetLayout: Int
        val widgetWidth: Int
        val widgetHeight: Int
        fun onUpdate(context: Context)
        fun onClick(context: Context)
    }
    
    interface EffectPlugin : ADeskPlugin {
        fun onDraw(canvas: android.graphics.Canvas)
        fun onTouch(x: Float, y: Float, action: Int)
        fun onScroll(dx: Float, dy: Float, x: Float, y: Float)
    }
    
    interface ActionPlugin : ADeskPlugin {
        val triggerType: TriggerType
        fun onTrigger(context: Context, data: Bundle?)
    }
    
    enum class TriggerType {
        SWIPE_LEFT,
        SWIPE_RIGHT,
        SWIPE_UP,
        SWIPE_DOWN,
        DOUBLE_TAP,
        LONG_PRESS,
        SHAKE
    }
}
