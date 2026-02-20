# ADesk 椤圭洰浠ｇ爜姹囨€籤n
鐢熸垚鏃堕棿: 2026-02-20 09:41:49

---


## app\src\main\java\com\thanksplay\adesk\ADeskApplication.kt

```kotlin
package com.thanksplay.adesk

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.thanksplay.adesk.model.AppInfo
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

```


## app\src\main\java\com\thanksplay\adesk\model\AppInfo.kt

```kotlin
package com.thanksplay.adesk.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val className: String,
    val label: String,
    val icon: Drawable,
    val installTime: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppInfo) return false
        return packageName == other.packageName && className == other.className
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + className.hashCode()
        return result
    }
}

```


## app\src\main\java\com\thanksplay\adesk\model\WeatherData.kt

```kotlin
package com.thanksplay.adesk.model

data class WeatherData(
    val temperature: Double,
    val description: String,
    val location: String
)

```


## app\src\main\java\com\thanksplay\adesk\util\PreferencesManager.kt

```kotlin
package com.thanksplay.adesk.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    var columnsPerPage: Int
        get() = prefs.getInt(KEY_COLUMNS_PER_PAGE, DEFAULT_COLUMNS)
        set(value) = prefs.edit().putInt(KEY_COLUMNS_PER_PAGE, value).apply()
    
    var labelPosition: Int
        get() = prefs.getInt(KEY_LABEL_POSITION, LABEL_POSITION_RIGHT)
        set(value) = prefs.edit().putInt(KEY_LABEL_POSITION, value).apply()
    
    var sortMethod: Int
        get() = prefs.getInt(KEY_SORT_METHOD, SORT_ABC)
        set(value) {
            prefs.edit().putInt(KEY_SORT_METHOD, value).apply()
            invalidateCache()
        }
    
    var favoriteApps: Set<String>
        get() = prefs.getStringSet(KEY_FAVORITE_APPS, emptySet()) ?: emptySet()
        set(value) {
            prefs.edit().putStringSet(KEY_FAVORITE_APPS, value).apply()
            invalidateCache()
        }
    
    var hiddenApps: Set<String>
        get() = prefs.getStringSet(KEY_HIDDEN_APPS, emptySet()) ?: emptySet()
        set(value) {
            prefs.edit().putStringSet(KEY_HIDDEN_APPS, value).apply()
            invalidateCache()
        }
    
    var customOrder: String
        get() = prefs.getString(KEY_CUSTOM_ORDER, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_CUSTOM_ORDER, value).apply()
            invalidateCache()
        }
    
    var showFavoritesOnly: Boolean
        get() = prefs.getBoolean(KEY_SHOW_FAVORITES_ONLY, false)
        set(value) {
            prefs.edit().putBoolean(KEY_SHOW_FAVORITES_ONLY, value).apply()
            invalidateCache()
        }
    
    var showOtherAppsAfterFavorites: Boolean
        get() = prefs.getBoolean(KEY_SHOW_OTHER_APPS_AFTER_FAVORITES, false)
        set(value) {
            prefs.edit().putBoolean(KEY_SHOW_OTHER_APPS_AFTER_FAVORITES, value).apply()
            invalidateCache()
        }
    
    var searchColumns: Int
        get() = prefs.getInt(KEY_SEARCH_COLUMNS, DEFAULT_SEARCH_COLUMNS)
        set(value) = prefs.edit().putInt(KEY_SEARCH_COLUMNS, value).apply()
    
    var homeApps: String
        get() = prefs.getString(KEY_HOME_APPS, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_HOME_APPS, value).apply()
            invalidateCache()
        }
    
    var homeColumns: Int
        get() = prefs.getInt(KEY_HOME_COLUMNS, DEFAULT_HOME_COLUMNS)
        set(value) = prefs.edit().putInt(KEY_HOME_COLUMNS, value).apply()
    
    var homeRows: Int
        get() = prefs.getInt(KEY_HOME_ROWS, DEFAULT_HOME_ROWS)
        set(value) = prefs.edit().putInt(KEY_HOME_ROWS, value).apply()
    
    var clockPosition: Int
        get() = prefs.getInt(KEY_CLOCK_POSITION, CLOCK_POSITION_CENTER)
        set(value) = prefs.edit().putInt(KEY_CLOCK_POSITION, value).apply()
    
    var wallpaperType: Int
        get() = prefs.getInt(KEY_WALLPAPER_TYPE, WALLPAPER_BLACK)
        set(value) = prefs.edit().putInt(KEY_WALLPAPER_TYPE, value).apply()
    
    var wallpaperColor: Int
        get() = prefs.getInt(KEY_WALLPAPER_COLOR, 0xFF000000.toInt())
        set(value) = prefs.edit().putInt(KEY_WALLPAPER_COLOR, value).apply()
    
    var wallpaperBlur: Boolean
        get() = prefs.getBoolean(KEY_WALLPAPER_BLUR, false)
        set(value) = prefs.edit().putBoolean(KEY_WALLPAPER_BLUR, value).apply()
    
    var wallpaperImageUri: String
        get() = prefs.getString(KEY_WALLPAPER_IMAGE_URI, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WALLPAPER_IMAGE_URI, value).apply()
    
    var particleEffect: Boolean
        get() = prefs.getBoolean(KEY_PARTICLE_EFFECT, false)
        set(value) = prefs.edit().putBoolean(KEY_PARTICLE_EFFECT, value).apply()
    
    var language: String
        get() = prefs.getString(KEY_LANGUAGE, LANGUAGE_CHINESE) ?: LANGUAGE_CHINESE
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()
    
    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
    
    var showHomeAppLabels: Boolean
        get() = prefs.getBoolean(KEY_SHOW_HOME_APP_LABELS, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_HOME_APP_LABELS, value).apply()
    
    var homeAppsOffset: Int
        get() = prefs.getInt(KEY_HOME_APPS_OFFSET, 0)
        set(value) = prefs.edit().putInt(KEY_HOME_APPS_OFFSET, value).apply()
    
    var showWeather: Boolean
        get() = prefs.getBoolean(KEY_SHOW_WEATHER, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_WEATHER, value).apply()
    
    var weatherCity: String
        get() = prefs.getString(KEY_WEATHER_CITY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEATHER_CITY, value).apply()
    
    var weatherApiUrl: String
        get() = prefs.getString(KEY_WEATHER_API_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEATHER_API_URL, value).apply()
    
    var weatherCacheTime: Long
        get() = prefs.getLong(KEY_WEATHER_CACHE_TIME, 0)
        set(value) = prefs.edit().putLong(KEY_WEATHER_CACHE_TIME, value).apply()
    
    var weatherCacheData: String
        get() = prefs.getString(KEY_WEATHER_CACHE_DATA, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEATHER_CACHE_DATA, value).apply()
    
    var weatherUpdateInterval: Int
        get() = prefs.getInt(KEY_WEATHER_UPDATE_INTERVAL, 120)
        set(value) = prefs.edit().putInt(KEY_WEATHER_UPDATE_INTERVAL, value).apply()
    
    fun isWeatherCacheValid(): Boolean {
        if (weatherCacheTime == 0L || weatherCacheData.isEmpty()) return false
        val elapsed = System.currentTimeMillis() - weatherCacheTime
        return elapsed < (weatherUpdateInterval * 60 * 1000L)
    }
    
    fun clearWeatherCache() {
        prefs.edit()
            .remove(KEY_WEATHER_CACHE_TIME)
            .remove(KEY_WEATHER_CACHE_DATA)
            .apply()
    }
    
    var cacheInvalidated: Boolean
        get() = prefs.getBoolean(KEY_CACHE_INVALIDATED, false)
        set(value) = prefs.edit().putBoolean(KEY_CACHE_INVALIDATED, value).apply()
    
    fun invalidateCache() {
        cacheInvalidated = true
    }
    
    fun clearCacheInvalidation() {
        cacheInvalidated = false
    }
    
    fun addFavoriteApp(packageName: String) {
        val favorites = favoriteApps.toMutableSet()
        favorites.add(packageName)
        favoriteApps = favorites
    }
    
    fun removeFavoriteApp(packageName: String) {
        val favorites = favoriteApps.toMutableSet()
        favorites.remove(packageName)
        favoriteApps = favorites
    }
    
    fun isFavorite(packageName: String): Boolean {
        return favoriteApps.contains(packageName)
    }
    
    fun hideApp(packageName: String) {
        val hidden = hiddenApps.toMutableSet()
        hidden.add(packageName)
        hiddenApps = hidden
    }
    
    fun showApp(packageName: String) {
        val hidden = hiddenApps.toMutableSet()
        hidden.remove(packageName)
        hiddenApps = hidden
    }
    
    fun isHidden(packageName: String): Boolean {
        return hiddenApps.contains(packageName)
    }
    
    companion object {
        private const val PREFS_NAME = "adesk_prefs"
        private const val KEY_COLUMNS_PER_PAGE = "columns_per_page"
        private const val KEY_LABEL_POSITION = "label_position"
        private const val KEY_SORT_METHOD = "sort_method"
        private const val KEY_FAVORITE_APPS = "favorite_apps"
        private const val KEY_HIDDEN_APPS = "hidden_apps"
        private const val KEY_CUSTOM_ORDER = "custom_order"
        private const val KEY_SHOW_FAVORITES_ONLY = "show_favorites_only"
        private const val KEY_SHOW_OTHER_APPS_AFTER_FAVORITES = "show_other_apps_after_favorites"
        private const val KEY_SEARCH_COLUMNS = "search_columns"
        private const val KEY_HOME_APPS = "home_apps"
        private const val KEY_HOME_COLUMNS = "home_columns"
        private const val KEY_HOME_ROWS = "home_rows"
        private const val KEY_CLOCK_POSITION = "clock_position"
        private const val KEY_WALLPAPER_TYPE = "wallpaper_type"
        private const val KEY_WALLPAPER_COLOR = "wallpaper_color"
        private const val KEY_WALLPAPER_BLUR = "wallpaper_blur"
        private const val KEY_WALLPAPER_IMAGE_URI = "wallpaper_image_uri"
        private const val KEY_PARTICLE_EFFECT = "particle_effect"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_SHOW_HOME_APP_LABELS = "show_home_app_labels"
        private const val KEY_HOME_APPS_OFFSET = "home_apps_offset"
        private const val KEY_SHOW_WEATHER = "show_weather"
        private const val KEY_WEATHER_CITY = "weather_city"
        private const val KEY_WEATHER_API_URL = "weather_api_url"
        private const val KEY_WEATHER_CACHE_TIME = "weather_cache_time"
        private const val KEY_WEATHER_CACHE_DATA = "weather_cache_data"
        private const val KEY_WEATHER_UPDATE_INTERVAL = "weather_update_interval"
        private const val KEY_CACHE_INVALIDATED = "cache_invalidated"
        
        const val DEFAULT_COLUMNS = 2
        const val DEFAULT_SEARCH_COLUMNS = 6
        const val DEFAULT_HOME_COLUMNS = 4
        const val DEFAULT_HOME_ROWS = 2
        const val LABEL_POSITION_RIGHT = 0
        const val LABEL_POSITION_BOTTOM = 1
        
        const val SORT_ABC = 0
        const val SORT_INSTALL_TIME = 1
        const val SORT_CUSTOM = 2
        
        const val CLOCK_POSITION_CENTER = 0
        const val CLOCK_POSITION_TOP = 1
        const val CLOCK_POSITION_HIDDEN = 2
        
        const val WALLPAPER_BLACK = 0
        const val WALLPAPER_COLOR = 1
        const val WALLPAPER_IMAGE = 2
        
        const val LANGUAGE_CHINESE = "zh"
        const val LANGUAGE_ENGLISH = "en"
    }
}

```


## app\src\main\java\com\thanksplay\adesk\util\WeatherService.kt

```kotlin
package com.thanksplay.adesk.util

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.thanksplay.adesk.model.WeatherData
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

object WeatherService {
    
    private const val DEFAULT_BASE_URL = "https://api.open-meteo.com/v1/forecast"
    
    fun fetchWeather(context: Context, lat: Double, lon: Double, callback: (WeatherData?) -> Unit, forceRefresh: Boolean = false) {
        val prefsManager = PreferencesManager(context)
        
        if (!forceRefresh && prefsManager.isWeatherCacheValid()) {
            val cachedData = parseCachedWeather(prefsManager.weatherCacheData)
            if (cachedData != null) {
                android.os.Handler(context.mainLooper).post {
                    callback(cachedData)
                }
                return
            }
        }
        
        val customApiUrl = prefsManager.weatherApiUrl
        val baseUrl = if (customApiUrl.isNotEmpty()) customApiUrl else DEFAULT_BASE_URL
        
        Thread {
            try {
                val url = URL("$baseUrl?latitude=$lat&longitude=$lon&current_weather=true&timezone=auto")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                val response = StringBuilder()
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                
                val json = JSONObject(response.toString())
                val currentWeather = json.getJSONObject("current_weather")
                val temperature = currentWeather.getDouble("temperature")
                val weatherCode = currentWeather.getInt("weathercode")
                
                val description = getWeatherDescription(weatherCode)
                val location = getLocationName(context, lat, lon)
                
                val data = WeatherData(temperature, description, location)
                
                prefsManager.weatherCacheTime = System.currentTimeMillis()
                prefsManager.weatherCacheData = "$temperature|$weatherCode|$location"
                
                android.os.Handler(context.mainLooper).post {
                    callback(data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (!forceRefresh) {
                    val cachedData = parseCachedWeather(prefsManager.weatherCacheData)
                    if (cachedData != null) {
                        android.os.Handler(context.mainLooper).post {
                            callback(cachedData)
                        }
                        return@Thread
                    }
                }
                android.os.Handler(context.mainLooper).post {
                    callback(null)
                }
            }
        }.start()
    }
    
    fun parseCachedWeather(cacheData: String): WeatherData? {
        if (cacheData.isEmpty()) return null
        return try {
            val parts = cacheData.split("|")
            if (parts.size == 3) {
                val temperature = parts[0].toDouble()
                val weatherCode = parts[1].toInt()
                val location = parts[2]
                WeatherData(temperature, getWeatherDescription(weatherCode), location)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun fetchWeatherByCity(context: Context, city: String, callback: (WeatherData?) -> Unit, forceRefresh: Boolean = false) {
        val prefsManager = PreferencesManager(context)
        
        if (!forceRefresh && prefsManager.isWeatherCacheValid()) {
            val cachedData = parseCachedWeather(prefsManager.weatherCacheData)
            if (cachedData != null) {
                android.os.Handler(context.mainLooper).post {
                    callback(cachedData)
                }
                return
            }
        }
        
        Thread {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(city, 1)
                
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    fetchWeather(context, address.latitude, address.longitude, callback, forceRefresh)
                } else {
                    android.os.Handler(context.mainLooper).post {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.os.Handler(context.mainLooper).post {
                    callback(null)
                }
            }
        }.start()
    }
    
    fun getCurrentLocation(context: Context, callback: (Double?, Double?) -> Unit) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    callback(location.latitude, location.longitude)
                }
                
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    locationManager.removeUpdates(this)
                    callback(null, null)
                }
            }
            
            val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            
            when {
                hasNetwork -> {
                    @Suppress("DEPRECATION")
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
                }
                hasGps -> {
                    @Suppress("DEPRECATION")
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                }
                else -> {
                    callback(null, null)
                }
            }
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                locationManager.removeUpdates(locationListener)
            }, 10000)
            
        } catch (e: SecurityException) {
            callback(null, null)
        } catch (e: Exception) {
            callback(null, null)
        }
    }
    
    private fun getLocationName(context: Context, lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                address.locality ?: address.subAdminArea ?: address.adminArea ?: getLocationNameFromNetwork(lat, lon)
            } else {
                getLocationNameFromNetwork(lat, lon)
            }
        } catch (e: Exception) {
            getLocationNameFromNetwork(lat, lon)
        }
    }
    
    private fun getLocationNameFromNetwork(lat: Double, lon: Double): String {
        return try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=10&accept-language=zh")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "ADesk/1.0")
            
            val response = StringBuilder()
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()
            
            val json = JSONObject(response.toString())
            val address = json.optJSONObject("address")
            when {
                address != null -> {
                    address.optString("city", 
                        address.optString("town", 
                            address.optString("county", 
                                address.optString("state", "Unknown"))))
                }
                else -> "Unknown"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }
    }
    
    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "晴"
            1, 2, 3 -> "多云"
            45, 48 -> "雾"
            51, 53, 55 -> "毛毛雨"
            56, 57 -> "冻雨"
            61, 63, 65 -> "雨"
            66, 67 -> "冻雨"
            71, 73, 75 -> "雪"
            77 -> "雪粒"
            80, 81, 82 -> "阵雨"
            85, 86 -> "阵雪"
            95 -> "雷暴"
            96, 99 -> "雷暴冰雹"
            else -> "未知"
        }
    }
}

```


## app\src\main\java\com\thanksplay\adesk\util\AppLoader.kt

```kotlin
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

```


## app\src\main\java\com\thanksplay\adesk\util\AppCacheManager.kt

```kotlin
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

```


## app\src\main\java\com\thanksplay\adesk\widget\ParticleView.kt

```kotlin
package com.thanksplay.adesk.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import java.util.Random

class ParticleView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    
    private val particles = mutableListOf<Particle>()
    private val random = Random()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handler = Handler(Looper.getMainLooper())
    
    private var lastX = 0f
    private var lastY = 0f
    private var velocityX = 0f
    private var velocityY = 0f
    
    private val colors = intArrayOf(
        0xFFFFFFFF.toInt(),
        0xFF4FC3F7.toInt(),
        0xFF81D4FA.toInt(),
        0xFFB3E5FC.toInt(),
        0xFFE1F5FE.toInt(),
        0xFFFFF59D.toInt(),
        0xFFFFF176.toInt()
    )
    
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateParticles()
            invalidate()
            handler.postDelayed(this, 16)
        }
    }
    
    fun onScroll(dx: Float, dy: Float, x: Float, y: Float) {
        velocityX = x - lastX
        velocityY = y - lastY
        lastX = x
        lastY = y
        
        val distance = kotlin.math.sqrt(velocityX * velocityX + velocityY * velocityY)
        val count = (distance / 5).toInt().coerceIn(1, 8)
        
        for (i in 0 until count) {
            createParticle(x, y, velocityX, velocityY)
        }
    }
    
    fun onTouchEnd() {
        velocityX = 0f
        velocityY = 0f
    }
    
    private fun createParticle(x: Float, y: Float, vx: Float, vy: Float) {
        val angle = random.nextFloat() * Math.PI * 2
        val speed = random.nextFloat() * 2 + 0.5f
        val size = random.nextFloat() * 8 + 2
        
        val particle = Particle(
            x = x + random.nextFloat() * 30 - 15,
            y = y + random.nextFloat() * 30 - 15,
            vx = (Math.cos(angle) * speed).toFloat() - vx * 0.05f,
            vy = (Math.sin(angle) * speed).toFloat() - vy * 0.05f,
            size = size,
            alpha = 255,
            color = colors[random.nextInt(colors.size)]
        )
        
        particles.add(particle)
        
        if (particles.size > 300) {
            particles.removeAt(0)
        }
    }
    
    private fun updateParticles() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.15f
            p.vx *= 0.98f
            p.alpha -= 4
            
            if (p.alpha <= 0) {
                iterator.remove()
            }
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        for (p in particles) {
            paint.color = p.color
            paint.alpha = p.alpha
            paint.setShadowLayer(p.size, 0f, 0f, p.color)
            canvas.drawCircle(p.x, p.y, p.size, paint)
        }
    }
    
    fun start() {
        handler.post(updateRunnable)
    }
    
    fun stop() {
        handler.removeCallbacks(updateRunnable)
    }
    
    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var size: Float,
        var alpha: Int,
        var color: Int
    )
}

```


## app\src\main\java\com\thanksplay\adesk\plugin\ADeskPlugin.kt

```kotlin
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

```


## app\src\main\java\com\thanksplay\adesk\plugin\PluginManager.kt

```kotlin
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

```


## app\src\main\java\com\thanksplay\adesk\adapter\AppAdapter.kt

```kotlin
package com.thanksplay.adesk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.AppInfo
import com.thanksplay.adesk.util.PreferencesManager

class AppAdapter(
    private val context: Context,
    private val prefsManager: PreferencesManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        private const val VIEW_TYPE_HORIZONTAL = 0
        private const val VIEW_TYPE_VERTICAL = 1
        private const val VIEW_TYPE_HEADER = 2
    }
    
    private var items: MutableList<ListItem> = mutableListOf()
    private var labelPosition: Int = PreferencesManager.LABEL_POSITION_RIGHT
    private var onSettingsClickListener: (() -> Unit)? = null
    private var columns: Int = 2
    
    fun setApps(apps: List<AppInfo>) {
        items.clear()
        apps.forEach { app ->
            items.add(ListItem.AppItem(app))
        }
        notifyDataSetChanged()
    }
    
    fun setItemsWithSeparator(favoriteApps: List<AppInfo>, otherApps: List<AppInfo>, separatorTitle: String) {
        items.clear()
        
        favoriteApps.forEach { app ->
            items.add(ListItem.AppItem(app))
        }
        
        if (otherApps.isNotEmpty()) {
            items.add(ListItem.Header(separatorTitle))
            otherApps.forEach { app ->
                items.add(ListItem.AppItem(app))
            }
        }
        
        notifyDataSetChanged()
    }
    
    fun setLabelPosition(position: Int) {
        this.labelPosition = position
        notifyDataSetChanged()
    }
    
    fun setColumns(columns: Int) {
        this.columns = columns
    }
    
    fun setOnSettingsClickListener(listener: () -> Unit) {
        onSettingsClickListener = listener
    }
    
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.Header -> VIEW_TYPE_HEADER
            is ListItem.AppItem -> if (labelPosition == PreferencesManager.LABEL_POSITION_RIGHT) {
                VIEW_TYPE_HORIZONTAL
            } else {
                VIEW_TYPE_VERTICAL
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_section_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_HORIZONTAL -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_app_horizontal, parent, false)
                AppViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_app_vertical, parent, false)
                AppViewHolder(view)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> {
                (holder as HeaderViewHolder).headerText.text = item.title
            }
            is ListItem.AppItem -> {
                val appHolder = holder as AppViewHolder
                appHolder.icon.setImageDrawable(item.app.icon)
                appHolder.label.text = item.app.label
                
                appHolder.itemView.setOnClickListener {
                    if (item.app.packageName == "com.thanksplay.adesk.settings") {
                        onSettingsClickListener?.invoke()
                    } else {
                        launchApp(item.app)
                    }
                }
                
                appHolder.itemView.setOnLongClickListener {
                    if (item.app.packageName != "com.thanksplay.adesk.settings" && context is OnAppActionListener) {
                        (context as OnAppActionListener).onAppLongClick(item.app)
                    }
                    true
                }
            }
        }
    }
    
    override fun getItemCount(): Int = items.size
    
    fun getSpanSize(position: Int): Int {
        return when (items[position]) {
            is ListItem.Header -> columns
            is ListItem.AppItem -> 1
        }
    }
    
    private fun launchApp(app: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private sealed class ListItem {
        data class Header(val title: String) : ListItem()
        data class AppItem(val app: AppInfo) : ListItem()
    }
    
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerText: TextView = itemView.findViewById(R.id.headerText)
    }
    
    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.appIcon)
        val label: TextView = itemView.findViewById(R.id.appLabel)
    }
    
    interface OnAppActionListener {
        fun onAppLongClick(app: AppInfo)
    }
}

```


## app\src\main\java\com\thanksplay\adesk\adapter\HomeAppAdapter.kt

```kotlin
package com.thanksplay.adesk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.AppInfo
import com.thanksplay.adesk.util.PreferencesManager

class HomeAppAdapter(private val context: Context) : RecyclerView.Adapter<HomeAppAdapter.ViewHolder>() {
    
    private var items: List<AppInfo> = emptyList()
    private val prefsManager = PreferencesManager(context)
    
    fun setItems(apps: List<AppInfo>) {
        this.items = apps
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_app, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = items[position]
        holder.icon.setImageDrawable(app.icon)
        
        if (prefsManager.showHomeAppLabels) {
            holder.label.text = app.label
            holder.label.visibility = View.VISIBLE
        } else {
            holder.label.visibility = View.GONE
        }
        
        holder.itemView.setOnClickListener {
            launchApp(app)
        }
    }
    
    override fun getItemCount(): Int = items.size
    
    private fun launchApp(app: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.appIcon)
        val label: TextView = itemView.findViewById(R.id.appLabel)
    }
}

```


## app\src\main\java\com\thanksplay\adesk\adapter\SearchAppAdapter.kt

```kotlin
package com.thanksplay.adesk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.AppInfo
import java.text.Collator
import java.util.Locale

class SearchAppAdapter(
    private val context: Context,
    private val columns: Int = 6
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_APP = 1
    }
    
    private var items: MutableList<ListItem> = mutableListOf()
    
    fun setItems(apps: List<AppInfo>) {
        items.clear()
        
        val groupedApps = apps.groupBy { app ->
            getFirstLetter(app.label)
        }.toSortedMap(compareBy { it })
        
        var hasZSection = false
        groupedApps.forEach { (letter, appList) ->
            items.add(ListItem.Header(letter))
            appList.sortedBy { it.label.lowercase() }.forEach { app ->
                items.add(ListItem.AppItem(app))
            }
            if (letter == "Z") {
                hasZSection = true
            }
        }
        
        if (hasZSection) {
            items.add(ListItem.Header(context.getString(R.string.thanksplay_easter_egg)))
        }
        
        notifyDataSetChanged()
    }
    
    private fun getFirstLetter(label: String): String {
        if (label.isEmpty()) return "#"
        
        val firstChar = label[0]
        
        if (firstChar in 'A'..'Z') return firstChar.toString()
        if (firstChar in 'a'..'z') return firstChar.uppercaseChar().toString()
        
        if (firstChar.code in 0x4E00..0x9FFF) {
            val pinyin = getPinyin(firstChar)
            if (pinyin.isNotEmpty()) {
                return pinyin[0].uppercaseChar().toString()
            }
        }
        
        if (firstChar.isLetter()) {
            return firstChar.uppercaseChar().toString()
        }
        
        return "#"
    }
    
    private fun getPinyin(c: Char): String {
        val collator = Collator.getInstance(Locale.CHINESE)
        val pinyinMap = mapOf(
            '阿' to "A", '啊' to "A", '安' to "A", '爱' to "A", '艾' to "A",
            '八' to "B", '百' to "B", '北' to "B", '本' to "B", '边' to "B",
            '才' to "C", '长' to "C", '城' to "C", '出' to "C", '从' to "C",
            '大' to "D", '道' to "D", '地' to "D", '电' to "D", '东' to "D",
            '二' to "E",
            '发' to "F", '方' to "F", '非' to "F", '风' to "F", '福' to "F",
            '高' to "G", '个' to "G", '工' to "G", '公' to "G", '国' to "G",
            '海' to "H", '和' to "H", '河' to "H", '黑' to "H", '红' to "H",
            '机' to "J", '基' to "J", '即' to "J", '几' to "J", '家' to "J",
            '开' to "K", '看' to "K", '可' to "K", '空' to "K", '口' to "K",
            '来' to "L", '老' to "L", '乐' to "L", '里' to "L", '力' to "L",
            '马' to "M", '美' to "M", '们' to "M", '名' to "M", '明' to "M",
            '南' to "N", '你' to "N", '年' to "N", '那' to "N", '能' to "N",
            '欧' to "O",
            '平' to "P", '朋' to "P", '普' to "P",
            '七' to "Q", '期' to "Q", '起' to "Q", '前' to "Q", '去' to "Q",
            '人' to "R", '日' to "R", '然' to "R", '让' to "R", '热' to "R",
            '三' to "S", '上' to "S", '生' to "S", '时' to "S", '手' to "S",
            '他' to "T", '天' to "T", '通' to "T", '同' to "T", '头' to "T",
            '为' to "W", '文' to "W", '我' to "W", '无' to "W", '五' to "W",
            '西' to "X", '下' to "X", '小' to "X", '新' to "X", '学' to "X",
            '一' to "Y", '以' to "Y", '有' to "Y", '于' to "Y", '月' to "Y",
            '在' to "Z", '中' to "Z", '这' to "Z", '主' to "Z", '子' to "Z"
        )
        
        return pinyinMap[c] ?: try {
            val chars = charArrayOf(c)
            val sb = StringBuilder()
            for (ch in chars) {
                if (ch.code in 0x4E00..0x9FFF) {
                    val pinyin = getSimplePinyin(ch)
                    sb.append(pinyin)
                } else {
                    sb.append(ch)
                }
            }
            sb.toString()
        } catch (e: Exception) {
            "#"
        }
    }
    
    private fun getSimplePinyin(c: Char): String {
        val code = c.code
        return when {
            code in 0x4E00..0x4EFF -> "A"
            code in 0x4F00..0x4FFF -> "B"
            code in 0x5000..0x50FF -> "C"
            code in 0x5100..0x51FF -> "D"
            code in 0x5200..0x52FF -> "E"
            code in 0x5300..0x53FF -> "F"
            code in 0x5400..0x54FF -> "G"
            code in 0x5500..0x55FF -> "H"
            code in 0x5600..0x56FF -> "J"
            code in 0x5700..0x57FF -> "K"
            code in 0x5800..0x58FF -> "L"
            code in 0x5900..0x59FF -> "M"
            code in 0x5A00..0x5AFF -> "N"
            code in 0x5B00..0x5BFF -> "O"
            code in 0x5C00..0x5CFF -> "P"
            code in 0x5D00..0x5DFF -> "Q"
            code in 0x5E00..0x5EFF -> "R"
            code in 0x5F00..0x5FFF -> "S"
            code in 0x6000..0x60FF -> "T"
            code in 0x6100..0x61FF -> "W"
            code in 0x6200..0x62FF -> "X"
            code in 0x6300..0x63FF -> "Y"
            code in 0x6400..0x64FF -> "Z"
            code in 0x6500..0x65FF -> "A"
            code in 0x6600..0x66FF -> "B"
            code in 0x6700..0x67FF -> "C"
            code in 0x6800..0x68FF -> "D"
            code in 0x6900..0x69FF -> "G"
            code in 0x6A00..0x6AFF -> "H"
            code in 0x6B00..0x6BFF -> "J"
            code in 0x6C00..0x6CFF -> "L"
            code in 0x6D00..0x6DFF -> "M"
            code in 0x6E00..0x6EFF -> "N"
            code in 0x6F00..0x6FFF -> "P"
            code in 0x7000..0x70FF -> "Q"
            code in 0x7100..0x71FF -> "R"
            code in 0x7200..0x72FF -> "S"
            code in 0x7300..0x73FF -> "T"
            code in 0x7400..0x74FF -> "W"
            code in 0x7500..0x75FF -> "X"
            code in 0x7600..0x76FF -> "Y"
            code in 0x7700..0x77FF -> "Z"
            code in 0x7800..0x78FF -> "A"
            code in 0x7900..0x79FF -> "B"
            code in 0x7A00..0x7AFF -> "C"
            code in 0x7B00..0x7BFF -> "D"
            code in 0x7C00..0x7CFF -> "E"
            code in 0x7D00..0x7DFF -> "F"
            code in 0x7E00..0x7EFF -> "G"
            code in 0x7F00..0x7FFF -> "H"
            code in 0x8000..0x80FF -> "J"
            code in 0x8100..0x81FF -> "K"
            code in 0x8200..0x82FF -> "L"
            code in 0x8300..0x83FF -> "M"
            code in 0x8400..0x84FF -> "N"
            code in 0x8500..0x85FF -> "O"
            code in 0x8600..0x86FF -> "P"
            code in 0x8700..0x87FF -> "Q"
            code in 0x8800..0x88FF -> "R"
            code in 0x8900..0x89FF -> "S"
            code in 0x8A00..0x8AFF -> "T"
            code in 0x8B00..0x8BFF -> "W"
            code in 0x8C00..0x8CFF -> "X"
            code in 0x8D00..0x8DFF -> "Y"
            code in 0x8E00..0x8EFF -> "Z"
            code in 0x8F00..0x8FFF -> "A"
            code in 0x9000..0x90FF -> "B"
            code in 0x9100..0x91FF -> "C"
            code in 0x9200..0x92FF -> "D"
            code in 0x9300..0x93FF -> "E"
            code in 0x9400..0x94FF -> "F"
            code in 0x9500..0x95FF -> "G"
            code in 0x9600..0x96FF -> "H"
            code in 0x9700..0x97FF -> "J"
            code in 0x9800..0x98FF -> "K"
            code in 0x9900..0x99FF -> "L"
            code in 0x9A00..0x9AFF -> "M"
            code in 0x9B00..0x9BFF -> "N"
            code in 0x9C00..0x9CFF -> "O"
            code in 0x9D00..0x9DFF -> "P"
            code in 0x9E00..0x9EFF -> "Q"
            code in 0x9F00..0x9FFF -> "R"
            else -> "#"
        }
    }
    
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.AppItem -> TYPE_APP
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_section_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_app_grid, parent, false)
                AppViewHolder(view)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> {
                (holder as HeaderViewHolder).headerText.text = item.letter
            }
            is ListItem.AppItem -> {
                val appHolder = holder as AppViewHolder
                appHolder.icon.setImageDrawable(item.app.icon)
                appHolder.label.text = item.app.label
                appHolder.itemView.setOnClickListener {
                    launchApp(item.app)
                }
            }
        }
    }
    
    override fun getItemCount(): Int = items.size
    
    private fun launchApp(app: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getSpanSize(position: Int): Int {
        return when (items[position]) {
            is ListItem.Header -> columns
            is ListItem.AppItem -> 1
        }
    }
    
    private sealed class ListItem {
        data class Header(val letter: String) : ListItem()
        data class AppItem(val app: AppInfo) : ListItem()
    }
    
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerText: TextView = itemView.findViewById(R.id.headerText)
    }
    
    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.appIcon)
        val label: TextView = itemView.findViewById(R.id.appLabel)
    }
}

```


## app\src\main\java\com\thanksplay\adesk\adapter\DesktopPagerAdapter.kt

```kotlin
package com.thanksplay.adesk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R

class DesktopPagerAdapter(
    private val pages: List<View>
) : RecyclerView.Adapter<DesktopPagerAdapter.PageViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return PageViewHolder(pages[viewType])
    }
    
    override fun getItemViewType(position: Int): Int {
        return position
    }
    
    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
    }
    
    override fun getItemCount(): Int = pages.size
    
    class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

```


## app\src\main\java\com\thanksplay\adesk\ui\MainActivity.kt

```kotlin
package com.thanksplay.adesk.ui

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.thanksplay.adesk.ADeskApplication
import com.thanksplay.adesk.R
import com.thanksplay.adesk.adapter.AppAdapter
import com.thanksplay.adesk.adapter.DesktopPagerAdapter
import com.thanksplay.adesk.adapter.HomeAppAdapter
import com.thanksplay.adesk.adapter.SearchAppAdapter
import com.thanksplay.adesk.model.AppInfo
import com.thanksplay.adesk.util.AppCacheManager
import com.thanksplay.adesk.util.AppLoader
import com.thanksplay.adesk.util.PreferencesManager
import com.thanksplay.adesk.widget.ParticleView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), AppAdapter.OnAppActionListener {
    
    companion object {
        private const val PAGE_HOME = 0
        private const val PAGE_APPS = 1
        private const val PAGE_SEARCH = 2
        
        private const val INDICATOR_SHOW_DURATION = 2000L
        private const val INDICATOR_FADE_DURATION = 500L
        
        private const val CALENDAR_PERMISSION_REQUEST_CODE = 1001
    }
    
    private lateinit var viewPager: ViewPager2
    private lateinit var pageIndicator: LinearLayout
    private lateinit var prefsManager: PreferencesManager
    private lateinit var appLoader: AppLoader
    private lateinit var appCacheManager: AppCacheManager
    
    private var allApps: List<AppInfo> = emptyList()
    private var displayedApps: List<AppInfo> = emptyList()
    private var packageToAppMap: Map<String, AppInfo> = emptyMap()
    
    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchInput: EditText
    
    private lateinit var tvTime: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvEvent: TextView
    private lateinit var homeAppsRecyclerView: RecyclerView
    private lateinit var spacerTop: View
    private lateinit var spacerBottom: View
    
    private lateinit var appsAdapter: AppAdapter
    private lateinit var searchAdapter: SearchAppAdapter
    private lateinit var homeAppsAdapter: HomeAppAdapter
    
    private lateinit var particleView: ParticleView
    private lateinit var wallpaperImageView: android.widget.ImageView
    
    private val timeFormat: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
    private val dateFormat: DateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.CHINA)
    
    private var clockJob: Job? = null
    private var needsReload = true
    private var isReceiverRegistered = false
    
    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            needsReload = true
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        prefsManager = PreferencesManager(this)
        appLoader = AppLoader(this)
        appCacheManager = AppCacheManager(this)
        
        if (prefsManager.isFirstLaunch) {
            showLanguageSelectionDialog()
        }
        
        requestRequiredPermissions()
        
        registerPackageReceiver()
        
        initViews()
        applyWallpaper()
        loadAppsAsync()
    }
    
    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (prefsManager.showWeather) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
        
        if (checkSelfPermission(android.Manifest.permission.READ_CALENDAR) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.READ_CALENDAR)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest.toTypedArray(), 1000)
        }
    }
    
    private fun showLanguageSelectionDialog() {
        val options = arrayOf("中文", "English")
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setItems(options) { _, which ->
                val language = if (which == 0) {
                    PreferencesManager.LANGUAGE_CHINESE
                } else {
                    PreferencesManager.LANGUAGE_ENGLISH
                }
                prefsManager.language = language
                prefsManager.isFirstLaunch = false
                recreate()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun registerPackageReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addDataScheme("package")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(packageReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(packageReceiver, filter)
            }
            isReceiverRegistered = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun applyWallpaper() {
        val rootLayout = findViewById<View>(android.R.id.content)
        
        when (prefsManager.wallpaperType) {
            PreferencesManager.WALLPAPER_BLACK -> {
                rootLayout.setBackgroundColor(Color.BLACK)
                wallpaperImageView.visibility = View.GONE
            }
            PreferencesManager.WALLPAPER_COLOR -> {
                rootLayout.setBackgroundColor(prefsManager.wallpaperColor)
                wallpaperImageView.visibility = View.GONE
            }
            PreferencesManager.WALLPAPER_IMAGE -> {
                val imageUri = prefsManager.wallpaperImageUri
                if (imageUri.isNotEmpty()) {
                    try {
                        val uri = android.net.Uri.parse(imageUri)
                        wallpaperImageView.setImageURI(uri)
                        wallpaperImageView.visibility = View.VISIBLE
                        rootLayout.setBackgroundColor(Color.BLACK)
                    } catch (e: Exception) {
                        rootLayout.setBackgroundColor(Color.BLACK)
                        wallpaperImageView.visibility = View.GONE
                    }
                } else {
                    rootLayout.setBackgroundColor(Color.BLACK)
                    wallpaperImageView.visibility = View.GONE
                }
            }
        }
    }
    
    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        pageIndicator = findViewById(R.id.pageIndicator)
        particleView = findViewById(R.id.particleView)
        wallpaperImageView = findViewById(R.id.wallpaperImage)
        
        val pages = createPages()
        val pagerAdapter = DesktopPagerAdapter(pages)
        viewPager.adapter = pagerAdapter
        
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position)
                showPageIndicator()
                when (position) {
                    PAGE_HOME -> { }
                    PAGE_APPS -> { }
                    PAGE_SEARCH -> {
                        searchInput.requestFocus()
                        searchAdapter.setItems(getVisibleApps())
                    }
                }
                if (position != PAGE_SEARCH) {
                    hideKeyboard()
                }
            }
        })
        
        startClock()
        
        if (prefsManager.particleEffect) {
            particleView.start()
        }
    }
    
    private fun startClock() {
        clockJob = lifecycleScope.launch {
            while (isActive) {
                updateTime()
                delay(1000)
            }
        }
    }
    
    private fun showPageIndicator() {
        pageIndicator.visibility = View.VISIBLE
        pageIndicator.animate().alpha(1f).setDuration(200).start()
        lifecycleScope.launch {
            delay(INDICATOR_SHOW_DURATION)
            pageIndicator.animate().alpha(0f).setDuration(INDICATOR_FADE_DURATION).withEndAction {
                pageIndicator.visibility = View.INVISIBLE
            }.start()
        }
    }
    
    private fun createPages(): List<View> {
        val inflater = LayoutInflater.from(this)
        return listOf(
            createHomePage(inflater),
            createAppsPage(inflater),
            createSearchPage(inflater)
        )
    }
    
    private fun createHomePage(inflater: LayoutInflater): View {
        val homePage = inflater.inflate(R.layout.page_home, viewPager, false)
        tvTime = homePage.findViewById(R.id.tvTime)
        tvDate = homePage.findViewById(R.id.tvDate)
        tvEvent = homePage.findViewById(R.id.tvEvent)
        homeAppsRecyclerView = homePage.findViewById(R.id.homeAppsRecyclerView)
        spacerTop = homePage.findViewById(R.id.spacerTop)
        spacerBottom = homePage.findViewById(R.id.spacerBottom)
        
        val widgetContainer = homePage.findViewById<android.widget.FrameLayout>(R.id.widgetContainer)
        loadWidget(widgetContainer)
        
        updateClockPosition()
        
        val homeColumns = prefsManager.homeColumns
        homeAppsRecyclerView.layoutManager = GridLayoutManager(this, homeColumns)
        homeAppsAdapter = HomeAppAdapter(this)
        homeAppsRecyclerView.adapter = homeAppsAdapter
        
        return homePage
    }
    
    private fun loadWidget(container: android.widget.FrameLayout) {
        if (prefsManager.showWeather) {
            container.visibility = View.VISIBLE
            loadWeather(container, false)
            
            container.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                if (lastWeatherClickTime > 0 && currentTime - lastWeatherClickTime < 500) {
                    loadWeather(container, true)
                }
                lastWeatherClickTime = currentTime
            }
        }
    }
    
    private var lastWeatherClickTime: Long = 0L
    
    private fun loadWeather(container: android.widget.FrameLayout, forceRefresh: Boolean) {
        val tvTemp = container.findViewById<TextView>(R.id.tvWeatherTemp)
        val tvDesc = container.findViewById<TextView>(R.id.tvWeatherDesc)
        val tvLocation = container.findViewById<TextView>(R.id.tvWeatherLocation)
        
        if (!forceRefresh && prefsManager.isWeatherCacheValid()) {
            val cachedData = com.thanksplay.adesk.util.WeatherService.parseCachedWeather(prefsManager.weatherCacheData)
            if (cachedData != null) {
                tvTemp.text = "${cachedData.temperature.toInt()}°"
                tvDesc.text = cachedData.description
                tvLocation.text = cachedData.location
                return
            }
        }
        
        if (forceRefresh) {
            tvTemp.text = "--°"
            tvDesc.text = getString(R.string.weather_loading)
            tvLocation.text = "--"
        }
        
        val city = prefsManager.weatherCity
        if (city.isNotEmpty()) {
            com.thanksplay.adesk.util.WeatherService.fetchWeatherByCity(this, city, { data ->
                if (data != null) {
                    tvTemp.text = "${data.temperature.toInt()}°"
                    tvDesc.text = data.description
                    tvLocation.text = data.location
                } else {
                    tvTemp.text = "--°"
                    tvDesc.text = getString(R.string.weather_load_failed)
                    tvLocation.text = city
                }
            }, forceRefresh)
        } else {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                com.thanksplay.adesk.util.WeatherService.getCurrentLocation(this) { lat, lon ->
                    if (lat != null && lon != null) {
                        com.thanksplay.adesk.util.WeatherService.fetchWeather(this, lat, lon, { data ->
                            if (data != null) {
                                tvTemp.text = "${data.temperature.toInt()}°"
                                tvDesc.text = data.description
                                tvLocation.text = data.location
                            } else {
                                tvTemp.text = "--°"
                                tvDesc.text = getString(R.string.weather_load_failed)
                                tvLocation.text = "--"
                            }
                        }, forceRefresh)
                    } else {
                        tvDesc.text = getString(R.string.weather_location_failed)
                    }
                }
            } else {
                tvTemp.text = "--°"
                tvDesc.text = getString(R.string.weather_location_failed)
                tvLocation.text = "--"
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 1002)
            }
        }
    }
    
    private fun createAppsPage(inflater: LayoutInflater): View {
        val appsPage = inflater.inflate(R.layout.page_apps, viewPager, false)
        appsRecyclerView = appsPage.findViewById(R.id.appsRecyclerView)
        updateLayoutManager()
        appsAdapter = AppAdapter(this, prefsManager)
        appsAdapter.setOnSettingsClickListener {
            openSettings()
        }
        appsRecyclerView.adapter = appsAdapter
        
        return appsPage
    }
    
    private fun createSearchPage(inflater: LayoutInflater): View {
        val searchPage = inflater.inflate(R.layout.page_search, viewPager, false)
        searchRecyclerView = searchPage.findViewById(R.id.searchResultsRecyclerView)
        val searchColumns = prefsManager.searchColumns
        val gridLayoutManager = GridLayoutManager(this, searchColumns)
        searchAdapter = SearchAppAdapter(this, searchColumns)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return searchAdapter.getSpanSize(position)
            }
        }
        searchRecyclerView.layoutManager = gridLayoutManager
        searchRecyclerView.adapter = searchAdapter
        
        searchInput = searchPage.findViewById(R.id.searchInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString() ?: "")
            }
        })
        return searchPage
    }
    
    private fun updateClockPosition() {
        when (prefsManager.clockPosition) {
            PreferencesManager.CLOCK_POSITION_TOP -> {
                spacerTop.layoutParams = LinearLayout.LayoutParams(0, 0, 0f)
                spacerBottom.layoutParams = LinearLayout.LayoutParams(0, 0, 2f)
                tvTime.visibility = View.VISIBLE
                tvDate.visibility = View.VISIBLE
            }
            PreferencesManager.CLOCK_POSITION_HIDDEN -> {
                spacerTop.layoutParams = LinearLayout.LayoutParams(0, 0, 0f)
                spacerBottom.layoutParams = LinearLayout.LayoutParams(0, 0, 2f)
                tvTime.visibility = View.GONE
                tvDate.visibility = View.GONE
                tvEvent.visibility = View.GONE
            }
            else -> {
                spacerTop.layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
                spacerBottom.layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
                tvTime.visibility = View.VISIBLE
                tvDate.visibility = View.VISIBLE
            }
        }
    }
    
    private fun updateTime() {
        val now = Date()
        tvTime.text = timeFormat.format(now)
        tvDate.text = dateFormat.format(now)
    }
    
    private fun loadAppsAsync() {
        val app = application as ADeskApplication
        val preloadedApps = app.cachedApps
        val isCacheInvalidated = prefsManager.cacheInvalidated
        
        if (preloadedApps != null && preloadedApps.isNotEmpty() && !needsReload && !isCacheInvalidated) {
            allApps = preloadedApps
            packageToAppMap = preloadedApps.associateBy { it.packageName }
            updateDisplayedApps()
            loadHomeApps()
            loadCalendarEvents()
        } else {
            lifecycleScope.launch {
                val cachedApps = withContext(Dispatchers.IO) {
                    appCacheManager.loadApps()
                }
                
                if (cachedApps != null && cachedApps.isNotEmpty() && !needsReload && !isCacheInvalidated) {
                    allApps = cachedApps
                    packageToAppMap = cachedApps.associateBy { it.packageName }
                    updateDisplayedApps()
                    loadHomeApps()
                    loadCalendarEvents()
                } else {
                    refreshAppsFromSystem()
                    prefsManager.clearCacheInvalidation()
                }
            }
        }
    }
    
    private fun refreshAppsFromSystem() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                appLoader.loadAllApps()
            }
            
            allApps = apps
            packageToAppMap = apps.associateBy { it.packageName }
            
            withContext(Dispatchers.IO) {
                appCacheManager.saveApps(apps)
            }
            
            needsReload = false
            updateDisplayedApps()
            loadHomeApps()
            loadCalendarEvents()
        }
    }
    
    fun forceRefreshCache() {
        appCacheManager.clearCache()
        needsReload = true
        refreshAppsFromSystem()
    }
    
    private fun getVisibleApps(): List<AppInfo> {
        val hiddenApps = prefsManager.hiddenApps
        return allApps.filter { !hiddenApps.contains(it.packageName) }
    }
    
    private fun loadCalendarEvents() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) 
            != PackageManager.PERMISSION_GRANTED) {
            requestCalendarPermission()
            return
        }
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance()
                val startTime = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val endTime = calendar.timeInMillis
                
                val projection = arrayOf(
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART
                )
                
                val selection = "(${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?)"
                val selectionArgs = arrayOf(startTime.toString(), endTime.toString())
                
                val cursor: Cursor? = contentResolver.query(
                    CalendarContract.Events.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    "${CalendarContract.Events.DTSTART} ASC"
                )
                
                var eventTitle: String? = null
                cursor?.use {
                    if (it.moveToFirst()) {
                        val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
                        eventTitle = it.getString(titleIndex)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    if (eventTitle != null) {
                        tvEvent.text = getString(R.string.calendar_event_format, eventTitle)
                        tvEvent.visibility = View.VISIBLE
                    } else {
                        tvEvent.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun requestCalendarPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CALENDAR),
                CALENDAR_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALENDAR_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadCalendarEvents()
                }
            }
            1000, 1002 -> {
                if (grantResults.isNotEmpty() && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
                    if (prefsManager.showWeather) {
                        val widgetContainer = findViewById<android.widget.FrameLayout>(R.id.widgetContainer)
                        if (widgetContainer.visibility == View.VISIBLE) {
                            loadWeather(widgetContainer, false)
                        }
                    }
                    loadCalendarEvents()
                }
            }
        }
    }
    
    private fun loadHomeApps() {
        val homeAppsOrder = prefsManager.homeApps
        val maxApps = prefsManager.homeColumns * prefsManager.homeRows
        
        if (homeAppsOrder.isNotEmpty()) {
            val orderList = homeAppsOrder.split(",").filter { it.isNotEmpty() }
            val homeApps = orderList.mapNotNull { packageName ->
                packageToAppMap[packageName]
            }.take(maxApps)
            homeAppsAdapter.setItems(homeApps)
        } else {
            val defaultApps = getVisibleApps()
                .sortedBy { it.label.lowercase() }
                .take(maxApps)
            homeAppsAdapter.setItems(defaultApps)
        }
        
        applyHomeAppsOffset()
    }
    
    private fun applyHomeAppsOffset() {
        val offset = prefsManager.homeAppsOffset
        homeAppsRecyclerView.translationY = -offset.toFloat()
    }
    
    private fun updateLayoutManager() {
        val columns = prefsManager.columnsPerPage
        val gridLayoutManager = GridLayoutManager(this, columns)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return appsAdapter.getSpanSize(position)
            }
        }
        appsRecyclerView.layoutManager = gridLayoutManager
    }
    
    private fun updateDisplayedApps() {
        val sortMethod = prefsManager.sortMethod
        val customOrder = prefsManager.customOrder
        val columns = prefsManager.columnsPerPage
        appsAdapter.setColumns(columns)
        
        if (prefsManager.showFavoritesOnly && prefsManager.showOtherAppsAfterFavorites) {
            val visibleApps = getVisibleApps()
            val favoriteApps = visibleApps.filter { prefsManager.isFavorite(it.packageName) }
            val otherApps = visibleApps.filter { !prefsManager.isFavorite(it.packageName) }
            
            val sortedFavorites = appLoader.sortApps(favoriteApps, sortMethod, customOrder)
            val sortedOthers = appLoader.sortApps(otherApps, sortMethod, customOrder)
            
            val finalFavorites = sortedFavorites.toMutableList()
            finalFavorites.add(createSettingsApp())
            
            updateLayoutManager()
            appsAdapter.setLabelPosition(prefsManager.labelPosition)
            appsAdapter.setItemsWithSeparator(finalFavorites, sortedOthers, getString(R.string.other_apps))
        } else {
            val appsToShow = if (prefsManager.showFavoritesOnly) {
                getVisibleApps().filter { prefsManager.isFavorite(it.packageName) }
            } else {
                getVisibleApps()
            }
            
            displayedApps = appLoader.sortApps(appsToShow, sortMethod, customOrder)
            
            val finalList = displayedApps.toMutableList()
            finalList.add(createSettingsApp())
            
            updateLayoutManager()
            appsAdapter.setLabelPosition(prefsManager.labelPosition)
            appsAdapter.setApps(finalList)
        }
    }
    
    private fun createSettingsApp(): AppInfo {
        return AppInfo(
            packageName = "com.thanksplay.adesk.settings",
            className = "SettingsActivity",
            label = getString(R.string.settings),
            icon = resources.getDrawable(android.R.drawable.ic_menu_manage, null),
            installTime = Long.MAX_VALUE
        )
    }
    
    private fun openSettings() {
        needsReload = true
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun filterApps(query: String) {
        val appsToSearch = getVisibleApps()
        
        if (query.isEmpty()) {
            searchAdapter.setItems(appsToSearch)
            return
        }
        
        val filtered = appsToSearch.filter { 
            it.label.lowercase().contains(query.lowercase()) 
        }
        searchAdapter.setItems(filtered)
    }
    
    private fun updatePageIndicator(position: Int) {
        for (i in 0 until pageIndicator.childCount) {
            val indicator = pageIndicator.getChildAt(i)
            val drawableRes = if (i == position) {
                R.drawable.indicator_selected
            } else {
                R.drawable.indicator_unselected
            }
            indicator.setBackgroundResource(drawableRes)
        }
    }
    
    override fun onAppLongClick(app: AppInfo) {
        if (app.packageName == "com.thanksplay.adesk.settings") {
            return
        }
        
        val isFavorite = prefsManager.isFavorite(app.packageName)
        val options = mutableListOf<String>()
        
        if (isFavorite) {
            options.add(getString(R.string.remove_from_favorites))
        } else {
            options.add(getString(R.string.add_to_favorites))
        }
        options.add(getString(R.string.app_info))
        
        AlertDialog.Builder(this)
            .setTitle(app.label)
            .setItems(options.toTypedArray()) { _, which ->
                when (which) {
                    0 -> {
                        if (isFavorite) {
                            prefsManager.removeFavoriteApp(app.packageName)
                            Toast.makeText(this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show()
                            if (prefsManager.showFavoritesOnly) {
                                updateDisplayedApps()
                            }
                        } else {
                            prefsManager.addFavoriteApp(app.packageName)
                            Toast.makeText(this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> {
                        openAppInfo(app)
                    }
                }
            }
            .show()
    }
    
    private fun openAppInfo(app: AppInfo) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${app.packageName}")
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }
    
    override fun dispatchTouchEvent(event: android.view.MotionEvent): Boolean {
        if (::particleView.isInitialized && prefsManager.particleEffect) {
            when (event.action) {
                android.view.MotionEvent.ACTION_MOVE -> {
                    particleView.onScroll(0f, 0f, event.rawX, event.rawY)
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    particleView.onTouchEnd()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
    
    override fun onResume() {
        super.onResume()
        updateTime()
        updateClockPosition()
        applyWallpaper()
        
        if (needsReload) {
            loadAppsAsync()
            needsReload = false
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        clockJob?.cancel()
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(packageReceiver)
                isReceiverRegistered = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (::particleView.isInitialized) {
            particleView.stop()
        }
    }
}

```


## app\src\main\java\com\thanksplay\adesk\ui\SettingsActivity.kt

```kotlin
package com.thanksplay.adesk.ui

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.thanksplay.adesk.ADeskApplication
import com.thanksplay.adesk.R
import com.thanksplay.adesk.util.PreferencesManager

class SettingsActivity : androidx.appcompat.app.AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    
    private lateinit var languageGroup: RadioGroup
    private lateinit var languageChinese: RadioButton
    private lateinit var languageEnglish: RadioButton
    private lateinit var clockPositionGroup: RadioGroup
    private lateinit var clockCenter: RadioButton
    private lateinit var clockTop: RadioButton
    private lateinit var clockHidden: RadioButton
    private lateinit var homeLayoutSpinner: Spinner
    private lateinit var btnManageHomeApps: Button
    private lateinit var showHomeAppLabelsSwitch: Switch
    private lateinit var homeAppsOffsetInput: EditText
    private lateinit var showWeatherSwitch: Switch
    private lateinit var weatherCityInput: EditText
    private lateinit var wallpaperTypeGroup: RadioGroup
    private lateinit var wallpaperBlack: RadioButton
    private lateinit var wallpaperColor: RadioButton
    private lateinit var wallpaperImage: RadioButton
    private lateinit var colorPickerLayout: LinearLayout
    private lateinit var colorPreview: View
    private lateinit var btnPickColor: Button
    private lateinit var btnPickImage: Button
    private lateinit var particleEffectSwitch: Switch
    private lateinit var columnsSpinner: Spinner
    private lateinit var labelPositionGroup: RadioGroup
    private lateinit var labelRight: RadioButton
    private lateinit var labelBottom: RadioButton
    private lateinit var sortMethodGroup: RadioGroup
    private lateinit var sortAbc: RadioButton
    private lateinit var sortInstallTime: RadioButton
    private lateinit var sortCustom: RadioButton
    private lateinit var showFavoritesSwitch: Switch
    private lateinit var showOtherAppsLayout: LinearLayout
    private lateinit var showOtherAppsSwitch: Switch
    private lateinit var btnManageFavorites: Button
    private lateinit var btnCustomOrder: Button
    private lateinit var btnManageHidden: Button
    private lateinit var btnSetAsDefault: Button
    private lateinit var btnRefreshCache: Button
    private lateinit var btnPluginManager: Button
    private lateinit var btnAboutAuthor: Button
    private lateinit var btnRestartLauncher: Button
    private lateinit var weatherApiUrlInput: EditText
    private lateinit var weatherUpdateIntervalSpinner: Spinner
    
    private val columnOptions = arrayOf("1", "2", "4", "6")
    private val homeLayoutOptions = arrayOf("4x2", "6x2", "6x3", "1x4", "1x6")
    private val intervalOptions = arrayOf("15分钟", "30分钟", "1小时", "2小时", "4小时", "6小时", "12小时", "24小时")
    private val intervalValues = arrayOf(15, 30, 60, 120, 240, 360, 720, 1440)
    
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            prefsManager.wallpaperImageUri = it.toString()
            Toast.makeText(this, R.string.wallpaper_set, Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        prefsManager = PreferencesManager(this)
        
        initViews()
        loadSettings()
        setupListeners()
    }
    
    private fun initViews() {
        languageGroup = findViewById(R.id.languageGroup)
        languageChinese = findViewById(R.id.languageChinese)
        languageEnglish = findViewById(R.id.languageEnglish)
        clockPositionGroup = findViewById(R.id.clockPositionGroup)
        clockCenter = findViewById(R.id.clockCenter)
        clockTop = findViewById(R.id.clockTop)
        clockHidden = findViewById(R.id.clockHidden)
        homeLayoutSpinner = findViewById(R.id.homeLayoutSpinner)
        btnManageHomeApps = findViewById(R.id.btnManageHomeApps)
        showHomeAppLabelsSwitch = findViewById(R.id.showHomeAppLabelsSwitch)
        homeAppsOffsetInput = findViewById(R.id.homeAppsOffsetInput)
        showWeatherSwitch = findViewById(R.id.showWeatherSwitch)
        weatherCityInput = findViewById(R.id.weatherCityInput)
        weatherApiUrlInput = findViewById(R.id.weatherApiUrlInput)
        weatherUpdateIntervalSpinner = findViewById(R.id.weatherUpdateIntervalSpinner)
        wallpaperTypeGroup = findViewById(R.id.wallpaperTypeGroup)
        wallpaperBlack = findViewById(R.id.wallpaperBlack)
        wallpaperColor = findViewById(R.id.wallpaperColor)
        wallpaperImage = findViewById(R.id.wallpaperImage)
        colorPickerLayout = findViewById(R.id.colorPickerLayout)
        colorPreview = findViewById(R.id.colorPreview)
        btnPickColor = findViewById(R.id.btnPickColor)
        btnPickImage = findViewById(R.id.btnPickImage)
        particleEffectSwitch = findViewById(R.id.particleEffectSwitch)
        columnsSpinner = findViewById(R.id.columnsSpinner)
        labelPositionGroup = findViewById(R.id.labelPositionGroup)
        labelRight = findViewById(R.id.labelRight)
        labelBottom = findViewById(R.id.labelBottom)
        sortMethodGroup = findViewById(R.id.sortMethodGroup)
        sortAbc = findViewById(R.id.sortAbc)
        sortInstallTime = findViewById(R.id.sortInstallTime)
        sortCustom = findViewById(R.id.sortCustom)
        showFavoritesSwitch = findViewById(R.id.showFavoritesSwitch)
        showOtherAppsLayout = findViewById(R.id.showOtherAppsLayout)
        showOtherAppsSwitch = findViewById(R.id.showOtherAppsSwitch)
        btnManageFavorites = findViewById(R.id.btnManageFavorites)
        btnCustomOrder = findViewById(R.id.btnCustomOrder)
        btnManageHidden = findViewById(R.id.btnManageHidden)
        btnSetAsDefault = findViewById(R.id.btnSetAsDefault)
        btnRefreshCache = findViewById(R.id.btnRefreshCache)
        btnPluginManager = findViewById(R.id.btnPluginManager)
        btnAboutAuthor = findViewById(R.id.btnAboutAuthor)
        btnRestartLauncher = findViewById(R.id.btnRestartLauncher)
        
        val columnAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, columnOptions)
        columnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        columnsSpinner.adapter = columnAdapter
        
        val homeLayoutAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, homeLayoutOptions)
        homeLayoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        homeLayoutSpinner.adapter = homeLayoutAdapter
        
        val intervalAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intervalOptions)
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        weatherUpdateIntervalSpinner.adapter = intervalAdapter
    }
    
    private fun loadSettings() {
        when (prefsManager.language) {
            PreferencesManager.LANGUAGE_CHINESE -> languageChinese.isChecked = true
            PreferencesManager.LANGUAGE_ENGLISH -> languageEnglish.isChecked = true
        }
        
        when (prefsManager.clockPosition) {
            PreferencesManager.CLOCK_POSITION_CENTER -> clockCenter.isChecked = true
            PreferencesManager.CLOCK_POSITION_TOP -> clockTop.isChecked = true
            PreferencesManager.CLOCK_POSITION_HIDDEN -> clockHidden.isChecked = true
        }
        
        val columns = prefsManager.columnsPerPage
        val columnIndex = when (columns) {
            1 -> 0
            2 -> 1
            4 -> 2
            6 -> 3
            else -> 1
        }
        columnsSpinner.setSelection(columnIndex)
        
        val homeColumns = prefsManager.homeColumns
        val homeRows = prefsManager.homeRows
        val homeLayoutIndex = when ("${homeColumns}x${homeRows}") {
            "4x2" -> 0
            "6x2" -> 1
            "6x3" -> 2
            "1x4" -> 3
            "1x6" -> 4
            else -> 0
        }
        homeLayoutSpinner.setSelection(homeLayoutIndex)
        
        showHomeAppLabelsSwitch.isChecked = prefsManager.showHomeAppLabels
        homeAppsOffsetInput.setText(prefsManager.homeAppsOffset.toString())
        
        showWeatherSwitch.isChecked = prefsManager.showWeather
        weatherCityInput.setText(prefsManager.weatherCity)
        weatherApiUrlInput.setText(prefsManager.weatherApiUrl)
        
        val currentInterval = prefsManager.weatherUpdateInterval
        val intervalIndex = intervalValues.indexOf(currentInterval)
        if (intervalIndex >= 0) {
            weatherUpdateIntervalSpinner.setSelection(intervalIndex)
        } else {
            weatherUpdateIntervalSpinner.setSelection(3)
        }
        
        when (prefsManager.wallpaperType) {
            PreferencesManager.WALLPAPER_BLACK -> wallpaperBlack.isChecked = true
            PreferencesManager.WALLPAPER_COLOR -> wallpaperColor.isChecked = true
            PreferencesManager.WALLPAPER_IMAGE -> wallpaperImage.isChecked = true
        }
        
        updateWallpaperOptionsVisibility()
        colorPreview.setBackgroundColor(prefsManager.wallpaperColor)
        
        particleEffectSwitch.isChecked = prefsManager.particleEffect
        
        when (prefsManager.labelPosition) {
            PreferencesManager.LABEL_POSITION_RIGHT -> labelRight.isChecked = true
            PreferencesManager.LABEL_POSITION_BOTTOM -> labelBottom.isChecked = true
        }
        
        when (prefsManager.sortMethod) {
            PreferencesManager.SORT_ABC -> sortAbc.isChecked = true
            PreferencesManager.SORT_INSTALL_TIME -> sortInstallTime.isChecked = true
            PreferencesManager.SORT_CUSTOM -> sortCustom.isChecked = true
        }
        
        showFavoritesSwitch.isChecked = prefsManager.showFavoritesOnly
        showOtherAppsSwitch.isChecked = prefsManager.showOtherAppsAfterFavorites
        updateShowOtherAppsVisibility()
    }
    
    private fun updateShowOtherAppsVisibility() {
        showOtherAppsLayout.visibility = if (prefsManager.showFavoritesOnly) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    
    private fun updateWallpaperOptionsVisibility() {
        colorPickerLayout.visibility = if (prefsManager.wallpaperType == PreferencesManager.WALLPAPER_COLOR) {
            View.VISIBLE
        } else {
            View.GONE
        }
        btnPickImage.visibility = if (prefsManager.wallpaperType == PreferencesManager.WALLPAPER_IMAGE) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    
    private fun setupListeners() {
        languageGroup.setOnCheckedChangeListener { _, checkedId ->
            val language = when (checkedId) {
                R.id.languageChinese -> PreferencesManager.LANGUAGE_CHINESE
                else -> PreferencesManager.LANGUAGE_ENGLISH
            }
            if (language != prefsManager.language) {
                prefsManager.language = language
                (application as ADeskApplication).applyLanguage(language)
                Toast.makeText(this, R.string.restart_to_apply, Toast.LENGTH_SHORT).show()
                recreate()
            }
        }
        
        clockPositionGroup.setOnCheckedChangeListener { _, checkedId ->
            val position = when (checkedId) {
                R.id.clockCenter -> PreferencesManager.CLOCK_POSITION_CENTER
                R.id.clockTop -> PreferencesManager.CLOCK_POSITION_TOP
                R.id.clockHidden -> PreferencesManager.CLOCK_POSITION_HIDDEN
                else -> PreferencesManager.CLOCK_POSITION_CENTER
            }
            prefsManager.clockPosition = position
        }
        
        homeLayoutSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val layout = homeLayoutOptions[position].split("x")
                prefsManager.homeColumns = layout[0].toInt()
                prefsManager.homeRows = layout[1].toInt()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        wallpaperTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.wallpaperBlack -> PreferencesManager.WALLPAPER_BLACK
                R.id.wallpaperColor -> PreferencesManager.WALLPAPER_COLOR
                else -> PreferencesManager.WALLPAPER_IMAGE
            }
            prefsManager.wallpaperType = type
            updateWallpaperOptionsVisibility()
        }
        
        btnPickColor.setOnClickListener {
            showColorPicker()
        }
        
        btnPickImage.setOnClickListener {
            imagePickerLauncher.launch(arrayOf("image/*"))
        }
        
        particleEffectSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.particleEffect = isChecked
        }
        
        columnsSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val columns = columnOptions[position].toInt()
                prefsManager.columnsPerPage = columns
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        labelPositionGroup.setOnCheckedChangeListener { _, checkedId ->
            val position = when (checkedId) {
                R.id.labelRight -> PreferencesManager.LABEL_POSITION_RIGHT
                else -> PreferencesManager.LABEL_POSITION_BOTTOM
            }
            prefsManager.labelPosition = position
        }
        
        sortMethodGroup.setOnCheckedChangeListener { _, checkedId ->
            val method = when (checkedId) {
                R.id.sortAbc -> PreferencesManager.SORT_ABC
                R.id.sortInstallTime -> PreferencesManager.SORT_INSTALL_TIME
                else -> PreferencesManager.SORT_CUSTOM
            }
            prefsManager.sortMethod = method
        }
        
        showFavoritesSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.showFavoritesOnly = isChecked
            updateShowOtherAppsVisibility()
        }
        
        showOtherAppsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.showOtherAppsAfterFavorites = isChecked
        }
        
        btnManageHomeApps.setOnClickListener {
            startActivity(Intent(this, HomeAppsActivity::class.java))
        }
        
        showHomeAppLabelsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.showHomeAppLabels = isChecked
        }
        
        homeAppsOffsetInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val offset = homeAppsOffsetInput.text.toString().toIntOrNull() ?: 0
                prefsManager.homeAppsOffset = offset
            }
        }
        
        showWeatherSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.showWeather = isChecked
        }
        
        weatherCityInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                prefsManager.weatherCity = weatherCityInput.text.toString()
            }
        }
        
        weatherApiUrlInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                prefsManager.weatherApiUrl = weatherApiUrlInput.text.toString()
            }
        }
        
        weatherUpdateIntervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefsManager.weatherUpdateInterval = intervalValues[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        btnManageFavorites.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
        
        btnCustomOrder.setOnClickListener {
            startActivity(Intent(this, CustomOrderActivity::class.java))
        }
        
        btnManageHidden.setOnClickListener {
            startActivity(Intent(this, HiddenAppsActivity::class.java))
        }
        
        btnSetAsDefault.setOnClickListener {
            openDefaultLauncherSettings()
        }
        
        btnRefreshCache.setOnClickListener {
            val cacheManager = com.thanksplay.adesk.util.AppCacheManager(this)
            cacheManager.clearCache()
            Toast.makeText(this, R.string.cache_refreshed, Toast.LENGTH_SHORT).show()
        }
        
        btnPluginManager.setOnClickListener {
            startActivity(Intent(this, PluginsActivity::class.java))
        }
        
        btnAboutAuthor.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        
        btnRestartLauncher.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
    
    private fun showColorPicker() {
        val colors = arrayOf(
            "#000000" to getString(R.string.color_black),
            "#1A1A1A" to getString(R.string.color_dark_gray),
            "#333333" to getString(R.string.color_gray),
            "#1E3A5F" to getString(R.string.color_dark_blue),
            "#1B5E20" to getString(R.string.color_dark_green),
            "#4A148C" to getString(R.string.color_dark_purple),
            "#BF360C" to getString(R.string.color_dark_red),
            "#FF6F00" to getString(R.string.color_orange)
        )
        
        val colorNames = colors.map { it.second }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle(R.string.select_color)
            .setItems(colorNames) { _, which ->
                val colorHex = colors[which].first
                val color = Color.parseColor(colorHex)
                prefsManager.wallpaperColor = color
                colorPreview.setBackgroundColor(color)
            }
            .show()
    }
    
    private fun openDefaultLauncherSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

```


## app\src\main\java\com\thanksplay\adesk\ui\AboutActivity.kt

```kotlin
package com.thanksplay.adesk.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thanksplay.adesk.R

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }
}

```


## app\src\main\java\com\thanksplay\adesk\ui\FavoritesActivity.kt

```kotlin
package com.thanksplay.adesk.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.AppInfo
import com.thanksplay.adesk.util.AppLoader
import com.thanksplay.adesk.util.PreferencesManager

class FavoritesActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var appLoader: AppLoader
    
    private lateinit var searchInput: EditText
    private lateinit var allAppsRecyclerView: RecyclerView
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var btnSave: android.widget.Button
    
    private var allApps: List<AppInfo> = emptyList()
    private var filteredApps: List<AppInfo> = emptyList()
    private var favoriteApps: MutableList<AppInfo> = mutableListOf()
    
    private lateinit var allAppsAdapter: AllAppsAdapter
    private lateinit var favoritesAdapter: FavoriteAppAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        
        prefsManager = PreferencesManager(this)
        appLoader = AppLoader(this)
        
        initViews()
        loadApps()
    }
    
    private fun initViews() {
        searchInput = findViewById(R.id.searchInput)
        allAppsRecyclerView = findViewById(R.id.allAppsRecyclerView)
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView)
        btnSave = findViewById(R.id.btnSave)
        
        allAppsRecyclerView.layoutManager = LinearLayoutManager(this)
        allAppsAdapter = AllAppsAdapter(
            onAddClick = { app -> addToFavorites(app) },
            onRemoveClick = { app -> removeFromFavorites(app) },
            isFavorite = { app -> prefsManager.isFavorite(app.packageName) }
        )
        allAppsRecyclerView.adapter = allAppsAdapter
        
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favoritesAdapter = FavoriteAppAdapter { app -> removeFromFavorites(app) }
        favoritesRecyclerView.adapter = favoritesAdapter
        
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString() ?: "")
            }
        })
        
        btnSave.setOnClickListener {
            finish()
        }
    }
    
    private fun loadApps() {
        allApps = appLoader.loadAllApps().sortedBy { it.label.lowercase() }
        val favoriteSet = prefsManager.favoriteApps
        
        favoriteApps = allApps.filter { favoriteSet.contains(it.packageName) }.toMutableList()
        filteredApps = allApps
        
        allAppsAdapter.setItems(filteredApps, favoriteApps.map { it.packageName }.toSet())
        favoritesAdapter.setItems(favoriteApps)
    }
    
    private fun filterApps(query: String) {
        filteredApps = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { it.label.lowercase().contains(query.lowercase()) }
        }
        allAppsAdapter.setItems(filteredApps, favoriteApps.map { it.packageName }.toSet())
    }
    
    private fun addToFavorites(app: AppInfo) {
        if (!favoriteApps.any { it.packageName == app.packageName }) {
            prefsManager.addFavoriteApp(app.packageName)
            favoriteApps.add(0, app)
            favoritesAdapter.setItems(favoriteApps)
            allAppsAdapter.setItems(filteredApps, favoriteApps.map { it.packageName }.toSet())
            Toast.makeText(this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun removeFromFavorites(app: AppInfo) {
        prefsManager.removeFavoriteApp(app.packageName)
        favoriteApps.removeAll { it.packageName == app.packageName }
        favoritesAdapter.setItems(favoriteApps)
        allAppsAdapter.setItems(filteredApps, favoriteApps.map { it.packageName }.toSet())
        Toast.makeText(this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show()
    }
    
    inner class AllAppsAdapter(
        private val onAddClick: (AppInfo) -> Unit,
        private val onRemoveClick: (AppInfo) -> Unit,
        private val isFavorite: (AppInfo) -> Boolean
    ) : RecyclerView.Adapter<AllAppsAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        private var favoriteSet: Set<String> = emptySet()
        
        fun setItems(items: List<AppInfo>, favoriteSet: Set<String>) {
            this.items = items
            this.favoriteSet = favoriteSet
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_horizontal, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            
            holder.itemView.setOnClickListener {
                if (favoriteSet.contains(app.packageName)) {
                    onRemoveClick(app)
                } else {
                    onAddClick(app)
                }
            }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
        }
    }
    
    inner class FavoriteAppAdapter(
        private val onRemoveClick: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<FavoriteAppAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        
        fun setItems(items: List<AppInfo>) {
            this.items = items
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_horizontal, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            
            holder.itemView.setOnClickListener {
                onRemoveClick(app)
            }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
        }
    }
}

```


## app\src\main\java\com\thanksplay\adesk\ui\HiddenAppsActivity.kt

```kotlin
package com.thanksplay.adesk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.AppInfo
import com.thanksplay.adesk.util.AppLoader
import com.thanksplay.adesk.util.PreferencesManager

class HiddenAppsActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var appLoader: AppLoader
    
    private lateinit var hiddenAppsRecyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    
    private var hiddenApps: MutableList<AppInfo> = mutableListOf()
    private lateinit var adapter: HiddenAppsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)
        
        prefsManager = PreferencesManager(this)
        appLoader = AppLoader(this)
        
        initViews()
        loadHiddenApps()
    }
    
    private fun initViews() {
        hiddenAppsRecyclerView = findViewById(R.id.hiddenAppsRecyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        
        hiddenAppsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HiddenAppsAdapter { app -> showApp(app) }
        hiddenAppsRecyclerView.adapter = adapter
    }
    
    private fun loadHiddenApps() {
        val allApps = appLoader.loadAllApps()
        val hiddenSet = prefsManager.hiddenApps
        
        hiddenApps = allApps.filter { hiddenSet.contains(it.packageName) }.toMutableList()
        
        if (hiddenApps.isEmpty()) {
            hiddenAppsRecyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            hiddenAppsRecyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            adapter.setItems(hiddenApps)
        }
    }
    
    private fun showApp(app: AppInfo) {
        prefsManager.showApp(app.packageName)
        hiddenApps.removeAll { it.packageName == app.packageName }
        adapter.setItems(hiddenApps)
        Toast.makeText(this, R.string.app_shown, Toast.LENGTH_SHORT).show()
        
        if (hiddenApps.isEmpty()) {
            hiddenAppsRecyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        }
    }
    
    inner class HiddenAppsAdapter(
        private val onShow: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<HiddenAppsAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        
        fun setItems(items: List<AppInfo>) {
            this.items = items
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_hidden_app, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            holder.itemView.setOnClickListener { onShow(app) }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
        }
    }
}

```


## app\src\main\java\com\thanksplay\adesk\ui\CustomOrderActivity.kt

```kotlin
package com.thanksplay.adesk.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.AppInfo
import com.thanksplay.adesk.util.AppLoader
import com.thanksplay.adesk.util.PreferencesManager

class CustomOrderActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var appLoader: AppLoader
    
    private lateinit var searchInput: EditText
    private lateinit var tvSearchResults: TextView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var orderRecyclerView: RecyclerView
    private lateinit var btnSave: android.widget.Button
    
    private var allApps: List<AppInfo> = emptyList()
    private var orderedApps: MutableList<AppInfo> = mutableListOf()
    private var searchResults: List<AppInfo> = emptyList()
    
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var searchAdapter: SearchAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_order)
        
        prefsManager = PreferencesManager(this)
        appLoader = AppLoader(this)
        
        initViews()
        loadApps()
    }
    
    private fun initViews() {
        searchInput = findViewById(R.id.searchInput)
        tvSearchResults = findViewById(R.id.tvSearchResults)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)
        orderRecyclerView = findViewById(R.id.orderRecyclerView)
        btnSave = findViewById(R.id.btnSave)
        
        orderRecyclerView.layoutManager = LinearLayoutManager(this)
        orderAdapter = OrderAdapter(
            onMoveToTop = { app -> moveToTop(app) },
            onHide = { app -> hideApp(app) },
            onStartDrag = { viewHolder -> }
        )
        orderRecyclerView.adapter = orderAdapter
        
        val callback = DragItemCallback(orderAdapter) { from, to ->
            val app = orderedApps.removeAt(from)
            orderedApps.add(to, app)
            orderAdapter.setItems(orderedApps)
        }
        val touchHelper = androidx.recyclerview.widget.ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(orderRecyclerView)
        
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchAdapter { app -> addToOrder(app) }
        searchResultsRecyclerView.adapter = searchAdapter
        
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString() ?: "")
            }
        })
        
        btnSave.setOnClickListener {
            saveOrder()
        }
    }
    
    private fun loadApps() {
        allApps = appLoader.loadAllApps().sortedBy { it.label.lowercase() }
        val customOrder = prefsManager.customOrder
        
        if (customOrder.isNotEmpty()) {
            val orderList = customOrder.split(",")
            orderedApps = orderList.mapNotNull { packageName ->
                allApps.find { it.packageName == packageName }
            }.toMutableList()
            
            allApps.filter { app ->
                !orderedApps.any { it.packageName == app.packageName }
            }.forEach { orderedApps.add(it) }
        } else {
            orderedApps = allApps.toMutableList()
        }
        
        orderAdapter.setItems(orderedApps)
    }
    
    private fun filterApps(query: String) {
        if (query.isEmpty()) {
            tvSearchResults.visibility = View.GONE
            searchResultsRecyclerView.visibility = View.GONE
            return
        }
        
        searchResults = allApps.filter { 
            it.label.lowercase().contains(query.lowercase()) 
        }
        
        if (searchResults.isNotEmpty()) {
            tvSearchResults.visibility = View.VISIBLE
            searchResultsRecyclerView.visibility = View.VISIBLE
            searchAdapter.setItems(searchResults)
        } else {
            tvSearchResults.visibility = View.GONE
            searchResultsRecyclerView.visibility = View.GONE
        }
    }
    
    private fun addToOrder(app: AppInfo) {
        if (!orderedApps.any { it.packageName == app.packageName }) {
            orderedApps.add(0, app)
            orderAdapter.setItems(orderedApps)
            searchInput.text.clear()
            tvSearchResults.visibility = View.GONE
            searchResultsRecyclerView.visibility = View.GONE
        }
    }
    
    private fun moveToTop(app: AppInfo) {
        val index = orderedApps.indexOfFirst { it.packageName == app.packageName }
        if (index > 0) {
            orderedApps.removeAt(index)
            orderedApps.add(0, app)
            orderAdapter.setItems(orderedApps)
            orderRecyclerView.scrollToPosition(0)
        }
    }
    
    private fun hideApp(app: AppInfo) {
        prefsManager.hideApp(app.packageName)
        orderedApps.removeAll { it.packageName == app.packageName }
        orderAdapter.setItems(orderedApps)
        Toast.makeText(this, R.string.app_hidden, Toast.LENGTH_SHORT).show()
    }
    
    private fun saveOrder() {
        val orderString = orderedApps.joinToString(",") { it.packageName }
        prefsManager.customOrder = orderString
        Toast.makeText(this, R.string.order_saved, Toast.LENGTH_SHORT).show()
        finish()
    }
    
    class DragItemCallback(
        private val adapter: OrderAdapter,
        private val onMove: (Int, Int) -> Unit
    ) : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
        androidx.recyclerview.widget.ItemTouchHelper.UP or androidx.recyclerview.widget.ItemTouchHelper.DOWN,
        0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition
            onMove(from, to)
            return true
        }
        
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }
    
    inner class OrderAdapter(
        private val onMoveToTop: (AppInfo) -> Unit,
        private val onHide: (AppInfo) -> Unit,
        private val onStartDrag: (RecyclerView.ViewHolder) -> Unit
    ) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        
        fun setItems(items: List<AppInfo>) {
            this.items = items
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_custom_order, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            
            holder.btnMoveToTop.setOnClickListener { onMoveToTop(app) }
            holder.btnHide.setOnClickListener { onHide(app) }
            
            holder.dragHandle.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    onStartDrag(holder)
                }
                false
            }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
            val dragHandle: ImageView = itemView.findViewById(R.id.dragHandle)
            val btnMoveToTop: ImageButton = itemView.findViewById(R.id.btnMoveToTop)
            val btnHide: ImageButton = itemView.findViewById(R.id.btnHide)
        }
    }
    
    inner class SearchAdapter(
        private val onAdd: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        
        fun setItems(items: List<AppInfo>) {
            this.items = items
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_horizontal, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            holder.itemView.setOnClickListener { onAdd(app) }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
        }
    }
}

```


## app\src\main\java\com\thanksplay\adesk\ui\HomeAppsActivity.kt

```kotlin
package com.thanksplay.adesk.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.AppInfo
import com.thanksplay.adesk.util.AppLoader
import com.thanksplay.adesk.util.PreferencesManager

class HomeAppsActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var appLoader: AppLoader
    
    private lateinit var searchInput: EditText
    private lateinit var homeAppsRecyclerView: RecyclerView
    private lateinit var allAppsRecyclerView: RecyclerView
    private lateinit var btnSave: android.widget.Button
    
    private var allApps: List<AppInfo> = emptyList()
    private var homeApps: MutableList<AppInfo> = mutableListOf()
    private var filteredApps: List<AppInfo> = emptyList()
    
    private lateinit var homeAppsAdapter: HomeAppsEditAdapter
    private lateinit var allAppsAdapter: AllAppsEditAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_apps)
        
        prefsManager = PreferencesManager(this)
        appLoader = AppLoader(this)
        
        initViews()
        loadApps()
    }
    
    private fun initViews() {
        searchInput = findViewById(R.id.searchInput)
        homeAppsRecyclerView = findViewById(R.id.homeAppsRecyclerView)
        allAppsRecyclerView = findViewById(R.id.allAppsRecyclerView)
        btnSave = findViewById(R.id.btnSave)
        
        val homeColumns = prefsManager.homeColumns
        homeAppsRecyclerView.layoutManager = GridLayoutManager(this, homeColumns)
        homeAppsAdapter = HomeAppsEditAdapter { app -> removeFromHome(app) }
        homeAppsRecyclerView.adapter = homeAppsAdapter
        
        allAppsRecyclerView.layoutManager = LinearLayoutManager(this)
        allAppsAdapter = AllAppsEditAdapter { app -> addToHome(app) }
        allAppsRecyclerView.adapter = allAppsAdapter
        
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString() ?: "")
            }
        })
        
        btnSave.setOnClickListener {
            saveHomeApps()
        }
    }
    
    private fun loadApps() {
        allApps = appLoader.loadAllApps().sortedBy { it.label.lowercase() }
        val homeAppsOrder = prefsManager.homeApps
        val maxApps = prefsManager.homeColumns * prefsManager.homeRows
        
        if (homeAppsOrder.isNotEmpty()) {
            val orderList = homeAppsOrder.split(",").filter { it.isNotEmpty() }
            homeApps = orderList.mapNotNull { packageName ->
                allApps.find { it.packageName == packageName }
            }.toMutableList()
        } else {
            homeApps = allApps.take(maxApps).toMutableList()
        }
        
        filteredApps = allApps
        homeAppsAdapter.setItems(homeApps)
        allAppsAdapter.setItems(filteredApps, homeApps.map { it.packageName }.toSet())
    }
    
    private fun filterApps(query: String) {
        filteredApps = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { it.label.lowercase().contains(query.lowercase()) }
        }
        allAppsAdapter.setItems(filteredApps, homeApps.map { it.packageName }.toSet())
    }
    
    private fun addToHome(app: AppInfo) {
        val maxApps = prefsManager.homeColumns * prefsManager.homeRows
        if (homeApps.size >= maxApps) {
            Toast.makeText(this, "主页应用已满", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!homeApps.any { it.packageName == app.packageName }) {
            homeApps.add(app)
            homeAppsAdapter.setItems(homeApps)
            allAppsAdapter.setItems(filteredApps, homeApps.map { it.packageName }.toSet())
        }
    }
    
    private fun removeFromHome(app: AppInfo) {
        homeApps.removeAll { it.packageName == app.packageName }
        homeAppsAdapter.setItems(homeApps)
        allAppsAdapter.setItems(filteredApps, homeApps.map { it.packageName }.toSet())
    }
    
    private fun saveHomeApps() {
        val orderString = homeApps.joinToString(",") { it.packageName }
        prefsManager.homeApps = orderString
        Toast.makeText(this, R.string.order_saved, Toast.LENGTH_SHORT).show()
        finish()
    }
    
    inner class HomeAppsEditAdapter(
        private val onRemove: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<HomeAppsEditAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        
        fun setItems(items: List<AppInfo>) {
            this.items = items
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_app, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            holder.itemView.setOnClickListener { onRemove(app) }
            holder.itemView.setOnLongClickListener {
                onRemove(app)
                true
            }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
        }
    }
    
    inner class AllAppsEditAdapter(
        private val onAdd: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<AllAppsEditAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        private var homeSet: Set<String> = emptySet()
        
        fun setItems(items: List<AppInfo>, homeSet: Set<String>) {
            this.items = items
            this.homeSet = homeSet
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_horizontal, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            
            if (homeSet.contains(app.packageName)) {
                holder.itemView.alpha = 0.5f
            } else {
                holder.itemView.alpha = 1.0f
            }
            
            holder.itemView.setOnClickListener { onAdd(app) }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
        }
    }
}

```


## app\src\main\java\com\thanksplay\adesk\ui\PluginsActivity.kt

```kotlin
package com.thanksplay.adesk.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.plugin.PluginManager

class PluginsActivity : AppCompatActivity() {
    
    private lateinit var pluginsRecyclerView: RecyclerView
    private lateinit var tvNoPlugins: TextView
    private lateinit var btnScanPlugins: Button
    private lateinit var pluginManager: PluginManager
    
    private var installedPlugins: List<PluginManager.PluginInfo> = emptyList()
    private var enabledPlugins: MutableSet<String> = mutableSetOf()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plugins)
        
        pluginManager = PluginManager.getInstance(this)
        enabledPlugins = getSharedPreferences("adesk_plugins", MODE_PRIVATE)
            .getStringSet("enabled_plugins", emptySet())?.toMutableSet() ?: mutableSetOf()
        
        initViews()
        scanPlugins()
    }
    
    private fun initViews() {
        pluginsRecyclerView = findViewById(R.id.pluginsRecyclerView)
        tvNoPlugins = findViewById(R.id.tvNoPlugins)
        btnScanPlugins = findViewById(R.id.btnScanPlugins)
        
        pluginsRecyclerView.layoutManager = LinearLayoutManager(this)
        
        btnScanPlugins.setOnClickListener {
            scanPlugins()
        }
    }
    
    private fun scanPlugins() {
        installedPlugins = pluginManager.scanPlugins()
        
        if (installedPlugins.isEmpty()) {
            pluginsRecyclerView.visibility = View.GONE
            tvNoPlugins.visibility = View.VISIBLE
        } else {
            pluginsRecyclerView.visibility = View.VISIBLE
            tvNoPlugins.visibility = View.GONE
            pluginsRecyclerView.adapter = PluginAdapter()
        }
    }
    
    private fun togglePlugin(plugin: PluginManager.PluginInfo, enable: Boolean) {
        if (enable) {
            enabledPlugins.add(plugin.id)
            pluginManager.loadPlugin(plugin)
            Toast.makeText(this, getString(R.string.plugin_enabled, plugin.name), Toast.LENGTH_SHORT).show()
        } else {
            enabledPlugins.remove(plugin.id)
            pluginManager.unloadPlugin(plugin.id)
            Toast.makeText(this, getString(R.string.plugin_disabled, plugin.name), Toast.LENGTH_SHORT).show()
        }
        
        getSharedPreferences("adesk_plugins", MODE_PRIVATE)
            .edit()
            .putStringSet("enabled_plugins", enabledPlugins)
            .apply()
    }
    
    private fun openPluginSettings(plugin: PluginManager.PluginInfo) {
        val intent = Intent()
        intent.action = "com.thanksplay.adesk.plugin.SETTINGS"
        intent.putExtra("plugin_id", plugin.id)
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.plugin_no_settings, Toast.LENGTH_SHORT).show()
        }
    }
    
    inner class PluginAdapter : RecyclerView.Adapter<PluginAdapter.ViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_plugin, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val plugin = installedPlugins[position]
            
            holder.pluginName.text = plugin.name
            holder.pluginVersion.text = "${plugin.version} · ${plugin.author}"
            holder.pluginDescription.text = plugin.description
            
            val isEnabled = enabledPlugins.contains(plugin.id)
            holder.btnToggle.text = if (isEnabled) getString(R.string.disable) else getString(R.string.enable)
            
            try {
                val appInfo = packageManager.getApplicationInfo(plugin.packageName, 0)
                holder.pluginIcon.setImageDrawable(appInfo.loadIcon(packageManager))
            } catch (e: Exception) {
                holder.pluginIcon.setImageResource(android.R.drawable.ic_menu_manage)
            }
            
            holder.btnToggle.setOnClickListener {
                togglePlugin(plugin, !isEnabled)
                notifyItemChanged(position)
            }
            
            holder.itemView.setOnLongClickListener {
                openPluginSettings(plugin)
                true
            }
        }
        
        override fun getItemCount(): Int = installedPlugins.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val pluginIcon: ImageView = itemView.findViewById(R.id.pluginIcon)
            val pluginName: TextView = itemView.findViewById(R.id.pluginName)
            val pluginVersion: TextView = itemView.findViewById(R.id.pluginVersion)
            val pluginDescription: TextView = itemView.findViewById(R.id.pluginDescription)
            val btnToggle: Button = itemView.findViewById(R.id.btnToggle)
        }
    }
}

```


## app\src\main\AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"
        tools:ignore="QueryAllPackagesPermission" />
    <uses-permission android:name="android.permission.READ_CALENDAR" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    <application
        android:name=".ADeskApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.ADesk"
        tools:targetApi="31">

        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:launchMode="singleTask"
            android:stateNotNeeded="true"
            android:excludeFromRecents="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.HOME" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".ui.SettingsActivity"
            android:exported="false"
            android:label="@string/settings"
            android:theme="@style/Theme.ADesk" />

        <activity
            android:name=".ui.CustomOrderActivity"
            android:exported="false"
            android:label="@string/customize_order"
            android:theme="@style/Theme.ADesk" />

        <activity
            android:name=".ui.FavoritesActivity"
            android:exported="false"
            android:label="@string/manage_favorites"
            android:theme="@style/Theme.ADesk" />

        <activity
            android:name=".ui.HiddenAppsActivity"
            android:exported="false"
            android:label="@string/hidden_apps"
            android:theme="@style/Theme.ADesk" />

        <activity
            android:name=".ui.HomeAppsActivity"
            android:exported="false"
            android:label="@string/manage_home_apps"
            android:theme="@style/Theme.ADesk" />

        <activity
            android:name=".ui.PluginsActivity"
            android:exported="false"
            android:label="@string/plugin_manager"
            android:theme="@style/Theme.ADesk" />

        <activity
            android:name=".ui.AboutActivity"
            android:exported="false"
            android:label="@string/about_author"
            android:theme="@style/Theme.ADesk" />

    </application>

</manifest>

```


## app\src\main\res\layout\activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/transparent">

    <ImageView
        android:id="@+id/wallpaperImage"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop" />

    <com.thanksplay.adesk.widget.ParticleView
        android:id="@+id/particleView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPager"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <LinearLayout
        android:id="@+id/pageIndicator"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|center_horizontal"
        android:layout_marginBottom="16dp"
        android:orientation="horizontal"
        android:gravity="center"
        android:alpha="0"
        android:visibility="invisible">

        <View
            android:id="@+id/indicator0"
            android:layout_width="8dp"
            android:layout_height="8dp"
            android:layout_margin="4dp"
            android:background="@drawable/indicator_selected" />

        <View
            android:id="@+id/indicator1"
            android:layout_width="8dp"
            android:layout_height="8dp"
            android:layout_margin="4dp"
            android:background="@drawable/indicator_unselected" />

        <View
            android:id="@+id/indicator2"
            android:layout_width="8dp"
            android:layout_height="8dp"
            android:layout_margin="4dp"
            android:background="@drawable/indicator_unselected" />

    </LinearLayout>

</FrameLayout>

```


## app\src\main\res\layout\activity_settings.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#000000"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/language_settings"
            android:textColor="#FFFFFF"
            android:textSize="18sp"
            android:textStyle="bold" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="@string/select_language"
            android:textColor="#888888"
            android:textSize="14sp" />

        <RadioGroup
            android:id="@+id/languageGroup"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <RadioButton
                android:id="@+id/languageChinese"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="中文"
                android:textColor="#FFFFFF" />

            <RadioButton
                android:id="@+id/languageEnglish"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="16dp"
                android:text="English"
                android:textColor="#FFFFFF" />

        </RadioGroup>

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:layout_marginTop="16dp"
            android:layout_marginBottom="16dp"
            android:background="#333333" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/home_page_settings"
            android:textColor="#FFFFFF"
            android:textSize="18sp"
            android:textStyle="bold" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="@string/clock_position"
            android:textColor="#888888"
            android:textSize="14sp" />

        <RadioGroup
            android:id="@+id/clockPositionGroup"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <RadioButton
                android:id="@+id/clockCenter"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/center"
                android:textColor="#FFFFFF" />

            <RadioButton
                android:id="@+id/clockTop"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="16dp"
                android:text="@string/top"
                android:textColor="#FFFFFF" />

            <RadioButton
                android:id="@+id/clockHidden"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="16dp"
                android:text="@string/hidden"
                android:textColor="#FFFFFF" />

        </RadioGroup>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="@string/home_page_layout"
            android:textColor="#888888"
            android:textSize="14sp" />

        <Spinner
            android:id="@+id/homeLayoutSpinner"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="8dp" />

        <Button
            android:id="@+id/btnManageHomeApps"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="8dp"
            android:text="@string/manage_home_apps" />

        <Switch
            android:id="@+id/showHomeAppLabelsSwitch"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="8dp"
            android:text="@string/show_home_app_labels"
            android:textColor="#FFFFFF" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="@string/weather_settings"
            android:textColor="#FFFFFF"
            android:textSize="18sp"
            android:textStyle="bold" />

        <Switch
            android:id="@+id/showWeatherSwitch"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="8dp"
            android:text="@string/show_weather"
            android:textColor="#FFFFFF" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginTop="8dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/weather_city"
                android:textColor="#FFFFFF"
                android:textSize="14sp" />

            <EditText
                android:id="@+id/weatherCityInput"
                android:layout_width="0dp"
                android:layout_height="48dp"
                android:layout_weight="1"
                android:layout_marginStart="16dp"
                android:inputType="text"
                android:hint="@string/weather_city_hint"
                android:textColor="#FFFFFF"
                android:textColorHint="#666666"
                android:gravity="center_vertical" />

        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginTop="8dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/weather_api_url"
                android:textColor="#FFFFFF"
                android:textSize="14sp" />

            <EditText
                android:id="@+id/weatherApiUrlInput"
                android:layout_width="0dp"
                android:layout_height="48dp"
                android:layout_weight="1"
                android:layout_marginStart="16dp"
                android:inputType="textUri"
                android:hint="@string/weather_api_url_hint"
                android:textColor="#FFFFFF"
                android:textColorHint="#666666"
                android:gravity="center_vertical" />

        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginTop="8dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/weather_update_interval"
                android:textColor="#FFFFFF"
                android:textSize="14sp" />

            <Spinner
                android:id="@+id/weatherUpdateIntervalSpinner"
                android:layout_width="0dp"
                android:layout_height="48dp"
                android:layout_weight="1"
                android:layout_marginStart="16dp" />

        </LinearLayout>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/weather_update_interval_hint"
            android:textColor="#888888"
            android:textSize="12sp"
            android:layout_marginTop="4dp" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginTop="8dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/home_apps_offset"
                android:textColor="#FFFFFF"
                android:textSize="14sp" />

            <EditText
                android:id="@+id/homeAppsOffsetInput"
                android:layout_width="80dp"
                android:layout_height="48dp"
                android:layout_marginStart="16dp"
                android:inputType="number"
                android:text="0"
                android:textColor="#FFFFFF"
                android:gravity="center" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="px"
                android:textColor="#888888"
                android:layout_marginStart="4dp" />

        </LinearLayout>

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:layout_marginTop="16dp"
            android:layout_marginBottom="16dp"
            android:background="#333333" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/wallpaper"
            android:textColor="#FFFFFF"
            android:textSize="18sp"
            android:textStyle="bold" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="@string/wallpaper_type"
            android:textColor="#888888"
            android:textSize="14sp" />

        <RadioGroup
            android:id="@+id/wallpaperTypeGroup"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <RadioButton
                android:id="@+id/wallpaperBlack"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/black"
                android:textColor="#FFFFFF" />

            <RadioButton
                android:id="@+id/wallpaperColor"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/custom_color"
                android:textColor="#FFFFFF" />

            <RadioButton
                android:id="@+id/wallpaperImage"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/custom_image"
                android:textColor="#FFFFFF" />

        </RadioGroup>

        <LinearLayout
            android:id="@+id/colorPickerLayout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:visibility="gone">

            <View
                android:id="@+id/colorPreview"
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:background="#FF0000" />

            <Button
                android:id="@+id/btnPickColor"
                android:layout_width="wrap_content"
                android:layout_height="48dp"
                android:layout_marginStart="8dp"
                android:text="@string/pick_color" />

        </LinearLayout>

        <Button
            android:id="@+id/btnPickImage"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:text="@string/select_image"
            android:visibility="gone" />

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:layout_marginTop="16dp"
            android:layout_marginBottom="16dp"
            android:background="#333333" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/effects"
            android:textColor="#FFFFFF"
            android:textSize="18sp"
            android:textStyle="bold" />

        <Switch
            android:id="@+id/particleEffectSwitch"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="8dp"
            android:text="@string/particle_effect"
            android:textColor="#FFFFFF" />

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:layout_marginTop="16dp"
            android:layout_marginBottom="16dp"
            android:background="#333333" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/app_list_settings"
            android:textColor="#FFFFFF"
            android:textSize="18sp"
            android:textStyle="bold" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="@string/columns_per_page"
            android:textColor="#888888"
            android:textSize="14sp" />

        <Spinner
            android:id="@+id/columnsSpinner"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="8dp" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="@string/label_position"
            android:textColor="#888888"
            android:textSize="14sp" />

        <RadioGroup
            android:id="@+id/labelPositionGroup"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <RadioButton
                android:id="@+id/labelRight"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/right"
                android:textColor="#FFFFFF" />

            <RadioButton
                android:id="@+id/labelBottom"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="16dp"
                android:text="@string/bottom"
                android:textColor="#FFFFFF" />

        </RadioGroup>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="@string/sort_method"
            android:textColor="#888888"
            android:textSize="14sp" />

        <RadioGroup
            android:id="@+id/sortMethodGroup"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <RadioButton
                android:id="@+id/sortAbc"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/alphabetical"
                android:textColor="#FFFFFF" />

            <RadioButton
                android:id="@+id/sortInstallTime"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/install_time"
                android:textColor="#FFFFFF" />

            <RadioButton
                android:id="@+id/sortCustom"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/customize_order"
                android:textColor="#FFFFFF" />

        </RadioGroup>

        <Switch
            android:id="@+id/showFavoritesSwitch"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="16dp"
            android:text="@string/show_favorites_only"
            android:textColor="#FFFFFF" />

        <LinearLayout
            android:id="@+id/showOtherAppsLayout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:layout_marginStart="24dp"
            android:visibility="gone">

            <Switch
                android:id="@+id/showOtherAppsSwitch"
                android:layout_width="match_parent"
                android:layout_height="48dp"
                android:text="@string/show_other_apps_after_favorites"
                android:textColor="#AAAAAA"
                android:textSize="14sp" />

        </LinearLayout>

        <Button
            android:id="@+id/btnManageFavorites"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="8dp"
            android:text="@string/manage_favorites" />

        <Button
            android:id="@+id/btnCustomOrder"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="8dp"
            android:text="@string/customize_order" />

        <Button
            android:id="@+id/btnManageHidden"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="8dp"
            android:text="@string/manage_hidden_apps" />

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:layout_marginTop="16dp"
            android:layout_marginBottom="16dp"
            android:background="#333333" />

        <Button
            android:id="@+id/btnSetAsDefault"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:text="@string/set_as_default_launcher" />

        <Button
            android:id="@+id/btnRefreshCache"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="8dp"
            android:text="@string/refresh_cache" />

        <Button
            android:id="@+id/btnPluginManager"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="8dp"
            android:text="@string/plugin_manager" />

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:layout_marginTop="16dp"
            android:layout_marginBottom="16dp"
            android:background="#333333" />

        <Button
            android:id="@+id/btnRestartLauncher"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:text="@string/restart_launcher" />

        <Button
            android:id="@+id/btnAboutAuthor"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginTop="8dp"
            android:text="@string/about_author" />

    </LinearLayout>

</ScrollView>

```


## app\src\main\res\layout\activity_about.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#000000"
    android:gravity="center"
    android:orientation="vertical">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/author_name"
        android:textColor="#FFFFFF"
        android:textSize="48sp"
        android:textStyle="bold" />

</LinearLayout>

```


## app\src\main\res\layout\page_home.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/transparent">

    <LinearLayout
        android:id="@+id/contentLayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="16dp"
        android:paddingBottom="80dp">

        <View
            android:id="@+id/spacerTop"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_weight="1" />

        <TextView
            android:id="@+id/tvTime"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:textSize="72sp"
            android:textColor="#FFFFFF"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvDate"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:textSize="18sp"
            android:textColor="#888888"
            android:layout_marginTop="8dp" />

        <TextView
            android:id="@+id/tvEvent"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="14sp"
            android:textColor="#1E88E5"
            android:layout_marginTop="16dp"
            android:padding="12dp"
            android:background="#1A1A1A"
            android:visibility="gone" />

        <FrameLayout
            android:id="@+id/widgetContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:visibility="gone">

            <include layout="@layout/widget_weather" />

        </FrameLayout>

        <View
            android:id="@+id/spacerBottom"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_weight="1" />

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/homeAppsRecyclerView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:translationY="-10dp" />

    </LinearLayout>

</FrameLayout>

```


## app\src\main\res\layout\page_apps.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/transparent">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/appsRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbars="vertical" />

</FrameLayout>

```


## app\src\main\res\layout\page_search.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@android:color/transparent">

    <EditText
        android:id="@+id/searchInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="@string/search_hint"
        android:textColorHint="#888888"
        android:textColor="#FFFFFF"
        android:backgroundTint="#444444"
        android:padding="12dp"
        android:singleLine="true" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/searchResultsRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:layout_marginTop="8dp"
        android:scrollbars="vertical" />

</LinearLayout>

```


## app\src\main\res\layout\widget_weather.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp"
    android:gravity="center">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center">

        <TextView
            android:id="@+id/tvWeatherTemp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="--°"
            android:textColor="#FFFFFF"
            android:textSize="48sp"
            android:textStyle="bold" />

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:layout_marginStart="16dp"
            android:gravity="center_vertical">

            <TextView
                android:id="@+id/tvWeatherDesc"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="--"
                android:textColor="#FFFFFF"
                android:textSize="16sp" />

            <TextView
                android:id="@+id/tvWeatherLocation"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="--"
                android:textColor="#888888"
                android:textSize="12sp" />

        </LinearLayout>

    </LinearLayout>

</LinearLayout>

```


## app\src\main\res\values\strings.xml

```xml
<resources>
    <string name="app_name">ADesk</string>
    <string name="settings">设置</string>
    <string name="search_hint">搜索应用...</string>
    <string name="app_icon_desc">应用图标</string>
    <string name="all_apps">所有应用</string>
    
    <string name="display_settings">显示设置</string>
    <string name="apps_per_row">每行显示应用数</string>
    <string name="label_position">应用名称位置</string>
    <string name="right">右侧</string>
    <string name="bottom">下方</string>
    
    <string name="sort_settings">排序设置</string>
    <string name="sort_abc">按字母排序 (A-Z)</string>
    <string name="sort_install_time">按安装时间排序</string>
    <string name="sort_custom">自定义排序</string>
    
    <string name="favorites_settings">收藏设置</string>
    <string name="show_favorites_only">仅显示收藏应用</string>
    <string name="show_other_apps_after_favorites">收藏应用后显示其他应用</string>
    <string name="other_apps">其他应用</string>
    <string name="thanksplay_easter_egg">-thanksplay-</string>
    <string name="manage_favorites">管理收藏</string>
    <string name="customize_order">自定义顺序</string>
    <string name="set_as_default_launcher">设为默认桌面</string>
    <string name="refresh_cache">刷新应用缓存</string>
    <string name="cache_refreshed">缓存已刷新</string>
    <string name="manage_hidden_apps">管理隐藏应用</string>
    
    <string name="add_to_favorites">添加到收藏</string>
    <string name="remove_from_favorites">从收藏移除</string>
    <string name="app_info">应用信息</string>
    <string name="added_to_favorites">已添加到收藏</string>
    <string name="removed_from_favorites">已从收藏移除</string>
    
    <string name="drag_to_reorder">拖动以重新排序</string>
    <string name="drag_handle_desc">拖动手柄</string>
    <string name="order_saved">顺序已保存</string>
    
    <string name="search_app">搜索应用</string>
    <string name="search_results">搜索结果</string>
    <string name="current_order">当前顺序</string>
    <string name="my_favorites">我的收藏</string>
    <string name="save">保存</string>
    <string name="move_to_top">移至顶层</string>
    <string name="move_up">上移</string>
    <string name="move_down">下移</string>
    <string name="hide_app">隐藏</string>
    <string name="app_hidden">应用已隐藏</string>
    <string name="hidden_apps">隐藏的应用</string>
    <string name="no_hidden_apps">没有隐藏的应用</string>
    <string name="app_shown">应用已显示</string>
    
    <string name="home_page_settings">主页设置</string>
    <string name="home_page_layout">主页排版</string>
    <string name="manage_home_apps">管理主页应用</string>
    <string name="no_events">今日无日程</string>
    <string name="calendar_event_format">📅 %s</string>
    
    <string name="clock_position">时钟位置</string>
    <string name="center">居中</string>
    <string name="top">顶部</string>
    <string name="hidden">不显示</string>
    <string name="wallpaper">壁纸</string>
    <string name="wallpaper_type">壁纸类型</string>
    <string name="black">黑色</string>
    <string name="system_wallpaper">系统壁纸</string>
    <string name="custom_color">自定义颜色</string>
    <string name="custom_image">自定义图片</string>
    <string name="select_image">选择图片</string>
    <string name="wallpaper_set">壁纸已设置</string>
    <string name="pick_color">选择颜色</string>
    <string name="blur_effect">毛玻璃效果</string>
    <string name="effects">特效</string>
    <string name="particle_effect">滑动粒子效果</string>
    <string name="app_list_settings">应用列表设置</string>
    <string name="columns_per_page">每页列数</string>
    <string name="sort_method">排序方式</string>
    <string name="alphabetical">按字母</string>
    <string name="install_time">安装时间</string>
    <string name="select_color">选择颜色</string>
    <string name="language_settings">语言设置</string>
    <string name="select_language">选择语言</string>
    <string name="restart_to_apply">重启应用以应用更改</string>
    <string name="color_black">黑色</string>
    <string name="color_dark_gray">深灰</string>
    <string name="color_gray">灰色</string>
    <string name="color_dark_blue">深蓝</string>
    <string name="color_dark_green">深绿</string>
    <string name="color_dark_purple">深紫</string>
    <string name="color_dark_red">深红</string>
    <string name="color_orange">橙色</string>
    <string name="show_home_app_labels">显示应用名称</string>
    <string name="home_apps_offset">应用垂直偏移</string>
    
    <string name="plugin_manager">插件管理</string>
    <string name="plugin_hint">插件可以扩展ADesk的功能。安装支持ADesk的插件后点击扫描即可发现。</string>
    <string name="no_plugins_found">未发现插件</string>
    <string name="scan_plugins">扫描插件</string>
    <string name="plugin_icon">插件图标</string>
    <string name="enable">启用</string>
    <string name="disable">禁用</string>
    <string name="plugin_enabled">%s 已启用</string>
    <string name="plugin_disabled">%s 已禁用</string>
    <string name="plugin_no_settings">该插件没有设置页面</string>
    
    <string name="weather_settings">天气设置</string>
    <string name="show_weather">显示天气</string>
    <string name="weather_city">城市</string>
    <string name="weather_city_hint">输入城市名称（留空自动定位）</string>
    <string name="weather_api_url">天气API地址</string>
    <string name="weather_api_url_hint">留空使用默认API</string>
    <string name="weather_update_interval">天气更新间隔</string>
    <string name="weather_update_interval_hint">更新太频繁可能增加耗电</string>
    <string name="weather_load_failed">加载失败</string>
    <string name="weather_loading">加载中...</string>
    <string name="weather_location_failed">无法获取位置</string>
    
    <string name="about_author">关于作者</string>
    <string name="restart_launcher">重启桌面</string>
    <string name="author_name">thanksplay</string>
</resources>

```


## app\src\main\res\values-en\strings.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">ADesk</string>
    <string name="settings">Settings</string>
    <string name="search_hint">Search apps...</string>
    <string name="app_icon_desc">App icon</string>
    <string name="all_apps">All Apps</string>
    
    <string name="display_settings">Display Settings</string>
    <string name="apps_per_row">Apps per row</string>
    <string name="label_position">Label Position</string>
    <string name="right">Right</string>
    <string name="bottom">Bottom</string>
    
    <string name="sort_settings">Sort Settings</string>
    <string name="sort_abc">Sort by name (A-Z)</string>
    <string name="sort_install_time">Sort by install time</string>
    <string name="sort_custom">Custom order</string>
    
    <string name="favorites_settings">Favorites Settings</string>
    <string name="show_favorites_only">Show favorites only</string>
    <string name="show_other_apps_after_favorites">Show other apps after favorites</string>
    <string name="other_apps">Other Apps</string>
    <string name="thanksplay_easter_egg">-thanksplay-</string>
    <string name="manage_favorites">Manage Favorites</string>
    <string name="customize_order">Custom Order</string>
    <string name="set_as_default_launcher">Set as Default Launcher</string>
    <string name="refresh_cache">Refresh App Cache</string>
    <string name="cache_refreshed">Cache refreshed</string>
    <string name="manage_hidden_apps">Manage Hidden Apps</string>
    
    <string name="add_to_favorites">Add to Favorites</string>
    <string name="remove_from_favorites">Remove from Favorites</string>
    <string name="app_info">App Info</string>
    <string name="added_to_favorites">Added to favorites</string>
    <string name="removed_from_favorites">Removed from favorites</string>
    
    <string name="drag_to_reorder">Drag to reorder</string>
    <string name="drag_handle_desc">Drag handle</string>
    <string name="order_saved">Order saved</string>
    
    <string name="search_app">Search App</string>
    <string name="search_results">Search Results</string>
    <string name="current_order">Current Order</string>
    <string name="my_favorites">My Favorites</string>
    <string name="save">Save</string>
    <string name="move_to_top">Move to Top</string>
    <string name="move_up">Move Up</string>
    <string name="move_down">Move Down</string>
    <string name="hide_app">Hide</string>
    <string name="app_hidden">App hidden</string>
    <string name="hidden_apps">Hidden Apps</string>
    <string name="no_hidden_apps">No hidden apps</string>
    <string name="app_shown">App shown</string>
    
    <string name="home_page_settings">Home Settings</string>
    <string name="home_page_layout">Home Layout</string>
    <string name="manage_home_apps">Manage Home Apps</string>
    <string name="no_events">No events today</string>
    <string name="calendar_event_format">📅 %s</string>
    
    <string name="clock_position">Clock Position</string>
    <string name="center">Center</string>
    <string name="top">Top</string>
    <string name="hidden">Hidden</string>
    <string name="wallpaper">Wallpaper</string>
    <string name="wallpaper_type">Wallpaper Type</string>
    <string name="black">Black</string>
    <string name="system_wallpaper">System Wallpaper</string>
    <string name="custom_color">Custom Color</string>
    <string name="custom_image">Custom Image</string>
    <string name="select_image">Select Image</string>
    <string name="wallpaper_set">Wallpaper set</string>
    <string name="pick_color">Pick Color</string>
    <string name="blur_effect">Blur Effect</string>
    <string name="effects">Effects</string>
    <string name="particle_effect">Particle Effect on Scroll</string>
    <string name="app_list_settings">App List Settings</string>
    <string name="columns_per_page">Columns per page</string>
    <string name="sort_method">Sort Method</string>
    <string name="alphabetical">Alphabetical</string>
    <string name="install_time">Install Time</string>
    <string name="select_color">Select Color</string>
    <string name="language_settings">Language</string>
    <string name="select_language">Select Language</string>
    <string name="restart_to_apply">Restart app to apply changes</string>
    <string name="color_black">Black</string>
    <string name="color_dark_gray">Dark Gray</string>
    <string name="color_gray">Gray</string>
    <string name="color_dark_blue">Dark Blue</string>
    <string name="color_dark_green">Dark Green</string>
    <string name="color_dark_purple">Dark Purple</string>
    <string name="color_dark_red">Dark Red</string>
    <string name="color_orange">Orange</string>
    <string name="show_home_app_labels">Show App Names</string>
    <string name="home_apps_offset">Apps Vertical Offset</string>
    
    <string name="plugin_manager">Plugin Manager</string>
    <string name="plugin_hint">Plugins can extend ADesk features. Install ADesk-compatible plugins and tap scan to discover.</string>
    <string name="no_plugins_found">No plugins found</string>
    <string name="scan_plugins">Scan Plugins</string>
    <string name="plugin_icon">Plugin icon</string>
    <string name="enable">Enable</string>
    <string name="disable">Disable</string>
    <string name="plugin_enabled">%s enabled</string>
    <string name="plugin_disabled">%s disabled</string>
    <string name="plugin_no_settings">This plugin has no settings</string>
    
    <string name="weather_settings">Weather Settings</string>
    <string name="show_weather">Show Weather</string>
    <string name="weather_city">City</string>
    <string name="weather_city_hint">Enter city name (leave empty for auto location)</string>
    <string name="weather_api_url">Weather API URL</string>
    <string name="weather_api_url_hint">Leave empty to use default API</string>
    <string name="weather_update_interval">Weather Update Interval</string>
    <string name="weather_update_interval_hint">Frequent updates may increase battery usage</string>
    <string name="weather_load_failed">Load failed</string>
    <string name="weather_loading">Loading...</string>
    <string name="weather_location_failed">Cannot get location</string>
    
    <string name="about_author">About Author</string>
    <string name="restart_launcher">Restart Launcher</string>
    <string name="author_name">thanksplay</string>
</resources>

```


## app\src\main\res\values\themes.xml

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Theme.ADesk" parent="Theme.MaterialComponents.NoActionBar">
        <item name="colorPrimary">#1E88E5</item>
        <item name="colorPrimaryVariant">#1565C0</item>
        <item name="colorOnPrimary">@android:color/white</item>
        <item name="colorSecondary">#42A5F5</item>
        <item name="colorSecondaryVariant">#1976D2</item>
        <item name="colorOnSecondary">@android:color/white</item>
        <item name="android:statusBarColor" tools:targetApi="21">@android:color/transparent</item>
        <item name="android:navigationBarColor" tools:targetApi="21">@android:color/transparent</item>
        <item name="android:windowBackground">@android:color/black</item>
        <item name="android:windowDrawsSystemBarBackgrounds" tools:targetApi="21">true</item>
    </style>
</resources>

```
