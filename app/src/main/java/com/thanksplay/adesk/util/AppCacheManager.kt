package com.thanksplay.adesk.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.thanksplay.adesk.model.AppInfo
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class AppCacheManager(private val context: Context) {
    
    private val cacheDir = File(context.cacheDir, "app_cache")
    private val cacheFile = File(cacheDir, "apps.json")
    
    init {
        cacheDir.mkdirs()
    }
    
    fun saveApps(apps: List<AppInfo>) {
        val jsonArray = JSONArray()
        
        apps.forEach { app ->
            val json = JSONObject().apply {
                put("packageName", app.packageName)
                put("className", app.className)
                put("label", app.label)
                put("installTime", app.installTime)
            }
            jsonArray.put(json)
        }
        
        cacheFile.writeText(jsonArray.toString())
    }
    
    fun loadApps(): List<AppInfo>? {
        if (!cacheFile.exists()) return null
        
        return try {
            val jsonArray = JSONArray(cacheFile.readText())
            val apps = mutableListOf<AppInfo>()
            
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                val packageName = json.getString("packageName")
                
                val icon = try {
                    context.packageManager.getApplicationInfo(packageName, 0).loadIcon(context.packageManager)
                } catch (e: Exception) {
                    null
                } ?: continue
                
                apps.add(AppInfo(
                    packageName = packageName,
                    className = json.getString("className"),
                    label = json.getString("label"),
                    icon = icon,
                    installTime = json.optLong("installTime", 0)
                ))
            }
            
            apps
        } catch (e: Exception) {
            null
        }
    }
    
    fun hasCache(): Boolean {
        return cacheFile.exists()
    }
    
    fun clearCache() {
        cacheFile.delete()
    }
}
