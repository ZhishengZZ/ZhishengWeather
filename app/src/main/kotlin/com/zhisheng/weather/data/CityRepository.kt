package com.zhisheng.weather.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zhisheng.weather.model.City
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zhisheng")

// 城市搜索 + 持久化（DataStore）
object CityRepository {

    private lateinit var store: DataStore<Preferences>

    private val KEY_CITIES = stringPreferencesKey("cities")
    private val KEY_SELECTED = stringPreferencesKey("selected_key")

    private val json = Json { ignoreUnknownKeys = true }
    private val cityListSerializer = ListSerializer(City.serializer())

    fun init(context: Context) {
        store = context.applicationContext.dataStore
    }

    // 首装种子默认城市（零配置体验：装好即有天气，无需手动加城市）；
    // 以 KEY_CITIES 是否存在判定“首装”，用户删光城市后不会重种
    suspend fun ensureDefaultCity() {
        val seeded = store.data.map { it.contains(KEY_CITIES) }.first()
        if (!seeded) {
            addCity(
                City(
                    name = "北京",
                    affiliation = "北京",
                    latitude = 39.90,
                    longitude = 116.41,
                    locationKey = "101010100",
                )
            )
        }
    }

    // 搜索城市（和风 GeoAPI 主，小米兜底）
    suspend fun search(query: String): List<City> {
        if (query.isBlank()) return emptyList()
        if (QWeatherApi.enabled) {
            // 透传 CancellationException：被取消的搜索不再继续跑小米兜底（v0.0.1）
            val qw = try {
                QWeatherApi.service.cityLookup(query)
            } catch (ce: kotlinx.coroutines.CancellationException) {
                throw ce
            } catch (_: Exception) {
                null
            }
            val qwList = qw?.location?.mapNotNull { loc ->
                val lat = loc.lat?.toDoubleOrNull() ?: return@mapNotNull null
                val lon = loc.lon?.toDoubleOrNull() ?: return@mapNotNull null
                City(
                    name = loc.name ?: "",
                    affiliation = listOf(loc.adm1, loc.adm2)
                        .filter { !it.isNullOrBlank() }.distinct().joinToString("·"),
                    latitude = lat,
                    longitude = lon,
                    locationKey = loc.id ?: "$lon,$lat",
                )
            }.orEmpty()
            if (qwList.isNotEmpty()) return qwList
        }
        return XiaomiApi.instance.searchCity(query)
            .filter { it.status == 0 }
            .mapNotNull {
                val lat = it.latitude?.toDoubleOrNull() ?: return@mapNotNull null
                val lon = it.longitude?.toDoubleOrNull() ?: return@mapNotNull null
                val key = it.locationKey ?: return@mapNotNull null
                City(
                    name = it.name ?: "",
                    affiliation = it.affiliation ?: "",
                    latitude = lat,
                    longitude = lon,
                    locationKey = key,
                )
            }
    }

    // 已保存城市
    val cities: Flow<List<City>> by lazy {
        store.data.map { prefs -> prefs.cities() }
    }

    val selectedCity: Flow<City?> by lazy {
        store.data.map { prefs ->
            val sel = prefs[KEY_SELECTED] ?: return@map null
            prefs.cities().firstOrNull { it.locationKey == sel }
        }
    }

    suspend fun addCity(city: City) {
        store.edit { prefs ->
            val list = prefs.cities().toMutableList()
            if (list.none { it.locationKey == city.locationKey }) {
                list.add(city)
            }
            prefs[KEY_CITIES] = json.encodeToString(cityListSerializer, list)
            prefs[KEY_SELECTED] = city.locationKey
        }
    }

    suspend fun removeCity(locationKey: String) {
        store.edit { prefs ->
            val list = prefs.cities().toMutableList()
            list.removeAll { it.locationKey == locationKey }
            prefs[KEY_CITIES] = json.encodeToString(cityListSerializer, list)
            if (prefs[KEY_SELECTED] == locationKey) {
                prefs[KEY_SELECTED] = list.firstOrNull()?.locationKey.orEmpty()
            }
        }
    }

    suspend fun selectCity(locationKey: String) {
        store.edit { prefs ->
            prefs[KEY_SELECTED] = locationKey
        }
    }

    private fun Preferences.cities(): List<City> {
        val raw = this[KEY_CITIES] ?: return emptyList()
        return try {
            json.decodeFromString(cityListSerializer, raw)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
