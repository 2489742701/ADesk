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
