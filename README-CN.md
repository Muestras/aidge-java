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

    static class ApiConfig{
        /**
         * The name and secret of your api key. e.g. 512345 and S4etzZ73nF08vOXVhk3wZjIaLSHw0123
         */
        public static String accessKeyName = "your api key name";
        public static String accessKeySecret = "your api key secret";

        /**
         * The domain of the API.
         * for api purchased on global site. set apiDomain to "api.aidc-ai.com"
         * 中文站购买的API请使用"cn-api.aidc-ai.com"域名 (for api purchased on chinese site) set apiDomain to "cn-api.aidc-ai.com"
         */
        public static String apiDomain = "api.aidc-ai.com";
        // public static String apiDomain = "cn-api.aidc-ai.com";

        /**
         * We offer trial quota to help you familiarize and test how to use the Aidge API in your account
         * To use trial quota, please set useTrialResource to true
         * If you set useTrialResource to false before you purchase the API
         * You will receive "Sorry, your calling resources have been exhausted........"
         * 我们为您的账号提供一定数量的免费试用额度可以试用任何API。请将useTrialResource设置为true用于试用。
         * 如设置为false，且您未购买该API，将会收到"Sorry, your calling resources have been exhausted........."的错误提示
         */
        public static boolean useTrialResource = false;
        // public static boolean useTrialResource = true;
    }

    public static void main(String[] args) throws IOException {
        // Your api request data
        String apiName = "api name"; // e.g. /ai/text/translation/and/polishment
        String data = "{your api request params}"; // e.g. for translation "{\"sourceTextList\":\"[\\\"how are you\\\"]\",\"sourceLanguage\":\"en\",\"targetLanguage\":\"ko\",\"formatType\":\"text\"}"
        String timestamp = System.currentTimeMillis() + "";

        // Calculate sign
        StringBuilder sign = new StringBuilder();
        try {
            javax.crypto.SecretKey secretKey = new javax.crypto.spec.SecretKeySpec(ApiConfig.accessKeySecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            byte[] bytes = mac.doFinal((ApiConfig.accessKeySecret + timestamp).getBytes(java.nio.charset.StandardCharsets.UTF_8));
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

        // Replace url with your real data
        String url = "https://[api domain]/rest[api name]?partner_id=aidge&sign_method=sha256&sign_ver=v2&app_key=[you api key name]&timestamp=[timestamp]&sign=[HmacSHA256 sign]";
        url = url.replace("[api domain]", ApiConfig.apiDomain)
                .replace("[api name]", apiName)
                .replace("[you api key name]", ApiConfig.accessKeyName)
                .replace("[timestamp]", timestamp)
                .replace("[HmacSHA256 sign]", sign);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        if (ApiConfig.useTrialResource) {
            // Add "x-iop-trial": "true" for trial
            headers.put("x-iop-trial", "true");
        }

        // Call api
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
