![Zhisheng Weather · ZHISHENG WEATHER TERMINAL](assets/banner.png)

<p align="center">
  <b>Zhisheng Weather</b> · ZHISHENG WEATHER TERMINAL<br/>
  No ads, no sign-in. Install it, open it, there's the weather.<br/>
  <sub>A phosphor terminal in black and cyan · MELCHIOR-1 · BALTHASAR-2 · CASPER-3</sub>
</p>

<p align="center">
  <a href="https://github.com/ZhishengZZ/ZhishengWeather/releases/latest"><b>⬇ Download the APK · 11 MB · ready to use</b></a>
</p>

<p align="center">
  <img alt="version" src="https://img.shields.io/badge/version-0.0.2-FF6F1E?style=flat-square"/>
  <img alt="size" src="https://img.shields.io/badge/size-11MB-3BFF8C?style=flat-square"/>
  <img alt="permissions" src="https://img.shields.io/badge/permissions-only%203-3BFF8C?style=flat-square"/>
  <img alt="ads and IAP" src="https://img.shields.io/badge/ads%20%26%20IAP-0-3BFF8C?style=flat-square"/>
  <img alt="license" src="https://img.shields.io/badge/license-MIT-3BFF8C?style=flat-square"/>
</p>

<p align="center">
  <b>English</b> · <a href="./README.md">简体中文</a>
</p>

---

I built this because of one small thing: I wanted to know whether to take an umbrella before leaving the house.

Turns out that's hard to do now. Five seconds of splash ad, a paywall on launch, sign up, grant a pile of permissions you'll never use — and the actual weather is somewhere on the third screen. I looked around for something better, didn't find it, and wrote my own.

So, Zhisheng Weather:

- **No ads.** No splash screen, no premium pop-up, no limited-time offer.
- **No account.** Nothing to sign up for, which means nothing to sell.
- **Three permissions total** — internet, network state, coarse location. Location is off by default and never requested unless you turn it on yourself.
- **No telemetry.** There isn't a single ad or analytics SDK in the dependency list; your location and usage go nowhere.
- **11 MB**, runs on Android 8.0 and up, fine on older hardware.
- **MIT licensed.** All the code is in this repo — if you'd rather not trust a stranger's APK, build your own.

The interface is a black-and-cyan phosphor terminal, which is admittedly a personal taste. Information density is high on purpose: current conditions, hourly, minute-level rain and the daily trend all live on one screen, so there's no paging back and forth.

## What it looks like

| Home | Telemetry | Search | Settings |
|:---:|:---:|:---:|:---:|
| <img src="assets/screen_home.png" width="210"/> | <img src="assets/screen_telemetry.png" width="210"/> | <img src="assets/screen_search.png" width="210"/> | <img src="assets/screen_settings.png" width="210"/> |
| Live · alerts · hourly · minute rain | Humidity / wind / pressure · air quality · indices | Multi-city search and switching | Feeds / units / module toggles |

## What it tells you

The things you actually check on your way out the door:

- **Umbrella or not** — a per-minute rain chart for the next two hours, so you can see when it starts and how long it lasts
- **What to wear** — feels-like temperature plus a dressing index, instead of guessing from a number
- **When it gets cold** — 15 days of highs and lows on auto-tinted trend bars; a cold snap picks itself out
- **Whether to open the windows** — headline AQI with a six-way split: PM2.5 / PM10 / O₃ / NO₂ / SO₂ / CO
- **Anything dangerous coming** — hazard alerts tinted yellow / orange / red, so storms, gales and hail arrive with warning

And further down:

- A 24-hour temperature curve with precipitation odds
- Yesterday's high, low, AQI and the delta, so you know how much today really differs
- Sunrise, sunset and moon phase, computed on-device — not one extra network request for it
- Typhoon tracking (auxiliary feed, empty in the off-season)
- Car wash / sports / cold-risk indices, with whichever says "don't" glowing orange
- Saved cities with one-tap switching; same-named cities carry their province so you can't pick the wrong one
- Home screen widgets in three sizes: 2x2 for a glance at the temperature, 4x2 adds hourly, 4x4 for everything
- Weather ambience — data-rain when it rains, drifting snow, breathing fog noise, thunderstorm sweeps. Drawn beneath the text, three intensity levels, or off entirely
- Global °C / °F, selectable wind and pressure units, and any module you don't want can be switched off

## Getting it

Grab `zhisheng-weather-v0.0.2.apk` from [Releases](https://github.com/ZhishengZZ/ZhishengWeather/releases/latest) — 11 MB, and there's weather on screen the moment you open it. A first install seeds Beijing so you don't have to configure anything first.

You don't need to arrange data access: the public build runs on the Open-Meteo global feed plus Xiaomi, no key required, with live conditions, hourly, 15-day, air quality, minute-level rain and city search all present. To wire in QWeather as the primary feed, see [building it yourself](#building-it-yourself).

> Android will warn about an "unknown source", because this package doesn't come from an app store. If that bothers you, build it yourself with the steps below — the output matches what's in Releases.

## Where the data comes from

The worst failure mode for a weather app is a dead feed and a blank screen. So three of them are wired in, and any one can drop without taking the display with it:

| Role | Feed | Covers |
|:--|:--|:--|
| Primary | QWeather | Live / alerts / hourly / daily / minute rain / air quality / life indices |
| Primary (key-free) | Open-Meteo | Global public feed, runs the full chain on its own; the public build's default |
| Supplement | Xiaomi Weather | Yesterday's recap / typhoons / the back half of the daily list / alert merging |

Settings let you pin one: auto-select, QWeather, Xiaomi or Open-Meteo. On auto, if the primary returns fewer than two hourly entries the timeline is rebuilt in the city's local timezone and Open-Meteo fills out all 24 hours; daily lists shorter than 15 days get their tail backfilled the same way, overseas cities included.

QWeather is called with an Ed25519 signed JWT. The key lives only in your local `local.properties` — no credentials ship in this repo.

## The icons are handmade

<p align="center"><img src="assets/icons_grid.png" width="500"/></p>

Fifteen terminal-grade weather glyphs: pure black ground, cyan duotone, crisp vector edges. Each one starts as AI text-to-image output and then goes through a local imaging pipeline:

```
AI-generated 1024² ─▶ luminance-to-alpha keying (black becomes transparent) ─▶ 32bpp edge smoothing ─▶ 512px normalize ─▶ bundled
```

Clear, partly cloudy, overcast, fog, light rain, heavy rain, thunderstorm, snow, wind and graupel are all covered, with a day and a night variant each.

## What's new in 0.0.2

A substantial round. Mostly about moving from "works" to "nice to use".

**You can pick your feed now.** A QWeather key used to be a hard requirement. Open-Meteo has been promoted to a primary feed in its own right — live conditions, hourly, daily, air quality, minute-level rain and city search, all without a key. Download the APK and what you get is the full experience, not a degraded fallback.

**Home screen widgets.** Three sizes (2x2 / 4x2 / 4x4) in the same terminal dress as the app, so the temperature is there without opening anything.

**Weather ambience.** Data-rain falls when it rains, snow drifts, fog breathes as noise, thunderstorms run sweep lines. All of it draws beneath the content so nothing gets obscured — three intensity levels, or off.

**Optional location.** Off by default. A single coarse-location request happens only when you enable the switch and tap locate, and it uses system location only, with no Google Play Services. Decline it and manual search works exactly as before.

**Settings rebuilt** around feeds, location, units (wind speed and pressure now included), visible modules, visual effects and about.

Among the fixes, a few were visibly wrong: a sun icon at night; the moon phase permanently stuck on "waning crescent" (the lunation index was benchmarked 30 years off); inverted daily highs and lows; a full-screen red error when both feeds failed; the hourly temperature curve, previously a row of disconnected half-arcs, now a continuous polyline across cells; the system back button quitting the app outright; rotation losing the current page; and alert cards misaligning once expanded.

For the record, v0.0.1 Preview was the first public drop: phosphor-terminal UI, 15 AI-generated glyphs, three-feed fusion, and the `-PpublicBuild` pipeline. Anything earlier lives in the commit history.

## Rough edges

Real software has them. Here they are up front, rather than after you've downloaded it:

- Typhoon tracking rides the auxiliary feed and may come up empty (QWeather's typhoon API has no free tier)
- Minute-level precipitation isn't available for overseas cities — the free tier doesn't serve it, so that block stays blank
- Cross-feed alert dedup matches on exact titles; when two feeds word things differently you can get duplicates
- The glyphs are single-stroke monochrome; a multicolor set exists only if motivation strikes
- Not on any app store, so installing means clearing the "unknown source" prompt

## Building it yourself

You'll need JDK 17 and Android SDK 34. The Gradle wrapper is in the repo.

```bash
git clone https://github.com/ZhishengZZ/ZhishengWeather.git
cd ZhishengWeather
./gradlew assembleDebug
```

**It builds and runs with no credentials at all**, on Open-Meteo plus Xiaomi. To add QWeather as the primary feed and unlock the life indices, create `local.properties` at the project root (never committed):

```properties
sdk.dir=<your Android SDK path>
qw.host=<API host, e.g. xxx.qweatherapi.com>
qw.project_id=<Project ID>
qw.kid=<Key ID>
qw.private_key=<Ed25519 private key, single line>
```

Release builds:

```bash
./gradlew assembleRelease                 # bring your own keystore/zhisheng.jks
./gradlew assembleRelease -PpublicBuild   # public build: credentials forced empty + in-repo public key
```

The stack is Kotlin 2.0.21 with Jetpack Compose and Material 3, MVVM on `ViewModel` / `StateFlow`, Retrofit 2.11 + OkHttp 4.12 with kotlinx-serialization for networking, DataStore for storage, and BouncyCastle for the Ed25519 signing. minSdk 26, targetSdk 34. Code layout and the longer notes are in [CONTRIBUTING.md](CONTRIBUTING.md).

## License & notes

- [MIT](LICENSE) licensed; issues and PRs welcome — [CONTRIBUTING](CONTRIBUTING.md) · [CODE_OF_CONDUCT](CODE_OF_CONDUCT.md) · [SECURITY](SECURITY.md)
- A personal, for-fun project. The interface is a fan homage to the EVA / NERV terminal aesthetic, non-commercial by intent.
- Weather data belongs to its providers: [QWeather](https://www.qweather.com/) · [Open-Meteo](https://open-meteo.com/) · Xiaomi Weather. Figures are for reference; for anything safety-critical, follow your official meteorological service.
- Using QWeather requires your own developer credentials and an Ed25519 key — see their docs.
- No credentials ship with this repo, and please don't commit yours to a public one either.

---

<p align="center"><sub>ZHISHENG WEATHER TERMINAL // PATTERN BLUE · built with phosphor and Kotlin</sub></p>

