package com.zhisheng.weather.data

import kotlinx.serialization.Serializable

// 小米天气 API 响应模型（精简为需要的字段）

@Serializable
data class XiaomiUnitValue(
    val unit: String? = null,
    val value: String? = null,
)

@Serializable
data class XiaomiWind(
    val direction: XiaomiUnitValue? = null,
    val speed: XiaomiUnitValue? = null,
)

@Serializable
data class XiaomiCurrent(
    val feelsLike: XiaomiUnitValue? = null,
    val humidity: XiaomiUnitValue? = null,
    val pressure: XiaomiUnitValue? = null,
    val pubTime: String? = null,
    val temperature: XiaomiUnitValue? = null,
    val uvIndex: String? = null,
    val visibility: XiaomiUnitValue? = null,
    val weather: String? = null,
    val wind: XiaomiWind? = null,
)

@Serializable
data class XiaomiFromTo(
    val from: String? = null,
    val to: String? = null,
)

@Serializable
data class XiaomiDailyTemperature(
    val unit: String? = null,
    val value: List<XiaomiFromTo>? = null,
)

@Serializable
data class XiaomiDailyWeather(
    val value: List<XiaomiFromTo>? = null,
)

@Serializable
data class XiaomiForecastDaily(
    val pubTime: String? = null,
    val temperature: XiaomiDailyTemperature? = null,
    val weather: XiaomiDailyWeather? = null,
    val sunRiseSet: XiaomiFromToList? = null,
    val precipitationProbability: XiaomiStrList? = null,
    val wind: XiaomiDailyWind? = null,
)

@Serializable
data class XiaomiFromToList(val value: List<XiaomiFromTo>? = null)

@Serializable
data class XiaomiStrList(val value: List<String>? = null)

@Serializable
data class XiaomiDailyWind(
    val direction: XiaomiFromToList? = null,
    val speed: XiaomiFromToList? = null,
)

@Serializable
data class XiaomiHourlyWindValue(
    val datetime: String? = null,
    val direction: String? = null,
    val speed: String? = null,
)

@Serializable
data class XiaomiHourlyWind(val value: List<XiaomiHourlyWindValue>? = null)

@Serializable
data class XiaomiIntList(
    val pubTime: String? = null,
    val value: List<Int>? = null,
)

@Serializable
data class XiaomiForecastHourly(
    val pubTime: String? = null,
    val temperature: XiaomiIntList? = null,
    val weather: XiaomiIntList? = null,
    val wind: XiaomiHourlyWind? = null,
    val aqi: XiaomiIntList? = null,
)

@Serializable
data class XiaomiAqi(
    val aqi: String? = null,
    val pm25: String? = null,
    val pm10: String? = null,
    val o3: String? = null,
    val no2: String? = null,
    val so2: String? = null,
    val co: String? = null,
    val pubTime: String? = null,
    val primary: String? = null,
    val suggest: String? = null,
)

@Serializable
data class XiaomiAlert(
    val title: String? = null,
    val level: String? = null,
    val type: String? = null,
    val detail: String? = null,
    val pubTime: String? = null,
)

@Serializable
data class XiaomiForecastResult(
    val current: XiaomiCurrent? = null,
    val forecastDaily: XiaomiForecastDaily? = null,
    val forecastHourly: XiaomiForecastHourly? = null,
    val aqi: XiaomiAqi? = null,
    val alerts: List<XiaomiAlert>? = null,
    val updateTime: String? = null,
    val minutely: XiaomiMinutely? = null,
    val indices: XiaomiIndices? = null,
    val yesterday: XiaomiYesterday? = null,
    val typhoon: List<XiaomiTyphoon>? = null,
)

@Serializable
data class XiaomiMinutely(
    val precipitation: XiaomiMinutelyPrecip? = null,
)

@Serializable
data class XiaomiMinutelyPrecip(
    val description: String? = null,
    val kmNum: String? = null,
)

@Serializable
data class XiaomiIndices(
    val indices: List<XiaomiIndexItem>? = null,
)

@Serializable
data class XiaomiIndexItem(
    val type: String? = null,
    val value: String? = null,
)

@Serializable
data class XiaomiYesterday(
    val date: String? = null,
    val tempMax: String? = null,
    val tempMin: String? = null,
    val aqi: String? = null,
    val weatherStart: String? = null,
    val weatherEnd: String? = null,
)

@Serializable
data class XiaomiTyphoon(
    val typhoonCname: String? = null,
    val typhoonEname: String? = null,
    val typhoonCode: String? = null,
    val typhoonType: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val centWindSpeed: Double? = null,
)

@Serializable
data class XiaomiLocationResult(
    val name: String? = null,
    val affiliation: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val locationKey: String? = null,
    val status: Int? = null,
)
