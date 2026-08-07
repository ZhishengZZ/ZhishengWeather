# 贡献指南 // CONTRIBUTING

感谢你对 **枳生天气 // ZHISHENG WEATHER TERMINAL** 的兴趣。
项目不大，规矩也简单，说几点：

## 01// 参与方式 WAYS TO CONTRIBUTE

- **报缺陷**：用 Issue 模板描述清楚——版本、设备、复现步骤、期望行为和实际行为。最好附截图。
- **提想法**：先在 Discussions 里聊聊，聊透了再开 Feature Request，省得做了半天方向不对。
- **交代码**：Fork → 开分支 → 自己测过 → Pull Request。改了界面的话附前后对比截图。
- **画图标**：美术贡献有风格要求，见 05//。

## 02// 开发环境 SETUP

- 需要 JDK 17、Android SDK 34，Gradle Wrapper 自带，clone 下来直接 `./gradlew assembleDebug` 就能跑。
- 用和风天气的话，在根目录建 `local.properties` 填凭据（见 README `07//`）；不填也能编译。
- 主题色统一引用 `ui/theme/Color.kt`，别硬编码色值。
- 网络模型和领域模型分开（`data/*Models.kt` vs `model/Weather.kt`）。
- Compose 状态尽量上提到 ViewModel，别堆在组件里。

## 03// 提交信息 COMMITS

一行描述清楚就行，建议带类型前缀：

| 前缀 | 含义 |
|:--|:--|
| `feat` | 新功能 |
| `fix` | 缺陷修复 |
| `docs` | 文档 |
| `style` | UI / 美术 |
| `refactor` | 重构（行为不变） |
| `chore` | 构建 / 工具链 |

例：`fix: 修复同名城市串台`

## 04// 代码风格 CODE STYLE

- 遵循 Kotlin 官方编码规范；Compose 状态上提至 `ViewModel`
- 主题色一律引用 `ui/theme/Color.kt`（PhosphorGreen / NERVOrange / WireframeCyan），禁止硬编码色值
- 界面文案沿用终端风格：节号 `01// 02// …`，区块 `中文 // ENGLISH`
- 网络模型与领域模型分离（`data/*Models.kt` vs `model/Weather.kt`）

## 05// 图标与美术 ASSETS

现在这套 15 枚图标走的是"纯黑底 → 亮度转透明 → 512px → `drawable-nodpi`"的管线，单色青双色调。新图标想加进来，跟这套风格保持一致，否则会被打回去。

## 06// 安全红线 SECURITY

- **永不提交** `local.properties`、`keystore/`、任何 Key / 私钥 / 口令
- 发现已泄露凭据：先轮换密钥，再走 SECURITY.md 通道私下联系维护者，别在 Issue 里喊

## 07// 许可 LICENSE

提交代码即视为同意以本项目许可证（MIT，见 [LICENSE](LICENSE)）发布。
