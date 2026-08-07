package com.zhisheng.weather.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// EVA · 固定暗色
private val ZhishengColorScheme = darkColorScheme(
    primary = ZhishengMint,
    onPrimary = Color(0xFF001500),
    primaryContainer = Color(0xFF003300),
    onPrimaryContainer = Color(0xFFB0FFB0),
    secondary = ZhishengCyan,
    onSecondary = Color(0xFF003333),
    background = ZhishengBg,
    onBackground = ZhishengText,
    surface = ZhishengSurface,
    onSurface = ZhishengText,
    surfaceVariant = ZhishengCard,
    onSurfaceVariant = ZhishengTextSecondary,
    outline = ZhishengCardBorder,
    outlineVariant = ZhishengCardBorder,
    error = ZhishengRed,
)

@Composable
fun ZhishengWeatherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ZhishengColorScheme,
        typography = ZhishengTypography,
        content = content,
    )
}
