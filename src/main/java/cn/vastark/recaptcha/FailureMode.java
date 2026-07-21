package cn.vastark.recaptcha;

/**
 * 故障策略
 */
public final class FailureMode {
    private FailureMode() {}

    /** 严格阻断 */
    public static final String STRICT_BLOCK = "strict_block";
    /** 降级放行 */
    public static final String DEGRADE_ALLOW = "degrade_allow";
}
