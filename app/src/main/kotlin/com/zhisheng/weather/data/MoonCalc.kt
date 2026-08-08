package com.zhisheng.weather.data

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.sin

// 本地月相计算（Meeus《天文算法》49 章，分秒级精度）——数据源缺 moonPhase 字段时的兜底。
// 计算朔/上弦/望/下弦的 UTC 时刻，标注约定与和风一致：
// 事件发生当日取事件名，其余日取最近已过事件的"时期名"，保证与和风源同日显示一致。
// 输出 key 与和风 moonPhase 相同，直接复用 Fmt.moonPhaseZh 翻译。
object MoonCalc {

    fun phaseKey(dateMillis: Long): String {
        // 日界按本地时区算：原来用本地日期却配 UTC 零点，窗口整体偏了一个时区偏移，
        // 东八区会把事件判到前一天（v0.0.2）
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(dateMillis).atZone(zone).toLocalDate()
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val events = eventsAround(dayStart)
        events.firstOrNull { it.first in dayStart until dayEnd }?.let { return it.second }
        val last = events.lastOrNull { it.first < dayStart } ?: return "waning-crescent"
        return when (last.second) {
            "new-moon" -> "waxing-crescent"
            "first-quarter" -> "waxing-gibbous"
            "full-moon" -> "waning-gibbous"
            else -> "waning-crescent"
        }
    }

    // 朔望月长度（毫秒）与 k=0 基准（2000-01-06 18:14 UTC 那次新月）
    private const val SYNODIC_MS = 2_551_442_877L
    private const val K0_EPOCH_MS = 946_728_000_000L + 476_040_000L

    // 目标日前后共 3 个朔望月的 12 个相位事件，升序（epochMillis → 事件名）
    private fun eventsAround(millis: Long): List<Pair<Long, String>> {
        // k 必须从 phaseMillis 的同一基准（2000 年那次新月）起算。
        // 原式先把 epoch 当作「1970 年起的儒略年数」再乘每年朔望月数，基准差了 30 年，
        // 算出的 k0≈700 对应 2056 年，12 个事件全部落在目标日之后 →
        // phaseKey 两个分支都落空，永远返回兜底的 "waning-crescent"（月相恒显示"残月"）。
        val k0 = Math.round((millis - K0_EPOCH_MS).toDouble() / SYNODIC_MS).toInt()
        val out = ArrayList<Pair<Long, String>>(20)
        for (k in k0 - 2..k0 + 2) {
            out.add(phaseMillis(k, 0.00) to "new-moon")
            out.add(phaseMillis(k, 0.25) to "first-quarter")
            out.add(phaseMillis(k, 0.50) to "full-moon")
            out.add(phaseMillis(k, 0.75) to "last-quarter")
        }
        return out.sortedBy { it.first }
    }

    // k=朔望月序号（2000-01 新月为 0），kind=相位偏移（0 朔 / .25 上弦 / .5 望 / .75 下弦）
    private fun phaseMillis(kInt: Int, kind: Double): Long {
        val k = kInt + kind
        val t = k / 1236.85
        var jde = 2451550.09766 + 29.530588861 * k + 0.00015437 * t * t -
            0.000000150 * t * t * t + 0.00000000073 * t * t * t * t
        val e = 1 - 0.002516 * t - 0.0000074 * t * t
        val m = rad(2.5534 + 29.10535670 * k - 0.0000014 * t * t)
        val mp = rad(201.5643 + 385.81693528 * k + 0.0107582 * t * t)
        val f = rad(160.7108 + 390.67050284 * k - 0.0016118 * t * t)
        val om = rad(124.7746 - 1.56375588 * k + 0.0020672 * t * t)
        val corr = when (kind) {
            0.00 -> -0.40720 * sin(mp) + 0.17241 * e * sin(m) + 0.01608 * sin(2 * mp) +
                0.01039 * sin(2 * f) + 0.00739 * e * sin(m - mp) - 0.00514 * e * sin(m + mp) +
                0.00208 * e * e * sin(2 * m) - 0.00111 * sin(mp - 2 * f) - 0.00057 * sin(mp + 2 * f) +
                0.00056 * e * sin(2 * m + mp) - 0.00042 * sin(3 * mp) + 0.00042 * e * sin(m + 2 * mp) +
                0.00038 * e * sin(m - 2 * mp) - 0.00024 * e * sin(2 * m - mp) - 0.00017 * sin(om) -
                0.00007 * sin(mp + 2 * m)
            0.50 -> -0.40614 * sin(mp) + 0.17303 * e * sin(m) + 0.01614 * sin(2 * mp) +
                0.01043 * sin(2 * f) + 0.00734 * e * sin(m - mp) - 0.00515 * e * sin(m + mp) +
                0.00209 * e * e * sin(2 * m) - 0.00111 * sin(mp - 2 * f) - 0.00057 * sin(mp + 2 * f) +
                0.00056 * e * sin(2 * m + mp) - 0.00042 * sin(3 * mp) + 0.00042 * e * sin(m + 2 * mp) +
                0.00038 * e * sin(m - 2 * mp) - 0.00024 * e * sin(2 * m - mp) - 0.00017 * sin(om) -
                0.00007 * sin(mp + 2 * m)
            else -> {
                val sgn = if (kind == 0.25) 1.0 else -1.0
                sgn * 0.00306 - 0.62801 * sin(mp) + 0.17172 * e * sin(m) -
                    0.01183 * e * sin(mp + m) + 0.00862 * sin(2 * mp) + 0.00804 * sin(2 * f) +
                    0.00454 * e * sin(m - mp) + 0.00204 * e * e * sin(2 * m) -
                    0.00180 * sin(mp - 2 * f) - 0.00070 * sin(mp + 2 * f) - 0.00040 * sin(3 * mp) -
                    0.00034 * e * sin(2 * mp - m) + 0.00032 * e * sin(m + 2 * mp) +
                    0.00032 * e * sin(m - 2 * mp) - 0.00028 * e * e * sin(mp + 2 * m) +
                    0.00027 * e * sin(2 * m - mp) - 0.00017 * sin(om)
            }
        }
        jde += corr
        // JDE → UTC epoch（ΔT≈70s 忽略，不影响日界判定）；J2000.0 = 2000-01-01T12:00Z
        return ((jde - 2451545.0) * 86_400_000.0).toLong() + 946_728_000_000L
    }

    private fun rad(deg: Double): Double = deg * Math.PI / 180.0
}
