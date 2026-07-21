package cn.vastark.recaptcha;

/**
 * SDK 统一异常
 */
public class SDKError extends RuntimeException {
    private final String kind;
    private final int code;

    /**
     * @param kind    错误类型（ErrorKind 常量）
     * @param message 错误信息
     */
    public SDKError(String kind, String message) {
        this(kind, message, 0);
    }

    /**
     * @param kind    错误类型（ErrorKind 常量）
     * @param message 错误信息
     * @param code    错误码
     */
    public SDKError(String kind, String message, int code) {
        super(kind + ": " + message);
        this.kind = kind;
        this.code = code;
    }

    public String getKind() {
        return kind;
    }

    public int getCode() {
        return code;
    }
}
