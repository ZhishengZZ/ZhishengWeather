![枳生天气 · ZHISHENG WEATHER TERMINAL](assets/banner.png)

<p align="center">
  <b>枳生天气</b> // ZHISHENG WEATHER TERMINAL<br/>
  一部 EVA 磷光终端美学的安卓天气终端。<br/>
  <sub>An Android weather terminal in the phosphor aesthetic of EVA.</sub><br/>
  <sub>MAGI // MELCHIOR-1 · BALTHASAR-2 · CASPER-3</sub>
</p>

---

## ⬇ 立即获取 GET IT NOW

| 方式 | 适合 | 说明 |
|:--|:--|:--|
| [**直接下 APK**](../../releases) | 只想用的用户 | Releases 的 `public` 公开版，零配置，装上即用 |
| **源码构建** | 开发者 / 满血版用户 | 见 `07//`；填自己的和风凭据解锁主源 |

| Method | For | Notes |
|:--|:--|:--|
| [**Grab the APK**](../../releases) | Just want to use it | The `public` build in Releases — zero config, install and go |
| **Build from source** | Devs / full-feed users | See `07//`; plug in your own QWeather credentials to unlock the primary feed |

> 公开版 APK 以 `-PpublicBuild` 构建：**不含任何和风凭据**，数据走小米源 + Open-Meteo 公共源；
> 满血版（和风主源：分钟级降水图 / 穿衣感冒指数等）需源码构建并自备凭据。
>
> *The public APK is built with `-PpublicBuild`: it ships **without any QWeather credentials** and runs on the Xiaomi feed plus Open-Meteo. The full feed — QWeather as primary, minute-level rain chart, dress/cold indices and all — requires building from source with your own keys.*

<p align="center">
  <img alt="version" src="https://img.shields.io/badge/version-0.0.1_Preview-FF6F1E?style=flat-square"/>
  <img alt="license" src="https://img.shields.io/badge/license-MIT-3BFF8C?style=flat-square"/>
  <img alt="release" src="https://img.shields.io/github/v/release/ZhishengZZ/ZhishengWeather?style=flat-square&color=FF6F1E"/>
  <img alt="build" src="https://img.shields.io/github/actions/workflow/status/ZhishengZZ/ZhishengWeather/.github/workflows/build.yml?style=flat-square&label=BUILD"/>
</p>

**枳生天气**是一款风格相当"个人向"的安卓天气应用：黑底单色青的磷光终端界面，信息密度拉满。没有广告，没有会员套路，不会动不动就要求你授权一堆东西。打开就是天气，别的都省了。

市面上的天气应用越做越重——开屏广告、会员弹窗、通知里夹带营销推送。我想要的只是一个打开就能看、没有多余东西的工具，找了一圈没有完全合意的，干脆自己写了一个。

*Zhisheng Weather is a deliberately personal Android weather app: a phosphor terminal in black and cyan, dense with signal. No ads, no premium nagging, no grab-bag of permissions. Open it and you get the weather — nothing else.*

*Most weather apps keep getting heavier — splash ads, paywall pop-ups, marketing shoved into your notifications. All I wanted was a tool that shows the sky the moment it opens. I looked around, found nothing quite right, and wrote one instead.*

---

## 01// 界面预览 SCREENSHOTS

| 主页 // HOME | 遥测 // TELEMETRY | 搜索 // SEARCH | 设置 // SETTINGS |
|:---:|:---:|:---:|:---:|
| <img src="assets/screen_home.png" width="220"/> | <img src="assets/screen_telemetry.png" width="220"/> | <img src="assets/screen_search.png" width="220"/> | <img src="assets/screen_settings.png" width="220"/> |
| 实时天气 · 灾害预警 · 逐时降水 | 湿度 / 风向 / 气压等遥测 · 空气质量 · 生活指数 | 城市检索 · 多城管理 | 单位 · 模块开关 · 数据源状态 |
| *Live weather · alerts · hourly rain* | *Humidity / wind / pressure · air quality · life indices* | *City search · multi-city list* | *Units · module toggles · feed status* |

## 02// 功能特性 FEATURES

- **实时天气**：温度、体感、天气现象，六边形图标一眼看懂
  *Live conditions — temperature, feels-like, and a hex glyph you can read at a glance.*
- **灾害预警**：气象预警按黄 / 橙 / 红分级着色，斜纹警示条，点开看详情
  *Hazard alerts tinted by severity — yellow / orange / red — with striped warning bars; tap through for the full text.*
- **逐时预报**：未来 24 小时温度曲线 + 降水概率
  *Hourly forecast — a 24-hour temperature curve with precipitation odds.*
- **分钟级降水**：未来两小时逐分钟降水柱状图，出门前扫一眼要不要带伞
  *Minute-level precipitation — a two-hour, per-minute rain chart; one glance before you decide on an umbrella.*
- **15 天趋势**：逐日高低温、归一化温度条、冷暖自动配色，哪天降温一目了然
  *15-day outlook — daily highs and lows on normalized temperature bars, cold/warm auto-tinted, so a cold snap jumps out at you.*
- **空气质量**：AQI 主值 + PM2.5 / PM10 / O₃ / NO₂ / SO₂ / CO 六项分测
  *Air quality — headline AQI plus a six-way split: PM2.5 / PM10 / O₃ / NO₂ / SO₂ / CO.*
- **生活指数**：洗车 / 运动 / 穿衣 / 感冒，不适合的那项标橙，别硬冲
  *Life indices — car wash / sports / dressing / cold risk; whichever says "don't" glows orange. Don't push it.*
- **昨日复盘**：昨天高低温、AQI、温差，今天体感热不热心里有数
  *Yesterday's retro — high/low, AQI and the temperature delta, so you know how today should feel.*
- **日月与月相**：日出日落 + 月相，本地天文算法直接算，不额外发请求
  *Sun & moon — sunrise, sunset and moon phase computed on-device with local astronomy; zero extra requests.*
- **台风路径**：台风动向追踪（辅助源，淡季或无数据时为空白）
  *Typhoon tracking — live storm watch on the auxiliary feed; blank in the off-season or when there's nothing to track.*
- **多城市**：搜索、收藏、一键切换；同名城市标注省份归属，不会选错
  *Multi-city — search, save, one-tap switching; same-named cities carry their province so you never pick the wrong one.*
- **单位与模块**：℃ / ℉ 全局切换，数据模块可单独开关
  *Units & modules — global °C / °F switch, with every data module individually toggleable.*

## 03// 图标系统 ICON SYSTEM

<p align="center"><img src="assets/icons_grid.png" width="520"/></p>

全套 **15 枚**终端风天气图标：纯黑底 · 单色青双色调 · 锐利矢量边缘。每枚由 AI 文生图模型逐枚生成，再走本地图像处理管线加工入库：

*A full set of **15** terminal-grade weather glyphs: pure-black origin, cyan duotone, crisp vector edges. Each one is generated by an AI text-to-image model, then run through a local imaging pipeline:*

```
AI 生成 1024² ──▶ 亮度→Alpha 键控（黑底转透明）──▶ 32bpp 边缘平滑 ──▶ 512px 归一 ──▶ drawable-nodpi
AI-generated 1024² ──▶ luminance→alpha keying (black to transparent) ──▶ 32bpp edge smoothing ──▶ 512px normalize ──▶ drawable-nodpi
```

覆盖：晴 / 多云 / 阴 / 雾 / 小雨 / 大雨 / 雷暴 / 雪 / 风 / 霰 等，昼夜变体（日 / 月）齐备。

*Covers clear / partly cloudy / overcast / fog / light rain / heavy rain / thunderstorm / snow / wind / graupel and more, with day and night (sun / moon) variants throughout.*

## 04// 数据融合 DATA SOURCES

和市面上很多天气应用不同，枳生天气不是只吃一家数据，而是把三个来源拼起来。任何一路出问题，界面都不会"开天窗"：

*Unlike most weather apps that drink from a single well, Zhisheng fuses three feeds. If any one of them fails, the screen never goes dark:*

| 链路角色 | 数据源 | 负责 |
|:--|:--|:--|
| **主源** | 和风天气 | 实时 / 预警 / 逐时 / 逐日 / 分钟降水 / 空气质量 / 生活指数 |
| **补充** | 小米天气 | 昨日复盘 / 台风 / 逐日后半段 / 预警合并 |
| **兜底** | Open-Meteo | 全球免费源，逐时 / 逐日不足时自动补齐 |

| Role | Feed | Covers |
|:--|:--|:--|
| **Primary** | QWeather | Live / alerts / hourly / daily / minute rain / air quality / life indices |
| **Supplement** | Xiaomi Weather | Yesterday's retro / typhoons / the back half of the daily list / alert merging |
| **Fallback** | Open-Meteo | Free global feed; automatically backfills hourly / daily gaps |

具体来说：主源逐时数据少于两条时，按城市本地时区折算时间轴，用 Open-Meteo 补满 24 小时；逐日不满 15 天时同样补尾。海外城市也凑得满 15 天。

*Concretely: when the primary feed returns fewer than two hourly entries, the timeline is rebuilt in the city's local timezone and Open-Meteo fills the full 24 hours; daily lists shorter than 15 days get their tail backfilled the same way. Even overseas cities always show a full 15 days.*

认证上，和风接口用 Ed25519（EdDSA）签名 JWT，密钥只放在本地 `local.properties`，仓库里不随附任何凭据。

*On auth: the QWeather API is called with an Ed25519 (EdDSA) signed JWT. The key lives only in your local `local.properties` — the repository ships no credentials whatsoever.*

## 05// 技术栈 STACK

| 层 | 选型 |
|:--|:--|
| 语言 / 构建 | Kotlin 2.0.21 · AGP 8.5.2 · JDK 17（minSdk 26 / targetSdk 34） |
| UI | Jetpack Compose + Material 3，整套磷光终端主题 |
| 架构 | MVVM，`ViewModel` + `StateFlow`，Repository 做三源融合 |
| 网络 | Retrofit 2.11 + OkHttp 4.12（kotlinx-serialization） |
| 安全 | BouncyCastle，Ed25519 JWT 运行时签名 |
| 存储 | DataStore（城市 / 偏好 / 模块开关） |
| 并发 | kotlinx-coroutines 1.9 |

| Layer | Choice |
|:--|:--|
| Language / build | Kotlin 2.0.21 · AGP 8.5.2 · JDK 17 (minSdk 26 / targetSdk 34) |
| UI | Jetpack Compose + Material 3, with a full phosphor-terminal theme |
| Architecture | MVVM — `ViewModel` + `StateFlow`, Repository as the three-feed fusion layer |
| Network | Retrofit 2.11 + OkHttp 4.12 (kotlinx-serialization) |
| Security | BouncyCastle — Ed25519 JWT signed at runtime |
| Storage | DataStore (cities / preferences / module toggles) |
| Concurrency | kotlinx-coroutines 1.9 |

## 06// 项目结构 STRUCTURE

```
app/src/main/kotlin/com/zhisheng/weather/
├── MainActivity.kt              # 单 Activity 入口 / single-Activity entry
├── data/
│   ├── QWeatherApi.kt / QwAuth.kt / QwModels.kt   # 主源 + Ed25519 JWT 签名 / primary feed + JWT signing
│   ├── XiaomiApi.kt / XiaomiModels.kt             # 补充链 / supplement feed
│   ├── OpenMeteoApi.kt                            # 兜底链（daily + hourly）/ fallback feed
│   ├── WeatherRepository.kt                       # 三源融合 · backfill 决策 / fusion + backfill decisions
│   ├── MoonCalc.kt                                # 本地月相算法（Meeus）/ on-device moon phase (Meeus)
│   └── CityRepository.kt / SettingsRepository.kt  # 城市 / 偏好 / cities & preferences
├── model/Weather.kt             # 领域模型 / domain models
└── ui/
    ├── WeatherViewModel.kt
    ├── home/HomeScreen.kt       # 主页终端面板 / home terminal panel
    ├── SearchScreen.kt / SettingsScreen.kt
    └── components/WeatherIcon.kt # 图标渲染 / glyph rendering
```

## 07// 构建与运行 BUILD

需要 JDK 17 和 Android SDK 34，Gradle Wrapper 自带。

*Requires JDK 17 and Android SDK 34; the Gradle wrapper is included.*

1. clone 仓库 / *Clone the repo:*

```bash
git clone https://github.com/ZhishengZZ/ZhishengWeather.git
```

2. 在工程根目录建 `local.properties`（不入库），填和风天气凭据：
   *Create `local.properties` at the project root (never committed) with your QWeather credentials:*

```properties
sdk.dir=<你的 Android SDK 路径 / your Android SDK path>
qw.host=<你的 API Host / your API host, e.g. xxx.qweatherapi.com>
qw.project_id=<Project ID>
qw.kid=<Key ID>
qw.private_key=<Ed25519 私钥，单行 / Ed25519 private key, single line>
# 可选：覆盖 release 签名口令 / optional: override release signing passwords
keystore.store_password=<...>
keystore.key_password=<...>
```

3. 构建 / *Build:*

```bash
./gradlew assembleDebug      # 调试包 / debug build
./gradlew assembleRelease    # 签名发布包（个人满血版，需自备 keystore/zhisheng.jks）/ signed release (personal full feed; bring your own keystore)
./gradlew assembleRelease -PpublicBuild   # 公开版：凭据强制为空 + 随库公开证书 / public build: credentials forced empty + in-repo public key
```

> **不填凭据也能跑**：主源自动停用，退化为小米源 + Open-Meteo，实况 / 逐时 / 逐日 / 空气质量 / 预警都还在。首次安装默认给你北京，装好就能看。
>
> *It runs fine with no credentials at all: the primary feed simply stands down and the app degrades to Xiaomi + Open-Meteo — live weather, hourly, daily, air quality and alerts all intact. A first install seeds Beijing by default, so there's something on screen the moment you open it.*
>
> release 签名默认读 `keystore/zhisheng.jks`（alias `zhisheng`）。出于安全，keystore 和凭据都不随仓库分发；只装调试包的话直接 `assembleDebug` 即可。
>
> *Release signing reads `keystore/zhisheng.jks` (alias `zhisheng`) by default. For security, neither the keystore nor any credentials ship with the repo; if you only need a debug build, `assembleDebug` is all it takes.*

## 08// 已知不足 KNOWN ISSUES

真实的项目总有不完美，先写在这：

*Real software has rough edges. Here they are, up front:*

- 台风模块用的是辅助数据源，可能为空（和风台风接口没有免费额度）
  *Typhoon tracking rides on the auxiliary feed and may come up empty — QWeather's typhoon API has no free tier.*
- 海外城市分钟级降水不可用（免费档接口限制），这个区块会是空的
  *Minute-level precipitation is unavailable for overseas cities (free-tier limit); that block stays blank.*
- 预警跨源去重按标题精确匹配，不同源文案略有出入时可能重复
  *Cross-feed alert dedup matches on exact titles; slight wording differences between feeds can produce duplicates.*
- 界面图标是单色 stroke 风，多色版看后续有没有动力做
  *Glyphs are single-stroke monochrome for now; a multicolor set exists only if motivation strikes.*

## 09// 版本记录 CHANGELOG

**v0.0.1 Preview**（当前 / *current*）

- 首个公开预览：磷光终端 UI · 15 枚 AI 生成图标 · 三源数据融合（和风 / 小米 / Open-Meteo）
  *First public preview: phosphor-terminal UI · 15 AI-generated glyphs · three-feed data fusion (QWeather / Xiaomi / Open-Meteo).*
- 零配置体验：首装种子默认城市，无凭据自动降级公共源
  *Zero-config experience: a first install seeds a default city and degrades to public feeds without credentials.*
- `-PpublicBuild` 公开版构建链路 + 随库公开证书，Release 附公开版 APK
  *A `-PpublicBuild` public pipeline with an in-repo public signing key; Releases carry the public APK.*

更早的改动见 commit 历史。 / *Earlier work lives in the commit history.*

## 10// 许可与声明 LICENSE & NOTES

- 基于 [MIT](LICENSE) 开源；社区规范见 [贡献指南](CONTRIBUTING.md) · [行为准则](CODE_OF_CONDUCT.md) · [安全说明](SECURITY.md)
  *Open-sourced under [MIT](LICENSE); community norms live in [CONTRIBUTING](CONTRIBUTING.md) · [CODE_OF_CONDUCT](CODE_OF_CONDUCT.md) · [SECURITY](SECURITY.md).*
- 个人学习与兴趣作品，UI 美学致敬 EVA / NERV 终端风格，仅作同人创作，不用于商业
  *A personal, for-fun project. The UI is a fan homage to the EVA / NERV terminal aesthetic — non-commercial by intent.*
- 天气数据版权归属：[和风天气](https://www.qweather.com/) · [Open-Meteo](https://open-meteo.com/) · 小米天气；数据仅供参考
  *Weather data remains the property of its providers: [QWeather](https://www.qweather.com/) · [Open-Meteo](https://open-meteo.com/) · Xiaomi Weather. Figures are for reference only.*
- 使用和风天气需自行申请开发者凭据与 Ed25519 密钥，详见其官方文档
  *Using QWeather requires your own developer credentials and an Ed25519 key — see their official docs.*
- 代码中不含任何随附凭据；请勿把你的 Key / 私钥提交到公开仓库
  *No credentials ship with the code; never commit your keys or private keys to a public repository.*

---

<p align="center"><sub>ZHISHENG WEATHER TERMINAL // PATTERN BLUE · made with phosphor & kotlin</sub></p>

<!-- ZHISHENG WEATHER TERMINAL // preview housekeeping -->
