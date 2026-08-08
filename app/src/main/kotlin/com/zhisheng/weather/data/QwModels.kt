package com.zhisheng.weather.data

import kotlinx.serialization.Serializable

// 和风天气新版 API 响应模型（weather/v1、weatheralert/v1、airquality/v1）
// 与旧版 v7 模型（minutely / indices / geo）共存

@Serializable
data class QwVal(val value: Double? = null, val unit: String? = null)

// icon 才带昼夜变体（晴天白天 100 / 夜间 150），code 恒为白天码。
// 只读 code 会让夜里显示太阳（v0.0.2 修复），故两者都收，优先用 icon。
@Serializable
data class QwCondition(
    val text: String? = null,
    val code: String? = null,
    val icon: String? = null,
)

@Serializable
data class QwWindDir(val degree: Double? = null, val compass: String? = null)

@Serializable
data class QwWind(
    val direction: QwWindDir? = null,
    val speed: QwVal? = null,
    val scale: Int? = null,
)

@Serializable
data class QwPrecip(
    val amount: QwVal? = null,
    val probability: Int? = null,
    val type: String? = null,
)

@Serializable
data class QwCurrent(
    val condition: QwCondition? = null,
    val temperature: QwVal? = null,
    val feelsLike: QwVal? = null,
    val humidity: Double? = null,
    val wind: QwWind? = null,
    val windGust: QwVal? = null,
    val precipitation: QwPrecip? = null,
    val pressure: QwVal? = null,
    val visibility: QwVal? = null,
    val dewPoint: QwVal? = null,
    val cloudCover: Double? = null,
    val uvIndex: Int? = null,
)

@Serializable
data class QwHour(
    val forecastTime: String? = null,
    val condition: QwCondition? = null,
    val temperature: QwVal? = null,
    val wind: QwWind? = null,
    val precipitation: QwPrecip? = null,
)

@Serializable
data class QwHourly(val hours: List<QwHour> = emptyList())

@Serializable
data class QwAstro(
    val sunrise: String? = null,
    val sunset: String? = null,
    val moonrise: String? = null,
    val moonset: String? = null,
    val moonPhase: String? = null,
)

@Serializable
data class QwDayPeriod(
    val condition: QwCondition? = null,
    val wind: QwWind? = null,
    val precipitation: QwPrecip? = null,
)

@Serializable
data class QwDay(
    val forecastStartTime: String? = null,
    val astro: QwAstro? = null,
    val temperatureMax: QwVal? = null,
    val temperatureMin: QwVal? = null,
    val daytime: QwDayPeriod? = null,
)

@Serializable
data class QwDaily(val days: List<QwDay> = emptyList())

@Serializable
data class QwEventType(val name: String? = null, val code: String? = null)

@Serializable
data class QwAlert(
    val headline: String? = null,
    val description: String? = null,
    val severity: String? = null,
    val issuedTime: String? = null,
    val eventType: QwEventType? = null,
)

@Serializable
data class QwAlerts(val alerts: List<QwAlert> = emptyList())

@Serializable
data class QwPollutantConc(val value: Double? = null, val unit: String? = null)

@Serializable
data class QwPollutant(
    val code: String? = null,
    val concentration: QwPollutantConc? = null,
)

@Serializable
data class QwPrimaryPollutant(val code: String? = null, val name: String? = null)

@Serializable
data class QwAirIndex(
    val code: String? = null,
    val aqi: Int? = null,
    val level: String? = null,
    val category: String? = null,
    val primaryPollutant: QwPrimaryPollutant? = null,
)

@Serializable
data class QwAir(
    val indexes: List<QwAirIndex> = emptyList(),
    val pollutants: List<QwPollutant> = emptyList(),
)

// —— 旧版 v7 风格接口 ——

@Serializable
data class QwMinutelyItem(
    val fxTime: String? = null,
    val precip: String? = null,
    val type: String? = null,
)

@Serializable
data class QwMinutely(
    val code: String? = null,
    val summary: String? = null,
    val minutely: List<QwMinutelyItem> = emptyList(),
)

@Serializable
data class QwIndexItem(
    val type: String? = null,
    val name: String? = null,
    val level: String? = null,
    val category: String? = null,
)

@Serializable
data class QwIndices(
    val code: String? = null,
    val daily: List<QwIndexItem> = emptyList(),
)

@Serializable
data class QwCityLoc(
    val name: String? = null,
    val id: String? = null,
    val lat: String? = null,
    val lon: String? = null,
    val adm1: String? = null,
    val adm2: String? = null,
)

@Serializable
data class QwCityLookup(
    val code: String? = null,
    val location: List<QwCityLoc> = emptyList(),
)
