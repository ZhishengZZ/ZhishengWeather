package com.zhisheng.weather.ui

import kotlin.math.roundToInt

// 显示格式工具：温度单位换算在此统一生效（c=摄氏 f=华氏）
object Fmt {

    fun temp(celsius: Double?, unit: String): String? = celsius?.let {
        (if (unit == "f") it * 9.0 / 5.0 + 32.0 else it).roundToInt().toString()
    }

    fun unitSuffix(unit: String): String = if (unit == "f") "°F" else "°C"

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
