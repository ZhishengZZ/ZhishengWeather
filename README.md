![枳生天气 · ZHISHENG WEATHER TERMINAL](assets/banner.png)

<p align="center">
  <img alt="version" src="https://img.shields.io/badge/VERSION-1.2.4-FF6F1E?style=flat-square"/>
  <img alt="kotlin" src="https://img.shields.io/badge/KOTLIN-2.0.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white"/>
  <img alt="compose" src="https://img.shields.io/badge/JETPACK_COMPOSE-2024.10-3BFF8C?style=flat-square"/>
  <img alt="minsdk" src="https://img.shields.io/badge/MIN_SDK-26-20F0FF?style=flat-square"/>
  <img alt="data" src="https://img.shields.io/badge/DATA-QWEATHER_%2F_OPEN--METEO-23232E?style=flat-square"/>
</p>

<p align="center">
  <b>枳生天气</b> // ZHISHENG WEATHER TERMINAL<br/>
  一部 EVA / NERV 磷光终端美学的 Android 天气终端。<br/>
  <sub>MAGI // MELCHIOR-1 · BALTHASAR-2 · CASPER-3</sub>
</p>

---

## 01// 界面预览 SCREENSHOTS

| 主页 // HOME | 遥测 // TELEMETRY | 搜索 // SEARCH | 设置 // SETTINGS |
|:---:|:---:|:---:|:---:|
| <img src="assets/screen_home.png" width="220"/> | <img src="assets/screen_telemetry.png" width="220"/> | <img src="assets/screen_search.png" width="220"/> | <img src="assets/screen_settings.png" width="220"/> |
| 实时天气 · 灾害预警 · 逐时降水 | 遥测矩阵 · 空气质量 · 生活指数 | 城市检索 · 多城管理 | 单位 · 模块 · 数据源状态 |

---

## 02// 功能特性 FEATURES

- **实时天气**：温度 / 体感 / 天气现象 / 六边形态势图标
- **灾害预警**：气象灾害预警卡片分级着色（黄 / 橙 / 红）
- **逐时预报**：未来 24 小时温度 · 天气 · 降水概率
- **分钟降水**：未来两小时分钟级降水柱状图
- **逐日预报**：多日趋势 · 高低温 · 天气概览
- **遥测矩阵**：湿度 / 风向风速 / 气压 / 紫外线 / 能见度 / 露点 / 云量 / 阵风 / 时降水
- **空气质量**：AQI 主值 + PM2.5 / PM10 / O3 / NO2 / SO2 / CO 六项分测
- **生活指数**：洗车 / 运动 / 穿衣 / 感冒，NG 项橙色框线告警
- **昨日复盘**：昨日高低温 · AQI · 温差 ΔT
- **日月天文**：日出日落 · 月相（本地天文算法，无额外请求）
- **台风关注**：台风路径动态追踪（辅助源）
- **多城管理**：城市搜索 · 收藏 · 切换
- **多源融合**：主源失效自动兜底，任何城市都不开天窗（见 04//）
- **偏好设置**：°C / °F 切换 · 数据模块独立开关 · 数据源链路状态

---

## 03// 图标系统 ICON SYSTEM

<p align="center"><img src="assets/icons_grid.png" width="520"/></p>

全套 **15 枚**终端风天气图标：纯黑底 · 单色青双色调 · 锐利矢量边缘，
由 **阿里云百炼 `qwen-image` 系列模型**文生图逐枚生成，再经本地图像处理管线加工入库：

```
AI 生成 1024² ──▶ 亮度→Alpha 键控（黑底转透明）──▶ 32bpp 边缘平滑 ──▶ 512px 归一 ──▶ drawable-nodpi
```

覆盖：晴 / 多云 / 阴 / 雾 / 小雨 / 大雨 / 雷暴 / 雪 / 风 / 霰 等，昼夜变体（日 / 月）齐备。

---

## 04// 数据融合架构 DATA FUSION

| 链路角色 | 数据源 | 职责 |
|:--|:--|:--|
| **主链 LINKED** | 和风天气 QWeather | 实时 / 预警 / 逐时 / 逐日 / 分钟降水 / AQI / 指数（JWT ES256 运行时签名） |
| **辅链 AUX** | 小米天气源 | 昨日复盘 · 台风关注补充 |
| **兜底 FALLBACK** | Open-Meteo | 免 Key 公共源；逐日 / 逐时缺失时自动回填 24h+ |

```
 QWEATHER ──JWT/ES256──▶ ┐
 XIAOMI   ──AUX 补充──▶  ├──▶ WeatherRepository ──▶ ViewModel ──▶ Compose UI
 OPEN-METEO ─免Key兜底─▶ ┘      hourly / daily 自动 backfill
```

> 主源 hourly 不足 2 条时，按城市 `utc_offset_seconds` 折算本地时间轴，
> 由 Open-Meteo 回填 24 小时逐时数据 —— 任何城市都有完整逐时预报。

---

## 05// 技术栈 STACK

| 层 | 选型 |
|:--|:--|
| 语言 / 构建 | Kotlin 2.0.21 · AGP 8.5.2 · JDK 17 · compileSdk 34 / minSdk 26 |
| UI | Jetpack Compose（BOM 2024.10）· Material 3 · 全声明式终端主题 |
| 架构 | MVVM · `ViewModel` + `StateFlow` · Repository 单例融合层 |
| 网络 | Retrofit 2.11 + kotlinx-serialization 转换器 · OkHttp 4.12 + Logging |
| 安全 | BouncyCastle · Ed25519 JWT 运行时签名（凭据仅存 `local.properties`） |
| 持久化 | DataStore Preferences（城市 / 偏好 / 模块开关） |
| 并发 | kotlinx-coroutines 1.9 |

---

## 06// 项目结构 STRUCTURE

```
app/src/main/kotlin/com/zhisheng/weather/
├── MainActivity.kt              # 单 Activity 入口
├── ZhishengApplication.kt
├── data/
│   ├── QWeatherApi.kt           # 主源接口
│   ├── QwAuth.kt                # ES256 JWT 运行时签名
│   ├── QwModels.kt
│   ├── XiaomiApi.kt / XiaomiModels.kt   # 辅链
│   ├── OpenMeteoApi.kt          # 兜底链（daily + hourly）
│   ├── WeatherRepository.kt     # 三源融合 · backfill 决策
│   ├── CityRepository.kt        # 城市搜索 / 收藏
│   ├── SettingsRepository.kt    # 偏好 / 模块开关
│   └── MoonCalc.kt              # 本地日月天文算法
├── model/Weather.kt             # 领域模型
└── ui/
    ├── WeatherViewModel.kt
    ├── home/HomeScreen.kt       # 主页终端面板
    ├── SearchScreen.kt / SettingsScreen.kt
    ├── components/WeatherIcon.kt# 图标渲染组件
    └── theme/                   # 磷光终端色板 · 字体
```

---

## 07// 构建与运行 BUILD

**环境**：JDK 17 · Android SDK 34 · Gradle Wrapper 自带。

1. 克隆仓库：

```bash
git clone https://github.com/ZhishengZZ/ZhishengWeather.git
```

2. 在工程根目录创建 `local.properties`（**不入库**），填入和风天气凭据：

```properties
sdk.dir=<你的 Android SDK 路径>
qw.host=<你的 API Host，如 xxx.qweatherapi.com>
qw.project_id=<Project ID>
qw.kid=<Key ID>
qw.private_key=<Ed25519 私钥，单行>
# 可选：覆盖 release 签名口令
keystore.store_password=<...>
keystore.key_password=<...>
```

3. 准备 release 签名：将自有 keystore 放置为 `keystore/zhisheng.jks`
（alias `zhisheng`），或按需修改 `app/build.gradle.kts` 的 `signingConfigs`。
> 出于安全，keystore 与凭据均不随仓库分发；仅调试可直接 `assembleDebug`。

4. 构建：

```bash
./gradlew assembleDebug      # 调试包
./gradlew assembleRelease    # 签名发布包
```

---

## 08// 版本记录 CHANGELOG

**v1.2.4**（当前）
- 全新 AI 生成图标系统：15 枚双色调终端风图标替换旧矢量图
- 逐时预报引入 Open-Meteo 兜底，修复部分城市逐时缺失
- 空状态 / 图标渲染细节优化

更早历史见 [commit 记录](../../commits/main)。

---

## 09// 声明 LICENSE & NOTES

- 本项目为个人学习与兴趣作品，UI 美学致敬 EVA / NERV 终端风格，仅作同人创作，不用于商业。
- 天气数据版权归属：[和风天气](https://www.qweather.com/) · [Open-Meteo](https://open-meteo.com/) · 小米天气源；数据仅供参考。
- 使用和风天气需自行申请开发者凭据与 Ed25519 密钥，详见其官方文档。
- 代码中不含任何随附凭据；请勿将你的 Key / 私钥提交到公开仓库。

---

<p align="center">
  <sub>ZHISHENG WEATHER TERMINAL // PATTERN BLUE · made with phosphor &amp; kotlin</sub>
</p>
