package com.zhisheng.weather.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.withFrameNanos
import com.zhisheng.weather.data.AmbienceLevel
import com.zhisheng.weather.model.WeatherCondition
import com.zhisheng.weather.ui.theme.ZhishengCyan
import com.zhisheng.weather.ui.theme.ZhishengMint
import com.zhisheng.weather.ui.theme.ZhishengText
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════
// 天气氛围层（v0.0.2）
// 设计约束：只在**内容之下**绘制，不拦触摸，透明度上限极低。
// 信息永远优先——氛围只是背景里的一层呼吸感，不抢读数。
//   雨   → 数据雨（下落的 0/1 字符列，NERV 终端感）
//   雪   → 飘点（缓慢横向漂移的圆点）
//   雾   → 呼吸噪点（整体明暗缓慢起伏的稀疏点阵）
//   雷暴 → 扫描线（偶发一道横向亮线扫过 + 极淡闪白）
// ═══════════════════════════════════════════════════════════

private enum class AmbienceKind { NONE, RAIN, SNOW, FOG, STORM }

private fun kindOf(c: WeatherCondition?): AmbienceKind = when (c) {
    WeatherCondition.RAIN, WeatherCondition.DRIZZLE, WeatherCondition.SLEET -> AmbienceKind.RAIN
    WeatherCondition.SNOW -> AmbienceKind.SNOW
    WeatherCondition.FOG, WeatherCondition.HAZE, WeatherCondition.SAND -> AmbienceKind.FOG
    WeatherCondition.THUNDERSTORM -> AmbienceKind.STORM
    else -> AmbienceKind.NONE
}

// 单个粒子：用确定性伪随机生成，避免每帧分配
private class Particle(seed: Int, val cols: Int) {
    val col = seed % cols
    val speed = 0.35f + (seed * 37 % 100) / 100f * 0.75f
    val phase = (seed * 61 % 100) / 100f
    val len = 3 + (seed * 13 % 5)
    val drift = ((seed * 29 % 100) / 100f - 0.5f) * 2f
}

@Composable
fun WeatherAmbience(
    condition: WeatherCondition?,
    level: AmbienceLevel,
    modifier: Modifier = Modifier,
) {
    val kind = kindOf(condition)
    if (level == AmbienceLevel.OFF || kind == AmbienceKind.NONE) return

    // 单一驱动时钟：所有效果共用一个 withFrameNanos 循环，
    // 不为每个粒子起协程（v0.0.2：避免 while(true) 满天飞）
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(kind) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) t += (now - last) / 1_000_000_000f
                last = now
            }
        }
    }

    val density = LocalDensity.current
    val f = level.factor
    val particles = remember(kind) { List(if (kind == AmbienceKind.SNOW) 34 else 46) { Particle(it * 7919 + 13, 22) } }
    val monoTypeface = remember { android.graphics.Typeface.MONOSPACE }

    Canvas(modifier = modifier.fillMaxSize()) {
        when (kind) {
            AmbienceKind.RAIN -> drawDataRain(t, f, particles, density.density, monoTypeface)
            AmbienceKind.SNOW -> drawSnow(t, f, particles)
            AmbienceKind.FOG -> drawFogNoise(t, f)
            AmbienceKind.STORM -> drawStormScan(t, f)
            AmbienceKind.NONE -> Unit
        }
    }
}

// —— 数据雨：0/1 字符成列下落，尾部渐隐 ——
private fun DrawScope.drawDataRain(
    t: Float,
    f: Float,
    particles: List<Particle>,
    densityScale: Float,
    typeface: android.graphics.Typeface,
) {
    val colW = size.width / 22f
    val glyph = 11f * densityScale
    val paint = android.graphics.Paint().apply {
        this.typeface = typeface
        textSize = glyph
        isAntiAlias = true
    }
    val baseAlpha = 0.052f * f
    particles.forEach { p ->
        val cycle = size.height + p.len * glyph * 1.4f
        val y = ((t * p.speed * 190f + p.phase * cycle) % cycle)
        val x = p.col * colW + colW * 0.28f
        for (i in 0 until p.len) {
            val yy = y - i * glyph * 1.4f
            if (yy < -glyph || yy > size.height) continue
            // 头字符最亮，向尾渐隐
            val a = baseAlpha * (1f - i.toFloat() / p.len)
            paint.color = ZhishengMint.copy(alpha = a.coerceIn(0f, 1f)).toArgb()
            // 字符按位置确定性取 0/1，不每帧随机（避免闪烁噪声）
            val ch = if (((p.col * 31 + i * 17 + (y / glyph).toInt()) % 2) == 0) "0" else "1"
            drawContext.canvas.nativeCanvas.drawText(ch, x, yy, paint)
        }
    }
}

// —— 飘雪：缓慢下落 + 正弦横向漂移 ——
private fun DrawScope.drawSnow(t: Float, f: Float, particles: List<Particle>) {
    val colW = size.width / 22f
    particles.forEach { p ->
        val cycle = size.height + 40f
        val y = ((t * p.speed * 42f + p.phase * cycle) % cycle)
        val sway = sin((t * 0.55f + p.phase * 6.28f).toDouble()).toFloat() * 13f * p.drift
        val x = p.col * colW + colW * 0.5f + sway
        val r = 1.15f + (p.len % 3) * 0.5f
        drawCircle(
            color = ZhishengText.copy(alpha = (0.085f * f).coerceIn(0f, 1f)),
            radius = r,
            center = Offset(x, y),
        )
    }
}

// —— 雾：稀疏点阵整体呼吸（明暗缓慢起伏） ——
private fun DrawScope.drawFogNoise(t: Float, f: Float) {
    // 呼吸周期约 7 秒
    val breathe = (sin((t * 0.9f).toDouble()).toFloat() + 1f) / 2f
    val alpha = (0.03f + 0.045f * breathe) * f
    val step = 26f
    var y = 0f
    var row = 0
    while (y < size.height) {
        var x = ((row % 2) * step / 2f)
        while (x < size.width) {
            // 位置确定性抖动，避免规则网格感
            val jx = ((row * 31 + (x / step).toInt() * 17) % 7 - 3).toFloat()
            val jy = ((row * 13 + (x / step).toInt() * 29) % 7 - 3).toFloat()
            drawCircle(
                color = Color.White.copy(alpha = alpha.coerceIn(0f, 1f)),
                radius = 1.05f,
                center = Offset(x + jx, y + jy),
            )
            x += step
        }
        y += step
        row++
    }
}

// —— 雷暴：一道扫描线周期性下扫，偶发极淡闪白 ——
private fun DrawScope.drawStormScan(t: Float, f: Float) {
    val period = 4.2f
    val local = t % period
    // 扫描线只在周期前 1.5s 出现
    if (local < 1.5f) {
        val prog = local / 1.5f
        val y = prog * size.height
        val bandH = 78f
        // 主线 + 上方拖尾
        drawRect(
            color = ZhishengCyan.copy(alpha = (0.05f * f).coerceIn(0f, 1f)),
            topLeft = Offset(0f, y - bandH),
            size = Size(size.width, bandH),
        )
        drawLine(
            color = ZhishengCyan.copy(alpha = (0.14f * f).coerceIn(0f, 1f)),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.4f,
        )
    }
    // 闪白：周期末尾两次短促脉冲
    val flashAt = period - 0.55f
    if (local > flashAt) {
        val d = local - flashAt
        val pulse = if (d < 0.08f) 1f else if (d in 0.18f..0.24f) 0.6f else 0f
        if (pulse > 0f) {
            drawRect(color = Color.White.copy(alpha = (0.030f * f * pulse).coerceIn(0f, 1f)))
        }
    }
}

