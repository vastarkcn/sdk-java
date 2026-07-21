package cn.vastark.recaptcha;

import java.util.concurrent.Callable;

/**
 * 客户端配置
 */
public class Config {
    private final String baseUrl;
    private final String apiKey;
    private final long timeoutMs;
    private final String failureMode;
    private final Callable<String> logIdFactory;

    /**
     * @param baseUrl     服务端地址
     * @param apiKey      API 密钥
     */
    public Config(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, 3000, FailureMode.STRICT_BLOCK, null);
    }

    /**
     * @param baseUrl      服务端地址
     * @param apiKey       API 密钥
     * @param timeoutMs    超时时间（毫秒）
     * @param failureMode  故障策略
     * @param logIdFactory X-Log-Id 生成回调
     */
    public Config(String baseUrl, String apiKey, long timeoutMs, String failureMode, Callable<String> logIdFactory) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.apiKey = apiKey;
        this.timeoutMs = timeoutMs > 0 ? timeoutMs : 3000;
        this.failureMode = failureMode != null ? failureMode : FailureMode.STRICT_BLOCK;
        this.logIdFactory = logIdFactory;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public String getFailureMode() {
        return failureMode;
    }

    public Callable<String> getLogIdFactory() {
        return logIdFactory;
    }
}
