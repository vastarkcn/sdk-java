package cn.vastark.recaptcha;

/**
 * 错误类型
 */
public final class ErrorKind {
    private ErrorKind() {}

    /** 网络错误 */
    public static final String NETWORK = "network_error";
    /** 超时错误 */
    public static final String TIMEOUT = "timeout_error";
    /** 上游错误 */
    public static final String UPSTREAM = "upstream_error";
    /** 业务错误 */
    public static final String BUSINESS = "business_error";
}
