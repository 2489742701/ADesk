package com.thanksplay.adesk.plugin

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import java.lang.ref.WeakReference

class PluginManager(private val context: Context) {
    
    private val plugins = mutableMapOf<String, PluginInfo>()
    private val widgetPlugins = mutableListOf<ADeskPlugin.WidgetPlugin>()
    private val effectPlugins = mutableListOf<ADeskPlugin.EffectPlugin>()
    private val actionPlugins = mutableListOf<ADeskPlugin.ActionPlugin>()
    
    companion object {
        const val PLUGIN_ACTION = "com.thanksplay.adesk.PLUGIN"
        const val META_PLUGIN_ID = "adesk_plugin_id"
        const val META_PLUGIN_NAME = "adesk_plugin_name"
        const val META_PLUGIN_VERSION = "adesk_plugin_version"
        const val META_PLUGIN_CLASS = "adesk_plugin_class"
        
        @Volatile
        private var instance: WeakReference<PluginManager>? = null
        
        fun getInstance(context: Context): PluginManager {
            return instance?.get() ?: synchronized(this) {
                instance?.get() ?: PluginManager(context.applicationContext).also {
                    instance = WeakReference(it)
                }
            }
        }
    }
    
    fun scanPlugins(): List<PluginInfo> {
        plugins.clear()
        
        val intent = android.content.Intent(PLUGIN_ACTION)
        val resolveInfos = context.packageManager.queryIntentServices(intent, PackageManager.GET_META_DATA)
        
        for (info in resolveInfos) {
            try {
                val pluginInfo = parsePluginInfo(info)
                if (pluginInfo != null) {
                    plugins[pluginInfo.id] = pluginInfo
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return plugins.values.toList()
    }
    
    private fun parsePluginInfo(resolveInfo: ResolveInfo): PluginInfo? {
        val serviceInfo = resolveInfo.serviceInfo ?: return null
        val metaData = serviceInfo.metaData ?: return null
        
        val id = metaData.getString(META_PLUGIN_ID) ?: return null
        val name = metaData.getString(META_PLUGIN_NAME) ?: id
        val version = metaData.getString(META_PLUGIN_VERSION) ?: "1.0"
        val className = metaData.getString(META_PLUGIN_CLASS) ?: return null
        
        return PluginInfo(
            id = id,
            name = name,
            version = version,
            packageName = serviceInfo.packageName,
            className = className,
            description = metaData.getString("adesk_plugin_description") ?: "",
            author = metaData.getString("adesk_plugin_author") ?: "Unknown"
        )
    }
    
    fun loadPlugin(pluginInfo: PluginInfo, config: Bundle? = null): ADeskPlugin? {
        return try {
            val className = pluginInfo.className
            val clazz = Class.forName(className)
            @Suppress("DEPRECATION")
            val plugin = clazz.newInstance() as ADeskPlugin
            
            plugin.onInit(context, config)
            
            when (plugin) {
                is ADeskPlugin.WidgetPlugin -> widgetPlugins.add(plugin)
                is ADeskPlugin.EffectPlugin -> effectPlugins.add(plugin)
                is ADeskPlugin.ActionPlugin -> actionPlugins.add(plugin)
            }
            
            plugin
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun unloadPlugin(pluginId: String) {
        widgetPlugins.removeAll { it.pluginId == pluginId }
        effectPlugins.removeAll { it.pluginId == pluginId }
        actionPlugins.removeAll { it.pluginId == pluginId }
    }
    
    fun getWidgetPlugins(): List<ADeskPlugin.WidgetPlugin> = widgetPlugins.toList()
    fun getEffectPlugins(): List<ADeskPlugin.EffectPlugin> = effectPlugins.toList()
    fun getActionPlugins(): List<ADeskPlugin.ActionPlugin> = actionPlugins.toList()
    
    fun getInstalledPlugins(): List<PluginInfo> = plugins.values.toList()
    
    data class PluginInfo(
        val id: String,
        val name: String,
        val version: String,
        val packageName: String,
        val className: String,
        val description: String,
        val author: String
    )
}
