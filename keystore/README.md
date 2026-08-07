# keystore // 签名证书

| 文件 | 用途 | 入库 |
|:--|:--|:--|
| `public.jks` | `-PpublicBuild` 公开版签名证书（alias `public` / 口令 `public123`） | ✅ 非敏感，随库分发 |
| `zhisheng.jks` | 个人满血版签名证书 | ❌ 敏感，永不入库 |

公开证书随库是设计使然：只保证公开版 APK 安装 / 升级的签名一致，不含任何凭据信息。
