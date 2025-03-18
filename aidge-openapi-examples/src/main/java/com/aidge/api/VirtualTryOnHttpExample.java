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
import org.json.JSONArray;
import org.json.JSONObject;

public class VirtualTryOnHttpExample {

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
    }

    public static void main(String[] args) throws IOException {
        // Call virtual try on submit
        String apiName = "/ai/virtual/tryon";

        /*
         * Create API request using JSONObject
         * You can use any other json library to build parameters
         */
        JSONObject params = new JSONObject()
                .put("clothesList", new JSONArray()
                        .put(new JSONObject()
                                // URL of the clothing image should be accessible from the public network.
                                // The resolution should be greater than 500x500 pixels and up to a maximum of 3000x3000 pixels
                                .put("imageUrl", "https://ae-pic-a1.aliexpress-media.com/kf/H7588ee37b7674fea814b55f2f516fda1z.jpg")
                                .put("type", "tops"))
                )
                .put("model", new JSONObject()
                        .put("base", "General")
                        .put("gender", "female")
                        .put("style", "universal_1")
                        .put("body", "slim"))
                .put("viewType", "mixed")
                .put("inputQualityDetect", 0)
                .put("generateCount", 4);

        String submitRequest = new JSONObject()
                .put("requestParams", new JSONArray().put(params).toString())
                .toString();

        // String submitRequest = "{\"requestParams\":\"[{\\\"clothesList\\\":[{\\\"imageUrl\\\":\\\"https://ae-pic-a1.aliexpress-media.com/kf/H7588ee37b7674fea814b55f2f516fda1z.jpg\\\",\\\"type\\\":\\\"tops\\\"}],\\\"model\\\":{\\\"base\\\":\\\"General\\\",\\\"gender\\\":\\\"female\\\",\\\"style\\\":\\\"universal_1\\\",\\\"body\\\":\\\"slim\\\"},\\\"viewType\\\":\\\"mixed\\\",\\\"inputQualityDetect\\\":0,\\\"generateCount\\\":4}]\"}";
        String submitResult = invokeApi(apiName, submitRequest);

        // You can use any other json library to parse result and handle error result
        JSONObject submitResultJson = new JSONObject(submitResult);
        String taskId = submitResultJson.optJSONObject("data")
                .optJSONObject("result")
                .optString("taskId");

        // Query task status
        String queryApiName = "/ai/virtual/tryon-results";
        JSONObject queryParams = new JSONObject().put("task_id", taskId);
        String queryRequest = queryParams.toString();
        String queryResult = null;
        while (true) {
            try {
                queryResult = invokeApi(queryApiName, queryRequest);
                JSONObject queryResultJson = new JSONObject(queryResult);
                String taskStatus = queryResultJson.optJSONObject("data").optString("taskStatus");
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
