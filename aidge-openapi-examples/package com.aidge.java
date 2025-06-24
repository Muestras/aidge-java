package com.aidge.api;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.io.entity.mime.ContentType;
import org.apache.hc.core5.http.io.entity.HttpEntity;
import org.apache.hc.client5.http.classic.CloseableHttpResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.UUID;

public class MultiImageUpload {

    public static void main(String[] args) {
        String accessKeyName = "507910";
        String accessKeySecret = "your_api_key_secret";
        String apiName = "/ai/image/upload";  // Ajusta según tu endpoint
        String apiDomain = "api.aidc-ai.com"; // Ajusta si es distinto

        String timestamp = String.valueOf(System.currentTimeMillis());
        String sign = hmacSha256(accessKeySecret + timestamp, accessKeySecret);

        String url = String.format("https://%s/rest%s?partner_id=aidge&sign_method=sha256&sign_ver=v2&app_key=%s&timestamp=%s&sign=%s",
                apiDomain, apiName, accessKeyName, timestamp, sign);

        // Archivos originales (pueden ser PNG, WEBP, etc.)
        File[] imageFiles = new File[]{
                new File("C:/ruta/imagen1.png"),
                new File("C:/ruta/imagen2.webp")
        };

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost upload = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            for (File imageFile : imageFiles) {
                File jpgFile = convertToJpg(imageFile);
                if (jpgFile != null) {
                    builder.addBinaryBody("files", jpgFile, ContentType.IMAGE_JPEG, jpgFile.getName());
                }
            }

            HttpEntity entity = builder.build();
            upload.setEntity(entity);
            upload.setHeader("x-iop-trial", "true"); // Opcional

            try (CloseableHttpResponse response = httpClient.execute(upload)) {
                System.out.println("Código HTTP: " + response.getCode());
                System.out.println("Respuesta: " + new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String hmacSha256(String data, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            Formatter formatter = new Formatter();
            for (byte b : hmacBytes) {
                formatter.format("%02x", b);
            }
            return formatter.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Error creando HMAC", e);
        }
    }

    // Convierte cualquier imagen a JPG
    public static File convertToJpg(File originalImage) {
        try {
            BufferedImage image = ImageIO.read(originalImage);
            if (image == null) {
                System.err.println("No se pudo leer la imagen: " + originalImage.getName());
                return null;
            }

            File jpgFile = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID() + ".jpg");
            ImageIO.write(image, "jpg", jpgFile);
            return jpgFile;
        } catch (IOException e) {
            System.err.println("Error convirtiendo la imagen: " + originalImage.getName());
            e.printStackTrace();
            return null;
        }
    }
}
