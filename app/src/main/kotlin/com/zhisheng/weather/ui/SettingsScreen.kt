package com.zhisheng.weather.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhisheng.weather.data.SettingsRepository
import com.zhisheng.weather.ui.theme.ZhishengBg
import com.zhisheng.weather.ui.theme.ZhishengCard
import com.zhisheng.weather.ui.theme.ZhishengCardBorder
import com.zhisheng.weather.ui.theme.ZhishengMint
import com.zhisheng.weather.ui.theme.ZhishengOrange
import com.zhisheng.weather.ui.theme.ZhishengText
import com.zhisheng.weather.ui.theme.ZhishengTextSecondary
import com.zhisheng.weather.ui.theme.ZhishengTextTertiary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    // rememberCoroutineScope：原写法每次重组新建 scope 且永不取消（v1.2.4）
    val scope = rememberCoroutineScope()
    val tempUnit by SettingsRepository.tempUnit.collectAsState(initial = "c")
    val showTyphoon by SettingsRepository.showTyphoon.collectAsState(initial = true)

    Column(
        modifier = Modifier.fillMaxSize().background(ZhishengBg)
            .statusBarsPadding().navigationBarsPadding().padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = ZhishengText)
            }
            Text("设置", style = MaterialTheme.typography.titleMedium, color = ZhishengOrange)
        }

        SectionTitle(1, "温度单位")
        Column(
            Modifier.fillMaxWidth().border(1.dp, ZhishengCardBorder, androidx.compose.ui.graphics.RectangleShape)
                .background(ZhishengCard),
        ) {
            SettingRow("摄氏度", "°C", tempUnit == "c") {
                scope.launch { SettingsRepository.setTempUnit("c") }
            }
            androidx.compose.material3.HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
            SettingRow("华氏度", "°F", tempUnit == "f") {
                scope.launch { SettingsRepository.setTempUnit("f") }
            }
        }

        SectionTitle(2, "数据模块")
        Column(
            Modifier.fillMaxWidth().border(1.dp, ZhishengCardBorder, androidx.compose.ui.graphics.RectangleShape)
                .background(ZhishengCard),
        ) {
            ToggleRow("台风关注", "显示台风实时动态", showTyphoon) {
                scope.launch { SettingsRepository.setShowTyphoon(!showTyphoon) }
            }
        }

        SectionTitle(3, "数据源")
        Column(
            Modifier.fillMaxWidth().border(1.dp, ZhishengCardBorder, androidx.compose.ui.graphics.RectangleShape)
                .background(ZhishengCard),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(width = 3.dp, height = 9.dp).background(
                    if (com.zhisheng.weather.data.QWeatherApi.enabled) ZhishengMint else ZhishengOrange
                ))
                Spacer(Modifier.width(10.dp))
                Text("和风天气", style = MaterialTheme.typography.titleSmall, color = ZhishengText)
                Spacer(Modifier.weight(1f))
                Text(
                    if (com.zhisheng.weather.data.QWeatherApi.enabled) "LINKED" else "STANDBY",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (com.zhisheng.weather.data.QWeatherApi.enabled) ZhishengMint else ZhishengOrange,
                )
            }
            androidx.compose.material3.HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(width = 3.dp, height = 9.dp).background(ZhishengCardBorder))
                Spacer(Modifier.width(10.dp))
                Text("小米源 / 昨日+台风补充", style = MaterialTheme.typography.titleSmall, color = ZhishengTextSecondary)
                Spacer(Modifier.weight(1f))
                Text("AUX", style = MaterialTheme.typography.labelMedium, color = ZhishengTextTertiary)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "枳生天气 v${com.zhisheng.weather.BuildConfig.VERSION_NAME} · EVA 数据终端",
            style = MaterialTheme.typography.labelSmall,
            color = ZhishengTextTertiary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun SectionTitle(index: Int, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("%02d//".format(index), style = MaterialTheme.typography.titleSmall, color = ZhishengOrange)
        Spacer(Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = ZhishengTextSecondary, letterSpacing = 2.sp)
    }
}

@Composable
private fun SettingRow(label: String, hint: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(width = 3.dp, height = 9.dp).background(if (selected) ZhishengMint else ZhishengCardBorder))
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.titleSmall, color = if (selected) ZhishengMint else ZhishengText)
        Spacer(Modifier.weight(1f))
        Text(hint, style = MaterialTheme.typography.labelMedium, color = ZhishengTextTertiary)
    }
}

@Composable
private fun ToggleRow(label: String, hint: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
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
