# 贡献指南

想一起把枳生天气弄好？先谢过了。这个项目不大，规矩也简单，说几点：

## 你能做什么

- **报缺陷**：用 Issue 模板描述清楚——版本、设备、复现步骤、期望行为和实际行为。最好附截图。
- **提想法**：先在 Discussions 里聊聊，聊透了再开 Feature Request，省得做了半天方向不对。
- **交代码**：Fork → 开分支 → 自己测过 → Pull Request。改了界面的话附前后对比截图。
- **画图标**：美术贡献有风格要求，见下文"图标与美术"。

## 开发环境

- 需要 JDK 17、Android SDK 34，Gradle Wrapper 自带，clone 下来直接 `./gradlew assembleDebug` 就能跑。
- 用和风天气的话，在根目录建 `local.properties` 填凭据（见 README"自己构建"一节）；不填也能编译。
- 主题色统一引用 `ui/theme/Color.kt`，别硬编码色值。
- 网络模型和领域模型分开（`data/*Models.kt` vs `model/Weather.kt`）。
- Compose 状态尽量上提到 ViewModel，别堆在组件里。

## 提交信息

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

## 图标与美术

现在这套 15 枚图标走的是"纯黑底 → 亮度转透明 → 512px → `drawable-nodpi`"的管线，单色青双色调。新图标想加进来，跟这套风格保持一致，否则会被打回去。

## 安全红线

- 永远别提交 `local.properties`、`keystore/`、任何 Key / 私钥 / 口令。
- 发现凭据泄露：先轮换密钥，再通过 SECURITY.md 的通道私下联系，别在 Issue 里喊。

## 许可

提交代码即视为同意以本项目的 MIT 许可证（见 [LICENSE](LICENSE)）发布。
