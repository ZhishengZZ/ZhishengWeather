package com.zhisheng.weather.ui

import java.util.Locale
import kotlin.math.roundToInt

// 显示格式工具：温度/风速/气压单位换算在此统一生效
object Fmt {

    fun temp(celsius: Double?, unit: String): String? = celsius?.let {
        (if (unit == "f") it * 9.0 / 5.0 + 32.0 else it).roundToInt().toString()
    }

    fun unitSuffix(unit: String): String = if (unit == "f") "°F" else "°C"

    // 内部一律以 km/h 存储，展示时换算
    fun wind(kmh: Double?, unit: String): String? = kmh?.let {
        when (unit) {
            "ms" -> String.format(Locale.US, "%.1f m/s", it / 3.6)
            "bft" -> "${beaufort(it)} 级"
            else -> "${it.roundToInt()} km/h"
        }
    }

    // 只要数值，用于逐时那种空间紧张的地方
    fun windValue(kmh: Double?, unit: String): String? = kmh?.let {
        when (unit) {
            "ms" -> String.format(Locale.US, "%.1f", it / 3.6)
            "bft" -> beaufort(it).toString()
            else -> it.roundToInt().toString()
        }
    }

    fun windUnitLabel(unit: String): String = when (unit) {
        "ms" -> "风速 m/s"
        "bft" -> "风力 级"
        else -> "风速 km/h"
    }

    // 蒲福风级（按 km/h 上界切分）
    private fun beaufort(kmh: Double): Int = when {
        kmh < 1.0 -> 0
        kmh < 6.0 -> 1
        kmh < 12.0 -> 2
        kmh < 20.0 -> 3
        kmh < 29.0 -> 4
        kmh < 39.0 -> 5
        kmh < 50.0 -> 6
        kmh < 62.0 -> 7
        kmh < 75.0 -> 8
        kmh < 89.0 -> 9
        kmh < 103.0 -> 10
        kmh < 118.0 -> 11
        else -> 12
    }

    // 内部以 hPa 存储
    fun pressure(hpa: Double?, unit: String): String? = hpa?.let {
        when (unit) {
            "mmhg" -> String.format(Locale.US, "%.0f mmHg", it * 0.750062)
            "inhg" -> String.format(Locale.US, "%.2f inHg", it * 0.02953)
            else -> "${it.roundToInt()} hPa"
        }
    }

    // 和风月相 ID → 中文（该字段不随 lang 参数翻译，需本地映射）
    fun moonPhaseZh(en: String?): String? = en?.let {
        when (it.lowercase().replace(' ', '-')) {
            "new-moon" -> "新月"
            "waxing-crescent" -> "娥眉月"
            "first-quarter" -> "上弦月"
            "waxing-gibbous" -> "盈凸月"
            "full-moon" -> "满月"
            "waning-gibbous" -> "亏凸月"
            "last-quarter" -> "下弦月"
            "waning-crescent" -> "残月"
            else -> en
        }
    }
}
