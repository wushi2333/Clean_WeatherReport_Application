package iss.nus.edu.sg.weather.data.repository

import android.util.Log
import com.google.gson.Gson
import iss.nus.edu.sg.weather.data.local.CityDatabase
import iss.nus.edu.sg.weather.data.local.PrefsManager
import iss.nus.edu.sg.weather.data.model.CachedWeather
import iss.nus.edu.sg.weather.data.model.City
import iss.nus.edu.sg.weather.data.model.DailyForecast
import iss.nus.edu.sg.weather.data.model.HourlyForecast
import iss.nus.edu.sg.weather.data.model.NowWeather
import iss.nus.edu.sg.weather.data.model.WeatherInfo
import iss.nus.edu.sg.weather.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow

class WeatherRepository(private val prefs: PrefsManager) {

    private val api = RetrofitClient.api
    private val gson = Gson()

    companion object {
        const val CACHE_DURATION_MS = 60 * 60 * 1000L
        private const val TAG = "WeatherRepo"
    }

    fun getAllCities(): Flow<List<City>> = prefs.citiesFlow

    /** Search city locally from built-in database */
    fun searchCity(query: String): List<CityDatabase.CityEntry> = CityDatabase.search(query)

    /** Add city by cityId from local database */
    suspend fun addCityById(cityId: String): Result<City> {
        val entry = CityDatabase.ALL.find { it.cityId == cityId }
            ?: return Result.failure(Exception("城市未找到"))
        if (prefs.getCities().any { it.cityId == cityId })
            return Result.failure(Exception("\"${entry.name}\"已经添加过了"))
        val city = City(name = entry.name, cityId = cityId, adm1 = entry.province)
        prefs.addCity(city)
        return Result.success(city)
    }

    suspend fun deleteCity(cityId: String) {
        prefs.deleteCity(cityId)
        prefs.deleteCachedWeather(cityId)
    }

    suspend fun initDefaultCity() = prefs.initDefaultCity()

    suspend fun getWeather(city: City, forceRefresh: Boolean = false): WeatherInfo {
        val loc = city.cityId
        if (!forceRefresh) {
            val cached = prefs.getCachedWeather(loc)
            if (cached != null && (System.currentTimeMillis() - cached.lastFetchTime) < CACHE_DURATION_MS) {
                return WeatherInfo(city, parseNow(cached.weatherJson), parseDaily(cached.forecastJson), parseHourly(cached.hourlyJson), true)
            }
        }
        return try {
            val nowResp = api.getNowWeather(loc)
            if (nowResp.code != "200") throw Exception("天气API返回错误码: ${nowResp.code}")
            val fcResp = api.getDailyForecast(loc)
            val hrList = try { api.getHourlyForecast(loc).hourly ?: emptyList() } catch (_: Exception) { emptyList() }

            val nowJson = gson.toJson(nowResp.now)
            val fcJson = gson.toJson(fcResp.daily ?: emptyList<DailyForecast>())
            val hrJson = gson.toJson(hrList)
            prefs.saveCachedWeather(loc, CachedWeather(loc, nowJson, fcJson, hrJson, System.currentTimeMillis()))
            WeatherInfo(city, nowResp.now, fcResp.daily ?: emptyList(), hrList, false)
        } catch (e: Exception) {
            Log.e(TAG, "weather fetch failed", e)
            val cached = prefs.getCachedWeather(loc)
            if (cached != null) WeatherInfo(city, parseNow(cached.weatherJson), parseDaily(cached.forecastJson), parseHourly(cached.hourlyJson), true)
            else WeatherInfo(city, null, emptyList(), emptyList(), false)
        }
    }

    private fun parseNow(json: String) = try { gson.fromJson(json, NowWeather::class.java) } catch (_: Exception) { null }
    private fun parseDaily(json: String): List<DailyForecast> = try {
        gson.fromJson(json, Array<DailyForecast>::class.java).toList()
    } catch (_: Exception) { emptyList() }
    private fun parseHourly(json: String): List<HourlyForecast> = try {
        gson.fromJson(json, Array<HourlyForecast>::class.java).toList()
    } catch (_: Exception) { emptyList() }
}
