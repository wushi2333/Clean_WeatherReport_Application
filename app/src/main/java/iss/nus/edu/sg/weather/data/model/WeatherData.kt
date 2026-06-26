package iss.nus.edu.sg.weather.data.model

import com.google.gson.annotations.SerializedName

// ----- City Search Response -----

data class CitySearchResponse(
    val code: String,
    val location: List<CityLocation>?
)

data class CityLocation(
    val id: String,           // 城市ID, e.g. "101220101"
    val name: String,         // 城市名
    val lat: String,
    val lon: String,
    val adm1: String = "",    // 省份
    val country: String = ""
)

// ----- Now Weather Response -----

data class NowWeatherResponse(
    val code: String,
    val updateTime: String? = null,
    val now: NowWeather? = null
)

data class NowWeather(
    val temp: String = "",         // 温度
    val feelsLike: String = "",    // 体感温度
    val icon: String = "",         // 天气图标代码
    val text: String = "",         // 天气描述
    val windDir: String = "",      // 风向
    val windScale: String = "",    // 风力等级
    val humidity: String = "",     // 湿度
    val precip: String = "",       // 降水量
    val pressure: String = "",     // 气压
    val vis: String = "",          // 能见度
    val cloud: String = "",        // 云量
    val dew: String = ""           // 露点温度
)

// ----- 3-Day Forecast Response -----

data class ForecastResponse(
    val code: String,
    val daily: List<DailyForecast>? = null
)

data class DailyForecast(
    val fxDate: String = "",       // 预报日期
    val sunrise: String = "",      // 日出时间
    val sunset: String = "",       // 日落时间
    val tempMax: String = "",      // 最高温度
    val tempMin: String = "",      // 最低温度
    val iconDay: String = "",      // 白天天气图标
    val textDay: String = "",      // 白天天气描述
    val iconNight: String = "",    // 夜间天气图标
    val textNight: String = "",    // 夜间天气描述
    val windDirDay: String = "",   // 白天风向
    val windScaleDay: String = "", // 白天风力
    val humidity: String = "",     // 湿度
    val precip: String = "",       // 降水量
    val pressure: String = "",     // 气压
    val vis: String = "",          // 能见度
    val cloud: String = ""         // 云量
)

// ----- 24-Hour Forecast Response -----

data class HourlyForecastResponse(
    val code: String,
    val hourly: List<HourlyForecast>? = null
)

data class HourlyForecast(
    val fxTime: String = "",   // 预报时间 "2026-06-27 14:00"
    val temp: String = "",     // 温度
    val icon: String = "",     // 天气图标
    val text: String = "",     // 天气描述
    val windDir: String = "",  // 风向
    val windScale: String = "",// 风力
    val humidity: String = "", // 湿度
    val pop: String = "",      // 降雨概率 %
    val precip: String = ""    // 降水量
)
