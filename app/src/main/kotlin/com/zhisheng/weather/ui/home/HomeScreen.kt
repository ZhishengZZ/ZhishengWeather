package com.zhisheng.weather.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhisheng.weather.model.AlertInfo
import com.zhisheng.weather.model.AqiInfo
import com.zhisheng.weather.model.CurrentWeather
import com.zhisheng.weather.model.DailyWeather
import com.zhisheng.weather.model.HourlyWeather
import com.zhisheng.weather.model.MinutePrecip
import com.zhisheng.weather.model.TyphoonInfo
import com.zhisheng.weather.model.WeatherCondition
import com.zhisheng.weather.model.WeatherData
import com.zhisheng.weather.model.YesterdayInfo
import com.zhisheng.weather.ui.Fmt
import com.zhisheng.weather.ui.HomeUiState
import com.zhisheng.weather.ui.WeatherViewModel
import com.zhisheng.weather.ui.components.WeatherIcon
import com.zhisheng.weather.ui.components.WeatherAmbience
import com.zhisheng.weather.ui.theme.ZhishengBg
import com.zhisheng.weather.ui.theme.ZhishengCard
import com.zhisheng.weather.ui.theme.ZhishengCardBorder
import com.zhisheng.weather.ui.theme.ZhishengCyan
import com.zhisheng.weather.ui.theme.ZhishengMint
import androidx.compose.ui.graphics.lerp as colorLerp
import com.zhisheng.weather.ui.theme.ZhishengOrange
import com.zhisheng.weather.ui.theme.ZhishengRed
import com.zhisheng.weather.ui.theme.ZhishengSurface
import com.zhisheng.weather.ui.theme.ZhishengText
import com.zhisheng.weather.ui.theme.ZhishengTextSecondary
import com.zhisheng.weather.ui.theme.ZhishengTextTertiary
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════
// 枳生天气 · EVA 数据终端 v2 主屏
// 布局序：状态行 → Hero → 预警 → 逐时(曲线) → 分钟降水 → 逐日(归一化温度条)
//        → 遥测卡格 → 空气质量 → 生活指数 → 昨日复盘 → 台风 → MAGI 页脚
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CityDrawer(
                uiState = uiState,
                onSelect = { key ->
                    viewModel.selectCity(key)
                    scope.launch { drawerState.close() }
                },
                onRemove = viewModel::removeCity,
                onAddCity = {
                    scope.launch { drawerState.close() }
                    onSearchClick()
                },
            )
        },
    ) {
        BackHandler(enabled = drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }
        Box(modifier = Modifier.fillMaxSize().background(ZhishengBg)) {
            // 氛围层在最底：内容之上不绘制任何东西，永远不遮读数（v0.0.2）
            WeatherAmbience(
                condition = uiState.weather?.current?.condition,
                level = uiState.prefs.ambience,
            )
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(
                    cityName = uiState.selectedCity?.name ?: "枳生天气",
                    loading = uiState.loading,
                    onMenu = { scope.launch { drawerState.open() } },
                    onRefresh = { viewModel.refresh() },
                    onSettings = onSettingsClick,
                )
                PullToRefreshBox(
                    isRefreshing = uiState.loading,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val contentKey = when {
                            uiState.cities.isEmpty() -> "empty"
                            uiState.loading && uiState.weather == null -> "loading"
                            uiState.weather?.error != null && uiState.weather?.current == null -> "error"
                            uiState.weather != null -> "data"
                            else -> "loading"
                        }
                        Crossfade(targetState = contentKey, animationSpec = tween(280), label = "content") { key ->
                            when (key) {
                                "empty" -> EmptyState(onSearchClick)
                                "error" -> ErrorState(uiState.weather?.error.orEmpty(), onSearchClick)
                                "data" -> WeatherContent(
                                    data = uiState.weather!!,
                                    city = uiState.selectedCity,
                                    unit = uiState.tempUnit,
                                    showTyphoon = uiState.showTyphoon,
                                    prefs = uiState.prefs,
                                )
                                else -> BootState()
                            }
                        }
                    }
                }
            }
            if (uiState.prefs.scanlines) Scanlines()
        }
    }
}

// —— CRT 扫描线氛围层（3dp 周期，3% 透明度，不拦截触摸） ——
@Composable
private fun Scanlines() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 3.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color.White.copy(alpha = 0.025f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
            y += step
        }
    }
}

@Composable
private fun TopBar(
    cityName: String,
    loading: Boolean,
    onMenu: () -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Filled.Menu, contentDescription = "城市列表", tint = ZhishengTextSecondary, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f).padding(start = 4.dp)) {
            Text(
                text = cityName,
                style = MaterialTheme.typography.titleMedium,
                color = ZhishengOrange,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "ZHISHENG WEATHER TERMINAL",
                style = MaterialTheme.typography.labelSmall,
                color = ZhishengTextTertiary,
                letterSpacing = 1.5.sp,
            )
        }
        // 刷新中持续旋转：原来是静态 360f，视觉上等于没转（v0.0.2）
        val spin = rememberInfiniteTransition(label = "spin")
        val angle by spin.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
            label = "angle",
        )
        IconButton(onClick = onRefresh, modifier = Modifier.size(48.dp)) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = if (loading) "正在刷新" else "刷新",
                tint = if (loading) ZhishengMint else ZhishengOrange,
                modifier = Modifier.size(20.dp).rotate(if (loading) angle else 0f),
            )
        }
        IconButton(onClick = onSettings, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Filled.Settings, contentDescription = "设置", tint = ZhishengTextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

// —— 交错入场动画容器（50ms 步进，300ms，M3 标准缓动） ——
// entered 由 WeatherContent 统一持有：只在数据首次入场时播放一次交错动画。
// 开关不能 remember 在 item 内部——LazyColumn 快滑时新入屏的 item 才现场组合，
// 逐项重置开关会重放淡入（还有 index*50ms 延迟），表现为快滑时卡片空白、停下才冒出来。
// 状态提升后，滚动中/回收后重组的卡片读到 entered=true，animateFloatAsState 初值即 1f，直接可见。
@Composable
private fun Stagger(index: Int, entered: Boolean, content: @Composable (Modifier) -> Unit) {
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        if (entered) 1f else 0f, tween(300, delayMillis = index * 50, easing = FastOutSlowInEasing), label = "sa",
    )
    val dy by androidx.compose.animation.core.animateFloatAsState(
        if (entered) 0f else 20f, tween(300, delayMillis = index * 50, easing = FastOutSlowInEasing), label = "sd",
    )
    content(
        Modifier.graphicsLayerAlpha(alpha, dy)
    )
}

private fun Modifier.graphicsLayerAlpha(a: Float, t: Float) =
    this.then(Modifier.graphicsLayer { alpha = a; translationY = t })

@Composable
private fun WeatherContent(
    data: WeatherData,
    city: com.zhisheng.weather.model.City?,
    unit: String,
    showTyphoon: Boolean,
    prefs: com.zhisheng.weather.ui.DisplayPrefs,
) {
    // 入场动画总开关：状态提升到 LazyColumn 之上，只驱动一次交错入场（v0.0.1 修复快滑闪卡）
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    // 区块编号改为渲染时现算：原来靠一个与渲染顺序不一致的 visible 数组预推，
    // 某些区块缺失时编号会跳号/错位（v0.0.2）
    var seq = 0
    var stagger = 0
    val nextIndex = { ++seq }
    val nextStagger = { stagger++ }

    val showHourly = data.hourly.isNotEmpty()
    val showPrecip = prefs.showPrecip && data.rainMinutes.isNotEmpty()
    val showDaily = data.daily.isNotEmpty()
    val showTele = prefs.showTelemetry && data.current != null
    val showAqi = prefs.showAqi && data.aqi != null
    val showIndices = prefs.showIndices &&
        (data.carWashOk != null || data.sportsOk != null || data.extraIndices.isNotEmpty())
    val showYesterday = prefs.showYesterday && data.yesterday != null
    val showTy = showTyphoon && data.typhoons.isNotEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        item { StatusLine(city, data) }
        data.current?.let { cur ->
            item { Stagger(nextStagger(), entered) { m -> HeroSection(cur, data, unit, prefs, m) } }
        }
        if (data.alerts.isNotEmpty()) {
            item { Stagger(nextStagger(), entered) { m -> AlertSection(data.alerts.take(3), m) } }
        }
        if (showHourly) {
            val n = nextIndex()
            item { SectionTitle(n, "逐时预报", "HOURLY") }
            item { Stagger(nextStagger(), entered) { m -> HourlySection(data.hourly, unit, prefs.windUnit, m) } }
        }
        if (showPrecip) {
            val n = nextIndex()
            item { SectionTitle(n, "分钟降水", "PRECIP") }
            item { Stagger(nextStagger(), entered) { m -> PrecipCard(data.rainMinutes, data.rainNowcast, m) } }
        }
        if (showDaily) {
            val n = nextIndex()
            item { SectionTitle(n, "逐日预报", "FORECAST") }
            item { Stagger(nextStagger(), entered) { m -> DailySection(data.daily, unit, prefs.windUnit, m) } }
        }
        if (showTele) {
            val n = nextIndex()
            item { SectionTitle(n, "遥测数据", "TELEMETRY") }
            item {
                Stagger(nextStagger(), entered) { m ->
                    TelemetryGrid(data.current!!, data.daily.firstOrNull(), unit, prefs, m)
                }
            }
        }
        if (showAqi) {
            val n = nextIndex()
            item { SectionTitle(n, "空气质量", "AIR QUALITY") }
            item { Stagger(nextStagger(), entered) { m -> AqiCard(data.aqi!!, m) } }
        }
        if (showIndices) {
            val n = nextIndex()
            item { SectionTitle(n, "生活指数", "INDICES") }
            item { Stagger(nextStagger(), entered) { m -> IndicesRow(data.carWashOk, data.sportsOk, data.extraIndices, m) } }
        }
        if (showYesterday) {
            val n = nextIndex()
            item { SectionTitle(n, "昨日复盘", "RETRO") }
            item { Stagger(nextStagger(), entered) { m -> YesterdayCard(data.yesterday!!, data.daily.firstOrNull(), unit, m) } }
        }
        if (showTy) {
            val n = nextIndex()
            item { SectionTitle(n, "台风关注", "TYPHOON") }
            item { Stagger(nextStagger(), entered) { m -> TyphoonCard(data.typhoons, m) } }
        }
        item { Stagger(nextStagger(), entered) { m -> Footer(data, m) } }
    }
}

// —— 状态行：坐标 / 更新时间 / 数据源 ——
@Composable
private fun StatusLine(city: com.zhisheng.weather.model.City?, data: WeatherData) {
    val coord = city?.let {
        // 负坐标按 S/W 显示，避免出现 "-33.90N" 这种矛盾写法（v0.0.1）
        String.format(
            Locale.US, "%.2f%s %.2f%s",
            Math.abs(it.latitude), if (it.latitude >= 0) "N" else "S",
            Math.abs(it.longitude), if (it.longitude >= 0) "E" else "W",
        )
    } ?: "----"
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(coord, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary, letterSpacing = 1.sp)
        Spacer(Modifier.weight(1f))
        Text(
            "UPD ${data.updateTime?.let { formatTime(it) } ?: "--"}  //  ${data.dataSource ?: "LINK"}",
            style = MaterialTheme.typography.labelSmall,
            color = ZhishengTextTertiary,
            letterSpacing = 1.sp,
        )
    }
}

// —— Hero：大温度 + 数字滚动 + 大图标 ——
@Composable
private fun HeroSection(
    cur: CurrentWeather,
    data: WeatherData,
    unit: String,
    prefs: com.zhisheng.weather.ui.DisplayPrefs,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = cur.weatherText ?: cur.condition?.label ?: "—",
                    style = MaterialTheme.typography.titleLarge,
                    color = ZhishengOrange,
                    fontWeight = FontWeight.Bold,
                )
                Row(verticalAlignment = Alignment.Top) {
                    AnimatedTemp(cur.temperature, unit)
                    Text(
                        text = "°",
                        style = MaterialTheme.typography.displayLarge,
                        color = ZhishengOrange,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(2.dp))
                val today = data.daily.firstOrNull()
                Text(
                    text = buildString {
                        append("体感${Fmt.temp(cur.feelsLike, unit) ?: "--"}°")
                        if (today?.high != null && today.low != null) {
                            // 源数据偶有高低倒挂，显示前排序（v0.0.2）
                            val hi = maxOf(today.high, today.low)
                            val lo = minOf(today.high, today.low)
                            append("  高${Fmt.temp(hi, unit)}° 低${Fmt.temp(lo, unit)}°")
                        }
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = ZhishengTextSecondary,
                    maxLines = 1,
                )
                // 风况直接进 Hero：最常看的一项，不用再往下滚到遥测区
                windLabel(cur, prefs.windUnit)?.let { w ->
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "风 $w",
                        style = MaterialTheme.typography.labelMedium,
                        color = ZhishengTextTertiary,
                        maxLines = 1,
                    )
                }
            }
            Box(contentAlignment = Alignment.Center) {
                // 六边形 AT 力场底纹
                Canvas(modifier = Modifier.size(116.dp)) {
                    val c = center
                    val r = size.minDimension / 2f
                    val path = Path().apply {
                        for (i in 0 until 6) {
                            val a = Math.toRadians(60.0 * i - 30.0)
                            val p = Offset(c.x + r * Math.cos(a).toFloat(), c.y + r * Math.sin(a).toFloat())
                            if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                        }
                        close()
                    }
                    drawPath(path, ZhishengOrange.copy(alpha = 0.22f), style = Stroke(1.5f))
                    drawPath(
                        androidx.compose.ui.graphics.Path().apply {
                            val r2 = r * 0.82f
                            for (i in 0 until 6) {
                                val a = Math.toRadians(60.0 * i - 30.0)
                                val p = Offset(c.x + r2 * Math.cos(a).toFloat(), c.y + r2 * Math.sin(a).toFloat())
                                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                            }
                            close()
                        },
                        ZhishengCyan.copy(alpha = 0.12f),
                        style = Stroke(1f),
                    )
                }
                WeatherIcon(cur.condition, Modifier.size(76.dp))
            }
        }
    }
}

// 温度数字滚动（400ms，emphasizedDecelerate 近似）
@Composable
private fun AnimatedTemp(celsius: Double?, unit: String) {
    if (celsius == null) {
        // 无数据显示 "--"，而不是误导性的 "0"（v0.0.1）
        Text(
            text = "--",
            style = MaterialTheme.typography.displayLarge,
            color = ZhishengText,
            fontWeight = FontWeight.Bold,
        )
        return
    }
    val target = if (unit == "f") celsius * 9.0 / 5.0 + 32.0 else celsius
    val anim = remember { Animatable(target.toFloat()) }
    LaunchedEffect(target) {
        anim.animateTo(target.toFloat(), tween(400))
    }
    Text(
        text = anim.value.roundToInt().toString(),
        style = MaterialTheme.typography.displayLarge,
        color = ZhishengText,
        fontWeight = FontWeight.Bold,
    )
}

// —— 预警横幅：警示斜纹 + 红边框 ——
@Composable
private fun AlertSection(alerts: List<AlertInfo>, modifier: Modifier) {
    // 展开态按标题记忆：原来按列表位置 remember，预警条数变化时展开态会错位到别条（v0.0.2）
    val expandedTitles = remember { mutableStateListOf<String>() }
    // 单一闪烁时钟：原来每条预警各起一个 while(true)，多条预警时多个协程各自计时（v0.0.2）
    val blinkOn = rememberBlink()
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        alerts.forEach { alert ->
            val expanded = alert.title in expandedTitles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RectangleShape)
                    .background(ZhishengCard)
                    .border(1.dp, ZhishengRed.copy(alpha = 0.7f), RectangleShape)
                    .clickable {
                        if (expanded) expandedTitles.remove(alert.title)
                        else expandedTitles.add(alert.title)
                    }
                    .padding(0.dp),
            ) {
                // 顶部警示斜纹
                Canvas(modifier = Modifier.fillMaxWidth().height(5.dp)) {
                    hazardStripes(this, ZhishengRed.copy(alpha = 0.75f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BlinkDot(blinkOn)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(alert.title, style = MaterialTheme.typography.titleSmall, color = ZhishengRed, fontWeight = FontWeight.Bold)
                        alert.pubTime?.let {
                            Text(formatAlertTime(it), style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
                        }
                    }
                    Text(
                        if (expanded) "[-]" else "[+]",
                        style = MaterialTheme.typography.labelMedium,
                        color = ZhishengRed,
                    )
                }
                if (expanded && !alert.detail.isNullOrBlank()) {
                    HorizontalDivider(color = ZhishengRed.copy(alpha = 0.3f), thickness = 1.dp)
                    Text(
                        alert.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = ZhishengTextSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

private fun hazardStripes(scope: DrawScope, color: Color) {
    with(scope) {
        val w = 10f
        var x = -size.height
        while (x < size.width) {
            val path = Path().apply {
                moveTo(x, size.height)
                lineTo(x + size.height, 0f)
                lineTo(x + size.height + w, 0f)
                lineTo(x + w, size.height)
                close()
            }
            drawPath(path, color)
            x += w * 2.4f
        }
    }
}

// 1Hz 闪烁时钟：整个预警区共用一个，随 composable 离开屏幕自动停
@Composable
private fun rememberBlink(): Boolean {
    var on by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            on = !on
        }
    }
    return on
}

@Composable
private fun BlinkDot(on: Boolean) {
    Box(
        Modifier
            .size(8.dp)
            .background(if (on) ZhishengRed else ZhishengRed.copy(alpha = 0.25f)),
    )
}

@Composable
private fun SectionTitle(index: Int, title: String, en: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("%02d//".format(index), style = MaterialTheme.typography.titleSmall, color = ZhishengOrange, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = ZhishengTextSecondary, letterSpacing = 2.sp)
        Spacer(Modifier.width(8.dp))
        Text(en, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary, letterSpacing = 1.5.sp)
        Spacer(Modifier.weight(1f))
        Text("─".repeat(6), style = MaterialTheme.typography.labelSmall, color = ZhishengCardBorder)
    }
}

// —— 角括号 HUD 卡片 ——
@Composable
private fun HudCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(RectangleShape)
            .background(ZhishengSurface)
            .then(Modifier.hudBorder())
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        content()
    }
}

private fun Modifier.hudBorder() = this
    .border(1.dp, ZhishengCardBorder, RectangleShape)
    .padding(0.dp)
    .then(
        Modifier.drawCornerBrackets(ZhishengOrange)
    )

private fun Modifier.drawCornerBrackets(color: Color) = this.then(
    Modifier.drawWithContent {
        drawContent()
        val len = 7.dp.toPx()
        val w = 1.6.dp.toPx()
        // 四角 L 形
        drawLine(color, Offset(0f, 0f), Offset(len, 0f), w)
        drawLine(color, Offset(0f, 0f), Offset(0f, len), w)
        drawLine(color, Offset(size.width, 0f), Offset(size.width - len, 0f), w)
        drawLine(color, Offset(size.width, 0f), Offset(size.width, len), w)
        drawLine(color, Offset(0f, size.height), Offset(len, size.height), w)
        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - len), w)
        drawLine(color, Offset(size.width, size.height), Offset(size.width - len, size.height), w)
        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - len), w)
    }
)

// —— 逐时：横向滚动 + 连续温度曲线 + 降水概率 ——
// v0.0.2 重做：原实现每格各画「本格中心→本格右边」的半段贝塞尔，格与格首尾不相接，
// 视觉上是一串断开的小弧线（用户反馈"那个线很丑"）。现改为每格画
// 「左邻中点→本格中心→右邻中点」的连续折线 + 渐隐面积填充，跨格严丝合缝。
@Composable
private fun HourlySection(
    hourly: List<HourlyWeather>,
    unit: String,
    windUnit: String,
    modifier: Modifier,
) {
    val temps = hourly.mapNotNull { h -> conv(h.temperature, unit) }
    val minT = temps.minOrNull() ?: 0.0
    val maxT = temps.maxOrNull() ?: 1.0
    HudCard(modifier = modifier.fillMaxWidth()) {
        Column {
            // key=时间戳：数据刷新时按身份复用 item，不整列重绑（v0.0.1）
            LazyRow(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                itemsIndexed(hourly, key = { _, h -> h.timeMillis }) { i, h ->
                    HourlyItem(
                        h = h,
                        prev = hourly.getOrNull(i - 1),
                        next = hourly.getOrNull(i + 1),
                        unit = unit,
                        minT = minT,
                        maxT = maxT,
                        first = i == 0,
                        windUnit = windUnit,
                    )
                }
            }
            // 图例：底部两行数字分别是降水概率与风速，去掉每格的 km/h 后在此说明一次
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(width = 6.dp, height = 2.dp).background(ZhishengCyan))
                Spacer(Modifier.width(5.dp))
                Text("降水概率", style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
                Spacer(Modifier.width(14.dp))
                Box(Modifier.size(width = 6.dp, height = 2.dp).background(ZhishengTextTertiary))
                Spacer(Modifier.width(5.dp))
                Text(Fmt.windUnitLabel(windUnit), style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
                Spacer(Modifier.weight(1f))
                Text(
                    "${hourly.size}H",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZhishengTextTertiary,
                )
            }
        }
    }
}

private fun conv(c: Double?, unit: String): Double? =
    c?.let { if (unit == "f") it * 9.0 / 5.0 + 32.0 else it }

@Composable
private fun HourlyItem(
    h: HourlyWeather,
    prev: HourlyWeather?,
    next: HourlyWeather?,
    unit: String,
    minT: Double,
    maxT: Double,
    first: Boolean,
    windUnit: String,
) {
    val itemW = 54.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(itemW),
    ) {
        Text(
            text = if (first) "现在" else formatHour(h.timeMillis),
            style = MaterialTheme.typography.labelSmall,
            color = if (first) ZhishengMint else ZhishengTextTertiary,
        )
        Spacer(Modifier.height(6.dp))
        WeatherIcon(h.condition, Modifier.size(24.dp))
        Spacer(Modifier.height(2.dp))
        // 温度读数放在曲线正上方，视线不用来回跳
        Text(
            text = Fmt.temp(h.temperature, unit)?.let { "$it°" } ?: "--",
            style = MaterialTheme.typography.titleSmall,
            color = ZhishengText,
        )
        Spacer(Modifier.height(3.dp))
        // 连续曲线：左半段接上一格中点，右半段接下一格中点
        Canvas(modifier = Modifier.fillMaxWidth().height(30.dp)) {
            val range = (maxT - minT).coerceAtLeast(1.0).toFloat()
            val top = 4f
            val usable = size.height - top - 4f
            fun yOf(v: Double?): Float? = v?.let {
                size.height - 4f - ((it - minT).toFloat() / range) * usable
            }

            val cx = size.width / 2f
            val yCur = yOf(conv(h.temperature, unit)) ?: return@Canvas
            val yPrev = yOf(prev?.let { conv(it.temperature, unit) })
            val yNext = yOf(next?.let { conv(it.temperature, unit) })

            // 左右邻的中点：与相邻格画出的同一点重合，所以跨格连续
            val pLeft = yPrev?.let { Offset(0f, (it + yCur) / 2f) }
            val pRight = yNext?.let { Offset(size.width, (it + yCur) / 2f) }
            val pCur = Offset(cx, yCur)

            // 面积填充（曲线到底边），极淡，给折线一点体积感
            val fill = Path().apply {
                moveTo(pLeft?.x ?: cx, pLeft?.y ?: yCur)
                lineTo(pCur.x, pCur.y)
                pRight?.let { lineTo(it.x, it.y) }
                lineTo(pRight?.x ?: cx, size.height)
                lineTo(pLeft?.x ?: cx, size.height)
                close()
            }
            drawPath(fill, ZhishengMint.copy(alpha = 0.07f))

            // 折线本体
            val line = Path().apply {
                moveTo(pLeft?.x ?: cx, pLeft?.y ?: yCur)
                lineTo(pCur.x, pCur.y)
                pRight?.let { lineTo(it.x, it.y) }
            }
            drawPath(line, ZhishengMint.copy(alpha = 0.75f), style = Stroke(1.6f))

            // 当前小时用实心亮点强调，其余用小空心点
            if (first) {
                drawCircle(ZhishengMint, 3.2f, pCur)
            } else {
                drawCircle(ZhishengBg, 2.6f, pCur)
                drawCircle(ZhishengMint.copy(alpha = 0.85f), 2.6f, pCur, style = Stroke(1.2f))
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = h.precipProb?.takeIf { it > 0 }?.let { "$it%" } ?: " ",
            style = MaterialTheme.typography.labelSmall,
            color = ZhishengCyan,
        )
        Text(
            text = Fmt.windValue(h.windSpeed, windUnit) ?: " ",
            style = MaterialTheme.typography.labelSmall,
            color = ZhishengTextTertiary,
        )
    }
}

// —— 分钟降水：柱状雷达图 ——
@Composable
private fun PrecipCard(minutes: List<MinutePrecip>, summary: String?, modifier: Modifier) {
    HudCard(modifier = modifier.fillMaxWidth()) {
        Column {
            summary?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = ZhishengText)
                Spacer(Modifier.height(8.dp))
            }
            Canvas(modifier = Modifier.fillMaxWidth().height(34.dp)) {
                val n = minutes.size
                if (n == 0) return@Canvas
                val maxP = minutes.maxOf { it.precip }.coerceAtLeast(0.3f)
                val bw = size.width / n
                minutes.forEachIndexed { i, m ->
                    val hgt = if (m.precip <= 0f) 1.5f else (m.precip / maxP) * (size.height - 4f) + 1.5f
                    drawRect(
                        color = if (m.precip > 0f) ZhishengCyan.copy(alpha = 0.85f) else ZhishengCardBorder,
                        topLeft = Offset(i * bw + bw * 0.2f, size.height - hgt),
                        size = androidx.compose.ui.geometry.Size(bw * 0.6f, hgt),
                    )
                }
                // 现在标记线：按首末条实际时间定位（minutely 自当前时刻起约 +120min，
                // 原写死 20% 位置+"-60min"标签与实际语义不符，v0.0.1）
                val nowMillis = System.currentTimeMillis()
                val t0 = minutes.first().timeMillis
                val t1 = minutes.last().timeMillis
                val frac = if (t1 > t0) (nowMillis - t0).toFloat() / (t1 - t0) else 0f
                val nowX = frac.coerceIn(0f, 1f) * size.width
                drawLine(ZhishengOrange, Offset(nowX, 0f), Offset(nowX, size.height), 1.4f)
            }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Text("现在", style = MaterialTheme.typography.labelSmall, color = ZhishengOrange)
                Spacer(Modifier.weight(1f))
                Text("+120min", style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
            }
        }
    }
}

// —— 逐日：全周归一化温度区间条 ——
@Composable
private fun DailySection(
    daily: List<DailyWeather>,
    unit: String,
    windUnit: String,
    modifier: Modifier,
) {
    val lows = daily.mapNotNull { conv(it.low, unit) }
    val highs = daily.mapNotNull { conv(it.high, unit) }
    val weekMin = lows.minOrNull() ?: 0.0
    val weekMax = highs.maxOrNull() ?: 1.0
    val range = (weekMax - weekMin).coerceAtLeast(1.0)

    HudCard(modifier = modifier.fillMaxWidth()) {
        Column {
            daily.forEachIndexed { index, d ->
                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatWeekday(d.dateMillis, index),
                            modifier = Modifier.width(44.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (index == 0) ZhishengMint else ZhishengText,
                        )
                        WeatherIcon(d.condition, Modifier.size(22.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = d.precipProbability?.takeIf { it > 0 }?.let { "$it%" } ?: "  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZhishengCyan,
                            modifier = Modifier.width(30.dp),
                        )
                        Text(
                            Fmt.temp(d.low, unit)?.let { "$it°" } ?: "--",
                            style = MaterialTheme.typography.titleSmall,
                            color = ZhishengTextTertiary,
                            modifier = Modifier.width(34.dp),
                            textAlign = TextAlign.End,
                        )
                        // 归一化温度条
                        BoxWithConstraints(
                            Modifier.padding(horizontal = 8.dp).weight(1f).height(4.dp)
                                .background(ZhishengCardBorder, RectangleShape)
                        ) {
                            // lo/hi 先排序再归一：源数据偶发把高低温写反（小米 from/to 语义不定），
                            // 若直接相减会得到负宽度，Modifier.width 抛异常（v0.0.2）
                            val a = (((conv(d.low, unit) ?: weekMin) - weekMin) / range).toFloat()
                            val b = (((conv(d.high, unit) ?: weekMax) - weekMin) / range).toFloat()
                            val lo = minOf(a, b).coerceIn(0f, 1f)
                            val hi = maxOf(a, b).coerceIn(0f, 1f)
                            val w = (hi - lo).coerceIn(0.03f, 1f - lo)
                            Box(
                                Modifier
                                    .offset(x = maxWidth * lo)
                                    .width(maxWidth * w)
                                    .fillMaxHeight()
                                    .background(tempColor(d.low), RectangleShape),
                            )
                        }
                        Text(
                            Fmt.temp(d.high, unit)?.let { "$it°" } ?: "--",
                            style = MaterialTheme.typography.titleSmall,
                            color = ZhishengText,
                            modifier = Modifier.width(34.dp),
                            textAlign = TextAlign.End,
                        )
                    }
                    if (d.windSpeed != null) {
                        Row(Modifier.padding(start = 50.dp)) {
                            Text("风 ${Fmt.wind(d.windSpeed, windUnit)}", style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
                        }
                    }
                }
                if (index < daily.size - 1) {
                    HorizontalDivider(color = ZhishengCardBorder.copy(alpha = 0.5f), thickness = 1.dp)
                }
            }
        }
    }
}

// 温度色：冷青 → 暖橙 线性插值
private fun tempColor(low: Double?): Color {
    val t = ((low ?: 10.0) + 10.0) / 45.0
    return colorLerp(ZhishengCyan, ZhishengOrange, t.toFloat().coerceIn(0f, 1f))
}

// —— 遥测卡格：2 列 HUD 小卡 ——
@Composable
private fun TelemetryGrid(
    cur: CurrentWeather,
    today: DailyWeather?,
    unit: String,
    prefs: com.zhisheng.weather.ui.DisplayPrefs,
    modifier: Modifier,
) {
    val items = listOf(
        Triple("湿度", "HUMIDITY", cur.humidity?.let { "${it.roundToInt()}%" }),
        Triple("风向风速", "WIND", windLabel(cur, prefs.windUnit)),
        Triple("气压", "PRESS", Fmt.pressure(cur.pressure, prefs.pressureUnit)),
        Triple("紫外线", "UV", cur.uvIndex?.let { uvText(it) }),
        Triple("能见度", "VIS", cur.visibility?.let { "${it.roundToInt()} km" }),
        Triple("露点", "DEW", cur.dewPoint?.let { "${Fmt.temp(it, unit)}°" }),
        Triple("云量", "CLOUD", cur.cloudCover?.let { "${it.roundToInt()}%" }),
        Triple("阵风", "GUST", Fmt.wind(cur.windGust, prefs.windUnit)),
        Triple("1时降水", "PRECIP", cur.precipMm?.let { String.format(Locale.US, "%.1f mm", it) }),
    )
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (cn, en, value) ->
                    TeleCell(cn, en, value, cur, Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }
        // 日出日落宽卡
        if (today?.sunrise != null || today?.sunset != null) {
            Box(
                Modifier.fillMaxWidth()
                    .clip(RectangleShape)
                    .background(ZhishengSurface)
                    .border(1.dp, ZhishengCardBorder, RectangleShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Column {
                    TeleLabel("日月", "LUMINARY")
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        today?.sunrise?.let {
                            Text("日出 ", style = MaterialTheme.typography.labelMedium, color = ZhishengTextSecondary)
                            Text(it, style = MaterialTheme.typography.titleSmall, color = ZhishengOrange, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(18.dp))
                        today?.sunset?.let {
                            Text("日落 ", style = MaterialTheme.typography.labelMedium, color = ZhishengTextSecondary)
                            Text(it, style = MaterialTheme.typography.titleSmall, color = ZhishengOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (today?.moonrise != null || today?.moonset != null || today?.moonPhase != null) {
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            today.moonPhase?.let {
                                Text(Fmt.moonPhaseZh(it) ?: it, style = MaterialTheme.typography.labelMedium, color = ZhishengTextSecondary)
                                Spacer(Modifier.width(18.dp))
                            }
                            today.moonrise?.let {
                                Text("月出 ", style = MaterialTheme.typography.labelMedium, color = ZhishengTextSecondary)
                                Text(it, style = MaterialTheme.typography.titleSmall, color = ZhishengCyan, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(18.dp))
                            }
                            today.moonset?.let {
                                Text("月落 ", style = MaterialTheme.typography.labelMedium, color = ZhishengTextSecondary)
                                Text(it, style = MaterialTheme.typography.titleSmall, color = ZhishengCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeleCell(
    cn: String,
    en: String,
    value: String?,
    cur: CurrentWeather,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(RectangleShape)
            .background(ZhishengSurface)
            .border(1.dp, ZhishengCardBorder, RectangleShape)
            .drawCornerBrackets(ZhishengOrange.copy(alpha = 0.8f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column {
            TeleLabel(cn, en)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (en == "WIND" && cur.windDirectionDeg != null) {
                    // 风向箭头（北=上，按度数旋转）
                    Canvas(Modifier.size(14.dp).rotate(cur.windDirectionDeg.toFloat() + 180f)) {
                        val c = Offset(size.width / 2, size.height / 2)
                        drawLine(ZhishengCyan, Offset(c.x, 1f), Offset(c.x, size.height - 1f), 1.6f)
                        drawLine(ZhishengCyan, Offset(c.x, 1f), Offset(c.x - 3f, 5f), 1.6f)
                        drawLine(ZhishengCyan, Offset(c.x, 1f), Offset(c.x + 3f, 5f), 1.6f)
                    }
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    value ?: "--",
                    style = MaterialTheme.typography.titleMedium,
                    color = ZhishengText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun TeleLabel(cn: String, en: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(width = 3.dp, height = 8.dp).background(ZhishengOrange))
        Spacer(Modifier.width(6.dp))
        Text(cn, style = MaterialTheme.typography.labelMedium, color = ZhishengTextSecondary)
        Spacer(Modifier.width(6.dp))
        Text(en, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary, letterSpacing = 1.sp)
    }
}

private fun windLabel(cur: CurrentWeather, windUnit: String): String? {
    val dir = com.zhisheng.weather.data.WeatherRepository.windDirection(cur.windDirectionDeg)
    val speed = Fmt.wind(cur.windSpeed, windUnit)
    return when {
        dir != null && speed != null -> "$dir $speed"
        dir != null -> dir
        speed != null -> speed
        else -> null
    }
}

private fun uvText(uv: Int): String = when {
    uv <= 2 -> "$uv 弱"
    uv <= 5 -> "$uv 中等"
    uv <= 7 -> "$uv 强"
    uv <= 10 -> "$uv 很强"
    else -> "$uv 极强"
}

// —— AQI ——
@Composable
private fun AqiCard(aqi: AqiInfo, modifier: Modifier) {
    HudCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = aqi.value?.toString() ?: "--",
                    style = MaterialTheme.typography.displaySmall,
                    color = aqiColor(aqi.value),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(aqi.level ?: "空气质量", style = MaterialTheme.typography.titleMedium, color = aqiColor(aqi.value), fontWeight = FontWeight.Bold)
                    Text("AQI // AIR QUALITY INDEX", style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary, letterSpacing = 1.sp)
                }
                Spacer(Modifier.weight(1f))
                aqi.primary?.let {
                    Text("首要污染物 $it", style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
                }
            }
            Spacer(Modifier.height(10.dp))
            // 刻度尺 + 游标
            Box(Modifier.fillMaxWidth().height(4.dp).background(ZhishengCardBorder, RectangleShape)) {
                Box(
                    Modifier
                        .fillMaxWidth((aqi.value?.toFloat() ?: 0f).coerceIn(0f, 500f) / 500f)
                        .height(4.dp)
                        .background(aqiColor(aqi.value), RectangleShape),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PollutantChip("PM2.5", aqi.pm25, Modifier.weight(1f))
                PollutantChip("PM10", aqi.pm10, Modifier.weight(1f))
                PollutantChip("O3", aqi.o3, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PollutantChip("NO2", aqi.no2, Modifier.weight(1f))
                PollutantChip("SO2", aqi.so2, Modifier.weight(1f))
                PollutantChip("CO", aqi.co, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PollutantChip(name: String, value: String?, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RectangleShape)
            .background(ZhishengCard)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
        Spacer(Modifier.weight(1f))
        Text(value ?: "--", style = MaterialTheme.typography.titleSmall, color = ZhishengText)
    }
}

private fun aqiColor(value: Int?): Color = when {
    value == null -> ZhishengTextTertiary
    value <= 50 -> ZhishengMint
    value <= 100 -> ZhishengMint.copy(alpha = 0.8f)
    value <= 150 -> ZhishengOrange
    value <= 200 -> ZhishengOrange.copy(alpha = 0.85f)
    value <= 300 -> ZhishengRed
    else -> ZhishengRed.copy(alpha = 0.8f)
}

// —— 生活指数 ——
@Composable
private fun IndicesRow(carWashOk: Boolean?, sportsOk: Boolean?, extra: List<com.zhisheng.weather.model.LifeIndexExtra>, modifier: Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            carWashOk?.let { IndexChip("洗车", "CAR WASH", it, Modifier.weight(1f)) }
            sportsOk?.let { IndexChip("运动", "SPORTS", it, Modifier.weight(1f)) }
        }
        if (extra.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            extra.chunked(2).forEach { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { ix ->
                        Row(
                            Modifier.weight(1f)
                                .clip(RectangleShape)
                                .background(ZhishengSurface)
                                .border(1.dp, ZhishengCardBorder, RectangleShape)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                TeleLabel(ix.name, ix.en)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    ix.category.ifBlank { "--" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ZhishengText,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun IndexChip(cn: String, en: String, ok: Boolean, modifier: Modifier = Modifier) {
    val c = if (ok) ZhishengMint else ZhishengOrange
    Row(
        modifier
            .clip(RectangleShape)
            .background(ZhishengSurface)
            .border(1.dp, c.copy(alpha = 0.5f), RectangleShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            TeleLabel(cn, en)
            Spacer(Modifier.height(4.dp))
            Text(
                if (ok) "适宜" else "不适宜",
                style = MaterialTheme.typography.titleMedium,
                color = c,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(if (ok) "[OK]" else "[NG]", style = MaterialTheme.typography.labelMedium, color = c)
    }
}

// —— 昨日复盘 ——
@Composable
private fun YesterdayCard(y: YesterdayInfo, today: DailyWeather?, unit: String, modifier: Modifier) {
    HudCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (y.condition != null) {
                WeatherIcon(y.condition, Modifier.size(30.dp))
                Spacer(Modifier.width(12.dp))
            }
            if (y.high != null && y.low != null) {
                Text(
                    "${Fmt.temp(y.high, unit)}° / ${Fmt.temp(y.low, unit)}°",
                    style = MaterialTheme.typography.titleMedium,
                    color = ZhishengText,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(10.dp))
            }
            y.aqi?.let {
                Text("AQI $it", style = MaterialTheme.typography.labelMedium, color = aqiColor(it))
            }
            Spacer(Modifier.weight(1f))
            today?.high?.let { th ->
                y.high?.let {
                    val diff = th.roundToInt() - it.roundToInt()
                    Text(
                        "ΔT ${if (diff >= 0) "+" else ""}$diff°",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (diff > 0) ZhishengOrange else ZhishengMint,
                    )
                }
            }
        }
    }
}

// —— 台风 ——
@Composable
private fun TyphoonCard(typhoons: List<TyphoonInfo>, modifier: Modifier) {
    HudCard(modifier = modifier.fillMaxWidth()) {
        Column {
            typhoons.forEachIndexed { i, t ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        t.type ?: "TY",
                        style = MaterialTheme.typography.labelMedium,
                        color = ZhishengOrange,
                        modifier = Modifier.width(34.dp),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(t.name ?: "", style = MaterialTheme.typography.titleSmall, color = ZhishengText)
                    Spacer(Modifier.width(8.dp))
                    t.ename?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
                    }
                    Spacer(Modifier.weight(1f))
                    t.windSpeed?.let {
                        Text("${it.roundToInt()}m/s", style = MaterialTheme.typography.labelMedium, color = ZhishengCyan)
                    }
                }
            }
        }
    }
}

// —— MAGI 页脚 ——
@Composable
private fun Footer(data: WeatherData, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 24.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "MAGI // MELCHIOR-1 · BALTHASAR-2 · CASPER-3",
            style = MaterialTheme.typography.labelSmall,
            color = ZhishengTextTertiary,
            letterSpacing = 1.5.sp,
        )
        Text(
            "DATA ${data.dataSource ?: "--"} · 枳生天气 v${com.zhisheng.weather.BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = ZhishengTextTertiary.copy(alpha = 0.7f),
            letterSpacing = 1.sp,
        )
    }
}

// —— 启动加载：EVA 开机序列 ——
@Composable
private fun BootState() {
    val lines = listOf(
        "ZHISHENG WEATHER TERMINAL v${com.zhisheng.weather.BuildConfig.VERSION_NAME}",
        "MAGI LINK ... ESTABLISHED",
        "SYNC ATMOSPHERIC DATA ...",
    )
    var count by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        lines.indices.forEach { i ->
            kotlinx.coroutines.delay(260)
            count = i + 1
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            lines.take(count).forEach { l ->
                Text(
                    "> $l",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZhishengMint,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(6.dp))
            }
            Text(
                "█",
                style = MaterialTheme.typography.bodySmall,
                color = ZhishengMint,
            )
        }
    }
}

@Composable
private fun EmptyState(onSearchClick: () -> Unit) {
    // 终端打字序列：与开屏 BootState 同款，逐字敲出 + █ 光标；文案不点名任何具体城市
    val lines = listOf(
        "NO CITY // 未接入城市",
        "SEARCH ANY CITY // 输入任意城市名",
        "AWAITING INPUT ...",
    )
    var doneCount by remember { mutableStateOf(0) }
    var chars by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        lines.forEachIndexed { i, l ->
            chars = 0
            l.indices.forEach { c ->
                kotlinx.coroutines.delay(26)
                chars = c + 1
            }
            kotlinx.coroutines.delay(240)
            doneCount = i + 1
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        WeatherIcon(WeatherCondition.CLEAR, Modifier.size(64.dp).alpha(0.6f))
        Spacer(Modifier.height(24.dp))
        Column(Modifier.align(Alignment.Start)) {
            lines.take(doneCount).forEach { l ->
                Text(
                    "> $l",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZhishengMint,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(6.dp))
            }
            if (doneCount < lines.size) {
                Text(
                    "> " + lines[doneCount].take(chars),
                    style = MaterialTheme.typography.bodySmall,
                    color = ZhishengMint,
                    letterSpacing = 1.sp,
                )
            }
            Text(
                "█",
                style = MaterialTheme.typography.bodySmall,
                color = ZhishengMint,
            )
        }
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier
                .clip(RectangleShape)
                .background(ZhishengSurface)
                .border(1.dp, ZhishengMint.copy(alpha = 0.6f), RectangleShape)
                .drawCornerBrackets(ZhishengMint)
                .clickable(role = Role.Button, onClickLabel = "添加城市") { onSearchClick() }
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text("[ + ADD CITY ]", style = MaterialTheme.typography.titleSmall, color = ZhishengMint, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun ErrorState(message: String, onSearchClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("!! LINK FAILURE", style = MaterialTheme.typography.titleMedium, color = ZhishengRed, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = ZhishengTextSecondary)
        Spacer(Modifier.height(16.dp))
        Text(
            "[ 换一个城市试试 ]",
            style = MaterialTheme.typography.bodyMedium,
            color = ZhishengMint,
            modifier = Modifier
                .clickable(role = Role.Button, onClickLabel = "换一个城市") { onSearchClick() }
                .padding(8.dp),
        )
    }
}

// —— 城市抽屉 ——
@Composable
private fun CityDrawer(
    uiState: HomeUiState,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    onAddCity: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZhishengSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
            Text("00//", style = MaterialTheme.typography.titleSmall, color = ZhishengOrange, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Text("城市", style = MaterialTheme.typography.titleMedium, color = ZhishengText, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text("CITY LIST", style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary, letterSpacing = 1.5.sp)
        }
        if (uiState.cities.isEmpty()) {
            Text("还没有保存的城市", style = MaterialTheme.typography.bodySmall, color = ZhishengTextTertiary)
        }
        uiState.cities.forEachIndexed { i, city ->
            val selected = city.locationKey == uiState.selectedCity?.locationKey
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RectangleShape)
                    .background(if (selected) ZhishengCard else Color.Transparent)
                    .clickable { onSelect(city.locationKey) }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "%02d".format(i + 1),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) ZhishengOrange else ZhishengTextTertiary,
                )
                Spacer(Modifier.width(10.dp))
                if (selected) {
                    Box(Modifier.size(width = 3.dp, height = 14.dp).background(ZhishengMint))
                    Spacer(Modifier.width(8.dp))
                }
                // 城市名 + 归属地：同名城市（金川区@金昌 vs 金川县@阿坝）必须可区分（v0.0.1）
                Column(Modifier.weight(1f)) {
                    Text(
                        city.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (selected) ZhishengMint else ZhishengText,
                    )
                    if (city.affiliation.isNotBlank()) {
                        Text(
                            city.affiliation,
                            style = MaterialTheme.typography.labelSmall,
                            color = ZhishengTextTertiary,
                        )
                    }
                }
                IconButton(onClick = { onRemove(city.locationKey) }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "删除${city.name}", tint = ZhishengTextTertiary, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RectangleShape)
                .background(ZhishengCard)
                .border(1.dp, ZhishengMint.copy(alpha = 0.5f), RectangleShape)
                .clickable { onAddCity() }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = ZhishengMint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("添加城市", style = MaterialTheme.typography.titleSmall, color = ZhishengMint, letterSpacing = 1.sp)
        }
    }
}

private val hourFmt = DateTimeFormatter.ofPattern("H时")
private val timeFmt = DateTimeFormatter.ofPattern("MM-dd HH:mm")

private fun formatHour(epoch: Long): String {
    val zoned = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault())
    return hourFmt.format(zoned)
}

private fun formatWeekday(epoch: Long, index: Int): String {
    val zoned = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault())
    if (index == 0) return "今天"
    return when (zoned.dayOfWeek.value) {
        1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"; 5 -> "周五"; 6 -> "周六"; else -> "周日"
    }
}

private fun formatTime(epoch: Long): String = timeFmt.format(Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()))

private fun formatAlertTime(s: String): String = try {
    s.substring(0, minOf(16, s.length)).replace("T", " ")
} catch (_: Exception) {
    s
}
