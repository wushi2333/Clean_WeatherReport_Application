package iss.nus.edu.sg.weather.viewmodel

import android.app.Application
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import iss.nus.edu.sg.weather.data.local.CityDatabase
import iss.nus.edu.sg.weather.data.local.PrefsManager
import iss.nus.edu.sg.weather.data.model.City
import iss.nus.edu.sg.weather.data.model.WeatherInfo
import iss.nus.edu.sg.weather.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeatherUiState(
    val cities: List<City> = emptyList(),
    val currentPageIndex: Int = 0,
    val weatherMap: Map<String, WeatherInfo> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchResults: List<CityDatabase.CityEntry> = emptyList()
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PrefsManager(application)
    val repository = WeatherRepository(prefs)
    private val locationManager = application.getSystemService(LocationManager::class.java)

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initDefaultCity()
            detectGpsCity()
            repository.getAllCities().collect { cities ->
                val oldCities = _uiState.value.cities
                _uiState.value = _uiState.value.copy(cities = cities)
                // Load weather for newly added cities that don't have data yet
                val newCity = cities.find { c -> !_uiState.value.weatherMap.containsKey(c.cityId) }
                if (newCity != null) {
                    loadWeatherForCity(newCity)
                }
                // Load first city if no weather loaded yet
                if (cities.isNotEmpty() && _uiState.value.weatherMap.isEmpty()) {
                    loadWeatherForCity(cities.first())
                }
            }
        }
    }

    /** Search cities locally */
    fun searchCity(query: String) {
        _uiState.value = _uiState.value.copy(searchResults = repository.searchCity(query))
    }

    /** Add city from local search and close dialog */
    fun addCity(cityId: String) {
        viewModelScope.launch {
            repository.addCityById(cityId)
                .onSuccess { city ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = emptyList(),
                        currentPageIndex = prefs.getCities().size - 1  // jump to new city
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = emptyList(),
                        errorMessage = e.message
                    )
                }
        }
    }

    /** Load weather for a single city (lazy — only when needed) */
    fun ensureCityWeatherLoaded(cityId: String) {
        if (_uiState.value.weatherMap.containsKey(cityId)) return
        val city = _uiState.value.cities.find { it.cityId == cityId } ?: return
        viewModelScope.launch { loadWeatherForCity(city) }
    }

    private suspend fun loadWeatherForCity(city: City) {
        try {
            val info = repository.getWeather(city)
            _uiState.value = _uiState.value.copy(
                weatherMap = _uiState.value.weatherMap + (city.cityId to info)
            )
        } catch (_: Exception) {}
    }

    private suspend fun detectGpsCity() {
        try {
            val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            var loc: android.location.Location? = null
            for (p in providers) {
                try { loc = locationManager.getLastKnownLocation(p); if (loc != null) break }
                catch (_: SecurityException) {}
            }
            if (loc == null) { Log.d("WeatherVM", "No GPS"); return }
            val gpsLon = loc.longitude
            val gpsLat = loc.latitude
            val nearest = CityDatabase.findNearest(gpsLon, gpsLat)
            val cityName = nearest?.name ?: "定位城市"
            val cityId = "$gpsLon,$gpsLat"
            Log.d("WeatherVM", "GPS nearest city: $cityName")
            prefs.upsertGpsCity(City(name = cityName, cityId = cityId,
                longitude = gpsLon.toString(), latitude = gpsLat.toString(),
                adm1 = nearest?.province ?: "", source = "gps"))
        } catch (e: Exception) { Log.d("WeatherVM", "GPS error: ${e.message}") }
    }

    fun retryGps() {
        viewModelScope.launch { detectGpsCity() }
    }

    fun clearSearchResults() { _uiState.value = _uiState.value.copy(searchResults = emptyList()) }

    suspend fun refreshCurrentCity() {
        val city = _uiState.value.cities.getOrNull(_uiState.value.currentPageIndex) ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            val info = repository.getWeather(city, forceRefresh = true)
            _uiState.value = _uiState.value.copy(
                weatherMap = _uiState.value.weatherMap + (city.cityId to info), isLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "刷新失败")
        }
    }

    fun setPageIndex(index: Int) {
        _uiState.value = _uiState.value.copy(currentPageIndex = index)
        // Trigger lazy load for the new page
        val city = _uiState.value.cities.getOrNull(index) ?: return
        ensureCityWeatherLoaded(city.cityId)
    }

    fun deleteCity(cityId: String) {
        viewModelScope.launch {
            repository.deleteCity(cityId)
            _uiState.value = _uiState.value.copy(
                weatherMap = _uiState.value.weatherMap - cityId,
                currentPageIndex = _uiState.value.currentPageIndex
                    .coerceAtMost(_uiState.value.cities.size - 2).coerceAtLeast(0)
            )
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }
}
