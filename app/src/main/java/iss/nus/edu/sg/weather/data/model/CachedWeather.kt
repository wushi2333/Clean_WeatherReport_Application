package iss.nus.edu.sg.weather.data.model

data class CachedWeather(
    val cityId: String,
    val weatherJson: String = "",
    val forecastJson: String = "",
    val hourlyJson: String = "",
    val lastFetchTime: Long = 0L
)
