package com.zhisheng.weather.model

import kotlinx.serialization.Serializable

// 枳生天气 · UI 数据模型

@Serializable
data class City(
    val name: String,
    val affiliation: String,
    val latitude: Double,
    val longitude: Double,
    val locationKey: String,
)

data class CurrentWeather(
    val temperature: Double? = null,
    val feelsLike: Double? = null,
    val condition: WeatherCondition? = null,
    val weatherText: String? = null,
    val humidity: Double? = null,
    val windSpeed: Double? = null,
    val windDirectionDeg: Double? = null,
    val pressure: Double? = null,
    val uvIndex: Int? = null,
    val visibility: Double? = null,
    val dewPoint: Double? = null,
    val cloudCover: Double? = null,
    val windGust: Double? = null,
    val precipMm: Double? = null,
)

data class HourlyWeather(
    val timeMillis: Long,
    val temperature: Double? = null,
    val condition: WeatherCondition? = null,
    val windSpeed: Double? = null,
    val precipProb: Int? = null,
    val aqi: Int? = null,
)

data class MinutePrecip(
    val timeMillis: Long,
    val precip: Float,
)

data class YesterdayInfo(
    val high: Double? = null,
    val low: Double? = null,
    val aqi: Int? = null,
    val condition: WeatherCondition? = null,
)

data class TyphoonInfo(
    val name: String? = null,
    val ename: String? = null,
    val type: String? = null,
    val windSpeed: Double? = null,
)

data class DailyWeather(
    val dateMillis: Long,
    val high: Double? = null,
    val low: Double? = null,
    val condition: WeatherCondition? = null,
    val windSpeed: Double? = null,
    val precipProbability: Int? = null,
    val sunrise: String? = null,
    val sunset: String? = null,
    val moonrise: String? = null,
    val moonset: String? = null,
    val moonPhase: String? = null,
)

data class AqiInfo(
    val value: Int? = null,
    val level: String? = null,
    val primary: String? = null,
    val pm25: String? = null,
    val pm10: String? = null,
    val o3: String? = null,
    val no2: String? = null,
    val so2: String? = null,
    val co: String? = null,
)

data class LifeIndexExtra(
    val name: String,
    val en: String,
    val category: String,
)

data class AlertInfo(
    val title: String,
    val detail: String? = null,
    val level: String? = null,
    val pubTime: String? = null,
)

data class WeatherData(
    val current: CurrentWeather? = null,
    val hourly: List<HourlyWeather> = emptyList(),
    val daily: List<DailyWeather> = emptyList(),
    val aqi: AqiInfo? = null,
    val alerts: List<AlertInfo> = emptyList(),
    val updateTime: Long? = null,
    val rainNowcast: String? = null,
    val rainMinutes: List<MinutePrecip> = emptyList(),
    val carWashOk: Boolean? = null,
    val sportsOk: Boolean? = null,
    val extraIndices: List<LifeIndexExtra> = emptyList(),
    val yesterday: YesterdayInfo? = null,
    val typhoons: List<TyphoonInfo> = emptyList(),
    val dataSource: String? = null,
    val error: String? = null,
)

enum class WeatherCondition(val label: String) {
    CLEAR("晴"),
    CLEAR_NIGHT("晴"),
    PARTLY_CLOUDY("多云"),
    PARTLY_CLOUDY_NIGHT("多云"),
    CLOUDY("阴"),
    OVERCAST("阴"),
    RAIN("雨"),
    DRIZZLE("小雨"),
    THUNDERSTORM("雷阵雨"),
    SNOW("雪"),
    SLEET("雨夹雪"),
    FOG("雾"),
    HAZE("霾"),
    SAND("沙尘"),
    WIND("大风");

    companion object {
        fun fromCode(code: String?): WeatherCondition = when (code) {
            "0", "00" -> CLEAR
            "1", "01" -> PARTLY_CLOUDY
            "3", "7", "8", "9", "03", "07", "08", "09", "10", "11", "12", "21", "22", "23", "24", "25" -> RAIN
            "4", "04" -> THUNDERSTORM
            "5", "05" -> RAIN
            "6", "06", "19" -> SLEET
            "13", "14", "15", "16", "17", "26", "27", "28" -> SNOW
            "18", "32", "49", "57" -> FOG
            "20", "29", "30" -> WIND
            "53", "54", "55", "56" -> HAZE
            else -> CLOUDY
        }

        // 和风天气图标码 → 条件（1xx 白天 / 15x 夜间 / 3xx 雨 / 4xx 雪 / 5xx 视程）
        fun fromQwCode(code: String?): WeatherCondition = when (code) {
            "100" -> CLEAR
            "150" -> CLEAR_NIGHT
            "101", "102", "103" -> PARTLY_CLOUDY
            "151", "152", "153" -> PARTLY_CLOUDY_NIGHT
            "104" -> OVERCAST
            "302", "303" -> THUNDERSTORM
            "304" -> THUNDERSTORM
            "309", "399" -> DRIZZLE
            "300", "301", "305", "306", "307", "308", "310", "311", "312", "313",
            "314", "315", "316", "317", "318" -> RAIN
            "404", "405" -> SLEET
            "400", "401", "402", "403", "406", "407", "408", "409", "410", "499" -> SNOW
            "500", "501", "509", "510" -> FOG
            "503", "504", "507", "508" -> SAND
            "502", "511", "512", "513", "514", "515" -> HAZE
            else -> CLOUDY
        }
    }
}
