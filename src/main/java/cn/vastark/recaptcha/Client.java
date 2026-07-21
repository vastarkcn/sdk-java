package cn.vastark.recaptcha;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * recaptcha 客户端，用于调用开放平台的生成和校验接口
 */
public class Client {
    private static final String GENERATE_PATH = "/saas/sdk/v1/recaptcha/secure/generate";
    private static final String VERIFY_PATH   = "/saas/sdk/v1/recaptcha/secure/verify";

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private final Config config;

    static {
        // 内网/自签名场景跳过 SSL 验证
        disableSSLVerification();
    }

    /**
     * @param config 客户端配置
     */
    public Client(Config config) {
        this.config = config;
    }

    /**
     * 调用开放平台接口生成验证码
     *
     * @param req 生成请求
     * @return 响应信封
     * @throws SDKError 业务/网络错误
     */
    public Envelope generate(GenerateRequest req) throws SDKError {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("site_id", req.getSiteId());
        if (!req.getType().isEmpty()) {
            payload.put("type", req.getType());
        }
        if (req.getWidth() > 0) {
            payload.put("width", req.getWidth());
        }
        payload.putAll(req.getExtra());
        return postJson(GENERATE_PATH, payload);
    }

    /**
     * 调用开放平台接口二次校验
     *
     * @param req 校验请求
     * @return 响应信封
     * @throws SDKError 业务/网络错误
     */
    public Envelope verify(VerifyRequest req) throws SDKError {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("captcha_id", req.getCaptchaId());
        payload.put("verify_ticket", req.getVerifyTicket());
        payload.put("clicks", req.getClicks());
        payload.putAll(req.getExtra());
        return postJson(VERIFY_PATH, payload);
    }

    /**
     * 坐标序列标准化：将 [[x, y], ...] 转为 [{"x": x, "y": y}, ...]
     *
     * @param points 坐标对列表
     * @return 标准化后的坐标列表
     */
    public static List<Map<String, Integer>> normalizeClicks(int[][] points) {
        List<Map<String, Integer>> out = new ArrayList<>(points.length);
        for (int[] p : points) {
            Map<String, Integer> m = new LinkedHashMap<>();
            m.put("x", p[0]);
            m.put("y", p[1]);
            out.add(m);
        }
        return out;
    }

    /**
     * 坐标序列标准化（List 版本）
     */
    public static List<Map<String, Integer>> normalizeClicks(List<int[]> points) {
        List<Map<String, Integer>> out = new ArrayList<>(points.size());
        for (int[] p : points) {
            Map<String, Integer> m = new LinkedHashMap<>();
            m.put("x", p[0]);
            m.put("y", p[1]);
            out.add(m);
        }
        return out;
    }

    private Envelope postJson(String path, Map<String, Object> body) throws SDKError {
        String url = config.getBaseUrl() + path;
        HttpURLConnection conn = null;

        try {
            String jsonBody = GSON.toJson(body);
            byte[] raw = jsonBody.getBytes("UTF-8");

            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout((int) config.getTimeoutMs());
            conn.setReadTimeout((int) config.getTimeoutMs());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-API-Key", config.getApiKey());
            if (config.getLogIdFactory() != null) {
                conn.setRequestProperty("X-Log-Id", config.getLogIdFactory().call());
            }
            conn.setRequestProperty("Content-Length", String.valueOf(raw.length));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(raw);
                os.flush();
            }

            int httpCode = conn.getResponseCode();
            String respBody = readStream(
                httpCode >= 400 ? conn.getErrorStream() : conn.getInputStream()
            );

            Map<String, Object> rawDict;
            try {
                rawDict = GSON.fromJson(respBody, MAP_TYPE);
            } catch (Exception e) {
                return handleError(new SDKError(ErrorKind.UPSTREAM, "invalid json", httpCode));
            }
            if (rawDict == null) {
                return handleError(new SDKError(ErrorKind.UPSTREAM, "empty response", httpCode));
            }

            Envelope env = mapToEnvelope(rawDict);

            if (httpCode >= 500) {
                return handleError(new SDKError(ErrorKind.UPSTREAM, env.getMessage(), httpCode));
            }

            if (env.getCode() != 0) {
                if (isBannedEnvelope(env)) {
                    if (env.getData() == null) {
                        env.setData(new LinkedHashMap<String, Object>());
                    }
                    env.getData().put("ban_blocked", true);
                    env.getData().put("error_enum", ErrorEnum.BANNED);
                }
                throw new SDKError(ErrorKind.BUSINESS, env.getMessage(), env.getCode());
            }

            return env;
        } catch (SDKError e) {
            throw e;
        } catch (SocketTimeoutException e) {
            return handleError(new SDKError(ErrorKind.TIMEOUT, e.getMessage()));
        } catch (IOException e) {
            return handleError(new SDKError(ErrorKind.NETWORK, e.getMessage()));
        } catch (Exception e) {
            return handleError(new SDKError(ErrorKind.NETWORK, e.getMessage()));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static boolean isBannedEnvelope(Envelope env) {
        if (env.getCode() == 1002) {
            return true;
        }
        Map<String, Object> data = env.getData();
        if (data == null) {
            return false;
        }
        if (data.containsKey("ban_blocked")) {
            Object v = data.get("ban_blocked");
            if (v instanceof Boolean && (Boolean) v) return true;
            if (v instanceof Number && ((Number) v).intValue() == 1) return true;
            String sv = String.valueOf(v).trim();
            if ("1".equals(sv) || "true".equalsIgnoreCase(sv)) return true;
        }
        if (data.containsKey("error_enum")) {
            String ev = String.valueOf(data.get("error_enum")).trim();
            if (ErrorEnum.BANNED.equalsIgnoreCase(ev)) return true;
        }
        return false;
    }

    private Envelope handleError(SDKError err) throws SDKError {
        if (FailureMode.DEGRADE_ALLOW.equals(config.getFailureMode())) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("degraded", true);
            return new Envelope(0, "degraded_allow", data);
        }
        throw err;
    }

    private static Envelope mapToEnvelope(Map<String, Object> raw) {
        Envelope env = new Envelope();
        Object code = raw.get("code");
        env.setCode(code instanceof Number ? ((Number) code).intValue() : 0);
        Object msg = raw.get("message");
        env.setMessage(msg != null ? String.valueOf(msg) : "");
        Object data = raw.get("data");
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> d = (Map<String, Object>) data;
            env.setData(d);
        }
        return env;
    }

    private static String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static void disableSSLVerification() {
        try {
            TrustManager[] trustAll = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] c, String a) {}
                    public void checkServerTrusted(X509Certificate[] c, String a) {}
                }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAll, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception ignored) {
        }
    }
}
