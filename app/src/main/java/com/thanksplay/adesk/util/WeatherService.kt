package com.thanksplay.adesk.util

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume

object WeatherService {
    
    private const val DEFAULT_BASE_URL = "https://api.open-meteo.com/v1/forecast"
    private const val LOCATION_TIMEOUT = 10000L
    
    suspend fun fetchWeather(
        context: Context, 
        lat: Double, 
        lon: Double, 
        forceRefresh: Boolean = false
    ): WeatherData? = withContext(Dispatchers.IO) {
        val prefsManager = PreferencesManager(context)
        
        if (!forceRefresh && prefsManager.isWeatherCacheValid()) {
            val cachedData = parseCachedWeather(context, prefsManager.weatherCacheData)
            if (cachedData != null) {
                return@withContext cachedData
            }
        }
        
        val customApiUrl = prefsManager.weatherApiUrl
        val baseUrl = if (customApiUrl.isNotEmpty()) customApiUrl else DEFAULT_BASE_URL
        
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
            
            val description = getWeatherDescription(context, weatherCode)
            val location = getLocationName(context, lat, lon)
            
            val data = WeatherData(temperature, description, location)
            
            prefsManager.weatherCacheTime = System.currentTimeMillis()
            prefsManager.weatherCacheData = "$temperature|$weatherCode|$location"
            
            data
        } catch (e: Exception) {
            e.printStackTrace()
            if (!forceRefresh) {
                parseCachedWeather(context, prefsManager.weatherCacheData)
            } else {
                null
            }
        }
    }
    
    suspend fun fetchWeatherByCity(
        context: Context, 
        city: String, 
        forceRefresh: Boolean = false
    ): WeatherData? = withContext(Dispatchers.IO) {
        val prefsManager = PreferencesManager(context)
        
        if (!forceRefresh && prefsManager.isWeatherCacheValid()) {
            val cachedData = parseCachedWeather(context, prefsManager.weatherCacheData)
            if (cachedData != null) {
                return@withContext cachedData
            }
        }
        
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(city, 1)
            
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                fetchWeather(context, address.latitude, address.longitude, forceRefresh)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun getCurrentLocation(context: Context): Pair<Double?, Double?> = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine<Pair<Double?, Double?>> { continuation ->
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                
                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(Pair(location.latitude, location.longitude))
                        }
                    }
                    
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(Pair(null, null))
                        }
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
                        continuation.resume(Pair(null, null))
                    }
                }
                
                continuation.invokeOnCancellation {
                    locationManager.removeUpdates(locationListener)
                }
            } catch (e: SecurityException) {
                continuation.resume(Pair(null, null))
            } catch (e: Exception) {
                continuation.resume(Pair(null, null))
            }
        }
    }
    
    fun parseCachedWeather(context: Context, cacheData: String): WeatherData? {
        if (cacheData.isEmpty()) return null
        return try {
            val parts = cacheData.split("|")
            if (parts.size == 3) {
                val temperature = parts[0].toDouble()
                val weatherCode = parts[1].toInt()
                val location = parts[2]
                WeatherData(temperature, getWeatherDescription(context, weatherCode), location)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun getLocationName(context: Context, lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        try {
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
    
    private fun getWeatherDescription(context: Context, code: Int): String {
        return when (code) {
            0 -> context.getString(R.string.weather_clear)
            1, 2, 3 -> context.getString(R.string.weather_cloudy)
            45, 48 -> context.getString(R.string.weather_fog)
            51, 53, 55 -> context.getString(R.string.weather_drizzle)
            56, 57 -> context.getString(R.string.weather_freezing_rain)
            61, 63, 65 -> context.getString(R.string.weather_rain)
            66, 67 -> context.getString(R.string.weather_freezing_rain)
            71, 73, 75 -> context.getString(R.string.weather_snow)
            77 -> context.getString(R.string.weather_snow_grains)
            80, 81, 82 -> context.getString(R.string.weather_showers)
            85, 86 -> context.getString(R.string.weather_snow_showers)
            95 -> context.getString(R.string.weather_thunderstorm)
            96, 99 -> context.getString(R.string.weather_thunderstorm_hail)
            else -> context.getString(R.string.weather_unknown)
        }
    }
}
