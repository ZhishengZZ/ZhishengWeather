# 贡献指南 // CONTRIBUTING

感谢你对 **枳生天气 // ZHISHENG WEATHER TERMINAL** 的兴趣。
项目不大，规矩也简单，说几点：

*Thanks for the interest in **Zhisheng Weather // ZHISHENG WEATHER TERMINAL**.
Small project, simple rules — a few notes below.*

## 01// 参与方式 WAYS TO CONTRIBUTE

- **报缺陷**：用 Issue 模板描述清楚——版本、设备、复现步骤、期望行为和实际行为。最好附截图。
  *Report bugs — fill the issue template properly: version, device, repro steps, expected vs. actual. Screenshots help.*
- **提想法**：先在 Discussions 里聊聊，聊透了再开 Feature Request，省得做了半天方向不对。
  *Propose ideas — talk it through in Discussions first, then open a Feature Request once the direction is settled. Saves everyone a wasted weekend.*
- **交代码**：Fork → 开分支 → 自己测过 → Pull Request。改了界面的话附前后对比截图。
  *Send code — fork, branch, test it yourself, open a PR. UI changes need before/after screenshots.*
- **画图标**：美术贡献有风格要求，见 05//。
  *Draw glyphs — art contributions have a style bar; see 05//.*

## 02// 开发环境 SETUP

- 需要 JDK 17、Android SDK 34，Gradle Wrapper 自带，clone 下来直接 `./gradlew assembleDebug` 就能跑。
  *Needs JDK 17 and Android SDK 34; the wrapper is included — clone and `./gradlew assembleDebug` just runs.*
- 用和风天气的话，在根目录建 `local.properties` 填凭据（见 README `07//`）；不填也能编译。
  *For the QWeather feed, drop your credentials into `local.properties` at the root (see README `07//`); it compiles fine without them.*
- 主题色统一引用 `ui/theme/Color.kt`，别硬编码色值。
  *Pull theme colors from `ui/theme/Color.kt`; never hardcode hex values.*
- 网络模型和领域模型分开（`data/*Models.kt` vs `model/Weather.kt`）。
  *Keep wire models and domain models apart (`data/*Models.kt` vs `model/Weather.kt`).*
- Compose 状态尽量上提到 ViewModel，别堆在组件里。
  *Hoist Compose state into the ViewModel; don't let it pile up inside widgets.*

## 03// 提交信息 COMMITS

一行描述清楚就行，建议带类型前缀：

*One clear line is enough; a type prefix is appreciated:*

| 前缀 / Prefix | 含义 / Meaning |
|:--|:--|
| `feat` | 新功能 / new feature |
| `fix` | 缺陷修复 / bug fix |
| `docs` | 文档 / documentation |
| `style` | UI / 美术 / UI & art |
| `refactor` | 重构（行为不变）/ refactor, no behavior change |
| `chore` | 构建 / 工具链 / build & tooling |

例 / *e.g.*：`fix: 修复同名城市串台` / `fix: stop same-named cities cross-wiring`

## 04// 代码风格 CODE STYLE

- 遵循 Kotlin 官方编码规范；Compose 状态上提至 `ViewModel`
  *Follow the official Kotlin coding conventions; hoist Compose state into the `ViewModel`.*
- 主题色一律引用 `ui/theme/Color.kt`（PhosphorGreen / NERVOrange / WireframeCyan），禁止硬编码色值
  *Theme colors come from `ui/theme/Color.kt` (PhosphorGreen / NERVOrange / WireframeCyan); hardcoded values get rejected.*
- 界面文案沿用终端风格：节号 `01// 02// …`，区块 `中文 // ENGLISH`
  *UI copy keeps the terminal voice: numbered sections `01// 02// …`, blocks labelled `中文 // ENGLISH`.*
- 网络模型与领域模型分离（`data/*Models.kt` vs `model/Weather.kt`）
  *Wire models stay separate from domain models (`data/*Models.kt` vs `model/Weather.kt`).*

## 05// 图标与美术 ASSETS

现在这套 15 枚图标走的是"纯黑底 → 亮度转透明 → 512px → `drawable-nodpi`"的管线，单色青双色调。新图标想加进来，跟这套风格保持一致，否则会被打回去。

*The current set of 15 glyphs runs a fixed pipeline — pure-black origin → luminance-to-transparent → 512px → `drawable-nodpi` — in a cyan duotone. New glyphs must match that style, or they go back over the wall.*

## 06// 安全红线 SECURITY

- **永不提交** `local.properties`、`keystore/`、任何 Key / 私钥 / 口令
  *Never commit `local.properties`, `keystore/`, or any key / private key / password.*
- 发现已泄露凭据：先轮换密钥，再走 SECURITY.md 通道私下联系维护者，别在 Issue 里喊
  *If you find leaked credentials: rotate them first, then contact the maintainer privately via the SECURITY.md channel — not in a public issue.*

## 07// 许可 LICENSE

提交代码即视为同意以本项目许可证（MIT，见 [LICENSE](LICENSE)）发布。

*Contributing code means you agree to release it under this project's license (MIT, see [LICENSE](LICENSE)).*
