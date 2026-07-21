# vastark recaptcha Java SDK

## 要求

- Java >= 8
- Maven 或 Gradle

## 安装

本地安装到 Maven 本地仓库：

```bash
cd sdk-java
mvn install -DskipTests
```

然后在你的项目 `pom.xml` 中引用：

```xml
<dependency>
    <groupId>cn.vastark</groupId>
    <artifactId>recaptcha-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 快速开始

```java
import cn.vastark.recaptcha.*;
import java.util.*;

Client client = new Client(new Config(
    "http://127.0.0.1:8090",
    "your-api-key",
    3000,
    FailureMode.STRICT_BLOCK,
    null
));

Map<String, Object> extra = new HashMap<>();
extra.put("device_fingerprint", "fp_xxx");

Envelope resp = client.generate(new GenerateRequest(
    "partner-site-a",
    "text",
    320,
    extra
));

System.out.println(resp.getData().get("captcha_id"));
System.out.println(resp.getData().get("verify_ticket"));
```

## 核心能力

- `Client.generate(GenerateRequest)` — 调用开放平台接口生成验证码（后端使用）
- `Client.verify(VerifyRequest)` — 调用开放平台接口二次校验（后端使用）
- `GenerateRequest.extra` / `VerifyRequest.extra` — 可透传扩展字段（例如 `device_fingerprint`）
- `Client.normalizeClicks(int[][])` — 坐标序列标准化：`[[x,y], ...]` → `[{"x": x, "y": y}, ...]`
- `X-Log-Id` 自动透传（通过 `Config.logIdFactory` 回调）
- 业务封禁可感知：`error_enum=E_BANNED`（通过 `SDKError` 异常抛出）
- 故障策略：
  - `FailureMode.STRICT_BLOCK`（严格阻断，默认）
  - `FailureMode.DEGRADE_ALLOW`（降级放行，返回 `{"degraded": true}` 的 `Envelope`）

## 兼容性

| Java 版本 | 状态 |
|-----------|------|
| 8 - 23+   | 支持 |

## 声明条款

1. 商业授权与 API 调用：
本项目采用 MIT/Apache-2.0 协议开源。使用本项目代码连接到官方 API 服务，不受本协议限制，仅需遵循服务条款。

2. 本地运行与修改：
允许任何人下载、修改、自用本项目代码。但若您将修改后的代码作为商业产品对外分发（例如打包成 SaaS 售卖、或封装成闭源插件），请务必联系我们获得商业豁免或授权（或说明必须保留我的版权信息）。

3. 免责条款：
本软件按"现状"提供，作者不对因使用本代码导致的任何数据丢失、服务器故障或法律纠纷承担任何责任。