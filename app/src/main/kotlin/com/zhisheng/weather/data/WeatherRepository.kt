package com.zhisheng.weather.data

import com.zhisheng.weather.model.AqiInfo
import com.zhisheng.weather.model.AlertInfo
import com.zhisheng.weather.model.City
import com.zhisheng.weather.model.CurrentWeather
import com.zhisheng.weather.model.DailyWeather
import com.zhisheng.weather.model.HourlyWeather
import com.zhisheng.weather.model.LifeIndexExtra
import com.zhisheng.weather.model.MinutePrecip
import com.zhisheng.weather.model.TyphoonInfo
import com.zhisheng.weather.model.WeatherCondition
import com.zhisheng.weather.model.WeatherData
import com.zhisheng.weather.model.YesterdayInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

// 天气仓储：和风天气为主源（并行 7 路），小米源补昨日复盘/台风，
// 和风不可用（未配置凭据/网络失败）时整体回退小米源。
object WeatherRepository {

    suspend fun fetchWeather(city: City): WeatherData {
        val data = if (QWeatherApi.enabled) {
            fetchQWeather(city) ?: fetchXiaomi(city)
        } else {
            fetchXiaomi(city)
        }
        // 逐日不足 15 天用 Open-Meteo 补齐（海外城市小米源天数少的兜底）
        return backfillHourly(backfillDaily(data, city), city)
    }

    // —— 和风天气主路径 ——
    // 每路请求带 1 次重试：手机网络下偶发超时/连接抖动若无重试，
    // 对应区块会静默消失（v0.0.1 修复：平舆丢月相即 daily 单发失败所致）
    // v0.0.1：透传 CancellationException（城市切换取消）；4xx 不重试（海外 minutely 400 等确定性失败）
    private suspend fun <T> qwRetry(times: Int = 2, block: suspend () -> T?): T? {
        repeat(times) { i ->
            try {
                val r = block()
                if (r != null) return r
            } catch (ce: kotlinx.coroutines.CancellationException) {
                throw ce
            } catch (he: retrofit2.HttpException) {
                if (he.code() < 500) return null
            } catch (_: Exception) {
            }
            if (i < times - 1) kotlinx.coroutines.delay(350L)
        }
        return null
    }

    private suspend fun fetchQWeather(city: City): WeatherData? = try {
        coroutineScope {
            val lat = QWeatherApi.lat(city.latitude)
            val lon = QWeatherApi.lat(city.longitude)
            val svc = QWeatherApi.service

            val now = async { qwRetry { svc.current(lat, lon) } }
            val daily = async {
                qwRetry { svc.daily(lat, lon, 10) }
                    ?: qwRetry { svc.daily(lat, lon, 7) }
                    ?: qwRetry { svc.daily(lat, lon, 3) }
            }
            val hourly = async { qwRetry { svc.hourly(lat, lon) } }
            val alerts = async { qwRetry { svc.alerts(lat, lon) } }
            val air = async { qwRetry { svc.air(lat, lon) } }
            val minutely = async { qwRetry { svc.minutely(QWeatherApi.lonLat(city)) } }
            val indices = async { qwRetry { svc.indices(QWeatherApi.lonLat(city), "1,2,3,9") } }
            // 小米源补：昨日复盘 + 台风 + 逐日扩展（按城市名反查小米 key，取距离最近命中，
            // 防同名异地串台——v0.0.1 修复：金川区(金昌)显示四川金川县预警）
            val supp = async {
                try {
                    val key = nearestXiaomiKey(city.name, city.latitude, city.longitude)
                    if (key == null) {
                        android.util.Log.i("ZhishengWeather", "小米源无 ${city.name} 附近同名城市，跳过补缺")
                        null
                    } else {
                        XiaomiApi.instance.getWeather(
                            latitude = city.latitude,
                            longitude = city.longitude,
                            locationKey = key,
                            days = 15,
                        )
                    }
                } catch (ce: kotlinx.coroutines.CancellationException) {
                    throw ce
                } catch (_: Exception) {
                    null
                }
            }

            val cur = now.await() ?: return@coroutineScope null
            val d = daily.await()
            val h = hourly.await()
            val w = alerts.await()
            val a = air.await()
            val m = minutely.await()
            val ix = indices.await()
            val s = supp.await()

            // v0.0.1：单路失败留痕，logcat 可查（此前区块静默消失无从定位）
            val missing = buildList {
                if (d == null) add("daily")
                if (h == null) add("hourly")
                if (w == null) add("alerts")
                if (a == null) add("air")
                if (m == null) add("minutely")
                if (ix == null) add("indices")
                if (s == null) add("xiaomi-supp")
            }
            if (missing.isNotEmpty()) {
                android.util.Log.w(
                    "ZhishengWeather",
                    "QWeather 城市=${city.name} 缺失路: $missing（对应区块由备源/本地兜底）",
                )
            }

            val idxLevel = { type: String ->
                ix?.daily?.firstOrNull { it.type == type }?.level?.toIntOrNull()
            }

            // 逐日 = 和风(≤10天) + 小米续接；月相缺失的行用本地 Meeus 计算补上
            // （小米源 moonPhase 恒空，和风 daily 单路失败时月相不再整行消失）
            val dailyList = buildList {
                d?.days?.mapNotNull { dd ->
                    val t = parseTimeMillis(dd.forecastStartTime)
                    if (t == 0L) null else DailyWeather(
                        dateMillis = t,
                        high = dd.temperatureMax?.value,
                        low = dd.temperatureMin?.value,
                        condition = WeatherCondition.fromQwCode(dd.daytime?.condition?.code),
                        windSpeed = speedKmh(dd.daytime?.wind?.speed),
                        precipProbability = dd.daytime?.precipitation?.probability,
                        sunrise = formatClock(dd.astro?.sunrise),
                        sunset = formatClock(dd.astro?.sunset),
                        moonrise = formatClock(dd.astro?.moonrise),
                        moonset = formatClock(dd.astro?.moonset),
                        moonPhase = dd.astro?.moonPhase,
                    )
                }?.let { addAll(it) }
                // 和风最多 10 天，超出部分用小米源续上
                val qwCount = size
                s?.let { mapXiaomiDaily(it) }?.drop(qwCount)?.let { addAll(it) }
            }.map { dd ->
                dd.takeIf { it.moonPhase != null }
                    ?: dd.copy(moonPhase = MoonCalc.phaseKey(dd.dateMillis))
            }

            WeatherData(
                current = CurrentWeather(
                    temperature = cur.temperature?.value,
                    feelsLike = cur.feelsLike?.value,
                    condition = WeatherCondition.fromQwCode(cur.condition?.code),
                    weatherText = cur.condition?.text,
                    humidity = pct(cur.humidity),
                    windSpeed = speedKmh(cur.wind?.speed),
                    windDirectionDeg = cur.wind?.direction?.degree,
                    pressure = cur.pressure?.value,
                    uvIndex = cur.uvIndex,
                    visibility = distKm(cur.visibility),
                    dewPoint = cur.dewPoint?.value,
                    cloudCover = pct(cur.cloudCover),
                    windGust = speedKmh(cur.windGust),
                    precipMm = cur.precipitation?.amount?.value,
                ),
                hourly = h?.hours?.mapNotNull { hh ->
                    val t = parseTimeMillis(hh.forecastTime)
                    if (t == 0L) null else HourlyWeather(
                        timeMillis = t,
                        temperature = hh.temperature?.value,
                        condition = WeatherCondition.fromQwCode(hh.condition?.code),
                        windSpeed = speedKmh(hh.wind?.speed),
                        precipProb = hh.precipitation?.probability,
                    )
                } ?: emptyList(),
                daily = dailyList,
                aqi = a?.indexes?.firstOrNull()?.let { idx ->
                    AqiInfo(
                        value = idx.aqi,
                        level = idx.category ?: idx.level,
                        primary = idx.primaryPollutant?.name,
                        pm25 = pollutant(a, "pm2p5"),
                        pm10 = pollutant(a, "pm10"),
                        o3 = pollutant(a, "o3"),
                        no2 = pollutant(a, "no2"),
                        so2 = pollutant(a, "so2"),
                        co = pollutant(a, "co"),
                    )
                } ?: s?.aqi?.let { sa ->
                    AqiInfo(
                        value = sa.aqi?.toIntOrNull(),
                        level = aqiLevel(sa.aqi?.toIntOrNull()),
                        pm25 = sa.pm25,
                        pm10 = sa.pm10,
                        o3 = sa.o3,
                        no2 = sa.no2,
                        so2 = sa.so2,
                        co = sa.co,
                    )
                },
                alerts = buildList {
                    w?.alerts?.forEach { al ->
                        add(
                            AlertInfo(
                                title = al.headline ?: al.eventType?.name ?: "天气预警",
                                detail = al.description,
                                level = al.severity,
                                pubTime = al.issuedTime,
                            )
                        )
                    }
                    s?.alerts?.forEach { xa ->
                        val title = xa.title ?: ""
                        if (title.isNotBlank() && none { it.title == title }) {
                            add(AlertInfo(title = title, detail = xa.detail, level = xa.level, pubTime = xa.pubTime))
                        }
                    }
                },
                updateTime = System.currentTimeMillis(),
                rainNowcast = m?.summary?.takeIf { m.code == "200" },
                rainMinutes = m?.minutely?.takeIf { m.code == "200" }?.mapNotNull { mi ->
                    val t = parseTimeMillis(mi.fxTime)
                    if (t == 0L) null else MinutePrecip(t, mi.precip?.toFloatOrNull() ?: 0f)
                } ?: emptyList(),
                carWashOk = idxLevel("2")?.let { it <= 2 },
                sportsOk = idxLevel("1")?.let { it <= 2 },
                extraIndices = ix?.daily?.mapNotNull { it2 ->
                    when (it2.type) {
                        "3" -> LifeIndexExtra("穿衣", "DRESS", it2.category ?: "")
                        "9" -> LifeIndexExtra("感冒", "COLD", it2.category ?: "")
                        else -> null
                    }
                } ?: emptyList(),
                yesterday = s?.yesterday?.let {
                    YesterdayInfo(
                        high = it.tempMax?.toDoubleOrNull(),
                        low = it.tempMin?.toDoubleOrNull(),
                        aqi = it.aqi?.toIntOrNull(),
                        condition = WeatherCondition.fromCode(it.weatherEnd),
                    )
                },
                typhoons = s?.typhoon?.mapNotNull { t ->
                    if (t.typhoonCname.isNullOrEmpty()) null
                    else TyphoonInfo(
                        name = t.typhoonCname,
                        ename = t.typhoonEname,
                        type = t.typhoonType,
                        windSpeed = t.centWindSpeed,
                    )
                } ?: emptyList(),
                dataSource = "QWEATHER",
            )
        }
    } catch (ce: kotlinx.coroutines.CancellationException) {
        throw ce
    } catch (e: Exception) {
        android.util.Log.w("ZhishengWeather", "QWeather 主路径整体失败，回退小米源", e)
        null
    }

    private fun pollutant(air: QwAir, code: String): String? =
        air.pollutants.firstOrNull { it.code == code }
            ?.concentration?.value?.let { if (it == it.toInt().toDouble()) it.toInt().toString() else it.toString() }

    // 和风新版单位换算：优先用 API 返回的 unit 字段判定（v0.0.1：启发式会把大雾
    // 能见度 500m 误显示成 500km），unit 缺失时才退回启发式
    private fun speedKmh(v: QwVal?): Double? = v?.value?.let {
        when (v.unit?.lowercase()) {
            "km/h", "kmh" -> it
            else -> it * 3.6 // 默认 m/s
        }
    }

    private fun distKm(v: QwVal?): Double? = v?.value?.let {
        when (v.unit?.lowercase()) {
            "km" -> it
            "m" -> it / 1000.0
            else -> if (it > 1000.0) it / 1000.0 else it
        }
    }

    private fun pct(v: Double?): Double? = v?.let { if (it <= 1.0) it * 100.0 else it }

    // —— 小米源兜底路径（原有逻辑） ——
    private suspend fun fetchXiaomi(city: City): WeatherData = try {
        // 兜底链路同样带重试：此前单次请求一抖就整屏红字，比主链路还脆弱（v0.0.1）
        val result = qwRetry {
            XiaomiApi.instance.getWeather(
                latitude = city.latitude,
                longitude = city.longitude,
                locationKey = resolveXiaomiKey(city),
                days = 15,
            )
        } ?: throw java.io.IOException("小米源请求失败")
        val data = mapXiaomiToWeatherData(result)
        // 小米源 moonPhase 恒空：本地计算补上，日月卡月相行不再整行消失
        val withMoon = data.copy(
            daily = data.daily.map { dd ->
                dd.takeIf { it.moonPhase != null }
                    ?: dd.copy(moonPhase = MoonCalc.phaseKey(dd.dateMillis))
            }
        )
        // 实况缺字段（能见度/露点/云量/阵风任一）即用 Open-Meteo 补缺
        val c = withMoon.current
        val needOm = c != null &&
            (c.visibility == null || c.dewPoint == null || c.cloudCover == null || c.windGust == null)
        if (needOm) {
            val om = OpenMeteoApi.fetch(city.latitude, city.longitude)
            if (om?.current != null) {
                withMoon.copy(
                    current = c.copy(
                        visibility = c.visibility ?: om.current.visibility?.let { it / 1000.0 },
                        dewPoint = c.dewPoint ?: om.current.dew_point_2m,
                        cloudCover = c.cloudCover ?: om.current.cloud_cover,
                        windGust = c.windGust ?: om.current.wind_gusts_10m,
                    )
                )
            } else withMoon
        } else withMoon
    } catch (ce: kotlinx.coroutines.CancellationException) {
        throw ce
    } catch (e: Exception) {
        WeatherData(error = e.message ?: "网络错误")
    }

    // 小米 key 形如 "weathercn:xxx"/"accu:xxx"；和风搜索存下的是和风 id（纯数字），
    // 直接拿去调小米接口会返回全空（v0.0.1 修复：和风整体失败时和风搜索的城市整屏空白）
    private suspend fun resolveXiaomiKey(city: City): String {
        if (city.locationKey.contains(":")) return city.locationKey
        return nearestXiaomiKey(city.name, city.latitude, city.longitude) ?: city.locationKey
    }

    // 按城市名反查小米 key：同名异地（金川区/金川县、朝阳…）必须取距离最近的命中，
    // 且超过 150km 视为无匹配，宁可缺数据也不串城市（v0.0.1）
    private const val XIAOMI_MATCH_MAX_KM = 150.0

    private suspend fun nearestXiaomiKey(name: String, lat: Double, lon: Double): String? = try {
        val hits = XiaomiApi.instance.searchCity(name)
            .filter { it.status == 0 && !it.locationKey.isNullOrBlank() }
        val hit = hits.minByOrNull { h ->
            val hl = h.latitude?.toDoubleOrNull()
            val ho = h.longitude?.toDoubleOrNull()
            if (hl == null || ho == null) Double.MAX_VALUE / 2 else distanceKm(lat, lon, hl, ho)
        }
        if (hit == null) {
            null
        } else {
            val hl = hit.latitude?.toDoubleOrNull()
            val ho = hit.longitude?.toDoubleOrNull()
            if (hl == null || ho == null) hit.locationKey // 命中无坐标，退化为直接用
            else if (distanceKm(lat, lon, hl, ho) <= XIAOMI_MATCH_MAX_KM) hit.locationKey else null
        }
    } catch (ce: kotlinx.coroutines.CancellationException) {
        throw ce
    } catch (_: Exception) {
        null
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dp = Math.toRadians(lat2 - lat1)
        val dl = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dp / 2) * Math.sin(dp / 2) +
            Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) * Math.sin(dl / 2)
        return 2 * r * Math.asin(Math.sqrt(a))
    }

    // —— Open-Meteo 逐日补齐（全球 16 天免 key）——
    // 和风逐日上限 10 天、小米海外城市仅约 5 天 → 东京等城市凑不满 15 天，
    // 用 Open-Meteo 把尾部补齐
    private suspend fun backfillDaily(data: WeatherData, city: City): WeatherData {
        if (data.error != null || data.daily.size >= 15) return data
        val om = OpenMeteoApi.fetchDaily(city.latitude, city.longitude) ?: return data
        val extra = omToDaily(om).drop(data.daily.size).take(15 - data.daily.size)
        if (extra.isEmpty()) return data
        val merged = (data.daily + extra).map { dd ->
            dd.takeIf { it.moonPhase != null }
                ?: dd.copy(moonPhase = MoonCalc.phaseKey(dd.dateMillis))
        }
        return data.copy(daily = merged)
    }

    private fun omToDaily(om: OpenMeteoDaily): List<DailyWeather> {
        val times = om.time ?: return emptyList()
        return times.mapIndexedNotNull { i, day ->
            val t = try {
                java.time.LocalDate.parse(day)
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
            } catch (_: Exception) {
                0L
            }
            if (t == 0L) null else DailyWeather(
                dateMillis = t,
                high = om.temperature_2m_max?.getOrNull(i),
                low = om.temperature_2m_min?.getOrNull(i),
                condition = fromWmoCode(om.weather_code?.getOrNull(i)),
                windSpeed = om.wind_speed_10m_max?.getOrNull(i),
                precipProbability = om.precipitation_probability_max?.getOrNull(i)?.let { Math.round(it).toInt() },
                sunrise = formatLocalClock(om.sunrise?.getOrNull(i)),
                sunset = formatLocalClock(om.sunset?.getOrNull(i)),
            )
        }
    }

    // 逐时补齐：和风/小米逐时缺失（海外 4xx 落空等）时用 Open-Meteo 取 24 小时；
    // OM 时间为城市本地墙上时间，用 utc_offset_seconds 折回真实 epoch，保证跨时区显示正确
    private suspend fun backfillHourly(data: WeatherData, city: City): WeatherData {
        if (data.error != null || data.hourly.size >= 2) return data
        val om = OpenMeteoApi.fetchHourly(city.latitude, city.longitude) ?: return data
        val h = om.hourly ?: return data
        val offsetMs = om.utc_offset_seconds * 1000L
        val cityLocalNow = System.currentTimeMillis() + offsetMs
        val list = h.time?.mapIndexedNotNull { i, t ->
            val local = try {
                java.time.LocalDateTime.parse(t).toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
            } catch (_: Exception) {
                null
            }
            if (local == null || local < cityLocalNow - 3_600_000L) null
            else HourlyWeather(
                timeMillis = local - offsetMs,
                temperature = h.temperature_2m?.getOrNull(i),
                condition = fromWmoCode(h.weather_code?.getOrNull(i)),
                windSpeed = h.wind_speed_10m?.getOrNull(i),
                precipProb = h.precipitation_probability?.getOrNull(i)?.let { Math.round(it).toInt() },
            )
        }?.take(24) ?: emptyList()
        return if (list.size >= 2) data.copy(hourly = list) else data
    }

    // WMO weather_code → 条件枚举（Open-Meteo）
    private fun fromWmoCode(code: Int?): WeatherCondition = when (code) {
        0 -> WeatherCondition.CLEAR
        1, 2 -> WeatherCondition.PARTLY_CLOUDY
        3 -> WeatherCondition.OVERCAST
        45, 48 -> WeatherCondition.FOG
        51, 53, 55, 56, 57 -> WeatherCondition.DRIZZLE
        61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAIN
        71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
        95, 96, 99 -> WeatherCondition.THUNDERSTORM
        else -> WeatherCondition.CLOUDY
    }

    // Open-Meteo 时间为无时区本地格式（2026-08-07T04:53）
    private fun formatLocalClock(s: String?): String? {
        if (s.isNullOrEmpty()) return null
        return try {
            java.time.LocalDateTime.parse(s).format(clockFmt)
        } catch (_: Exception) {
            null
        }
    }

    // 小米源逐日映射（15 天）
    private fun mapXiaomiDaily(r: XiaomiForecastResult): List<DailyWeather> {
        val dailyWindSpeed = r.forecastDaily?.wind?.speed?.value
        val dailyPrecip = r.forecastDaily?.precipitationProbability?.value
        val dailySun = r.forecastDaily?.sunRiseSet?.value
        return buildList {
            val highs = r.forecastDaily?.temperature?.value
            val codes = r.forecastDaily?.weather?.value
            // pubTime 解析失败退回当日 0 点，避免逐日日期全部掉回 1970（v0.0.1）
            val start = parseTimeMillis(r.forecastDaily?.pubTime).takeIf { it != 0L }
                ?: todayStartMillis()
            val n = minOf(highs?.size ?: 0, codes?.size ?: 0, 15)
            for (i in 0 until n) {
                val t = highs?.getOrNull(i)
                val w = codes?.getOrNull(i)
                val sun = dailySun?.getOrNull(i)
                add(
                    DailyWeather(
                        dateMillis = start + i * 86400_000L,
                        high = t?.from?.toDoubleOrNull(),
                        low = t?.to?.toDoubleOrNull(),
                        condition = WeatherCondition.fromCode(w?.from),
                        windSpeed = dailyWindSpeed?.getOrNull(i)?.from?.toDoubleOrNull(),
                        precipProbability = dailyPrecip?.getOrNull(i)?.toIntOrNull(),
                        sunrise = formatClock(sun?.from),
                        sunset = formatClock(sun?.to),
                    )
                )
            }
        }
    }

    private fun mapXiaomiToWeatherData(r: XiaomiForecastResult): WeatherData {
        val current = r.current?.let { cur ->
            CurrentWeather(
                temperature = cur.temperature?.value?.toDoubleOrNull(),
                feelsLike = cur.feelsLike?.value?.toDoubleOrNull(),
                condition = WeatherCondition.fromCode(cur.weather),
                weatherText = WeatherCondition.fromCode(cur.weather).label,
                humidity = cur.humidity?.value?.toDoubleOrNull(),
                // 小米风速带 unit 字段：km/h 透传，m/s 换算（v0.0.1）
                windSpeed = cur.wind?.speed?.let { w ->
                    w.value?.toDoubleOrNull()?.let { v -> if (w.unit == "m/s") v * 3.6 else v }
                },
                windDirectionDeg = cur.wind?.direction?.value?.toDoubleOrNull(),
                pressure = cur.pressure?.value?.toDoubleOrNull(),
                uvIndex = cur.uvIndex?.toIntOrNull(),
                visibility = cur.visibility?.value?.toDoubleOrNull(),
            )
        }

        val hourlyWind = r.forecastHourly?.wind?.value
        val hourlyAqi = r.forecastHourly?.aqi?.value
        val hourly = buildList {
            val temps = r.forecastHourly?.temperature?.value
            val codes = r.forecastHourly?.weather?.value
            val start = parseTimeMillis(r.forecastHourly?.temperature?.pubTime
                ?: r.forecastHourly?.pubTime).takeIf { it != 0L }
                ?: (System.currentTimeMillis() / 3_600_000L * 3_600_000L)
            val n = minOf(temps?.size ?: 0, codes?.size ?: 0, 24)
            for (i in 0 until n) {
                add(
                    HourlyWeather(
                        timeMillis = start + i * 3600_000L,
                        temperature = temps?.getOrNull(i)?.toDouble(),
                        condition = WeatherCondition.fromCode(codes?.getOrNull(i)?.toString()),
                        windSpeed = hourlyWind?.getOrNull(i)?.speed?.toDoubleOrNull(),
                        aqi = hourlyAqi?.getOrNull(i),
                    )
                )
            }
        }

        val daily = mapXiaomiDaily(r)

        val aqi = r.aqi?.let { a ->
            AqiInfo(
                value = a.aqi?.toIntOrNull(),
                level = aqiLevel(a.aqi?.toIntOrNull()),
                pm25 = a.pm25,
                pm10 = a.pm10,
                o3 = a.o3,
                no2 = a.no2,
                so2 = a.so2,
                co = a.co,
            )
        }

        val alerts = r.alerts?.map { a ->
            AlertInfo(
                title = a.title ?: "",
                detail = a.detail,
                level = a.level,
                pubTime = a.pubTime,
            )
        } ?: emptyList()

        return WeatherData(
            current = current,
            hourly = hourly,
            daily = daily,
            aqi = aqi,
            alerts = alerts,
            updateTime = parseTimeMillis(r.updateTime).takeIf { it != 0L } ?: System.currentTimeMillis(),
            rainNowcast = r.minutely?.precipitation?.description,
            carWashOk = r.indices?.indices?.firstOrNull { it.type == "carWash" }?.value?.let { it == "0" },
            sportsOk = r.indices?.indices?.firstOrNull { it.type == "sports" }?.value?.let { it == "0" },
            yesterday = r.yesterday?.let {
                YesterdayInfo(
                    high = it.tempMax?.toDoubleOrNull(),
                    low = it.tempMin?.toDoubleOrNull(),
                    aqi = it.aqi?.toIntOrNull(),
                    condition = WeatherCondition.fromCode(it.weatherEnd),
                )
            },
            typhoons = r.typhoon?.mapNotNull { t ->
                if (t.typhoonCname.isNullOrEmpty()) null
                else TyphoonInfo(
                    name = t.typhoonCname,
                    ename = t.typhoonEname,
                    type = t.typhoonType,
                    windSpeed = t.centWindSpeed,
                )
            } ?: emptyList(),
            dataSource = "XIAOMI",
        )
    }

    // 风向方位（度数 → 中文）
    fun windDirection(deg: Double?): String? {
        if (deg == null) return null
        val dirs = arrayOf("北", "东北", "东", "东南", "南", "西南", "西", "西北")
        val idx = (((deg + 22.5) / 45.0).toInt()) % 8
        return dirs[idx]
    }

    // 时刻（HH:mm）
    private val clockFmt = DateTimeFormatter.ofPattern("HH:mm")
    private fun formatClock(s: String?): String? {
        if (s.isNullOrEmpty()) return null
        return try {
            OffsetDateTime.parse(s).format(clockFmt)
        } catch (_: Exception) {
            null
        }
    }

    fun aqiLevel(value: Int?): String? = when {
        value == null -> null
        value <= 50 -> "优"
        value <= 100 -> "良"
        value <= 150 -> "轻度污染"
        value <= 200 -> "中度污染"
        value <= 300 -> "重度污染"
        else -> "严重污染"
    }

    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    // 当日 0 点（系统时区）：小米源 pubTime 解析失败时的日期兜底基准
    private fun todayStartMillis(): Long =
        java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()

    private fun parseTimeMillis(s: String?): Long {
        if (s.isNullOrEmpty()) return 0L
        return try {
            OffsetDateTime.parse(s, formatter).toInstant().toEpochMilli()
        } catch (_: Exception) {
            0L
        }
    }
}
