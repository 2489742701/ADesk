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
    
    private const val BASE_URL = "https://api.open-meteo.com/v1/forecast"
    
    fun fetchWeather(context: Context, lat: Double, lon: Double, callback: (WeatherData?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL?latitude=$lat&longitude=$lon&current_weather=true&timezone=auto")
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
                
                android.os.Handler(context.mainLooper).post {
                    callback(data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.os.Handler(context.mainLooper).post {
                    callback(null)
                }
            }
        }.start()
    }
    
    fun fetchWeatherByCity(context: Context, city: String, callback: (WeatherData?) -> Unit) {
        Thread {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(city, 1)
                
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    fetchWeather(context, address.latitude, address.longitude, callback)
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
