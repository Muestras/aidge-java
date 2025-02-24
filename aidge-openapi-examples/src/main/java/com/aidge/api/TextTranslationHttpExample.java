/**
 * Copyright (C) 2024 NEURALNETICS PTE. LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aidge.api;

import com.aidge.utils.HttpUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextTranslationHttpExample {
    public static void main(String[] args) {
        try {
            // Your personal data. In this sample, we use environment variable to get access key and secret.
            String accessKeyName = System.getenv("accessKey");  // e.g. 512345
            String accessKeySecret = System.getenv("secret");

            // Call api
            String apiName = "/ai/text/marco/translator";
            String apiDomain = "api.aidc-ai.com";  // for api purchased on global site
            // String apiDomain = "cn-api.aidc-ai.com";  // 中文站购买的API请使用此域名 (for api purchased on chinese site)
            String apiRequest = "{\"text\":\"[\\\"Pen for iPad, 13 mins Fast Charging Stylus with Palm Rejection, Tilt Sensitivity, Compatible with 2018-2022 iPad Air 3/4/5, iPad Mini 5/6(Black)\\\"]\",\"sourceLanguage\":\"en\",\"targetLanguage\":\"ko\",\"formatType\":\"text\"}";
            String apiResponse = invokeApi(accessKeyName, accessKeySecret, apiName, apiDomain, apiRequest);

            // Final result
            System.out.println(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String invokeApi(String accessKeyName, String accessKeySecret, String apiName, String apiDomain, String data) throws IOException {
        String timestamp = System.currentTimeMillis() + "";

        // Calculate sign
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

        String url = "https://[api domain]/rest[api name]?partner_id=aidge&sign_method=sha256&sign_ver=v2&app_key=[you api key name]&timestamp=[timestamp]&sign=[HmacSHA256 sign]";
        url = url.replace("[api domain]", apiDomain)
                .replace("[api name]", apiName)
                .replace("[you api key name]", accessKeyName)
                .replace("[timestamp]", timestamp)
                .replace("[HmacSHA256 sign]", sign);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        // Add "x-iop-trial": "true" for trial
        // headers.put("x-iop-trial", "true");


        // Call api
        String result = HttpUtils.doPost(url, data, headers);
        System.out.println(result);
        return result;
    }
}
