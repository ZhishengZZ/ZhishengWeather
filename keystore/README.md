# keystore // 签名证书 // SIGNING KEYS

| 文件 / File | 用途 / Purpose | 入库 / In repo |
|:--|:--|:--|
| `public.jks` | `-PpublicBuild` 公开版签名证书（alias `public` / 口令 `public123`）· *signing key for the `-PpublicBuild` public APK (alias `public`, password `public123`)* | ✅ 非敏感，随库分发 · *non-sensitive, shipped on purpose* |
| `zhisheng.jks` | 个人满血版签名证书 · *personal full-feed signing key* | ❌ 敏感，永不入库 · *sensitive, never committed* |

公开证书随库是设计使然：只保证公开版 APK 安装 / 升级的签名一致，不含任何凭据信息。

*The public key being in the repo is by design: it only keeps install/upgrade signatures consistent for the public APK, and carries no credentials whatsoever.*
