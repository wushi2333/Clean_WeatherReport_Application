package iss.nus.edu.sg.weather.data.model

data class WeatherInfo(
    val city: City,
    val now: NowWeather?,
    val dailyList: List<DailyForecast>,
    val hourlyList: List<HourlyForecast> = emptyList(),
    val isFromCache: Boolean = false
)
