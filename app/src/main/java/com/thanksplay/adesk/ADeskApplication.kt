package com.thanksplay.adesk

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.thanksplay.adesk.model.AppInfo
import com.thanksplay.adesk.util.ADeskLog
import com.thanksplay.adesk.util.AppCacheManager
import com.thanksplay.adesk.util.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale

class ADeskApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    var cachedApps: List<AppInfo>? = null
        private set
    
    var isDataLoaded = false
        private set
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var appCacheManager: AppCacheManager
    
    override fun attachBaseContext(base: Context) {
        prefsManager = PreferencesManager(base)
        val context = updateLanguage(base, prefsManager.language)
        super.attachBaseContext(context)
    }
    
    override fun onCreate() {
        super.onCreate()
        ADeskLog.init(BuildConfig.DEBUG)
        appCacheManager = AppCacheManager(this)
        preloadData()
    }
    
    private fun preloadData() {
        applicationScope.launch {
            val isCacheInvalidated = prefsManager.cacheInvalidated
            val cachedApps = appCacheManager.loadApps()
            
            if (cachedApps != null && cachedApps.isNotEmpty() && !isCacheInvalidated) {
                this@ADeskApplication.cachedApps = cachedApps
            }
            
            isDataLoaded = true
        }
    }
    
    fun applyLanguage(language: String) {
        val locale = when (language) {
            PreferencesManager.LANGUAGE_ENGLISH -> Locale.ENGLISH
            else -> Locale.CHINA
        }
        Locale.setDefault(locale)
        
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    
    private fun updateLanguage(context: Context, language: String): Context {
        val locale = when (language) {
            PreferencesManager.LANGUAGE_ENGLISH -> Locale.ENGLISH
            else -> Locale.CHINA
        }
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
