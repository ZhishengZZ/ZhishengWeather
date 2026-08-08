![枳生天气 · ZHISHENG WEATHER TERMINAL](assets/banner.png)

<p align="center">
  <b>枳生天气</b> · ZHISHENG WEATHER TERMINAL<br/>
  没有广告，不用登录，装完打开就是天气。<br/>
  <sub>黑底磷光的终端界面 · MELCHIOR-1 · BALTHASAR-2 · CASPER-3</sub>
</p>

<p align="center">
  <a href="https://github.com/ZhishengZZ/ZhishengWeather/releases/latest"><b>⬇ 下载 APK · 11 MB · 装上就能用</b></a>
</p>

<p align="center">
  <img alt="版本" src="https://img.shields.io/badge/版本-0.0.2-FF6F1E?style=flat-square"/>
  <img alt="体积" src="https://img.shields.io/badge/体积-11MB-3BFF8C?style=flat-square"/>
  <img alt="权限" src="https://img.shields.io/badge/权限-仅3项-3BFF8C?style=flat-square"/>
  <img alt="广告与内购" src="https://img.shields.io/badge/广告与内购-0-3BFF8C?style=flat-square"/>
  <img alt="许可" src="https://img.shields.io/badge/许可-MIT-3BFF8C?style=flat-square"/>
</p>

<p align="center">
  <b>简体中文</b> · <a href="./README.en.md">English</a>
</p>

---

我做这个 App 的起因很简单：早上出门前想看一眼今天要不要带伞。

这件事现在的天气应用干不了。开屏广告五秒，进去弹会员，让你注册、让你授权一堆用不上的东西，天气本身反倒藏在第三屏。找了一圈没有完全合意的，干脆自己写了一个。

于是有了枳生天气：

- **没有广告**，没有开屏，没有会员弹窗，没有"限时特惠"
- **不用注册登录**，没有账号系统，也就没有能被卖掉的账号
- **只要三个权限**：联网、网络状态、粗略定位——定位默认关着，你不主动点它就永远不申请
- **不埋点**：依赖清单里没有任何广告或统计 SDK，你的位置和使用行为不会被传去任何地方
- **11 MB**，Android 8.0 以上都能装，老机器也带得动
- **MIT 开源**：代码全在这个仓库，不放心可以自己编一个

界面是黑底配单色青的磷光终端风，挺个人向的，喜欢的应该会很喜欢。信息密度拉得比较满，一屏能同时看到实况、逐时、分钟降水和逐日趋势，不用来回翻页。

## 长什么样

| 主页 | 遥测 | 搜索 | 设置 |
|:---:|:---:|:---:|:---:|
| <img src="assets/screen_home.png" width="210"/> | <img src="assets/screen_telemetry.png" width="210"/> | <img src="assets/screen_search.png" width="210"/> | <img src="assets/screen_settings.png" width="210"/> |
| 实况 · 预警 · 逐时 · 分钟降水 | 湿度 / 风 / 气压 · 空气质量 · 生活指数 | 多城搜索与切换 | 数据源 / 单位 / 模块开关 |

## 它能告诉你什么

出门前最常用的几件事：

- **要不要带伞** — 未来两小时逐分钟降水柱状图，雨什么时候来、下多久，一眼看完
- **今天穿什么** — 体感温度 + 穿衣指数，冷暖不靠温度数字猜
- **哪天降温** — 15 天高低温趋势条，冷暖自动配色，寒潮那几天自己会跳出来
- **能不能开窗** — AQI 主值加 PM2.5 / PM10 / O₃ / NO₂ / SO₂ / CO 六项分测
- **有没有危险天气** — 气象预警按黄 / 橙 / 红分级着色，暴雨大风冰雹提前知道

再往下还有这些：

- 未来 24 小时温度曲线，配降水概率
- 昨天的高低温、AQI 和温差，今天到底比昨天热多少心里有数
- 日出日落和月相，本地天文算法直接算，不为这个多发一次网络请求
- 台风路径追踪（辅助源，淡季会是空的）
- 洗车 / 运动 / 感冒指数，不适合的那项标橙
- 多城市收藏一键切换，同名城市带省份标注，不会点错
- 桌面小组件三档：2x2 看一眼温度、4x2 加逐时、4x4 全都要
- 天气氛围层：下雨飘数据雨、下雪飘点、雾天呼吸噪点、雷暴走扫描线，只在内容底下画，三档强度可调，嫌花可以直接关
- ℃ / ℉ 全局切换，风速气压单位可选，用不上的模块能单独关掉

## 怎么装

去 [Releases](https://github.com/ZhishengZZ/ZhishengWeather/releases/latest) 下 `zhisheng-weather-v0.0.2.apk`，11 MB，装完打开就有天气——首次安装默认给你北京，不用先做任何设置。

数据不需要你操心：公开版走 Open-Meteo 全球公共源加小米源，免密钥，实况、逐时、15 天、空气质量、分钟降水、中文城市搜索全都在。想接和风天气当主源的话，见下面的[自己编译](#自己编译)。

> 系统会提示"来自未知来源"，因为这个包没走应用商店。介意的话可以照后面的步骤自己编一个，产物跟 Release 里的一样。

## 数据从哪来

天气应用最怕的是数据源挂了整屏空白。所以这里同时接了三家，谁掉线都还有画面：

| 角色 | 数据源 | 负责 |
|:--|:--|:--|
| 主源 | 和风天气 | 实况 / 预警 / 逐时 / 逐日 / 分钟降水 / 空气质量 / 生活指数 |
| 主源（免密钥） | Open-Meteo | 全球公共源，独立跑完整链路，公开版默认用它 |
| 补充 | 小米天气 | 昨日复盘 / 台风 / 逐日后半段 / 预警合并 |

设置里可以手动指定：自动优选、和风、小米、Open-Meteo 四选一。自动模式下，主源逐时不足两条时会按城市本地时区重建时间轴、用 Open-Meteo 补满 24 小时；逐日不满 15 天同样补尾，海外城市也凑得齐。

和风接口用 Ed25519 签名 JWT 认证，密钥只放本地 `local.properties`，仓库里不含任何凭据。

## 图标是自己做的

<p align="center"><img src="assets/icons_grid.png" width="500"/></p>

15 枚终端风天气图标，纯黑底、单色青双色调、锐利矢量边缘。每枚先用 AI 文生图生成，再走本地图像管线加工：

```
AI 生成 1024² ─▶ 亮度转 Alpha 键控（黑底转透明）─▶ 32bpp 边缘平滑 ─▶ 512px 归一 ─▶ 入库
```

晴、多云、阴、雾、小雨、大雨、雷暴、雪、风、霰都有，昼夜各一套。

## 0.0.2 更新了什么

这一版改动不小，主要是把"能用"推到了"好用"。

**数据源可以自己选了。** 以前和风密钥是硬门槛，现在 Open-Meteo 升成了独立主源——免密钥就能跑实况、逐时、逐日、空气质量、分钟降水和中文城市搜索。也就是说下载 APK 直接用，体验是完整的，不再是降级版。

**桌面小组件。** 2x2 / 4x2 / 4x4 三档，跟 App 同一套终端皮。锁屏抬手就能看温度，不用点进来。

**天气氛围层。** 下雨的时候屏幕上飘数据雨，下雪飘点，雾天是呼吸感噪点，雷暴走扫描线。都画在内容底下不挡字，三档强度可调，不喜欢直接关。

**可选定位。** 默认关闭，只有你打开开关并主动点定位时才申请一次粗略位置，而且只用系统定位，不引入 Google Play 服务。拒绝也没关系，手动搜城市照常用。

**设置页重做**，数据源、定位、单位（加了风速和气压）、显示模块、界面效果、关于，分门别类摊开。

修的问题里有几个挺影响观感的：夜里显示太阳图标；月相永远显示"残月"（朔望月序号基准差了 30 年）；逐日高低温倒挂；两个源同时失败时整屏红字报错；逐时温度曲线本来是一串断开的半段弧，现在改成跨格连续折线；系统返回键会直接退出 App；转屏丢失当前页面；预警卡片展开后错位。

0.0.1 Preview 的记录：首个公开预览，磷光终端 UI、15 枚 AI 生成图标、三源融合、`-PpublicBuild` 公开版构建链路。更早的改动看 commit 历史。

## 还没做好的地方

真实的项目总有不完美，先摊在这，免得下载了才发现：

- 台风模块用的是辅助源，可能是空的（和风台风接口没有免费额度）
- 海外城市看不到分钟级降水，免费档接口不给，这块会留白
- 预警跨源去重按标题精确匹配，两个源文案有出入时可能出现重复条目
- 图标是单色 stroke 风，多色版看后面有没有动力做
- 没上应用商店，安装时要过一遍"未知来源"提示

## 自己编译

需要 JDK 17 和 Android SDK 34，Gradle Wrapper 仓库自带。

```bash
git clone https://github.com/ZhishengZZ/ZhishengWeather.git
cd ZhishengWeather
./gradlew assembleDebug
```

**不填任何凭据就能跑**，走 Open-Meteo 加小米源。想接和风主源解锁生活指数、穿衣感冒这些，在工程根目录建 `local.properties`（不入库）：

```properties
sdk.dir=<你的 Android SDK 路径>
qw.host=<API Host，如 xxx.qweatherapi.com>
qw.project_id=<Project ID>
qw.kid=<Key ID>
qw.private_key=<Ed25519 私钥，单行>
```

发布包：

```bash
./gradlew assembleRelease                 # 需自备 keystore/zhisheng.jks
./gradlew assembleRelease -PpublicBuild   # 公开版：凭据强制为空 + 随库公开证书
```

技术栈是 Kotlin 2.0.21 + Jetpack Compose + Material 3，MVVM 配 `ViewModel` / `StateFlow`，网络用 Retrofit 2.11 + OkHttp 4.12 加 kotlinx-serialization，存储 DataStore，和风签名用 BouncyCastle 做 Ed25519。minSdk 26，targetSdk 34。代码结构和更细的说明见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 许可与声明

- [MIT](LICENSE) 开源，欢迎提 issue 和 PR：[贡献指南](CONTRIBUTING.md) · [行为准则](CODE_OF_CONDUCT.md) · [安全说明](SECURITY.md)
- 个人兴趣作品，界面美学致敬 EVA / NERV 终端风格，同人创作，不作商业用途
- 天气数据版权归 [和风天气](https://www.qweather.com/) · [Open-Meteo](https://open-meteo.com/) · 小米天气，数据仅供参考，防灾请以气象部门发布为准
- 用和风天气需要自己申请开发者凭据和 Ed25519 密钥，看他们官方文档
- 仓库不含任何随附凭据，也请你别把自己的 Key 提交到公开仓库

---

<p align="center"><sub>ZHISHENG WEATHER TERMINAL // PATTERN BLUE · 用磷光和 Kotlin 写的</sub></p>

