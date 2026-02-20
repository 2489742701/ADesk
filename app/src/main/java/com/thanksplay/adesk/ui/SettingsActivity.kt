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
import android.widget.TextView
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
    private val intervalOptions by lazy {
        arrayOf(
            getString(R.string.interval_15_min),
            getString(R.string.interval_30_min),
            getString(R.string.interval_1_hour),
            getString(R.string.interval_2_hours),
            getString(R.string.interval_4_hours),
            getString(R.string.interval_6_hours),
            getString(R.string.interval_12_hours),
            getString(R.string.interval_24_hours)
        )
    }
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
        
        val tvVersion = findViewById<TextView>(R.id.tvVersion)
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            tvVersion.text = "ADesk v${packageInfo.versionName}"
        } catch (e: Exception) {
            tvVersion.text = "ADesk v1.0 Demo"
        }
        
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
