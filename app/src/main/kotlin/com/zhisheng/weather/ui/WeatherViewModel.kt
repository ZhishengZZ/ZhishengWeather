package com.zhisheng.weather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zhisheng.weather.data.CityRepository
import com.zhisheng.weather.data.SettingsRepository
import com.zhisheng.weather.data.WeatherRepository
import com.zhisheng.weather.model.City
import com.zhisheng.weather.model.WeatherData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val cities: List<City> = emptyList(),
    val selectedCity: City? = null,
    val weather: WeatherData? = null,
    val loading: Boolean = false,
    val tempUnit: String = "c",
    val showTyphoon: Boolean = true,
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val _weather = MutableStateFlow<WeatherData?>(null)
    private val _loading = MutableStateFlow(false)

    private var lastFetchedKey: String? = null
    private var lastFetchKey: String? = null
    private var lastFetchAt: Long = 0L

    // 同一时间只保留一次抓取：换城市立即取消旧任务，
    // 避免新旧城市结果乱序覆盖（v0.0.1：切城市偶发数据错乱的修复）
    private var fetchJob: kotlinx.coroutines.Job? = null

    val cities: StateFlow<List<City>> = CityRepository.cities
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val selectedCity: StateFlow<City?> = CityRepository.selectedCity
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiState: StateFlow<HomeUiState> = combine(
        cities, selectedCity, _weather, _loading,
        SettingsRepository.tempUnit, SettingsRepository.showTyphoon,
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        HomeUiState(
            cities = arr[0] as List<City>,
            selectedCity = arr[1] as City?,
            weather = arr[2] as WeatherData?,
            loading = arr[3] as Boolean,
            tempUnit = arr[4] as String,
            showTyphoon = arr[5] as Boolean,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    init {
        viewModelScope.launch { CityRepository.ensureDefaultCity() }
        viewModelScope.launch {
            selectedCity.collect { city ->
                if (city != null && city.locationKey != lastFetchedKey) {
                    lastFetchedKey = city.locationKey
                    refresh(city)
                }
            }
        }
    }

    // force=false 用于 ON_RESUME 自动刷新：同城 10 分钟内不重复拉，
    // 避免与启动时 selectedCity 首发射叠加成双份请求（v0.0.1）
    fun refresh(city: City? = null, force: Boolean = true) {
        val target = city ?: selectedCity.value ?: return
        val now = System.currentTimeMillis()
        if (!force && target.locationKey == lastFetchKey && now - lastFetchAt < 10 * 60_000L) return
        if (target.locationKey != lastFetchedKey) lastFetchedKey = target.locationKey
        lastFetchKey = target.locationKey
        lastFetchAt = now
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _loading.value = true
            _weather.value = WeatherRepository.fetchWeather(target)
            _loading.value = false
        }
    }

    fun selectCity(locationKey: String) {
        viewModelScope.launch { CityRepository.selectCity(locationKey) }
    }

    fun addCityAndSelect(city: City) {
        viewModelScope.launch {
            CityRepository.addCity(city)
        }
        lastFetchedKey = city.locationKey
        refresh(city)
    }

    fun removeCity(locationKey: String) {
        viewModelScope.launch { CityRepository.removeCity(locationKey) }
    }
}
