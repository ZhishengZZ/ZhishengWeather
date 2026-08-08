![Zhisheng Weather · ZHISHENG WEATHER TERMINAL](assets/banner.png)

<p align="center">
  <b>Weather belongs on the first screen.</b><br/>
  A dark, terminal-style weather app for Android. No ads, no account, ready after install.
</p>

<p align="center">
  <a href="https://github.com/ZhishengZZ/ZhishengWeather/releases/download/v0.0.2/zhisheng-weather-v0.0.2.apk"><b>Download Zhisheng Weather 0.0.2</b></a>
  · <a href="#install">Install</a>
  · <a href="README.md">简体中文</a>
</p>

<p align="center">
  <img alt="Android 8.0+" src="https://img.shields.io/badge/Android-8.0%2B-3BFF8C?style=flat-square"/>
  <img alt="Version 0.0.2" src="https://img.shields.io/badge/version-0.0.2-FF6F1E?style=flat-square"/>
  <img alt="No ads" src="https://img.shields.io/badge/ads-none-3BFF8C?style=flat-square"/>
  <img alt="MIT License" src="https://img.shields.io/badge/license-MIT-3BFF8C?style=flat-square"/>
</p>

## A quick look

<table>
  <tr>
    <td align="center"><a href="assets/screenshot-home.jpg"><img src="assets/screenshot-home.jpg" width="165" alt="Zhisheng Weather home screen"/></a></td>
    <td align="center"><a href="assets/screenshot-details.jpg"><img src="assets/screenshot-details.jpg" width="165" alt="Detailed weather data"/></a></td>
    <td align="center"><a href="assets/screenshot-cities.jpg"><img src="assets/screenshot-cities.jpg" width="165" alt="Saved cities"/></a></td>
    <td align="center"><a href="assets/screenshot-add-city.jpg"><img src="assets/screenshot-add-city.jpg" width="165" alt="City search"/></a></td>
    <td align="center"><a href="assets/screenshot-settings.jpg"><img src="assets/screenshot-settings.jpg" width="165" alt="Settings"/></a></td>
  </tr>
  <tr>
    <td align="center"><sub>Home</sub></td>
    <td align="center"><sub>Details</sub></td>
    <td align="center"><sub>Cities</sub></td>
    <td align="center"><sub>Search</sub></td>
    <td align="center"><sub>Settings</sub></td>
  </tr>
</table>

<p align="center"><sub>Click a screenshot for the full image. These are from a v0.0.2 development build; personal status-bar details have been removed.</sub></p>

## Why I built it

Before leaving home, I usually want three answers: how cold it feels, whether rain is coming, and what the air is like.

I did not want to sit through a splash ad or create an account to get them, so I wrote Zhisheng Weather for myself. Open the app and the useful parts come first: current conditions, alerts, the next 24 hours, and the daily outlook. Humidity, pressure, air quality, and life indices are one scroll farther down.

The look is personal too. Black backgrounds, thin rules, phosphor colors, and a lot of information per screen. It will not suit everyone. If you like terminal interfaces, though, it should feel familiar immediately.

## What it answers

- **Should I take an umbrella?** The next two hours of precipitation are plotted directly. QWeather can supply minute-level data; public Open-Meteo data uses 15-minute intervals.
- **What should I wear?** Temperature, feels-like temperature, wind, and the dressing index sit together instead of leaving you to judge from one number.
- **When will it cool down?** The home screen keeps both a 24-hour curve and a 15-day high/low outlook.
- **Should I open the windows?** Expand AQI for PM2.5, PM10, O₃, NO₂, SO₂, and CO readings.
- **Is severe weather on the way?** Alerts are shown with their level and full text when the active provider supplies them. Missing fields stay missing; the app does not invent replacements.

There are also saved cities, sunrise and sunset, moon phase, yesterday's weather, auxiliary typhoon data, and home-screen widgets in 2x2, 4x2, and 4x4 sizes. Rain, snow, fog, and thunderstorms have optional background effects with adjustable intensity.

Temperature, wind-speed, and pressure units are configurable. Individual sections can be hidden as well.

## The icons belong to the interface

<p align="center"><img src="assets/icons_grid.png" width="560" alt="Terminal-style weather icon set"/></p>

These are not stock icons assembled from a library. The set contains 15 weather glyphs. Each started as generated artwork, then went through a local image pass to remove the black background, clean the edges, normalize it to 512 px, and prepare it for the app's day and night states.

```text
1024² source → luminance to alpha → edge smoothing → 512 px normalize → bundle
```

They keep the same cyan phosphor and edge treatment as the rest of the interface.

## Install

1. [Download `zhisheng-weather-v0.0.2.apk`](https://github.com/ZhishengZZ/ZhishengWeather/releases/download/v0.0.2/zhisheng-weather-v0.0.2.apk) (about 12 MB).
2. Read the notes first on the [v0.0.2 release page](https://github.com/ZhishengZZ/ZhishengWeather/releases/tag/v0.0.2), if you prefer.
3. Install it on Android 8.0 or later.

The APK is distributed on GitHub rather than through an app store, so Android will ask you to allow installation from an unknown source. That is an install-channel warning, not an extra permission requested by the app. Beijing is shown on first launch; you can then search for and save your own cities.

The public APK contains no QWeather credentials. Xiaomi Weather and Open-Meteo work without setup. If you want to use your own QWeather account, build from source using the instructions below.

## What changed in 0.0.2

Version 0.0.2 makes the public APK useful without private credentials and cleans up a long list of UI and data issues.

- Choose between Auto, QWeather, Xiaomi Weather, and Open-Meteo
- Run a complete key-free path through Open-Meteo for global current, hourly, daily, air-quality, and 15-minute precipitation data
- Add home-screen widgets in 2x2, 4x2, and 4x4 sizes
- Add optional rain, snow, fog, and thunderstorm ambience with an intensity control
- Make approximate location opt-in and disabled by default
- Reorganize settings and add wind, pressure, and per-section controls
- Fix night icons, moon phase, daily highs and lows, the hourly curve, back navigation, rotation state, and expanded alert layout

## Permissions and data

The app declares three permissions:

| Permission | Purpose |
|:--|:--|
| Internet | Fetch weather data and city-search results |
| Network state | Check whether a connection is available |
| Approximate location | Optional; requested only after location is enabled and you tap “Locate current city” |

There is no ad SDK, analytics SDK, account system, or project-operated backend. Saved cities and preferences stay in local storage. Weather requests send the selected city's coordinates to the active provider. If you use location, coordinates are also used to resolve the city name. The relevant code is under [`app/src/main/kotlin/com/zhisheng/weather/data`](app/src/main/kotlin/com/zhisheng/weather/data).

## Data providers

| Provider | Setup | Main coverage |
|:--|:--|:--|
| QWeather | Your own developer credentials | Current conditions, alerts, hourly/daily, minute precipitation, AQI, and life indices |
| Xiaomi Weather | None | Domestic weather, city search, yesterday's weather, typhoons, and supplementary fields |
| Open-Meteo | None | Global current/hourly/daily data, AQI, 15-minute precipitation, and fallback coverage |

Auto mode falls back according to availability. Coverage differs between providers: Open-Meteo does not supply Chinese official alerts or life indices, while auxiliary Xiaomi fields such as typhoons and yesterday's weather may be empty. The app leaves those gaps visible instead of manufacturing a complete-looking result.

QWeather uses an Ed25519-signed JWT. Public builds forcibly clear its credentials so a developer key cannot end up in the APK.

## Build from source

You need JDK 17 and Android SDK 34. The Gradle Wrapper is included.

```bash
git clone https://github.com/ZhishengZZ/ZhishengWeather.git
cd ZhishengWeather
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

The app builds and runs without QWeather credentials. Put the Android SDK path and optional QWeather configuration in the ignored root-level `local.properties` file.

```properties
sdk.dir=<Android SDK path>
qw.host=<API host>
qw.project_id=<Project ID>
qw.kid=<Key ID>
qw.private_key=<single-line Ed25519 private key>
```

For the public release variant:

```bash
./gradlew assembleRelease -PpublicBuild
```

`-PpublicBuild` clears QWeather credentials and uses the public signing file stored in the repository. That signature only keeps public builds upgrade-compatible; it is not a private or trusted identity credential.

The project uses Kotlin 2.0.21, Jetpack Compose, Material 3, ViewModel/StateFlow, Retrofit, OkHttp, kotlinx-serialization, and DataStore. It has `minSdk 26` and `targetSdk 34`. See [CONTRIBUTING.md](CONTRIBUTING.md) for the code layout and contribution notes.

## Current limitations

- The public APK has no QWeather credentials, so QWeather-only alerts and life indices may be unavailable
- Open-Meteo precipitation uses 15-minute intervals, not a minute-by-minute radar nowcast
- Typhoon and yesterday's weather depend on an auxiliary provider and may be empty
- Alerts are deduplicated by exact title; differently worded copies can appear twice
- This is still an early project. For safety decisions, follow your local meteorological authority

## License

The code is available under the [MIT License](LICENSE). Issues are welcome, as are changes that follow [CONTRIBUTING.md](CONTRIBUTING.md).

Weather data remains subject to the terms of [QWeather](https://www.qweather.com/), [Open-Meteo](https://open-meteo.com/), and Xiaomi Weather.

---

<p align="center"><sub>ZHISHENG WEATHER TERMINAL // PATTERN BLUE · built with phosphor and Kotlin</sub></p>
