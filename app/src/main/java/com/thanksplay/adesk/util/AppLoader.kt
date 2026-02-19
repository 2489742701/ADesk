package com.thanksplay.adesk.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.thanksplay.adesk.model.AppInfo

class AppLoader(private val context: Context) {
    
    private val packageManager: PackageManager = context.packageManager
    
    fun loadAllApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val resolveInfoList: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
        
        return resolveInfoList
            .filter { it.activityInfo != null }
            .map { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo
                val appInfo = try {
                    val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getApplicationInfo(
                            activityInfo.packageName,
                            PackageManager.ApplicationInfoFlags.of(0)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getApplicationInfo(activityInfo.packageName, 0)
                    }
                    appInfo
                } catch (e: Exception) {
                    null
                }
                
                val installTime = try {
                    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getPackageInfo(
                            activityInfo.packageName,
                            PackageManager.PackageInfoFlags.of(0)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(activityInfo.packageName, 0)
                    }
                    packageInfo.firstInstallTime
                } catch (e: Exception) {
                    0L
                }
                
                AppInfo(
                    packageName = activityInfo.packageName,
                    className = activityInfo.name,
                    label = resolveInfo.loadLabel(packageManager)?.toString() 
                        ?: activityInfo.packageName,
                    icon = resolveInfo.loadIcon(packageManager) 
                        ?: context.resources.getDrawable(android.R.drawable.sym_def_app_icon, null),
                    installTime = installTime
                )
            }
            .filter { it.packageName != context.packageName }
    }
    
    fun sortApps(apps: List<AppInfo>, sortMethod: Int, customOrder: String = ""): List<AppInfo> {
        return when (sortMethod) {
            PreferencesManager.SORT_ABC -> {
                apps.sortedBy { it.label.lowercase() }
            }
            PreferencesManager.SORT_INSTALL_TIME -> {
                apps.sortedByDescending { it.installTime }
            }
            PreferencesManager.SORT_CUSTOM -> {
                if (customOrder.isEmpty()) {
                    apps
                } else {
                    val orderList = customOrder.split(",")
                    val orderMap = orderList.withIndex().associate { it.value to it.index }
                    apps.sortedBy { orderMap[it.packageName] ?: Int.MAX_VALUE }
                }
            }
            else -> apps.sortedBy { it.label.lowercase() }
        }
    }
}
