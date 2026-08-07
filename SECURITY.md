# 安全政策 // SECURITY POLICY

## 01// 受支持版本 SUPPORTED VERSIONS

| 版本 | 安全支持 |
|:--|:--|
| 1.2.x | ✅ |
| < 1.2 | ❌ |

## 02// 漏洞上报 REPORTING A VULNERABILITY

- **不要**通过公开 Issue 报告安全问题
- 首选通道：仓库 **Security → Advisories → Report a vulnerability**（私密漏洞上报）
- 备选通道：私信维护者
- 报告应包含：受影响版本、复现步骤、影响面评估、修复建议（如有）
- **报告中请勿附带真实凭据、私钥或个人数据**

## 03// 响应承诺 RESPONSE COMMITMENT

- 48 小时内确认收到
- 7 天内给出评估结论与修复排期
- 修复发布后在 Release Notes 中致谢（如报告者愿意署名）

## 04// 特别说明 NOTES

- 和风天气凭据仅存于各人本地 `local.properties`，不随仓库分发
- 构建产物（APK）会内嵌凭据，**请勿公开发布你带凭据构建的 APK**
- 若在仓库历史中发现任何凭据痕迹，请按安全漏洞上报，勿公开扩散
