package com.zhisheng.weather.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.zhisheng.weather.R
import com.zhisheng.weather.model.WeatherCondition

// 图标资源映射（平面双色调 PNG，颜色已内置于位图，不再统一染色）
private val ICON_RES = mapOf(
    WeatherCondition.CLEAR to R.drawable.weather_sun,
    WeatherCondition.CLEAR_NIGHT to R.drawable.weather_moon,
    WeatherCondition.PARTLY_CLOUDY to R.drawable.weather_cloud_sun,
    WeatherCondition.PARTLY_CLOUDY_NIGHT to R.drawable.weather_cloud_moon,
    WeatherCondition.CLOUDY to R.drawable.weather_cloud,
    WeatherCondition.OVERCAST to R.drawable.weather_clouds,
    WeatherCondition.RAIN to R.drawable.weather_rain,
    WeatherCondition.DRIZZLE to R.drawable.weather_drizzle,
    WeatherCondition.THUNDERSTORM to R.drawable.weather_bolt,
    WeatherCondition.SNOW to R.drawable.weather_snow,
    WeatherCondition.SLEET to R.drawable.weather_sleet,
    WeatherCondition.FOG to R.drawable.weather_fog,
    WeatherCondition.HAZE to R.drawable.weather_haze,
    WeatherCondition.SAND to R.drawable.weather_sand,
    WeatherCondition.WIND to R.drawable.weather_wind,
)

@Composable
fun WeatherIcon(
    condition: WeatherCondition?,
    modifier: Modifier = Modifier,
) {
    val res = condition?.let { ICON_RES[it] }
    if (res != null) {
        Image(
            painter = painterResource(res),
            contentDescription = condition?.label,
            modifier = modifier,
        )
    }
}
