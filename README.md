# 枳生天气

一个黑底终端风的 Android 天气应用。没有广告和账号系统，下载安装后可以直接查看天气。

[下载最新版 APK](https://github.com/ZhishengZZ/ZhishengWeather/releases/latest) · [English](README.en.md)

当前版本：`0.0.2` · Android 8.0 及以上 · [MIT License](LICENSE)

## 截图

<table>
  <tr>
    <th width="50%">首页</th>
    <th width="50%">详细数据</th>
  </tr>
  <tr>
    <td><img src="assets/screenshot-home.jpg" alt="枳生天气首页：实况、预警、逐时预报和降水趋势" /></td>
    <td><img src="assets/screenshot-details.jpg" alt="枳生天气详细数据：湿度、风、气压和空气质量" /></td>
  </tr>
</table>

<table>
  <tr>
    <th width="33%">城市列表</th>
    <th width="33%">添加城市</th>
    <th width="33%">设置</th>
  </tr>
  <tr>
    <td><img src="assets/screenshot-cities.jpg" alt="枳生天气城市列表" /></td>
    <td><img src="assets/screenshot-add-city.jpg" alt="枳生天气添加城市界面" /></td>
    <td><img src="assets/screenshot-settings.jpg" alt="枳生天气设置界面" /></td>
  </tr>
</table>

截图取自 0.0.2 的开发构建，其中配置了和风天气。GitHub Release 里的公开 APK 不附带和风凭据，小米天气和 Open-Meteo 可以免配置使用。

## 能做什么

- 查看实况、气象预警、24 小时预报和 15 天趋势
- 查看未来两小时降水、空气质量、日出日落、月相和生活指数
- 保存多个城市并快速切换；首次安装默认添加北京
- 使用 2x2、4x2、4x4 三种桌面小组件
- 切换摄氏度/华氏度、风速和气压单位，按需关闭页面模块
- 开启雨、雪、雾、雷暴对应的背景效果，也可以全部关闭

不同数据源能提供的项目不完全一样。例如 Open-Meteo 不提供国内官方预警和生活指数；小米天气的台风、昨日数据等辅助项目也可能为空。应用会保留缺项，不会用猜测值填充。

## 安装

1. 打开 [Releases](https://github.com/ZhishengZZ/ZhishengWeather/releases/latest)。
2. 下载 `zhisheng-weather-v0.0.2.apk`。
3. 在 Android 8.0 或更高版本上安装。

APK 目前只通过 GitHub 发布，没有上架应用商店，因此 Android 会要求确认“允许安装未知应用”。如果不想安装现成包，可以按下文从源码构建。

## 0.0.2 的变化

- 增加数据源选择：自动优选、和风天气、小米天气、Open-Meteo
- 增加三种尺寸的桌面小组件
- 增加可关闭、可调强度的天气背景效果
- 增加按需定位；默认关闭，只申请粗略位置
- 重做设置页，补上风速、气压单位和模块开关
- 修正夜间图标、月相、逐日高低温、逐时曲线、返回键和转屏状态等问题

## 权限和数据

应用声明三个权限：

| 权限 | 用途 |
|:--|:--|
| 网络访问 | 请求天气和城市搜索数据 |
| 网络状态 | 判断当前是否可联网 |
| 粗略位置 | 可选；只在设置中启用定位并点击“定位当前城市”后申请 |

项目没有接入广告 SDK、统计 SDK，也没有账号或自建后端。城市列表和设置保存在本机。天气请求会把所选城市的坐标发送给当前数据源；使用“定位当前城市”时，坐标还会用于反查城市名称。具体调用可以在 [`app/src/main/kotlin/com/zhisheng/weather/data`](app/src/main/kotlin/com/zhisheng/weather/data) 中查看。

## 数据源

| 数据源 | 是否需要配置 | 主要用途 |
|:--|:--|:--|
| 和风天气 | 需要自己的开发者凭据 | 实况、预警、逐时/逐日、分钟降水、空气质量、生活指数 |
| 小米天气 | 不需要 | 国内天气、城市搜索、昨日数据和台风等补充项目 |
| Open-Meteo | 不需要 | 全球实况、逐时/逐日、空气质量、15 分钟降水和缺项补充 |

自动模式会按可用性降级。公开构建会强制清空和风凭据，不会把开发者的密钥打进 APK。

## 从源码构建

需要 JDK 17 和 Android SDK 34。Gradle Wrapper 已包含在仓库中。

```bash
git clone https://github.com/ZhishengZZ/ZhishengWeather.git
cd ZhishengWeather
./gradlew assembleDebug
```

Windows 可以运行：

```powershell
.\gradlew.bat assembleDebug
```

不配置和风天气也能编译和使用。Android SDK 路径以及可选的和风凭据放在根目录的 `local.properties` 中，这个文件已被忽略：

```properties
sdk.dir=<Android SDK 路径>
qw.host=<API Host>
qw.project_id=<Project ID>
qw.kid=<Key ID>
qw.private_key=<Ed25519 私钥，单行>
```

构建公开发布包：

```bash
./gradlew assembleRelease -PpublicBuild
```

`-PpublicBuild` 会清空和风凭据，并使用仓库中的公开签名文件。这个签名只用于让公开构建之间能够覆盖升级，不应当被视为私密或可信的身份凭证。

技术栈：Kotlin 2.0.21、Jetpack Compose、Material 3、ViewModel/StateFlow、Retrofit、OkHttp、kotlinx-serialization 和 DataStore。项目要求 `minSdk 26`，目标版本为 `targetSdk 34`。参与开发前可阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 已知限制

- 公开 APK 不包含和风天气凭据，依赖和风的官方预警和生活指数可能不可用
- Open-Meteo 的短时降水是 15 分钟粒度，不是逐分钟雷达临近预报
- 台风和昨日数据依赖辅助源，接口没有返回时相应区域会留空
- 跨数据源预警目前按标题去重，措辞不同的同一条预警可能重复出现
- 项目仍在早期阶段，天气数据仅供参考；防灾信息以当地气象部门发布为准

## 许可

代码以 [MIT License](LICENSE) 发布。天气数据的使用受各提供方条款约束： [和风天气](https://www.qweather.com/)、[Open-Meteo](https://open-meteo.com/) 和小米天气。
