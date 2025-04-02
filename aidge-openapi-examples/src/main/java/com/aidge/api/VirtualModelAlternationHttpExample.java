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
import org.json.JSONObject;

public class VirtualModelAlternationHttpExample {

    static class ApiConfig{
        /**
         * The name and secret of your api key. e.g. 512345 and S4etzZ73nF08vOXVhk3wZjIaLSHw0123
         * In this sample, we use environment variable to get access key and secret.
         */
        public static String accessKeyName = System.getenv("accessKey");
        public static String accessKeySecret = System.getenv("secret");

        /**
         * The domain of the API.
         * for api purchased on global site. set apiDomain to "api.aidc-ai.com"
         * 中文站购买的API请使用"cn-api.aidc-ai.com"域名 (for api purchased on chinese site) set apiDomain to "cn-api.aidc-ai.com"
         */
        public static String apiDomain = "api domain";

        /**
         * We offer trial quota to help you familiarize and test how to use the Aidge API in your account
         * To use trial quota, please set useTrialResource to true
         * If you set useTrialResource to false before you purchase the API
         * You will receive "Sorry, your calling resources have been exhausted........"
         * 我们为您的账号提供一定数量的免费试用额度可以试用任何API。请将useTrialResource设置为true用于试用。
         * 如设置为false，且您未购买该API，将会收到"Sorry, your calling resources have been exhausted........."的错误提示
         */
        public static boolean useTrialResource = true/false;
        /**
        * FAQ for API response
        * FAQ:https://app.gitbook.com/o/pBUcuyAewroKoYr3CeVm/s/cXGtrD26wbOKouIXD83g/getting-started/faq
        * FAQ(中文/Simple Chinese):https://aidge.yuque.com/org-wiki-aidge-bzb63a/brbggt/ny2tgih89utg1aha
        */
    }
    public static void main(String[] args) throws IOException {
        // Call virtual try on submit
        String apiName = "/ai/virtual/model/generation/batch";

        /*
         * Create API request using JSONObject
         * You can use any other json library to build parameters
         * Note: the array type parameter needs to be converted to a string
         */
        JSONObject submitParams = new JSONObject()
                .put("maskKeepBg", "true")
                .put("dimension", "768")
                .put("age", "YOUTH")
                .put("bgStyle", "room")
                .put("model", "WHITE")
                .put("gender", "FEMALE")
                .put("count", "2")
                .put("imageStyle", "realPhoto")
                .put("imageBase64", "")
                .put("imageUrl", "https://ae01.alicdn.com/kf/H873d9e029746449ca21737fcf595b781X.jpg");

        String submitRequest = submitParams.toString();

//        String submitRequest = "{\"maskKeepBg\":\"true\",\"dimension\":\"768\",\"age\":\"YOUTH\", \"bgStyle\":\"room\",\"model\":\"WHITE\",\"gender\":\"FEMALE\",\"count\":\"2\",\"imageStyle\":\"realPhoto\",\"imageBase64\":\"\",\"imageUrl\":\"https://ae01.alicdn.com/kf/H873d9e029746449ca21737fcf595b781X.jpg\"}";
        String submitResult = invokeApi(apiName, submitRequest);

        // You can use any other json library to parse result and handle error result
        JSONObject submitResultJson = new JSONObject(submitResult);
        String taskId = submitResultJson.optJSONObject("data") != null ?
                submitResultJson.getJSONObject("data").optString("taskId") : null;

        // Query task status
        String queryApiName = "/ai/virtual/model/generation/query";
        JSONObject queryParams = new JSONObject().put("taskId", taskId);
        String queryRequest = queryParams.toString();
        String queryResult = null;
        while (true) {
            try {
                queryResult = invokeApi(queryApiName, queryRequest);
                JSONObject queryResultJson = new JSONObject(queryResult);
                String taskStatus = queryResultJson.optJSONObject("data") != null ?
                        queryResultJson.getJSONObject("data").optString("taskStatus") : null;
                if ("finished".equals(taskStatus)) {
                    break;
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Final result for the virtual try on
        System.out.println(queryResult);
    }


    private static String invokeApi(String apiName, String data) throws IOException {
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
        // FAQ:https://app.gitbook.com/o/pBUcuyAewroKoYr3CeVm/s/cXGtrD26wbOKouIXD83g/getting-started/faq
        // FAQ(中文/Simple Chinese):https://aidge.yuque.com/org-wiki-aidge-bzb63a/brbggt/ny2tgih89utg1aha
        System.out.println(result);
        return result;
    }
}
