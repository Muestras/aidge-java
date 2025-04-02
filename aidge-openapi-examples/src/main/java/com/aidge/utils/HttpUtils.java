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

package com.aidge.utils;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public abstract class HttpUtils {
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static boolean ignoreSSLCheck = true;
    private static boolean ignoreHostCheck = true;

    private HttpUtils() {
    }

    public static String doPost(String url, String body, Map<String, String> headers) throws IOException {
        String ctype = "application/json;charset=" + DEFAULT_CHARSET;
        byte[] content = body.getBytes(DEFAULT_CHARSET);
        return _doPost(url, ctype, content, headers, 15000, 30000, null);
    }

    private static String _doPost(String url, String ctype, byte[] content, Map<String, String> headers, int connectTimeout, int readTimeout, Proxy proxy) throws IOException {
        HttpURLConnection conn = null;
        OutputStream out = null;
        String rsp = null;

        try {
            conn = getConnection(new URL(url), "POST", ctype, headers, proxy);
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            out = conn.getOutputStream();
            out.write(content);
            rsp = getResponseAsString(conn);
        } finally {
            if (out != null) {
                out.close();
            }

            if (conn != null) {
                conn.disconnect();
            }

        }

        return rsp;
    }

    public static String doGet(String fullUrl, Map<String, String> headerParams) throws IOException {
        HttpURLConnection conn = null;
        String rsp = null;
        try {
            conn = getConnection(new URL(fullUrl), "GET", "application/x-www-form-urlencoded;charset=UTF-8", headerParams, null);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            rsp = getResponseAsString(conn);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return rsp;
    }

    private static HttpURLConnection getConnection(URL url, String method, String ctype, Map<String, String> headers, Proxy proxy) throws IOException {
        HttpURLConnection conn = null;
        if (proxy == null) {
            conn = (HttpURLConnection)url.openConnection();
        } else {
            conn = (HttpURLConnection)url.openConnection(proxy);
        }

        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection connHttps = (HttpsURLConnection)conn;
            if (ignoreSSLCheck) {
                try {
                    SSLContext ctx = SSLContext.getInstance("TLS");
                    ctx.init((KeyManager[])null, new TrustManager[]{new TrustAllTrustManager()}, new SecureRandom());
                    connHttps.setSSLSocketFactory(ctx.getSocketFactory());
                    connHttps.setHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                } catch (Exception var8) {
                    throw new IOException(var8.toString());
                }
            } else if (ignoreHostCheck) {
                connHttps.setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }

            conn = connHttps;
        }

        ((HttpURLConnection)conn).setRequestMethod(method);
        ((HttpURLConnection)conn).setDoInput(true);
        ((HttpURLConnection)conn).setDoOutput(true);
        ((HttpURLConnection)conn).setRequestProperty("Host", url.getHost());
        ((HttpURLConnection)conn).setRequestProperty("Accept", "text/xml,text/javascript");
        ((HttpURLConnection)conn).setRequestProperty("User-Agent", "iop-sdk-java");
        ((HttpURLConnection)conn).setRequestProperty("Content-Type", ctype);
        if (headers != null) {
            Iterator var9 = headers.entrySet().iterator();

            while(var9.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry)var9.next();
                ((HttpURLConnection)conn).setRequestProperty((String)entry.getKey(), (String)entry.getValue());
            }
        }

        return (HttpURLConnection)conn;
    }

    protected static String getResponseAsString(HttpURLConnection conn) throws IOException {
        String charset = getResponseCharset(conn.getContentType());
        if (conn.getResponseCode() < 400) {
            String contentEncoding = conn.getContentEncoding();
            return "gzip".equalsIgnoreCase(contentEncoding) ? getStreamAsString(new GZIPInputStream(conn.getInputStream()), charset) : getStreamAsString(conn.getInputStream(), charset);
        } else {
            if (conn.getResponseCode() == 400) {
                InputStream error = conn.getErrorStream();
                if (error != null) {
                    return getStreamAsString(error, charset);
                }
            }

            throw new IOException(conn.getResponseCode() + " " + conn.getResponseMessage());
        }
    }

    public static String getStreamAsString(InputStream stream, String charset) throws IOException {
        try {
            Reader reader = new InputStreamReader(stream, charset);
            StringBuilder response = new StringBuilder();
            char[] buff = new char[1024];
            int read;
            while((read = reader.read(buff)) > 0) {
                response.append(buff, 0, read);
            }

            String var6 = response.toString();
            return var6;
        } finally {
            if (stream != null) {
                stream.close();
            }

        }
    }

    public static String getResponseCharset(String ctype) {
        String charset = "UTF-8";
        if (isEmpty(ctype)) {
            String[] params = ctype.split(";");
            String[] var3 = params;
            int var4 = params.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String param = var3[var5];
                param = param.trim();
                if (param.startsWith("charset")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2 && isEmpty(pair[1])) {
                        charset = pair[1].trim();
                    }
                    break;
                }
            }
        }

        return charset;
    }

    public static boolean isEmpty(String value) {
        int strLen;
        if (value != null && (strLen = value.length()) != 0) {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(value.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static class TrustAllTrustManager implements X509TrustManager {
        public TrustAllTrustManager() {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }
}
