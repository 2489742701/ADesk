package com.thanksplay.adesk.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.thanksplay.adesk.model.AppInfo
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class AppCacheManager(private val context: Context) {
    
    private val cacheDir = File(context.cacheDir, "app_cache")
    private val cacheFile = File(cacheDir, "apps.json")
    private val iconDir = File(cacheDir, "icons")
    
    init {
        cacheDir.mkdirs()
        iconDir.mkdirs()
    }
    
    fun saveApps(apps: List<AppInfo>) {
        val jsonArray = JSONArray()
        
        apps.forEach { app ->
            val json = JSONObject().apply {
                put("packageName", app.packageName)
                put("className", app.className)
                put("label", app.label)
                put("installTime", app.installTime)
                put("iconHash", saveIcon(app.icon, app.packageName))
            }
            jsonArray.put(json)
        }
        
        cacheFile.writeText(jsonArray.toString())
    }
    
    private fun saveIcon(drawable: Drawable, packageName: String): String {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        
        val hash = packageName.hashCode().toString()
        val iconFile = File(iconDir, "$hash.png")
        
        FileOutputStream(iconFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        return hash
    }
    
    fun loadApps(): List<AppInfo>? {
        if (!cacheFile.exists()) return null
        
        return try {
            val jsonArray = JSONArray(cacheFile.readText())
            val apps = mutableListOf<AppInfo>()
            
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                val packageName = json.getString("packageName")
                
                val icon = loadIcon(json.getString("iconHash"))
                    ?: try {
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
    
    private fun loadIcon(hash: String): Drawable? {
        val iconFile = File(iconDir, "$hash.png")
        if (!iconFile.exists()) return null
        
        return try {
            val bitmap = android.graphics.BitmapFactory.decodeFile(iconFile.absolutePath)
            android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
        } catch (e: Exception) {
            null
        }
    }
    
    fun hasCache(): Boolean {
        return cacheFile.exists()
    }
    
    fun clearCache() {
        cacheFile.delete()
        iconDir.deleteRecursively()
        iconDir.mkdirs()
    }
}
