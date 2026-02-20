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
