package iss.nus.edu.sg.weather.data.remote

import iss.nus.edu.sg.weather.data.model.ForecastResponse
import iss.nus.edu.sg.weather.data.model.HourlyForecastResponse
import iss.nus.edu.sg.weather.data.model.NowWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface QWeatherApi {

    @GET("v7/weather/now")
    suspend fun getNowWeather(
        @Query("location") cityId: String
    ): NowWeatherResponse

    @GET("v7/weather/7d")
    suspend fun getDailyForecast(
        @Query("location") cityId: String
    ): ForecastResponse

    @GET("v7/weather/24h")
    suspend fun getHourlyForecast(
        @Query("location") cityId: String
    ): HourlyForecastResponse
}
