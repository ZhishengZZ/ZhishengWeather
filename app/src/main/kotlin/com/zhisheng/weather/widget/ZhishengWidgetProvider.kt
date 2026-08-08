package com.zhisheng.weather.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.zhisheng.weather.MainActivity
import com.zhisheng.weather.R
import com.zhisheng.weather.data.WidgetCache
import com.zhisheng.weather.data.WidgetSnapshot
import com.zhisheng.weather.model.WeatherCondition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// 终端风桌面小组件（v0.0.2）
// 三个 Provider = 桌面选择器里三个独立条目（2x2 / 4x2 / 4x4）；
// 每个仍可拉伸，布局按实际尺寸自适应。
// 数据来自 WidgetCache（主 App 抓取后写入），小组件本身不发网络请求。
open class ZhishengWidgetProvider : AppWidgetProvider() {

    // 子类固定档位；null = 按实际尺寸自适应
    protected open val forcedLayout: Int? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id -> renderAsync(context, manager, id) }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        id: Int,
        newOptions: Bundle?,
    ) {
        renderAsync(context, manager, id)
    }

    private fun renderAsync(context: Context, manager: AppWidgetManager, id: Int) {
        val pending = goAsync()
        scope.launch {
            try {
                val snap = WidgetCache.load(context)
                val views = build(context, manager, id, snap)
                manager.updateAppWidget(id, views)
            } catch (_: Exception) {
                // 渲染失败就保持上一帧，不要把小组件刷成空白
            } finally {
                pending.finish()
            }
        }
    }

    private fun build(
        context: Context,
        manager: AppWidgetManager,
        id: Int,
        snap: WidgetSnapshot?,
    ): RemoteViews {
        val opts = manager.getAppWidgetOptions(id)
        val minW = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
        val minH = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)

        val layout = forcedLayout ?: when {
            minW >= 250 && minH >= 250 -> R.layout.widget_large
            minW >= 250 -> R.layout.widget_medium
            else -> R.layout.widget_small
        }
        val v = RemoteViews(context.packageName, layout)

        // 整块点击进 App
        val intent = Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        v.setOnClickPendingIntent(
            R.id.widget_root,
            PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )

        if (snap == null || snap.temp == null) {
            v.setTextViewText(R.id.w_city, "枳生天气")
            v.setTextViewText(R.id.w_temp, "--")
            v.setTextViewText(R.id.w_range, "打开 App 同步数据")
            return v
        }

        v.setTextViewText(R.id.w_city, snap.city.ifBlank { "枳生天气" })
        v.setTextViewText(R.id.w_temp, "${snap.temp}°")
        v.setTextViewText(
            R.id.w_range,
            buildString {
                if (snap.text.isNotBlank()) append(snap.text)
                if (snap.high != null && snap.low != null) {
                    if (isNotEmpty()) append("  ")
                    append("${snap.high}°/${snap.low}°")
                }
            },
        )
        v.setImageViewResource(R.id.w_icon, iconRes(snap.conditionName))
        v.setTextViewText(R.id.w_upd, "UPD ${clock(snap.updateMillis)}")

        if (layout != R.layout.widget_small) {
            val hourIds = listOf(
                Triple(R.id.h1_t, R.id.h1_i, R.id.h1_v),
                Triple(R.id.h2_t, R.id.h2_i, R.id.h2_v),
                Triple(R.id.h3_t, R.id.h3_i, R.id.h3_v),
                Triple(R.id.h4_t, R.id.h4_i, R.id.h4_v),
            )
            hourIds.forEachIndexed { i, (tId, iId, vId) ->
                val h = snap.hours.getOrNull(i)
                if (h == null) {
                    v.setTextViewText(tId, "")
                    v.setTextViewText(vId, "")
                } else {
                    v.setTextViewText(tId, h.label)
                    v.setTextViewText(vId, h.temp?.let { "$it°" } ?: "--")
                    v.setImageViewResource(iId, iconRes(h.conditionName))
                }
            }
        }

        if (layout == R.layout.widget_large) {
            val dayIds = listOf(
                Triple(R.id.d1_t, R.id.d1_i, R.id.d1_v),
                Triple(R.id.d2_t, R.id.d2_i, R.id.d2_v),
                Triple(R.id.d3_t, R.id.d3_i, R.id.d3_v),
            )
            dayIds.forEachIndexed { i, (tId, iId, vId) ->
                val d = snap.days.getOrNull(i)
                if (d == null) {
                    v.setTextViewText(tId, "")
                    v.setTextViewText(vId, "")
                } else {
                    v.setTextViewText(tId, d.label)
                    v.setTextViewText(
                        vId,
                        if (d.high != null && d.low != null) "${d.low}° ~ ${d.high}°" else "--",
                    )
                    v.setImageViewResource(iId, iconRes(d.conditionName))
                }
            }
            if (snap.aqi != null) {
                v.setViewVisibility(R.id.w_aqi, View.VISIBLE)
                v.setTextViewText(R.id.w_aqi, "AQI ${snap.aqi} ${snap.aqiLevel}")
            } else {
                v.setViewVisibility(R.id.w_aqi, View.GONE)
            }
        }
        return v
    }

    private fun clock(ms: Long): String =
        if (ms <= 0) "--" else java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
            .format(java.util.Date(ms))

    private fun iconRes(name: String): Int = when (runCatching {
        WeatherCondition.valueOf(name)
    }.getOrNull()) {
        WeatherCondition.CLEAR -> R.drawable.weather_sun
        WeatherCondition.CLEAR_NIGHT -> R.drawable.weather_moon
        WeatherCondition.PARTLY_CLOUDY -> R.drawable.weather_cloud_sun
        WeatherCondition.PARTLY_CLOUDY_NIGHT -> R.drawable.weather_cloud_moon
        WeatherCondition.CLOUDY -> R.drawable.weather_cloud
        WeatherCondition.OVERCAST -> R.drawable.weather_clouds
        WeatherCondition.RAIN -> R.drawable.weather_rain
        WeatherCondition.DRIZZLE -> R.drawable.weather_drizzle
        WeatherCondition.THUNDERSTORM -> R.drawable.weather_bolt
        WeatherCondition.SNOW -> R.drawable.weather_snow
        WeatherCondition.SLEET -> R.drawable.weather_sleet
        WeatherCondition.FOG -> R.drawable.weather_fog
        WeatherCondition.HAZE -> R.drawable.weather_haze
        WeatherCondition.SAND -> R.drawable.weather_sand
        WeatherCondition.WIND -> R.drawable.weather_wind
        null -> R.drawable.weather_cloud
    }

    companion object {
        // 主 App 抓到新数据后调用，立即刷新所有已放置的小组件（三个规格都刷）
        fun refreshAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            listOf(
                ZhishengWidgetSmall::class.java,
                ZhishengWidgetMedium::class.java,
                ZhishengWidgetLarge::class.java,
            ).forEach { cls ->
                val ids = mgr.getAppWidgetIds(ComponentName(context, cls))
                if (ids.isNotEmpty()) {
                    context.sendBroadcast(
                        Intent(context, cls)
                            .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    )
                }
            }
        }
    }
}

class ZhishengWidgetSmall : ZhishengWidgetProvider() {
    override val forcedLayout = R.layout.widget_small
}

class ZhishengWidgetMedium : ZhishengWidgetProvider() {
    override val forcedLayout = R.layout.widget_medium
}

class ZhishengWidgetLarge : ZhishengWidgetProvider() {
    override val forcedLayout = R.layout.widget_large
}
