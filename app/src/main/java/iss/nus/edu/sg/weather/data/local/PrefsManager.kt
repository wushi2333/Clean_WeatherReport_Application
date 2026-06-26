package iss.nus.edu.sg.weather.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import iss.nus.edu.sg.weather.data.model.City
import iss.nus.edu.sg.weather.data.model.CachedWeather
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple SharedPreferences-based storage. No Room, no annotation processors.
 */
class PrefsManager(context: Context) {

    private val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // In-memory live list, backed by prefs
    private val _cities = MutableStateFlow(loadCities())
    val citiesFlow: Flow<List<City>> = _cities.asStateFlow()

    // ---- Cities ----

    fun getCities(): List<City> = _cities.value

    suspend fun addCity(city: City) {
        val list = _cities.value.toMutableList()
        city.id = (list.maxOfOrNull { it.id } ?: 0) + 1
        city.sortOrder = list.size
        list.add(city)
        saveCities(list)
        _cities.value = list
    }

    suspend fun deleteCity(cityId: String) {
        val list = _cities.value.filter { it.cityId != cityId }
        saveCities(list)
        _cities.value = list
    }

    suspend fun initDefaultCity() {
        if (_cities.value.isEmpty()) {
            val hefei = City(
                id = 1, name = "合肥", cityId = "101220101",
                adm1 = "安徽省", sortOrder = 0, source = "default"
            )
            _cities.value = listOf(hefei)
            saveCities(_cities.value)
        }
    }

    /** Insert/update GPS-detected city at position 0. Remove if back in Hefei. */
    suspend fun upsertGpsCity(city: City) {
        val list = _cities.value.toMutableList()
        // Remove any existing GPS city
        list.removeAll { it.source == "gps" }
        // If GPS is within ~10km of Hefei, skip
        val gpsLon = city.longitude.toDoubleOrNull() ?: 0.0
        val gpsLat = city.latitude.toDoubleOrNull() ?: 0.0
        if (kotlin.math.abs(gpsLon - 117.28) < 0.12 && kotlin.math.abs(gpsLat - 31.86) < 0.12) {
            removeGpsCity(); return
        }
        // Check if this city already exists as manual/default
        val existing = list.find { it.cityId == city.cityId }
        if (existing != null) {
            // Move it to position 0
            list.remove(existing)
        }
        // Insert GPS city at front
        val gpsCity = city.copy(id = (list.maxOfOrNull { it.id } ?: 0) + 1, source = "gps")
        list.add(0, gpsCity)
        // Re-index sortOrder
        list.forEachIndexed { i, c -> c.sortOrder = i }
        saveCities(list)
        _cities.value = list
    }

    /** Remove GPS auto-detected city (called when back in Hefei) */
    suspend fun removeGpsCity() {
        val list = _cities.value.toMutableList()
        if (list.removeAll { it.source == "gps" }) {
            list.forEachIndexed { i, c -> c.sortOrder = i }
            saveCities(list)
            _cities.value = list
        }
    }

    private fun loadCities(): List<City> {
        val json = prefs.getString(KEY_CITIES, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<City>>() {}.type)
        } catch (_: Exception) { emptyList() }
    }

    private fun saveCities(list: List<City>) {
        prefs.edit().putString(KEY_CITIES, gson.toJson(list)).apply()
    }

    // ---- Weather Cache ----

    fun getCachedWeather(cityId: String): CachedWeather? {
        val json = prefs.getString(cacheKey(cityId), null) ?: return null
        return try { gson.fromJson(json, CachedWeather::class.java) } catch (_: Exception) { null }
    }

    fun saveCachedWeather(cityId: String, cached: CachedWeather) {
        prefs.edit().putString(cacheKey(cityId), gson.toJson(cached)).apply()
    }

    fun deleteCachedWeather(cityId: String) {
        prefs.edit().remove(cacheKey(cityId)).apply()
    }

    /** Remove weather cache entries older than 24 hours */
    fun cleanOldCache() {
        val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        val editor = prefs.edit()
        for (key in prefs.all.keys) {
            if (!key.startsWith("weather_cache_")) continue
            val json = prefs.getString(key, null) ?: continue
            try {
                val cached = gson.fromJson(json, CachedWeather::class.java)
                if (cached.lastFetchTime < cutoff) editor.remove(key)
            } catch (_: Exception) { editor.remove(key) }
        }
        editor.apply()
    }

    private fun cacheKey(cityId: String) = "weather_cache_$cityId"

    companion object {
        private const val KEY_CITIES = "cities_list"
    }
}
