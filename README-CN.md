[English](./README.md) | 简体中文

<p align="center">

<h1 align="center">Aidge API Java 示例</h1>

Aidge API Java 示例为您提供了示例代码，用于访问包括文本翻译在内的Aidge API。

## 环境要求

- 要运行示例，您必须拥有 Aidge API 帐户以及 `API key name` 和 `API key secret`。您可以在 Aidge 管理后台上创建并查看您的 API key信息。您可以联系您的服务
- 要使用Aidge API 示例访问产品的 API，您必须先在 [Aidge 控制台](https://www.aidge.com) 上激活该产品。
- Aidge API 示例需要 JDK 1.8 或更高版本。

## 快速使用

以下这个代码示例向您展示了访问Aidge API的核心代码。

```java
package com.aidge.api;

import com.aidge.utils.HttpUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GeneralHttpExample {
    public static void main(String[] args) throws IOException {
        // your personal data
        String timestamp = System.currentTimeMillis() + "";
        String accessKeyName = "your api key name";  // e.g. 512345
        String accessKeySecret = "your api key secret";
        String apiName = "api name";  // e.g. ai/text/translation/and/polishment
        String apiDomain = "api domain";  // e.g. api.aidc-ai.com or cn-api.aidc-ai.com
        String data = "{your api request params}";

        // calculate sign
        StringBuilder sign = new StringBuilder();
        try {
            javax.crypto.SecretKey secretKey = new javax.crypto.spec.SecretKeySpec(accessKeySecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            byte[] bytes = mac.doFinal((accessKeySecret + timestamp).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(bytes[i] & 0xFF);
                if (hex.length() == 1) {
                    sign.append("0");
                }
                sign.append(hex.toUpperCase());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // replace url with your real data
        String url = "https://[api domain]/rest/[api name]?partner_id=aidge&sign_method=sha256&sign_ver=v2&app_key=[you api key name]&timestamp=[timestamp]&sign=[HmacSHA256 sign]";
        url = url.replace("[api domain]", apiDomain)
                .replace("[api name]", apiName)
                .replace("[you api key name]", accessKeyName)
                .replace("[timestamp]", timestamp)
                .replace("[HmacSHA256 sign]", sign);

        // add headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("x-iop-trial", "false"); // "true" for trial;

        // call api, you can use any other http tools instead
        String result = HttpUtils.doPost(url, data, headers);
        System.out.println(result);
    }
}

```

> 出于安全原因，我们不建议在源代码中硬编码凭据信息。您应该从外部配置或环境变量访问凭据。

## Changelog

每个版本的详细更改都记录在 [release notes](./ChangeLog.txt).


## References

- [Aidge官方网站](https://www.aidge.com/)

## License

This project is licensed under [Apache License Version 2](./LICENSE-2.0.txt) (SPDX-License-identifier: Apache-2.0).
