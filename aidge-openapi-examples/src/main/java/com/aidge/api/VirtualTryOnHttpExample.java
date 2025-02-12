package com.aidge.api;

import com.aidge.utils.HttpUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.json.JsonObject;

import static javax.json.Json.createReader;

/**
 * description
 *
 * @Author zhishan
 * @Date 2025-02-12
 */
public class VirtualTryOnHttpExample {
    public static void main(String[] args) throws IOException {
        // Your personal data. In the example, we use JVM system property to pass the personal data
        String accessKeyName = System.getProperty("accessKey");  // e.g. 512345
        String accessKeySecret = System.getProperty("secret");

        String apiDomain = "api.aidc-ai.com";  // cn-api.aidc-ai.com for cn region

        // Call virtual try on submit
        String apiName = "/ai/virtual/tryon";
        String submitRequest = "{\"requestParams\":\"[{\\\"clothesList\\\":[{\\\"imageUrl\\\":\\\"https://ae-pic-a1.aliexpress-media.com/kf/H7588ee37b7674fea814b55f2f516fda1z.jpg\\\",\\\"type\\\":\\\"tops\\\"}],\\\"model\\\":{\\\"base\\\":\\\"General\\\",\\\"gender\\\":\\\"female\\\",\\\"style\\\":\\\"universal_1\\\",\\\"body\\\":\\\"slim\\\"},\\\"viewType\\\":\\\"mixed\\\",\\\"inputQualityDetect\\\":0,\\\"generateCount\\\":4}]\"}"; // e.g. for translation "{\"sourceTextList\":\"[\\\"how are you\\\"]\",\"sourceLanguage\":\"en\",\"targetLanguage\":\"ko\",\"formatType\":\"text\"}"
        String submitResult = invokeApi(accessKeyName, accessKeySecret, apiName, apiDomain, submitRequest);

        // You can use any other json library to parse result and handle error result
        JsonObject submitResultJson = createReader(new StringReader(submitResult)).readObject();
        String taskId = Optional.ofNullable(submitResultJson.getJsonObject("data"))
                .map(i->i.getJsonObject("result"))
                .map(i->i.getString("taskId"))
                .orElse(null);

        // Query task status
        String queryApiName = "/ai/virtual/tryon-results";
        String queryRequest = "{\"task_id\":\"" + taskId + "\"}";
        String queryResult = null;
        while (true) {
            try {
                queryResult = invokeApi(accessKeyName, accessKeySecret, queryApiName, apiDomain, queryRequest);
                JsonObject queryResultJson = createReader(new StringReader(queryResult)).readObject();
                String taskStatus = Optional.ofNullable(queryResultJson.getJsonObject("data")).map(i->i.getString("taskStatus")).orElse(null);
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

        // Add "x-iop-trial": "true" for trial
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        // Call api
        String result = HttpUtils.doPost(url, data, headers);
        System.out.println(result);
        return result;
    }
}
