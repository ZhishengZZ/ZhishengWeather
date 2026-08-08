package com.zhisheng.weather.data

import com.zhisheng.weather.model.AqiInfo
import com.zhisheng.weather.model.City
import com.zhisheng.weather.model.CurrentWeather
import com.zhisheng.weather.model.HourlyWeather
import com.zhisheng.weather.model.MinutePrecip
import com.zhisheng.weather.model.WeatherCondition
import com.zhisheng.weather.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

// Open-Meteo 作为**独立主源**（v0.0.2）：完全免 key、全球覆盖。
// 装不上和风凭据的用户在设置里锁定本源即可拿到实况/逐时/逐日/空气质量/分钟降水的完整体验。
// 与 OpenMeteoApi（只做补缺）分开：那个是补漏工具，这个是完整链路。
object OpenMeteoSource {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private suspend inline fun <reified T> get(url: String): T? = withContext(Dispatchers.IO) {
        try {
            okHttp.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                val body = resp.body?.string()
                if (!resp.isSuccessful || body == null) null else json.decodeFromString<T>(body)
            }
        } catch (ce: kotlinx.coroutines.CancellationException) {
            throw ce
        } catch (_: Exception) {
            null
        }
    }

    suspend fun fetch(city: City): WeatherData = try {
        coroutineScope {
            val lat = city.latitude
            val lon = city.longitude
            val mainDeferred = async {
                get<OmFull>(
                    "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                        "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day," +
                        "precipitation,weather_code,cloud_cover,pressure_msl,wind_speed_10m," +
                        "wind_direction_10m,wind_gusts_10m,visibility,dew_point_2m" +
                        "&hourly=temperature_2m,weather_code,wind_speed_10m,precipitation_probability" +
                        "&daily=temperature_2m_max,temperature_2m_min,weather_code,wind_speed_10m_max," +
                        "precipitation_probability_max,sunrise,sunset,uv_index_max" +
                        "&minutely_15=precipitation" +
                        "&forecast_days=16&forecast_hours=24&timezone=auto"
                )
            }
            val aqiDeferred = async {
                get<OmAirResult>(
                    "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$lat&longitude=$lon" +
                        "&current=pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone,us_aqi" +
                        "&timezone=auto"
                )
            }

            val m = mainDeferred.await() ?: return@coroutineScope WeatherData(error = "公共源请求失败")
            val air = aqiDeferred.await()
            val offsetMs = m.utc_offset_seconds * 1000L

            // OM 的时间串是城市本地墙上时间且不带偏移，减去 offset 折回真实 epoch
            fun epochOf(local: String?): Long? = local?.let {
                try {
                    java.time.LocalDateTime.parse(it)
                        .toInstant(java.time.ZoneOffset.UTC).toEpochMilli() - offsetMs
                } catch (_: Exception) {
                    null
                }
            }

            val cur = m.current
            val isDay = cur?.is_day != 0
            val current = cur?.let {
                CurrentWeather(
                    temperature = it.temperature_2m,
                    feelsLike = it.apparent_temperature,
                    condition = wmo(it.weather_code, isDay),
                    weatherText = wmo(it.weather_code, isDay).label,
                    humidity = it.relative_humidity_2m,
                    windSpeed = it.wind_speed_10m,
                    windDirectionDeg = it.wind_direction_10m,
                    pressure = it.pressure_msl,
                    uvIndex = m.daily?.uv_index_max?.firstOrNull()?.let { u -> Math.round(u).toInt() },
                    visibility = it.visibility?.let { v -> v / 1000.0 },
                    dewPoint = it.dew_point_2m,
                    cloudCover = it.cloud_cover,
                    windGust = it.wind_gusts_10m,
                    precipMm = it.precipitation,
                )
            }

            val nowMs = System.currentTimeMillis()
            val hourly = m.hourly?.let { h ->
                h.time?.mapIndexedNotNull { i, t ->
                    val e = epochOf(t) ?: return@mapIndexedNotNull null
                    // 逐时保留当前整点及以后
                    if (e < nowMs - 3_600_000L) null else HourlyWeather(
                        timeMillis = e,
                        temperature = h.temperature_2m?.getOrNull(i),
                        condition = wmo(h.weather_code?.getOrNull(i), isDayAt(e, offsetMs)),
                        windSpeed = h.wind_speed_10m?.getOrNull(i),
                        precipProb = h.precipitation_probability?.getOrNull(i)?.let { p -> Math.round(p).toInt() },
                    )
                }?.take(24)
            } ?: emptyList()

            val daily = m.daily?.let { d ->
                d.time?.mapIndexedNotNull { i, day ->
                    val e = try {
                        java.time.LocalDate.parse(day).atStartOfDay(java.time.ZoneOffset.UTC)
                            .toInstant().toEpochMilli() - offsetMs
                    } catch (_: Exception) {
                        return@mapIndexedNotNull null
                    }
                    com.zhisheng.weather.model.DailyWeather(
                        dateMillis = e,
                        high = d.temperature_2m_max?.getOrNull(i),
                        low = d.temperature_2m_min?.getOrNull(i),
                        condition = wmo(d.weather_code?.getOrNull(i), true),
                        windSpeed = d.wind_speed_10m_max?.getOrNull(i),
                        precipProbability = d.precipitation_probability_max?.getOrNull(i)
                            ?.let { p -> Math.round(p).toInt() },
                        sunrise = clockOf(d.sunrise?.getOrNull(i)),
                        sunset = clockOf(d.sunset?.getOrNull(i)),
                        moonPhase = MoonCalc.phaseKey(e),
                    )
                }?.take(15)
            } ?: emptyList()

            // 15 分钟粒度降水：取当前之后 2 小时（8 段），与和风分钟降水卡语义对齐
            val precip = m.minutely_15?.let { mm ->
                mm.time?.mapIndexedNotNull { i, t ->
                    val e = epochOf(t) ?: return@mapIndexedNotNull null
                    if (e < nowMs - 900_000L) null
                    else MinutePrecip(e, mm.precipitation?.getOrNull(i)?.toFloat() ?: 0f)
                }?.take(9)
            } ?: emptyList()

            val aqiInfo = air?.current?.let { a ->
                AqiInfo(
                    value = a.us_aqi?.let { Math.round(it).toInt() },
                    level = WeatherRepository.aqiLevel(a.us_aqi?.let { Math.round(it).toInt() }),
                    pm25 = a.pm2_5?.let { fmt1(it) },
                    pm10 = a.pm10?.let { fmt1(it) },
                    o3 = a.ozone?.let { fmt1(it) },
                    no2 = a.nitrogen_dioxide?.let { fmt1(it) },
                    so2 = a.sulphur_dioxide?.let { fmt1(it) },
                    co = a.carbon_monoxide?.let { fmt1(it) },
                )
            }

            WeatherData(
                current = current,
                hourly = hourly,
                daily = daily,
                aqi = aqiInfo,
                alerts = emptyList(), // 公共源不提供官方预警
                updateTime = System.currentTimeMillis(),
                rainNowcast = precipSummary(precip),
                rainMinutes = if (precip.size >= 2) precip else emptyList(),
                dataSource = "OPEN-METEO",
            )
        }
    } catch (ce: kotlinx.coroutines.CancellationException) {
        throw ce
    } catch (e: Exception) {
        WeatherData(error = e.message ?: "公共源网络错误")
    }

    private fun fmt1(v: Double): String =
        if (v == Math.floor(v)) v.toInt().toString() else String.format(java.util.Locale.US, "%.1f", v)

    private fun precipSummary(list: List<MinutePrecip>): String? {
        if (list.isEmpty()) return null
        val total = list.sumOf { it.precip.toDouble() }
        return if (total <= 0.0) "未来两小时无降水" else "未来两小时有降水，累计约 ${fmt1(total)}mm"
    }

    // 逐时图标昼夜：按城市本地小时判断（6-18 视为白天），避免夜里整排太阳
    private fun isDayAt(epochMs: Long, offsetMs: Long): Boolean {
        val localHour = ((epochMs + offsetMs) / 3_600_000L % 24L).toInt()
        return localHour in 6..18
    }

    private fun clockOf(s: String?): String? {
        if (s.isNullOrEmpty()) return null
        return try {
            java.time.LocalDateTime.parse(s).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        } catch (_: Exception) {
            null
        }
    }

    // WMO code → 条件枚举，带昼夜变体
    private fun wmo(code: Int?, isDay: Boolean): WeatherCondition = when (code) {
        0 -> if (isDay) WeatherCondition.CLEAR else WeatherCondition.CLEAR_NIGHT
        1, 2 -> if (isDay) WeatherCondition.PARTLY_CLOUDY else WeatherCondition.PARTLY_CLOUDY_NIGHT
        3 -> WeatherCondition.OVERCAST
        45, 48 -> WeatherCondition.FOG
        51, 53, 55, 56, 57 -> WeatherCondition.DRIZZLE
        61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAIN
        71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
        95, 96, 99 -> WeatherCondition.THUNDERSTORM
        else -> WeatherCondition.CLOUDY
    }

    // —— 城市检索（Open-Meteo Geocoding，免 key，支持中文） ——
    suspend fun searchCity(query: String): List<City> {
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        val r = get<OmGeoResult>(
            "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=10&language=zh&format=json"
        ) ?: return emptyList()
        return r.results.orEmpty().map { g ->
            City(
                name = g.name.orEmpty(),
                affiliation = listOfNotNull(g.admin1, g.admin2)
                    .filter { it.isNotBlank() }.distinct().joinToString("·"),
                latitude = g.latitude,
                longitude = g.longitude,
                locationKey = "om:${g.latitude},${g.longitude}",
            )
        }.filter { it.name.isNotBlank() }
    }
}

@Serializable
data class OmFull(
    val utc_offset_seconds: Int = 0,
    val current: OmCurrentFull? = null,
    val hourly: OpenMeteoHourly? = null,
    val daily: OmDailyFull? = null,
    val minutely_15: OmMinutely15? = null,
)

@Serializable
data class OmCurrentFull(
    val temperature_2m: Double? = null,
    val relative_humidity_2m: Double? = null,
    val apparent_temperature: Double? = null,
    val is_day: Int? = null,
    val precipitation: Double? = null,
    val weather_code: Int? = null,
    val cloud_cover: Double? = null,
    val pressure_msl: Double? = null,
    val wind_speed_10m: Double? = null,
    val wind_direction_10m: Double? = null,
    val wind_gusts_10m: Double? = null,
    val visibility: Double? = null,
    val dew_point_2m: Double? = null,
)

@Serializable
data class OmDailyFull(
    val time: List<String>? = null,
    val temperature_2m_max: List<Double?>? = null,
    val temperature_2m_min: List<Double?>? = null,
    val weather_code: List<Int?>? = null,
    val wind_speed_10m_max: List<Double?>? = null,
    val precipitation_probability_max: List<Double?>? = null,
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null,
    val uv_index_max: List<Double?>? = null,
)

@Serializable
data class OmMinutely15(
    val time: List<String>? = null,
    val precipitation: List<Double?>? = null,
)

@Serializable
data class OmAirResult(val current: OmAirCurrent? = null)

@Serializable
data class OmAirCurrent(
    val pm10: Double? = null,
    val pm2_5: Double? = null,
    val carbon_monoxide: Double? = null,
    val nitrogen_dioxide: Double? = null,
    val sulphur_dioxide: Double? = null,
    val ozone: Double? = null,
    val us_aqi: Double? = null,
)

@Serializable
data class OmGeoResult(val results: List<OmGeoItem>? = null)

@Serializable
data class OmGeoItem(
    val name: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val admin1: String? = null,
    val admin2: String? = null,
    val country: String? = null,
)
