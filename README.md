![枳生天气 · ZHISHENG WEATHER TERMINAL](assets/banner.png)

<p align="center">
  <b>把天气摆在第一屏。</b><br/>
  一个磷光终端风的 Android 天气 App。没有广告，不用登录，装好就能看。
</p>

<p align="center">
  <a href="https://github.com/ZhishengZZ/ZhishengWeather/releases/download/v0.0.2/zhisheng-weather-v0.0.2.apk"><b>下载公共版 0.0.2</b></a>
  · <a href="#安装">安装说明</a>
  · <a href="README.en.md">English</a>
</p>

<p align="center"><sub>Release 提供免配置公共版；自备和风天气凭据，可从源码构建满血版。</sub></p>

<p align="center">
  <img alt="Android 8.0+" src="https://img.shields.io/badge/Android-8.0%2B-3BFF8C?style=flat-square"/>
  <img alt="版本 0.0.2" src="https://img.shields.io/badge/版本-0.0.2-FF6F1E?style=flat-square"/>
  <img alt="没有广告" src="https://img.shields.io/badge/广告-没有-3BFF8C?style=flat-square"/>
  <img alt="MIT License" src="https://img.shields.io/badge/License-MIT-3BFF8C?style=flat-square"/>
</p>

## 先看界面

<table>
  <tr>
    <td align="center"><a href="assets/screenshot-home.jpg"><img src="assets/screenshot-home.jpg" width="165" alt="枳生天气首页"/></a></td>
    <td align="center"><a href="assets/screenshot-details.jpg"><img src="assets/screenshot-details.jpg" width="165" alt="详细天气数据"/></a></td>
    <td align="center"><a href="assets/screenshot-cities.jpg"><img src="assets/screenshot-cities.jpg" width="165" alt="城市列表"/></a></td>
    <td align="center"><a href="assets/screenshot-add-city.jpg"><img src="assets/screenshot-add-city.jpg" width="165" alt="添加城市"/></a></td>
    <td align="center"><a href="assets/screenshot-settings.jpg"><img src="assets/screenshot-settings.jpg" width="165" alt="设置界面"/></a></td>
  </tr>
  <tr>
    <td align="center"><sub>首页</sub></td>
    <td align="center"><sub>详细数据</sub></td>
    <td align="center"><sub>城市</sub></td>
    <td align="center"><sub>搜索</sub></td>
    <td align="center"><sub>设置</sub></td>
  </tr>
</table>

<p align="center"><sub>点击截图可查看原图。截图来自 0.0.2 开发构建，已去除状态栏中的地址、电量等个人信息。</sub></p>

## 我为什么做它

每天出门前，我想知道的其实很具体：冷不冷，会不会下雨，空气怎么样。

我不想先看开屏广告，也不想为了看天气注册账号。于是我给自己写了枳生天气。打开 App，实况、预警、逐时变化和未来几天的趋势都在前面；想看湿度、气压、空气质量和生活指数，接着往下滑就行。

界面也是照自己的喜好做的。黑色底、细线、单色磷光，高信息密度，没有大块图片占掉半个屏幕。它不是所有人都会喜欢的样子，但喜欢磷光终端界面的人，大概一眼就知道它在做什么。

## 出门前能看什么

- **带不带伞：** 未来两小时降水直接画出来。和风天气可提供分钟级数据；Open-Meteo 的公开数据是 15 分钟粒度。
- **今天穿多少：** 当前温度、体感温度、风和穿衣指数放在一起看，不用只凭一个温度数字猜。
- **哪天会降温：** 24 小时曲线和 15 天高低温趋势都保留在主界面。
- **空气适不适合开窗：** 除了 AQI，还能展开看 PM2.5、PM10、O₃、NO₂、SO₂ 和 CO。
- **有没有危险天气：** 数据源提供预警时，App 会按等级显示标题和正文；缺少的数据就留空，不拿估算值补位。

还有多城市收藏、日出日落、月相、昨日天气、台风辅助数据，以及 2x2、4x2、4x4 三种桌面小组件。雨、雪、雾和雷暴有对应的背景效果，强度可调，不喜欢也可以全部关闭。

温度、风速和气压单位可以分别设置，用不到的页面模块也能单独关掉。

## 图标也是界面的一部分

<p align="center"><img src="assets/icons_grid.png" width="560" alt="枳生天气磷光终端风天气图标"/></p>

这组图标不是从现成图标库里拼出来的。15 枚天气图标最初由生成模型产出，再经过本地处理：去黑底、转透明、修边、统一到 512 px，最后按白天和夜间状态接进 App。

```text
1024² 原图 → 亮度转 Alpha → 边缘平滑 → 512 px 归一 → 入库
```

它们沿用界面的青色磷光，在黑底上保持同一套轮廓和明暗关系。

## 公共版和满血版

GitHub Release 里提供的是公共版。它不是试用包：实况、24 小时预报、15 天趋势、空气质量、15 分钟降水、城市搜索和桌面小组件都能直接使用。

| | 公共版 | 满血版 |
|:--|:--|:--|
| 怎么获得 | 从 [Release](https://github.com/ZhishengZZ/ZhishengWeather/releases/tag/v0.0.2) 直接下载 | 拉取源码后自行构建 |
| 数据链路 | Open-Meteo + 小米天气 | 和风天气主源 + 小米天气、Open-Meteo 补充 |
| 是否需要配置 | 不需要 | 需要自己的和风天气 Ed25519 凭据 |
| 主要区别 | 开箱即用，和风独占项目留空 | 可显示和风官方预警、分钟级降水和生活指数，具体取决于账号接口权限 |

只想装上使用，下载公共版即可。满血版不能预先打包进 Release，因为开发者凭据不能跟着 APK 一起公开。

## 安装

1. [下载 `zhisheng-weather-v0.0.2.apk`](https://github.com/ZhishengZZ/ZhishengWeather/releases/download/v0.0.2/zhisheng-weather-v0.0.2.apk)（约 12 MB）。
2. 如果想先看更新记录，可以打开 [v0.0.2 Release 页面](https://github.com/ZhishengZZ/ZhishengWeather/releases/tag/v0.0.2)。
3. 在 Android 8.0 或更高版本上安装。

APK 目前只在 GitHub 发布，所以 Android 会提示允许“未知来源应用”。这是安装渠道提示，不是 App 额外申请的权限。首次打开会先显示北京，之后可以搜索并保存自己的城市。

上面的 APK 是公共版，不附带和风天气密钥。想接入自己的和风天气账号，可以按后面的步骤构建满血版。

## 0.0.2 改了什么

0.0.2 先解决公开 APK 不填私有凭据也要好用的问题，也把一批长期看着别扭的界面和数据错误收了一遍。

- 数据源可以在自动优选、和风天气、小米天气和 Open-Meteo 之间切换
- Open-Meteo 可以独立提供全球实况、逐时、逐日、空气质量和 15 分钟降水，公开版不填密钥也能运行
- 新增 2x2、4x2、4x4 三种桌面小组件
- 新增雨、雪、雾、雷暴背景效果，并提供强度和总开关
- 定位改为按需启用，默认关闭，只申请粗略位置
- 设置页重新整理，补上风速、气压单位和模块开关
- 修正夜间图标、月相、逐日高低温、逐时曲线、返回键、转屏状态和预警卡片错位等问题

## 权限和数据

应用只声明三个权限：

| 权限 | 用途 |
|:--|:--|
| 网络访问 | 请求天气和城市搜索数据 |
| 网络状态 | 判断当前是否联网 |
| 粗略位置 | 可选；启用定位并点击“定位当前城市”后才申请 |

项目没有广告 SDK、统计 SDK、账号系统或自建后端。城市列表和设置保存在本机。天气请求会把所选城市的坐标发给当前数据源；使用定位时，坐标还会用于反查城市名称。相关代码在 [`app/src/main/kotlin/com/zhisheng/weather/data`](app/src/main/kotlin/com/zhisheng/weather/data) 下，可以直接检查。

## 数据从哪里来

| 数据源 | 是否需要配置 | 主要提供 |
|:--|:--|:--|
| 和风天气 | 需要自己的开发者凭据 | 实况、预警、逐时/逐日、分钟降水、空气质量、生活指数 |
| 小米天气 | 不需要 | 国内天气、城市搜索、昨日天气、台风等辅助数据 |
| Open-Meteo | 不需要 | 全球实况、逐时/逐日、空气质量、15 分钟降水和缺项补充 |

自动模式会根据数据是否可用进行降级。不同来源能给出的项目并不完全一致：Open-Meteo 没有国内官方预警和生活指数，小米天气的台风、昨日天气等辅助项目也可能为空。App 会把缺项留出来，不会伪造一个看似完整的结果。

和风天气使用 Ed25519 签名 JWT。公开构建会强制清空相关凭据，开发者密钥不会被打进 APK。

## 从源码构建

需要 JDK 17 和 Android SDK 34，Gradle Wrapper 已包含在仓库中。

```bash
git clone https://github.com/ZhishengZZ/ZhishengWeather.git
cd ZhishengWeather
```

构建满血版时，把 Android SDK 路径和自己的和风凭据放在根目录的 `local.properties` 中；这个文件已被 Git 忽略。

```properties
sdk.dir=<Android SDK 路径>
qw.host=<API Host>
qw.project_id=<Project ID>
qw.kid=<Key ID>
qw.private_key=<Ed25519 私钥，单行>
```

然后构建调试包：

```bash
./gradlew assembleDebug
```

Windows 使用 `.\gradlew.bat assembleDebug`。只要 `local.properties` 中的和风凭据有效，这个包就是满血版；不填凭据也能运行，但会使用与公共版相同的免密钥数据链路。

发布构建分为两种：

```bash
./gradlew assembleRelease                 # 满血版；需要自备 keystore/zhisheng.jks
./gradlew assembleRelease -PpublicBuild   # 公共版；强制清空和风凭据
```

`-PpublicBuild` 会清空和风凭据，并使用仓库中的公开签名文件。这个签名只用于让公开构建之间能够覆盖升级，不应当被当作私密或可信的身份凭证。

项目使用 Kotlin 2.0.21、Jetpack Compose、Material 3、ViewModel/StateFlow、Retrofit、OkHttp、kotlinx-serialization 和 DataStore。`minSdk 26`，`targetSdk 34`。代码结构和提交说明见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 目前的限制

- 公开 APK 不包含和风天气凭据，依赖和风的官方预警和生活指数可能不可用
- Open-Meteo 的短时降水是 15 分钟粒度，不是逐分钟雷达临近预报
- 台风和昨日天气依赖辅助数据源，接口没有返回时对应区域会留空
- 跨数据源预警目前按标题去重，措辞不同的同一条预警可能重复出现
- 项目还在早期阶段；涉及防灾的信息，请以当地气象部门发布为准

## 许可

代码以 [MIT License](LICENSE) 发布，欢迎提交 [Issue](https://github.com/ZhishengZZ/ZhishengWeather/issues) 或参照 [CONTRIBUTING.md](CONTRIBUTING.md) 参与开发。

天气数据的使用受各提供方条款约束：[和风天气](https://www.qweather.com/)、[Open-Meteo](https://open-meteo.com/) 和小米天气。

---

<p align="center"><sub>ZHISHENG WEATHER TERMINAL // PATTERN BLUE · 用磷光和 Kotlin 写的</sub></p>
