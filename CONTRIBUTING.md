# 贡献指南 // CONTRIBUTING

感谢你对 **枳生天气 // ZHISHENG WEATHER TERMINAL** 的兴趣。
本文件约定参与方式与代码风格，让每一次合并都干净利落。

---

## 01// 参与路径 WAYS TO CONTRIBUTE

| 方式 | 入口 |
|:--|:--|
| 报缺陷 | Issue 模板 `Bug Report // 缺陷报告` |
| 提功能 | 先在 Discussions 讨论，再建 `Feature Request // 功能请求` |
| 交代码 | Fork → 分支 → 自测 → Pull Request |
| 美术 / 图标 | 见 05//，风格强约束 |

---

## 02// 开发环境 SETUP

- JDK 17 · Android SDK 34 · Gradle Wrapper 自带
- 根目录创建 `local.properties` 填入和风天气凭据（见 README `07//`）
- `./gradlew assembleDebug` 验证

---

## 03// 提交规范 COMMIT CONVENTION

类型前缀 + 简短描述，例：`feat: 逐时预报增加阵风字段`

```
feat     新功能
fix      缺陷修复
docs     文档
style    UI / 美术
refactor 重构（无行为变化）
chore    构建 / 工具链
```

---

## 04// 代码风格 CODE STYLE

- 遵循 Kotlin 官方编码规范；Compose 状态上提至 `ViewModel`
- 主题色一律引用 `ui/theme/Color.kt`（PhosphorGreen / NERVOrange / WireframeCyan），禁止硬编码色值
- 界面文案沿用终端双语风格：`中文 // ENGLISH`，节号 `01// 02// …`
- 网络模型与领域模型分离（`data/*Models.kt` vs `model/Weather.kt`）

---

## 05// 图标与美术 ASSETS

- 图标由百炼 `qwen-image` 系列生成 + 亮度→Alpha 透明化管线加工（见 README `03//`）
- 新图标须与现有 15 枚双色调风格一致：**纯黑底生成 → 透明化 → 512px → `drawable-nodpi`**
- README / 仓库展示素材统一放 `assets/`

---

## 06// 安全红线 SECURITY LINES

- **永不提交** `local.properties`、`keystore/`、任何 Key / 私钥 / 口令
- 发现已泄露凭据：先轮换密钥，再私聊维护者，走 SECURITY.md 流程

---

## 07// 许可 LICENSE

提交代码即视为同意以本项目许可证（MIT，见 [LICENSE](LICENSE)）发布。
