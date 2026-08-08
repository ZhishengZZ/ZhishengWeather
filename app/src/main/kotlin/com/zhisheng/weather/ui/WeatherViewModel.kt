package com.zhisheng.weather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zhisheng.weather.data.AmbienceLevel
import com.zhisheng.weather.data.CityRepository
import com.zhisheng.weather.data.LocationSource
import com.zhisheng.weather.data.SettingsRepository
import com.zhisheng.weather.data.SourcePref
import com.zhisheng.weather.data.WeatherRepository
import com.zhisheng.weather.data.WidgetCache
import com.zhisheng.weather.data.WidgetDay
import com.zhisheng.weather.data.WidgetHour
import com.zhisheng.weather.data.WidgetSnapshot
import com.zhisheng.weather.model.City
import com.zhisheng.weather.model.WeatherData
import com.zhisheng.weather.widget.ZhishengWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class DisplayPrefs(
    val showAqi: Boolean = true,
    val showIndices: Boolean = true,
    val showYesterday: Boolean = true,
    val showPrecip: Boolean = true,
    val showTelemetry: Boolean = true,
    val windUnit: String = "kmh",
    val pressureUnit: String = "hpa",
    val scanlines: Boolean = true,
    val ambience: AmbienceLevel = AmbienceLevel.SUBTLE,
)

data class HomeUiState(
    val cities: List<City> = emptyList(),
    val selectedCity: City? = null,
    val weather: WeatherData? = null,
    val loading: Boolean = false,
    val tempUnit: String = "c",
    val showTyphoon: Boolean = true,
    val sourcePref: SourcePref = SourcePref.AUTO,
    val prefs: DisplayPrefs = DisplayPrefs(),
    val locating: Boolean = false,
    val locateMessage: String? = null,
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val _weather = MutableStateFlow<WeatherData?>(null)
    private val _loading = MutableStateFlow(false)
    private val _locating = MutableStateFlow(false)
    private val _locateMessage = MutableStateFlow<String?>(null)

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

    // 数据源偏好单独暴露：refresh 时要读当前值（combine 的 6 元上限已满）
    val sourcePref: StateFlow<SourcePref> = SettingsRepository.sourcePref
        .stateIn(viewModelScope, SharingStarted.Eagerly, SourcePref.AUTO)

    private val displayPrefs: StateFlow<DisplayPrefs> = combine(
        SettingsRepository.showAqi,
        SettingsRepository.showIndices,
        SettingsRepository.showYesterday,
        SettingsRepository.showPrecip,
        SettingsRepository.showTelemetry,
    ) { aqi, ix, y, p, tele ->
        DisplayPrefs(showAqi = aqi, showIndices = ix, showYesterday = y, showPrecip = p, showTelemetry = tele)
    }.combine(
        combine(
            SettingsRepository.windUnit,
            SettingsRepository.pressureUnit,
            SettingsRepository.scanlines,
            SettingsRepository.ambience,
        ) { w, pr, sl, amb -> listOf(w, pr, sl, amb) }
    ) { base, extra ->
        base.copy(
            windUnit = extra[0] as String,
            pressureUnit = extra[1] as String,
            scanlines = extra[2] as Boolean,
            ambience = extra[3] as AmbienceLevel,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DisplayPrefs())

    private val baseState: StateFlow<HomeUiState> = combine(
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

    val uiState: StateFlow<HomeUiState> = combine(
        baseState, displayPrefs, sourcePref, _locating, _locateMessage,
    ) { base, prefs, src, locating, msg ->
        base.copy(prefs = prefs, sourcePref = src, locating = locating, locateMessage = msg)
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
        // 数据源改了就立即重拉当前城市，不用等用户手动下拉（v0.0.2）
        viewModelScope.launch {
            var first = true
            sourcePref.collect {
                if (first) { first = false; return@collect }
                refresh()
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
            val result = WeatherRepository.fetchWeather(target, sourcePref.value)
            _weather.value = result
            _loading.value = false
            // 抓到有效数据就写一份小组件快照并刷新桌面（失败不影响主流程）
            if (result.current != null) {
                runCatching { saveWidgetSnapshot(target, result) }
            }
        }
    }

    private suspend fun saveWidgetSnapshot(city: City, data: WeatherData) {
        val ctx = getApplication<Application>()
        val unit = SettingsRepository.tempUnit.first()
        fun t(v: Double?): Int? = v?.let {
            (if (unit == "f") it * 9.0 / 5.0 + 32.0 else it).roundToInt()
        }
        val today = data.daily.firstOrNull()
        val hi = if (today?.high != null && today.low != null) maxOf(today.high, today.low) else today?.high
        val lo = if (today?.high != null && today.low != null) minOf(today.high, today.low) else today?.low
        val hourFmt = DateTimeFormatter.ofPattern("H时")
        val zone = ZoneId.systemDefault()

        WidgetCache.save(
            ctx,
            WidgetSnapshot(
                city = city.name,
                temp = t(data.current?.temperature),
                high = t(hi),
                low = t(lo),
                feelsLike = t(data.current?.feelsLike),
                text = data.current?.weatherText ?: data.current?.condition?.label.orEmpty(),
                conditionName = data.current?.condition?.name.orEmpty(),
                aqi = data.aqi?.value,
                aqiLevel = data.aqi?.level.orEmpty(),
                updateMillis = data.updateTime ?: System.currentTimeMillis(),
                source = data.dataSource.orEmpty(),
                // 跳过"现在"那格，小组件右侧展示接下来的四小时
                hours = data.hourly.drop(1).take(4).map { h ->
                    WidgetHour(
                        label = hourFmt.format(Instant.ofEpochMilli(h.timeMillis).atZone(zone)),
                        temp = t(h.temperature),
                        conditionName = h.condition?.name.orEmpty(),
                    )
                },
                days = data.daily.take(3).mapIndexed { i, d ->
                    val dh = if (d.high != null && d.low != null) maxOf(d.high, d.low) else d.high
                    val dl = if (d.high != null && d.low != null) minOf(d.high, d.low) else d.low
                    WidgetDay(
                        label = if (i == 0) "今天" else weekdayZh(d.dateMillis, zone),
                        high = t(dh),
                        low = t(dl),
                        conditionName = d.condition?.name.orEmpty(),
                    )
                },
            ),
        )
        ZhishengWidgetProvider.refreshAll(ctx)
    }

    private fun weekdayZh(millis: Long, zone: ZoneId): String =
        when (Instant.ofEpochMilli(millis).atZone(zone).dayOfWeek.value) {
            1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
            5 -> "周五"; 6 -> "周六"; else -> "周日"
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

    // —— 定位（v0.0.2）——
    // 只在用户主动触发时调用；权限申请由 UI 层负责，这里假定已授权。
    fun locateCurrentCity() {
        if (_locating.value) return
        viewModelScope.launch {
            _locating.value = true
            _locateMessage.value = null
            when (val r = LocationSource.locate(getApplication())) {
                is LocationSource.Result.Ok -> {
                    _locateMessage.value = "已定位：${r.city.name}"
                    addCityAndSelect(r.city)
                }
                is LocationSource.Result.Failed -> _locateMessage.value = r.message
            }
            _locating.value = false
        }
    }

    fun clearLocateMessage() {
        _locateMessage.value = null
    }
}
