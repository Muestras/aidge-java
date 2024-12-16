English | [简体中文](./README-CN.md)

<p align="center">

<h1 align="center">Aidge API Examples for Java</h1>

The Aidge API examples for java provide you  to access Aidge services such as Text Translation.

## Requirements

- To run the examples, you must have an Aidge API account as well as an `API Key Name` and an `API Key Secret`. Create and view your AccessKey on Aidge dashboard.
- To use the Aidge API examples for Java to access the APIs of a product, you must first activate the product on the [Aidge console](https://www.aidge.com) if required.
- The Aidge API examples require JDK 1.8 or later.

## Quick Examples

The following code example:

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

> For security reason, we don't recommend to hard code credentials information in source code. You should access
> credentials from external configurations or environment variables.

## Changelog

Detailed changes for each release are documented in the [release notes](./ChangeLog.txt).


## References

- [Aidge Home Page](https://www.aidge.com/)

## License

This project is licensed under [Apache License Version 2](./LICENSE-2.0.txt) (SPDX-License-identifier: Apache-2.0).