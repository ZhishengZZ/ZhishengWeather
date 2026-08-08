package com.zhisheng.weather.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhisheng.weather.data.AmbienceLevel
import com.zhisheng.weather.data.LocationSource
import com.zhisheng.weather.data.QWeatherApi
import com.zhisheng.weather.data.SettingsRepository
import com.zhisheng.weather.data.SourcePref
import com.zhisheng.weather.ui.theme.ZhishengBg
import com.zhisheng.weather.ui.theme.ZhishengCard
import com.zhisheng.weather.ui.theme.ZhishengCardBorder
import com.zhisheng.weather.ui.theme.ZhishengCyan
import com.zhisheng.weather.ui.theme.ZhishengMint
import com.zhisheng.weather.ui.theme.ZhishengOrange
import com.zhisheng.weather.ui.theme.ZhishengRed
import com.zhisheng.weather.ui.theme.ZhishengSurface
import com.zhisheng.weather.ui.theme.ZhishengText
import com.zhisheng.weather.ui.theme.ZhishengTextSecondary
import com.zhisheng.weather.ui.theme.ZhishengTextTertiary
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════
// 设置（v0.0.2 重做）
// 01 数据源（可选：自动/和风/小米/公共源）  02 定位（默认关，严格可选）
// 03 单位  04 显示模块  05 界面效果  06 关于
// ═══════════════════════════════════════════════════════════

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLocate: () -> Unit,
    locating: Boolean,
    locateMessage: String?,
    onClearLocateMessage: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val tempUnit by SettingsRepository.tempUnit.collectAsState(initial = "c")
    val windUnit by SettingsRepository.windUnit.collectAsState(initial = "kmh")
    val pressureUnit by SettingsRepository.pressureUnit.collectAsState(initial = "hpa")
    val showTyphoon by SettingsRepository.showTyphoon.collectAsState(initial = true)
    val source by SettingsRepository.sourcePref.collectAsState(initial = SourcePref.AUTO)
    val ambience by SettingsRepository.ambience.collectAsState(initial = AmbienceLevel.SUBTLE)
    val scanlines by SettingsRepository.scanlines.collectAsState(initial = true)
    val locationEnabled by SettingsRepository.locationEnabled.collectAsState(initial = false)
    val showAqi by SettingsRepository.showAqi.collectAsState(initial = true)
    val showIndices by SettingsRepository.showIndices.collectAsState(initial = true)
    val showYesterday by SettingsRepository.showYesterday.collectAsState(initial = true)
    val showPrecip by SettingsRepository.showPrecip.collectAsState(initial = true)
    val showTelemetry by SettingsRepository.showTelemetry.collectAsState(initial = true)
    val bootAnim by SettingsRepository.bootAnim.collectAsState(initial = true)
    val keepScreenOn by SettingsRepository.keepScreenOn.collectAsState(initial = false)

    var permDenied by remember { mutableStateOf(false) }

    // 权限申请器：只在用户点「定位当前城市」时触发，App 启动/刷新绝不调用
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permDenied = false
            onLocate()
        } else {
            permDenied = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(ZhishengBg)
            .statusBarsPadding().navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = ZhishengText)
            }
            Column {
                Text("设置", style = MaterialTheme.typography.titleMedium, color = ZhishengOrange, fontWeight = FontWeight.Bold)
                Text(
                    "SYSTEM CONFIG",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZhishengTextTertiary,
                    letterSpacing = 1.5.sp,
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // ——— 01 数据源 ———
            SectionTitle(1, "数据源", "DATA SOURCE")
            Hint("选「自动优选」按可用性降级；装不上和风凭据的话，直接锁「Open-Meteo」就有完整体验。")
            CardBox {
                SourcePref.entries.forEachIndexed { i, p ->
                    if (i > 0) HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                    SourceRow(
                        pref = p,
                        selected = source == p,
                        status = sourceStatus(p),
                        onClick = { scope.launch { SettingsRepository.setSourcePref(p) } },
                    )
                }
            }

            // ——— 02 定位 ———
            SectionTitle(2, "定位", "LOCATION")
            Hint(
                if (locationEnabled) "已开启。仅在你点下方按钮时申请一次粗略位置权限，平时不会读取位置。"
                else "关闭状态下 App 不申请、也不读取任何位置权限。"
            )
            CardBox {
                ToggleRow(
                    "定位当前城市",
                    if (locationEnabled) "开启·按需申请粗略位置" else "关闭·不申请任何位置权限",
                    locationEnabled,
                ) {
                    scope.launch {
                        SettingsRepository.setLocationEnabled(!locationEnabled)
                        if (locationEnabled) onClearLocateMessage()
                    }
                }
                if (locationEnabled) {
                    HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                    ActionRow(
                        label = if (locating) "定位中 ..." else "⌖ 定位当前城市",
                        enabled = !locating,
                        color = ZhishengMint,
                    ) {
                        onClearLocateMessage()
                        if (LocationSource.hasPermission(context)) onLocate()
                        else permLauncher.launch(LocationSource.PERMISSION)
                    }
                    locateMessage?.let { msg ->
                        HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                        Text(
                            "> $msg",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (msg.startsWith("已定位")) ZhishengMint else ZhishengOrange,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                    if (permDenied) {
                        HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                "> 已拒绝位置权限，定位不可用（手动搜索城市不受影响）",
                                style = MaterialTheme.typography.labelMedium,
                                color = ZhishengOrange,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "[ 去系统设置授权 ]",
                                style = MaterialTheme.typography.labelMedium,
                                color = ZhishengCyan,
                                modifier = Modifier
                                    .clickable(role = Role.Button) { openAppSettings(context) }
                                    .padding(vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            // ——— 03 单位 ———
            SectionTitle(3, "单位", "UNITS")
            CardBox {
                SegmentRow(
                    "温度", listOf("摄氏 °C" to "c", "华氏 °F" to "f"), tempUnit,
                ) { scope.launch { SettingsRepository.setTempUnit(it) } }
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                SegmentRow(
                    "风速", listOf("km/h" to "kmh", "m/s" to "ms", "级" to "bft"), windUnit,
                ) { scope.launch { SettingsRepository.setWindUnit(it) } }
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                SegmentRow(
                    "气压", listOf("hPa" to "hpa", "mmHg" to "mmhg", "inHg" to "inhg"), pressureUnit,
                ) { scope.launch { SettingsRepository.setPressureUnit(it) } }
            }

            // ——— 04 显示模块 ———
            SectionTitle(4, "显示模块", "MODULES")
            Hint("关掉用不上的区块，主屏更短。")
            CardBox {
                ToggleRow("分钟降水", "未来两小时降水柱图", showPrecip) {
                    scope.launch { SettingsRepository.setShowPrecip(!showPrecip) }
                }
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                ToggleRow("遥测数据", "湿度/风/气压/能见度等", showTelemetry) {
                    scope.launch { SettingsRepository.setShowTelemetry(!showTelemetry) }
                }
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                ToggleRow("空气质量", "AQI 与六项污染物", showAqi) {
                    scope.launch { SettingsRepository.setShowAqi(!showAqi) }
                }
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                ToggleRow("生活指数", "洗车/运动/穿衣/感冒", showIndices) {
                    scope.launch { SettingsRepository.setShowIndices(!showIndices) }
                }
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                ToggleRow("昨日复盘", "昨日高低温与温差", showYesterday) {
                    scope.launch { SettingsRepository.setShowYesterday(!showYesterday) }
                }
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                ToggleRow("台风关注", "台风实时动态（辅助源，可能为空）", showTyphoon) {
                    scope.launch { SettingsRepository.setShowTyphoon(!showTyphoon) }
                }
            }

            // ——— 05 界面效果 ———
            SectionTitle(5, "界面效果", "VISUAL")
            Hint("氛围层只在背景绘制，不遮挡读数；嫌费电可以关。")
            CardBox {
                SegmentRow(
                    "天气氛围层",
                    listOf("关闭" to "off", "克制" to "subtle", "明显" to "vivid"),
                    ambience.key,
                ) { v -> scope.launch { SettingsRepository.setAmbience(AmbienceLevel.from(v)) } }
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                ToggleRow("CRT 扫描线", "整屏细横纹，终端质感", scanlines) {
                    scope.launch { SettingsRepository.setScanlines(!scanlines) }
                }
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                ToggleRow("开机自检动画", "启动时的终端打字序列", bootAnim) {
                    scope.launch { SettingsRepository.setBootAnim(!bootAnim) }
                }
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                ToggleRow("常亮屏幕", "看天气时不自动息屏", keepScreenOn) {
                    scope.launch { SettingsRepository.setKeepScreenOn(!keepScreenOn) }
                }
            }

            // ——— 06 关于 ———
            SectionTitle(6, "关于", "ABOUT")
            CardBox {
                InfoRow("版本", "v${com.zhisheng.weather.BuildConfig.VERSION_NAME}")
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                InfoRow("和风凭据", if (QWeatherApi.enabled) "已配置" else "未配置")
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                InfoRow("权限", "仅网络；位置为可选且默认关闭")
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "枳生天气 · EVA 数据终端",
                style = MaterialTheme.typography.labelSmall,
                color = ZhishengTextTertiary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                "数据来源：和风天气 / 小米天气 / Open-Meteo",
                style = MaterialTheme.typography.labelSmall,
                color = ZhishengTextTertiary.copy(alpha = 0.75f),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp, bottom = 28.dp),
            )
        }
    }
}

private fun sourceStatus(p: SourcePref): Pair<String, Boolean> = when (p) {
    SourcePref.AUTO -> "READY" to true
    SourcePref.QWEATHER -> if (QWeatherApi.enabled) "LINKED" to true else "NO KEY" to false
    SourcePref.XIAOMI -> "READY" to true
    SourcePref.OPEN_METEO -> "READY" to true
}

private fun openAppSettings(context: Context) {
    runCatching {
        context.startActivity(
            Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", context.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

@Composable
private fun SectionTitle(index: Int, title: String, en: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 2.dp, top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("%02d//".format(java.util.Locale.US, index), style = MaterialTheme.typography.titleSmall, color = ZhishengOrange, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = ZhishengTextSecondary, letterSpacing = 2.sp)
        Spacer(Modifier.width(8.dp))
        Text(en, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary, letterSpacing = 1.5.sp)
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = ZhishengTextTertiary,
        modifier = Modifier.padding(start = 2.dp, bottom = 8.dp, end = 8.dp),
    )
}

@Composable
private fun CardBox(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .background(ZhishengCard)
            .border(1.dp, ZhishengCardBorder, RectangleShape),
    ) {
        content()
    }
}

@Composable
private fun SourceRow(
    pref: SourcePref,
    selected: Boolean,
    status: Pair<String, Boolean>,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(role = Role.RadioButton) { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(width = 3.dp, height = 22.dp)
                .background(if (selected) ZhishengMint else ZhishengCardBorder)
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    pref.cn,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) ZhishengMint else ZhishengText,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                )
                Spacer(Modifier.width(8.dp))
                Text(pref.en, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary, letterSpacing = 1.sp)
            }
            Text(pref.desc, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
        }
        Text(
            status.first,
            style = MaterialTheme.typography.labelMedium,
            color = if (status.second) ZhishengCyan else ZhishengOrange,
        )
        if (selected) {
            Spacer(Modifier.width(8.dp))
            Text("[✓]", style = MaterialTheme.typography.labelMedium, color = ZhishengMint)
        }
    }
}

// 分段选择器：一行内 2-3 个互斥选项
@Composable
private fun SegmentRow(
    label: String,
    options: List<Pair<String, String>>,
    current: String,
    onPick: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = ZhishengText)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (text, value) ->
                val on = current == value
                Box(
                    Modifier.weight(1f)
                        .background(if (on) ZhishengMint.copy(alpha = 0.14f) else ZhishengSurface)
                        .border(1.dp, if (on) ZhishengMint else ZhishengCardBorder, RectangleShape)
                        .clickable(role = Role.RadioButton) { onPick(value) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (on) ZhishengMint else ZhishengTextSecondary,
                        fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, hint: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(role = Role.Switch) { onToggle() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = ZhishengText)
            Text(hint, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = ZhishengMint,
                uncheckedThumbColor = ZhishengTextTertiary,
                uncheckedTrackColor = ZhishengCardBorder,
                uncheckedBorderColor = ZhishengCardBorder,
            ),
        )
    }
}

@Composable
private fun ActionRow(label: String, enabled: Boolean, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button) { onClick() }
            .padding(horizontal = 14.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = if (enabled) color else ZhishengTextTertiary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = ZhishengTextSecondary)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelMedium, color = ZhishengText)
    }
}
