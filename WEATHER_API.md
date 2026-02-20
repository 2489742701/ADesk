# ADesk 天气功能文档

## 概述

ADesk 使用 Open-Meteo 免费 API 获取天气数据，支持自动定位和手动输入城市名称两种方式。

---

## API 信息

### 使用的 API
- **API 提供商**: Open-Meteo
- **API 地址**: `https://api.open-meteo.com/v1/forecast`
- **费用**: 免费，无需 API Key
- **文档地址**: https://open-meteo.com/en/docs

### API 请求示例
```
https://api.open-meteo.com/v1/forecast?latitude=39.9042&longitude=116.4074&current_weather=true&timezone=auto
```

### 请求参数
| 参数 | 说明 |
|------|------|
| latitude | 纬度 |
| longitude | 经度 |
| current_weather | 是否获取当前天气 (true/false) |
| timezone | 时区设置 (auto 为自动) |

### API 响应示例
```json
{
  "latitude": 39.9042,
  "longitude": 116.4074,
  "generationtime_ms": 0.123,
  "utc_offset_seconds": 28800,
  "timezone": "Asia/Shanghai",
  "current_weather": {
    "temperature": 25.5,
    "windspeed": 10.2,
    "winddirection": 180,
    "weathercode": 0,
    "time": "2024-01-15T14:00"
  }
}
```

---

## 代码结构

### 1. 数据模型 (WeatherData.kt)
```kotlin
package com.thanksplay.adesk.model

data class WeatherData(
    val temperature: Double,  // 温度（摄氏度）
    val description: String,  // 天气描述
    val location: String      // 位置名称
)
```

### 2. 天气服务 (WeatherService.kt)

#### 主要方法

| 方法 | 参数 | 说明 |
|------|------|------|
| `fetchWeather()` | context, lat, lon, callback | 根据经纬度获取天气 |
| `fetchWeatherByCity()` | context, city, callback | 根据城市名获取天气 |
| `getCurrentLocation()` | context, callback | 获取当前位置经纬度 |

#### 完整代码
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
                addresses[0].locality ?: addresses[0].adminArea ?: "Unknown"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
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

---

## 天气代码对照表 (WMO Weather Code)

| 代码 | 描述 | 英文描述 |
|------|------|----------|
| 0 | 晴 | Clear sky |
| 1, 2, 3 | 多云 | Mainly clear, partly cloudy, overcast |
| 45, 48 | 雾 | Fog and depositing rime fog |
| 51, 53, 55 | 毛毛雨 | Drizzle: Light, moderate, dense |
| 56, 57 | 冻雨 | Freezing Drizzle: Light, dense |
| 61, 63, 65 | 雨 | Rain: Slight, moderate, heavy |
| 66, 67 | 冻雨 | Freezing Rain: Light, heavy |
| 71, 73, 75 | 雪 | Snow fall: Slight, moderate, heavy |
| 77 | 雪粒 | Snow grains |
| 80, 81, 82 | 阵雨 | Rain showers: Slight, moderate, violent |
| 85, 86 | 阵雪 | Snow showers: Slight, heavy |
| 95 | 雷暴 | Thunderstorm |
| 96, 99 | 雷暴冰雹 | Thunderstorm with hail |

---

## 权限要求

### AndroidManifest.xml
```xml
<!-- 网络权限 -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- 位置权限（用于自动定位） -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

---

## 使用方式

### 在 MainActivity 中调用
```kotlin
// 方式1：根据城市名获取天气
WeatherService.fetchWeatherByCity(this, "北京") { data ->
    if (data != null) {
        tvTemp.text = "${data.temperature.toInt()}°"
        tvDesc.text = data.description
        tvLocation.text = data.location
    }
}

// 方式2：自动定位获取天气（需要位置权限）
if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
    WeatherService.getCurrentLocation(this) { lat, lon ->
        if (lat != null && lon != null) {
            WeatherService.fetchWeather(this, lat, lon) { data ->
                if (data != null) {
                    tvTemp.text = "${data.temperature.toInt()}°"
                    tvDesc.text = data.description
                    tvLocation.text = data.location
                }
            }
        }
    }
}
```

---

## 可能的问题

1. **网络问题**: API 请求需要网络连接，超时设置为 5 秒
2. **定位问题**: 自动定位需要用户授权位置权限
3. **Geocoder 问题**: 某些设备或地区 Geocoder 可能不可用，导致城市名解析失败
4. **API 限制**: Open-Meteo 免费版有请求频率限制，但一般应用场景足够

---

## 相关文件

- `app/src/main/java/com/thanksplay/adesk/util/WeatherService.kt` - 天气服务
- `app/src/main/java/com/thanksplay/adesk/model/WeatherData.kt` - 数据模型
- `app/src/main/res/layout/widget_weather.xml` - 天气组件布局
- `app/src/main/java/com/thanksplay/adesk/ui/MainActivity.kt` - 调用天气服务的页面
